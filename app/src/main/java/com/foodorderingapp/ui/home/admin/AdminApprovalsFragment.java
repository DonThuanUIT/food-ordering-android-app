package com.foodorderingapp.ui.home.admin;

import android.content.res.ColorStateList;
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
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.ui.adapter.AdminPendingShopAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.AdminViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminApprovalsFragment extends Fragment {
    private static final int PAGE_SIZE = 20;

    private AdminViewModel viewModel;
    private AdminPendingShopAdapter adapter;
    private TextView tvPendingCount;
    private TextView tvEmpty;
    private String pendingActionStatus;
    private String selectedStatus = "PENDING";
    private MaterialButton btnPending;
    private MaterialButton btnApproved;
    private MaterialButton btnRejected;
    private MaterialButton btnBanned;
    private MaterialButton btnLoadMore;
    private int currentPage;
    private int loadedShopCount;
    private boolean isLastPage = true;
    private boolean isLoading;

    public AdminApprovalsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_approvals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        tvPendingCount = view.findViewById(R.id.tvAdminPendingCount);
        tvEmpty = view.findViewById(R.id.tvAdminPendingEmpty);
        btnPending = view.findViewById(R.id.btnAdminStatusPending);
        btnApproved = view.findViewById(R.id.btnAdminStatusApproved);
        btnRejected = view.findViewById(R.id.btnAdminStatusRejected);
        btnBanned = view.findViewById(R.id.btnAdminStatusBanned);
        btnLoadMore = view.findViewById(R.id.btnAdminShopsLoadMore);

        RecyclerView recyclerView = view.findViewById(R.id.rvAdminPendingShops);
        adapter = new AdminPendingShopAdapter();
        adapter.setOnShopClickListener(this::showShopDetailSheet);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        setupStatusFilters();
        btnLoadMore.setOnClickListener(v -> loadNextPage());
        viewModel.getPendingShops().observe(getViewLifecycleOwner(), this::bindShops);
        viewModel.getShopStatusResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                return;
            }
            if (result) {
                ToastUtils.success(requireContext(), successMessage());
                loadSelectedStatus(true);
                viewModel.loadOverview();
            } else {
                ToastUtils.error(requireContext(), "Không thể cập nhật trạng thái cửa hàng");
            }
            pendingActionStatus = null;
            viewModel.clearShopStatusResult();
        });

        loadSelectedStatus(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSelectedStatus(true);
    }

    private void setupStatusFilters() {
        btnPending.setOnClickListener(v -> selectStatus("PENDING"));
        btnApproved.setOnClickListener(v -> selectStatus("APPROVED"));
        btnRejected.setOnClickListener(v -> selectStatus("REJECTED"));
        btnBanned.setOnClickListener(v -> selectStatus("BANNED"));
        updateStatusButtonStates();
    }

    private void selectStatus(String status) {
        selectedStatus = status;
        updateStatusButtonStates();
        loadSelectedStatus(true);
    }

    private void loadSelectedStatus(boolean reset) {
        if (viewModel == null) {
            return;
        }
        if (reset) {
            currentPage = 0;
            loadedShopCount = 0;
            isLastPage = false;
            adapter.submitList(null);
        } else if (isLoading || isLastPage) {
            return;
        }
        isLoading = true;
        updateLoadMoreButton();
        viewModel.loadShops(selectedStatus, currentPage, PAGE_SIZE);
    }

    private void loadNextPage() {
        currentPage++;
        loadSelectedStatus(false);
    }

    private void bindShops(PageResponse<ShopResponse> page) {
        isLoading = false;
        if (page == null) {
            if (currentPage > 0) {
                currentPage--;
            } else {
                loadedShopCount = 0;
                adapter.submitList(null);
            }
            ToastUtils.error(requireContext(), "Không tải được danh sách cửa hàng");
            updateLoadMoreButton();
            return;
        }

        List<ShopResponse> shops = page == null ? null : page.getContent();
        int count = shops == null ? 0 : shops.size();
        if (currentPage == 0) {
            loadedShopCount = count;
            adapter.submitList(shops);
        } else {
            loadedShopCount += count;
            adapter.appendList(shops);
        }

        long total = page.getTotalElements();
        isLastPage = page.isLast() || loadedShopCount >= total;
        tvPendingCount.setText(total + " cửa hàng");
        tvEmpty.setVisibility(loadedShopCount == 0 ? View.VISIBLE : View.GONE);
        tvEmpty.setText("Không có cửa hàng " + statusLabel(selectedStatus).toLowerCase());
        updateLoadMoreButton();
    }

    private void updateLoadMoreButton() {
        if (btnLoadMore == null) {
            return;
        }
        boolean hasMore = loadedShopCount > 0 && !isLastPage;
        btnLoadMore.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        btnLoadMore.setEnabled(!isLoading);
        btnLoadMore.setText(isLoading ? "Đang tải..." : "Tải thêm");
    }

    private void showShopDetailSheet(ShopResponse shop) {
        if (getContext() == null || shop == null) {
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_admin_shop_detail, null);
        dialog.setContentView(content);

        TextView tvShopName = content.findViewById(R.id.tvDialogShopName);
        TextView tvShopOwner = content.findViewById(R.id.tvDialogShopOwner);
        TextView tvShopStatus = content.findViewById(R.id.tvDialogShopStatus);
        TextView tvShopAddress = content.findViewById(R.id.tvDialogShopAddress);
        TextView tvShopContact = content.findViewById(R.id.tvDialogShopContact);
        TextView tvShopTime = content.findViewById(R.id.tvDialogShopTime);
        TextView tvShopBank = content.findViewById(R.id.tvDialogShopBank);
        View closeButton = content.findViewById(R.id.btnCloseAdminShopDetail);
        MaterialButton primaryButton = content.findViewById(R.id.btnApproveShop);
        MaterialButton secondaryButton = content.findViewById(R.id.btnRejectShop);

        tvShopName.setText(nullToDefault(shop.getName(), "Chưa có tên quán"));
        tvShopOwner.setText("Liên hệ: " + nullToDefault(firstNonBlank(
                shop.getPhone(), shop.getEmail(), shop.getAddress()), "Chưa cập nhật"));
        tvShopStatus.setText(nullToDefault(shop.getStatus(), "PENDING"));
        tvShopAddress.setText("Địa chỉ: " + nullToDefault(shop.getAddress(), "Chưa cập nhật"));
        tvShopContact.setText("Liên hệ: " + nullToDefault(firstNonBlank(shop.getPhone(), shop.getEmail()), "Chưa cập nhật"));
        tvShopTime.setText("Giờ mở cửa: " + formatTimeRange(shop.getOpenTime(), shop.getCloseTime()));
        tvShopBank.setText("Ngân hàng: " + formatBankInfo(shop));

        closeButton.setOnClickListener(v -> dialog.dismiss());
        configureShopActions(dialog, shop, primaryButton, secondaryButton);
        dialog.show();
    }

    private void configureShopActions(BottomSheetDialog dialog,
                                      ShopResponse shop,
                                      MaterialButton primaryButton,
                                      MaterialButton secondaryButton) {
        String status = shop.getStatus() == null ? "PENDING" : shop.getStatus().toUpperCase();
        if ("APPROVED".equals(status)) {
            bindAction(primaryButton, "Tạm khóa", "BANNED", dialog, shop);
            bindAction(secondaryButton, "Từ chối", "REJECTED", dialog, shop);
        } else if ("REJECTED".equals(status)) {
            bindAction(primaryButton, "Duyệt lại", "APPROVED", dialog, shop);
            bindAction(secondaryButton, "Tạm khóa", "BANNED", dialog, shop);
        } else if ("BANNED".equals(status)) {
            bindAction(primaryButton, "Mở khóa", "APPROVED", dialog, shop);
            secondaryButton.setVisibility(View.GONE);
        } else {
            bindAction(primaryButton, "Phê duyệt", "APPROVED", dialog, shop);
            bindAction(secondaryButton, "Từ chối", "REJECTED", dialog, shop);
        }
    }

    private void bindAction(MaterialButton button,
                            String label,
                            String targetStatus,
                            BottomSheetDialog dialog,
                            ShopResponse shop) {
        button.setText(label);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> {
            pendingActionStatus = targetStatus;
            viewModel.updateShopStatus(shop.getId(), targetStatus);
            dialog.dismiss();
        });
    }

    private void updateStatusButtonStates() {
        setFilterButtonState(btnPending, "PENDING".equals(selectedStatus));
        setFilterButtonState(btnApproved, "APPROVED".equals(selectedStatus));
        setFilterButtonState(btnRejected, "REJECTED".equals(selectedStatus));
        setFilterButtonState(btnBanned, "BANNED".equals(selectedStatus));
    }

    private void setFilterButtonState(MaterialButton button, boolean selected) {
        int backgroundColor = requireContext().getColor(selected ? R.color.brand_orange : R.color.white);
        int strokeColor = requireContext().getColor(selected ? R.color.brand_orange : R.color.profile_divider);
        int textColor = requireContext().getColor(selected ? R.color.white : R.color.text_primary);
        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setStrokeWidth(dp(selected ? 0 : 1));
        button.setTextColor(textColor);
    }

    private String successMessage() {
        if ("APPROVED".equals(pendingActionStatus)) {
            return "Đã phê duyệt cửa hàng";
        }
        if ("REJECTED".equals(pendingActionStatus)) {
            return "Đã từ chối cửa hàng";
        }
        if ("BANNED".equals(pendingActionStatus)) {
            return "Đã khóa cửa hàng";
        }
        return "Đã cập nhật trạng thái cửa hàng";
    }

    private String statusLabel(String status) {
        if ("APPROVED".equals(status)) {
            return "Đã duyệt";
        }
        if ("REJECTED".equals(status)) {
            return "Từ chối";
        }
        if ("BANNED".equals(status)) {
            return "Đã khóa";
        }
        return "Chờ duyệt";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private String formatTimeRange(String openTime, String closeTime) {
        if ((openTime == null || openTime.trim().isEmpty())
                && (closeTime == null || closeTime.trim().isEmpty())) {
            return "Chưa cập nhật";
        }
        return nullToDefault(openTime, "--:--") + " - " + nullToDefault(closeTime, "--:--");
    }

    private String formatBankInfo(ShopResponse shop) {
        String bankName = shop.getBankName();
        String accountNumber = shop.getBankAccountNumber();
        String accountOwner = shop.getBankAccountOwner();
        if ((bankName == null || bankName.trim().isEmpty())
                && (accountNumber == null || accountNumber.trim().isEmpty())
                && (accountOwner == null || accountOwner.trim().isEmpty())) {
            return "Chưa cập nhật";
        }

        StringBuilder builder = new StringBuilder();
        if (bankName != null && !bankName.trim().isEmpty()) {
            builder.append(bankName.trim());
        }
        if (accountNumber != null && !accountNumber.trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" - ");
            }
            builder.append(accountNumber.trim());
        }
        if (accountOwner != null && !accountOwner.trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" - ");
            }
            builder.append(accountOwner.trim());
        }
        return builder.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
