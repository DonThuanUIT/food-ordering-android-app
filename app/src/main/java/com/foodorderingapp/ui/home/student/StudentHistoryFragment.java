package com.foodorderingapp.ui.home.student;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.ui.adapter.OrderAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.utils.TokenManager;
import com.foodorderingapp.viewmodel.OrderViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import android.content.Intent;
import java.util.ArrayList;
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.foodorderingapp.ui.order.RateOrderActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StudentHistoryFragment extends Fragment {

    private static final String PREF_HIDDEN_HISTORY = "student_hidden_order_history";
    private static final String KEY_PREFIX_HIDDEN_ORDERS = "hidden_orders_";

    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private RecyclerView rvOrderHistory;
    private TextView tvOrderHistoryEmpty;
    private MaterialButton btnDateFilter;
    private MaterialButton btnClearFilters;
    private ChipGroup chipStatusFilter;
    private SharedPreferences hiddenHistoryPrefs;

    private final List<OrderResponse> allOrders = new ArrayList<>();
    private final Set<String> hiddenOrderIds = new HashSet<>();
    private final DateTimeFormatter dateLabelFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
    private LocalDate selectedDate;
    private String selectedStatus;

    public StudentHistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrderHistory = view.findViewById(R.id.rvOrderHistory);
        tvOrderHistoryEmpty = view.findViewById(R.id.tvOrderHistoryEmpty);
        btnDateFilter = view.findViewById(R.id.btnOrderHistoryDate);
        btnClearFilters = view.findViewById(R.id.btnOrderHistoryClearFilters);
        chipStatusFilter = view.findViewById(R.id.chipOrderHistoryStatus);
        hiddenHistoryPrefs = requireContext().getSharedPreferences(PREF_HIDDEN_HISTORY, Context.MODE_PRIVATE);
        loadHiddenOrderIds();

        orderAdapter = new OrderAdapter();
        orderAdapter.setShowReviewAction(true);
        orderAdapter.setOnReviewClickListener(this::startRateOrderActivity);
        orderAdapter.setOnHideClickListener(this::hideOrderFromHistory);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderHistory.setAdapter(orderAdapter);
        rvOrderHistory.setNestedScrollingEnabled(false);
        setupFilters();

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderViewModel.getOrderHistory().observe(getViewLifecycleOwner(), orders -> {
            allOrders.clear();
            if (orders != null) {
                allOrders.addAll(orders);
            }
            applyHistoryFilters();
        });
        orderViewModel.getReviewResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                orderViewModel.loadOrderHistory();
            }
        });
        orderViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                ToastUtils.info(getContext(), message);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (orderViewModel != null) {
            orderViewModel.loadOrderHistory();
        }
    }

    private void setupFilters() {
        btnDateFilter.setOnClickListener(v -> showDatePicker());
        btnClearFilters.setOnClickListener(v -> clearFilters());
        chipStatusFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedStatus = null;
            } else {
                selectedStatus = statusForChip(checkedIds.get(0));
            }
            updateFilterControls();
            applyHistoryFilters();
        });
        updateFilterControls();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.set(Calendar.YEAR, selectedDate.getYear());
            calendar.set(Calendar.MONTH, selectedDate.getMonthValue() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, selectedDate.getDayOfMonth());
        }

        new DatePickerDialog(
                requireContext(),
                (picker, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    updateFilterControls();
                    applyHistoryFilters();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void clearFilters() {
        selectedDate = null;
        selectedStatus = null;
        chipStatusFilter.check(R.id.chipHistoryAll);
        updateFilterControls();
        applyHistoryFilters();
    }

    private void hideOrderFromHistory(OrderResponse order) {
        if (order == null || isBlank(order.getId())) {
            return;
        }

        hiddenOrderIds.add(order.getId());
        saveHiddenOrderIds();
        orderAdapter.setPendingHideOrderId(null);
        applyHistoryFilters();
        ToastUtils.success(getContext(), "Đã ẩn đơn khỏi lịch sử");
    }

    private void applyHistoryFilters() {
        List<OrderResponse> visibleOrders = new ArrayList<>();
        for (OrderResponse order : allOrders) {
            if (order == null) {
                continue;
            }
            if (!isBlank(order.getId()) && hiddenOrderIds.contains(order.getId())) {
                continue;
            }
            if (selectedStatus != null && !selectedStatus.equalsIgnoreCase(order.getStatus())) {
                continue;
            }
            if (selectedDate != null && !selectedDate.equals(getHistoryDate(order))) {
                continue;
            }
            visibleOrders.add(order);
        }

        boolean empty = visibleOrders.isEmpty();
        tvOrderHistoryEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvOrderHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvOrderHistoryEmpty.setText(emptyMessage());
        orderAdapter.submitList(visibleOrders);
    }

    private String emptyMessage() {
        if (allOrders.isEmpty()) {
            return "Chưa có lịch sử đơn hàng";
        }
        if (selectedDate != null || selectedStatus != null) {
            return "Không có đơn phù hợp bộ lọc";
        }
        return "Không có đơn hiển thị";
    }

    private void updateFilterControls() {
        btnDateFilter.setText(selectedDate == null
                ? "Chọn ngày"
                : selectedDate.format(dateLabelFormatter));
        btnClearFilters.setVisibility(selectedDate != null || selectedStatus != null
                ? View.VISIBLE
                : View.GONE);
    }

    @Nullable
    private String statusForChip(int checkedId) {
        if (checkedId == R.id.chipHistoryCompleted) {
            return "COMPLETED";
        }
        if (checkedId == R.id.chipHistoryReceived) {
            return "RECEIVED";
        }
        if (checkedId == R.id.chipHistoryCancelled) {
            return "CANCELLED";
        }
        if (checkedId == R.id.chipHistoryRejected) {
            return "REJECTED";
        }
        if (checkedId == R.id.chipHistoryFailed) {
            return "FAILED";
        }
        return null;
    }

    @Nullable
    private LocalDate getHistoryDate(OrderResponse order) {
        String value = displayDateTimeForHistory(order);
        if (isBlank(value)) {
            return null;
        }

        String normalized = value.trim();
        int dotIndex = normalized.indexOf('.');
        if (dotIndex > 0) {
            normalized = normalized.substring(0, dotIndex);
        }

        try {
            return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        if (normalized.length() >= 10) {
            try {
                return LocalDate.parse(normalized.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    private String displayDateTimeForHistory(OrderResponse order) {
        if (order == null) {
            return "";
        }
        if (("COMPLETED".equalsIgnoreCase(order.getStatus()) || "RECEIVED".equalsIgnoreCase(order.getStatus()))
                && !isBlank(order.getCompletedAt())) {
            return order.getCompletedAt();
        }
        return order.getCreatedAt();
    }

    private void loadHiddenOrderIds() {
        hiddenOrderIds.clear();
        if (hiddenHistoryPrefs == null) {
            return;
        }
        Set<String> savedIds = hiddenHistoryPrefs.getStringSet(hiddenOrdersKey(), null);
        if (savedIds != null) {
            hiddenOrderIds.addAll(savedIds);
        }
    }

    private void saveHiddenOrderIds() {
        if (hiddenHistoryPrefs == null) {
            return;
        }
        hiddenHistoryPrefs.edit()
                .putStringSet(hiddenOrdersKey(), new HashSet<>(hiddenOrderIds))
                .apply();
    }

    private String hiddenOrdersKey() {
        String phone = null;
        try {
            phone = TokenManager.getInstance().getPhone();
        } catch (RuntimeException ignored) {
        }
        if (isBlank(phone)) {
            phone = "guest";
        }
        return KEY_PREFIX_HIDDEN_ORDERS + phone.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void startRateOrderActivity(OrderResponse order) {
        if (getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), RateOrderActivity.class);
        intent.putExtra("ORDER_ID", order.getId());

        ArrayList<String> foodIds = new ArrayList<>();
        ArrayList<String> foodNames = new ArrayList<>();
        ArrayList<String> imageUrls = new ArrayList<>();

        if (order.getDetails() != null) {
            for (OrderDetailResponse detail : order.getDetails()) {
                if (detail.getFoodId() != null) {
                    foodIds.add(detail.getFoodId());
                    foodNames.add(detail.getFoodName() != null ? detail.getFoodName() : "");
                    imageUrls.add(detail.getImageUrl() != null ? detail.getImageUrl() : "");
                }
            }
        }

        intent.putStringArrayListExtra("FOOD_IDS", foodIds);
        intent.putStringArrayListExtra("FOOD_NAMES", foodNames);
        intent.putStringArrayListExtra("FOOD_IMAGES", imageUrls);

        startActivity(intent);
    }
}
