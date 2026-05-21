package com.foodorderingapp.ui.home.student;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.viewmodel.OrderViewModel;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class StudentOrdersFragment extends Fragment {

    private static final int COLOR_ACTIVE = Color.parseColor("#A13A00");
    private static final int COLOR_DONE = Color.parseColor("#111111");
    private static final int COLOR_INACTIVE = Color.parseColor("#8A8A8A");

    private OrderViewModel orderViewModel;

    private View orderImageContainer;
    private View timelineContainer;
    private View shopCard;
    private View summaryCard;
    private TextView tvEmptyOrders;
    private TextView tvPendingLabel;
    private TextView tvConfirmedLabel;
    private TextView tvConfirmedTime;
    private TextView tvDeliveringLabel;
    private TextView tvCompletedLabel;
    private TextView dotPending;
    private TextView dotConfirmed;
    private TextView dotDelivering;
    private TextView dotCompleted;
    private TextView tvShopName;
    private TextView tvOrderNumber;
    private TextView tvOrderAddress;
    private TextView tvOrderTotal;
    private LinearLayout orderItemsContainer;

    public StudentOrdersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderViewModel.getActiveOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders == null) {
                Toast.makeText(getContext(), "Khong tai duoc don hang", Toast.LENGTH_SHORT).show();
                showEmptyState();
                return;
            }

            if (orders.isEmpty()) {
                showEmptyState();
                return;
            }

            showOrder(orders.get(0));
        });

        orderViewModel.loadActiveOrders();
    }

    private void bindViews(View view) {
        orderImageContainer = view.findViewById(R.id.orderImageContainer);
        timelineContainer = view.findViewById(R.id.timelineContainer);
        shopCard = view.findViewById(R.id.shopCard);
        summaryCard = view.findViewById(R.id.summaryCard);
        tvEmptyOrders = view.findViewById(R.id.tvEmptyOrders);

        tvPendingLabel = view.findViewById(R.id.tvPendingLabel);
        tvConfirmedLabel = view.findViewById(R.id.tvConfirmedLabel);
        tvConfirmedTime = view.findViewById(R.id.tvConfirmedTime);
        tvDeliveringLabel = view.findViewById(R.id.tvDeliveringLabel);
        tvCompletedLabel = view.findViewById(R.id.tvCompletedLabel);

        dotPending = view.findViewById(R.id.dotPending);
        dotConfirmed = view.findViewById(R.id.dotConfirmed);
        dotDelivering = view.findViewById(R.id.dotDelivering);
        dotCompleted = view.findViewById(R.id.dotCompleted);

        tvShopName = view.findViewById(R.id.tvShopName);
        tvOrderNumber = view.findViewById(R.id.tvOrderNumber);
        tvOrderAddress = view.findViewById(R.id.tvOrderAddress);
        tvOrderTotal = view.findViewById(R.id.tvOrderTotal);
        orderItemsContainer = view.findViewById(R.id.orderItemsContainer);
    }

    private void showEmptyState() {
        tvEmptyOrders.setVisibility(View.VISIBLE);
        orderImageContainer.setVisibility(View.GONE);
        timelineContainer.setVisibility(View.GONE);
        shopCard.setVisibility(View.GONE);
        summaryCard.setVisibility(View.GONE);
    }

    private void showOrder(OrderResponse order) {
        tvEmptyOrders.setVisibility(View.GONE);
        orderImageContainer.setVisibility(View.VISIBLE);
        timelineContainer.setVisibility(View.VISIBLE);
        shopCard.setVisibility(View.VISIBLE);
        summaryCard.setVisibility(View.VISIBLE);

        String status = order.getStatus() == null ? "PENDING" : order.getStatus();
        updateTimeline(status, order.getCreatedAt());

        tvShopName.setText(nullToDefault(order.getShopName(), "Cua hang"));
        tvOrderNumber.setText("Don hang #" + shortOrderId(order.getId()));
        tvOrderAddress.setText("Nhan tai: "
                + nullToDefault(order.getBuilding(), "Chua co toa nha")
                + " - "
                + nullToDefault(order.getDropOff(), "Chua co diem nhan"));
        tvOrderTotal.setText(formatPrice(order.getTotalPrice()));
        bindOrderDetails(order.getDetails());
    }

    private void updateTimeline(String status, String createdAt) {
        int step = statusToStep(status);

        updateStep(tvPendingLabel, dotPending, step >= 1, step == 1);
        updateStep(tvConfirmedLabel, dotConfirmed, step >= 2, step == 2);
        updateStep(tvDeliveringLabel, dotDelivering, step >= 3, step == 3);
        updateStep(tvCompletedLabel, dotCompleted, step >= 4, step == 4);

        if (step >= 2) {
            tvConfirmedTime.setVisibility(View.VISIBLE);
            tvConfirmedTime.setText(formatDateTime(createdAt));
        } else {
            tvConfirmedTime.setVisibility(View.GONE);
        }
    }

    private void updateStep(TextView label, TextView dot, boolean reached, boolean current) {
        label.setTextColor(current ? COLOR_ACTIVE : (reached ? COLOR_DONE : COLOR_INACTIVE));
        dot.setAlpha(reached ? 1f : 0.35f);
    }

    private int statusToStep(String status) {
        switch (status.toUpperCase(Locale.ROOT)) {
            case "CONFIRMED":
                return 2;
            case "DELIVERING":
            case "RECEIVED":
                return 3;
            case "COMPLETED":
                return 4;
            case "PENDING":
            default:
                return 1;
        }
    }

    private void bindOrderDetails(List<OrderDetailResponse> details) {
        orderItemsContainer.removeAllViews();

        if (details == null || details.isEmpty()) {
            TextView empty = createDetailText("Khong co chi tiet mon", "");
            orderItemsContainer.addView(empty);
            return;
        }

        for (OrderDetailResponse detail : details) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(6), 0, dp(6));

            TextView name = createDetailText(
                    detail.getQuantity() + "x " + nullToDefault(detail.getFoodName(), "Mon an"),
                    ""
            );
            name.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView price = createDetailText(formatPrice(detail.getPrice() * detail.getQuantity()), "");
            price.setGravity(android.view.Gravity.END);

            row.addView(name);
            row.addView(price);
            orderItemsContainer.addView(row);
        }
    }

    private TextView createDetailText(String text, String fallback) {
        TextView textView = new TextView(requireContext());
        textView.setText(nullToDefault(text, fallback));
        textView.setTextColor(Color.parseColor("#222222"));
        textView.setTextSize(16);
        return textView;
    }

    private String shortOrderId(String id) {
        if (id == null || id.isEmpty()) return "------";
        return id.length() <= 8 ? id : id.substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String formatDateTime(String value) {
        if (value == null || value.isEmpty()) return "";
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value);
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
        } catch (Exception ignored) {
            return value;
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "d";
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
