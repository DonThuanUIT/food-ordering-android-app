package com.foodorderingapp.ui.home.admin;

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

import java.util.List;

public class AdminApprovalsFragment extends Fragment {

    private AdminViewModel viewModel;
    private AdminPendingShopAdapter adapter;
    private TextView tvPendingCount;
    private TextView tvEmpty;
    private String pendingActionStatus;

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

        RecyclerView recyclerView = view.findViewById(R.id.rvAdminPendingShops);
        adapter = new AdminPendingShopAdapter();
        adapter.setOnShopClickListener(this::showShopDetailSheet);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getPendingShops().observe(getViewLifecycleOwner(), this::bindPendingShops);
        viewModel.getShopStatusResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                return;
            }
            if (result) {
                ToastUtils.success(requireContext(), successMessage());
                viewModel.loadPendingShops();
                viewModel.loadOverview();
            } else {
                ToastUtils.error(requireContext(), "Không thể cập nhật trạng thái cửa hàng");
            }
            pendingActionStatus = null;
            viewModel.clearShopStatusResult();
        });

        viewModel.loadPendingShops();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadPendingShops();
        }
    }

    private void bindPendingShops(PageResponse<ShopResponse> page) {
        List<ShopResponse> shops = page == null ? null : page.getContent();
        int count = shops == null ? 0 : shops.size();
        tvPendingCount.setText(count + " mới");
        tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        adapter.submitList(shops);
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
        View closeButton = content.findViewById(R.id.btnCloseAdminShopDetail);
        TextView approveButton = content.findViewById(R.id.btnApproveShop);
        TextView rejectButton = content.findViewById(R.id.btnRejectShop);

        tvShopName.setText(nullToDefault(shop.getName(), "Chưa có tên quán"));
        tvShopOwner.setText("Liên hệ: " + nullToDefault(firstNonBlank(shop.getPhone(), shop.getEmail(), shop.getAddress()), "Chưa cập nhật"));
        tvShopStatus.setText(nullToDefault(shop.getStatus(), "PENDING"));

        closeButton.setOnClickListener(v -> dialog.dismiss());
        approveButton.setOnClickListener(v -> {
            pendingActionStatus = "APPROVED";
            viewModel.updateShopStatus(shop.getId(), "APPROVED");
            dialog.dismiss();
        });
        rejectButton.setOnClickListener(v -> {
            pendingActionStatus = "REJECTED";
            viewModel.updateShopStatus(shop.getId(), "REJECTED");
            dialog.dismiss();
        });

        dialog.show();
    }

    private String successMessage() {
        if ("APPROVED".equals(pendingActionStatus)) {
            return "Đã phê duyệt cửa hàng";
        }
        if ("REJECTED".equals(pendingActionStatus)) {
            return "Đã từ chối cửa hàng";
        }
        return "Đã cập nhật trạng thái cửa hàng";
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
}
