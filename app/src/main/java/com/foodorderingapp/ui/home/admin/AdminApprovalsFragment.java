package com.foodorderingapp.ui.home.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.foodorderingapp.R;
import com.foodorderingapp.utils.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AdminApprovalsFragment extends Fragment {

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

        View pendingShopCard = view.findViewById(R.id.cardPendingShop);
        pendingShopCard.setOnClickListener(v -> showShopDetailSheet());
    }

    private void showShopDetailSheet() {
        if (getContext() == null) {
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_admin_shop_detail, null);
        dialog.setContentView(content);

        View closeButton = content.findViewById(R.id.btnCloseAdminShopDetail);
        TextView approveButton = content.findViewById(R.id.btnApproveShop);
        TextView rejectButton = content.findViewById(R.id.btnRejectShop);

        closeButton.setOnClickListener(v -> dialog.dismiss());
        approveButton.setOnClickListener(v -> {
            ToastUtils.success(requireContext(), "Đã phê duyệt cửa hàng mẫu");
            dialog.dismiss();
        });
        rejectButton.setOnClickListener(v -> {
            ToastUtils.error(requireContext(), "Đã từ chối cửa hàng mẫu");
            dialog.dismiss();
        });

        dialog.show();
    }
}
