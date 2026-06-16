package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.ui.auth.LoginActivity;
import com.foodorderingapp.utils.TokenManager;

public class StudentProfileFragment extends Fragment {

    public StudentProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindSessionInfo(view);
        setupActions(view);
    }

    private void bindSessionInfo(View view) {
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
        TextView tvUserTag = view.findViewById(R.id.tvUserTag);
        TextView tvMonthlySpending = view.findViewById(R.id.tvMonthlySpending);

        TokenManager tokenManager = TokenManager.getInstance();
        String role = tokenManager.getRole();
        String phone = tokenManager.getPhone();
        String fullName = tokenManager.getFullName();

        tvUserName.setText(isBlank(fullName) ? defaultDisplayName(role) : fullName);
        tvUserEmail.setText(isBlank(phone) ? "Chưa có số điện thoại" : phone);
        tvUserTag.setText("Vai trò: " + formatRole(role));
        tvMonthlySpending.setText("0đ");
    }

    private void setupActions(View view) {
        view.findViewById(R.id.rowPaymentMethod).setOnClickListener(v -> showPaymentInfo());
        view.findViewById(R.id.rowMyReviews).setOnClickListener(v -> openHistoryTab());
        view.findViewById(R.id.rowSupport).setOnClickListener(v -> showSupportInfo());
        view.findViewById(R.id.btnEditAvatar).setOnClickListener(v -> showAvatarComingSoon());
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
    }

    private void showPaymentInfo() {
        Toast.makeText(getContext(), "Thanh toán khi nhận hàng", Toast.LENGTH_SHORT).show();
    }

    private void showSupportInfo() {
        if (getContext() == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Hỗ trợ")
                .setMessage("Vui lòng liên hệ quầy hỗ trợ UniEats nếu đơn hàng hoặc tài khoản gặp vấn đề.")
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    private void showAvatarComingSoon() {
        Toast.makeText(getContext(), "Chỉnh sửa ảnh đại diện chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
    }

    private void openHistoryTab() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("USER_ROLE", "STUDENT");
        intent.putExtra("OPEN_TAB", "HISTORY");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void confirmLogout() {
        if (getContext() == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn muốn đăng xuất khỏi tài khoản này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        TokenManager.getInstance().clearTokens();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String formatRole(String role) {
        if ("STUDENT".equalsIgnoreCase(role)) {
            return "Sinh viên";
        }
        if (isBlank(role)) {
            return "Sinh viên";
        }
        return role;
    }

    private String defaultDisplayName(String role) {
        return "STUDENT".equalsIgnoreCase(role) ? "Sinh viên UniEats" : "Người dùng UniEats";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
