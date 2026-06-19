package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.SpendingSummaryResponse;
import com.foodorderingapp.model.response.UserProfileResponse;
import com.foodorderingapp.ui.auth.LoginActivity;
import com.foodorderingapp.utils.TokenManager;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.StudentProfileViewModel;

import java.text.NumberFormat;
import java.util.Locale;

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
        loadRemoteProfile(view);
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
        tvUserEmail.setText(isBlank(phone) ? "Chua co so dien thoai" : phone);
        tvUserTag.setText("Vai tro: " + formatRole(role));
        tvMonthlySpending.setText("0d");
    }

    private void loadRemoteProfile(View view) {
        StudentProfileViewModel viewModel = new ViewModelProvider(this).get(StudentProfileViewModel.class);
        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                bindProfile(view, profile);
            }
        });
        viewModel.getSpendingSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                bindSpending(view, summary);
            }
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                ToastUtils.info(getContext(), message);
            }
        });

        viewModel.loadProfile();
        viewModel.loadSpendingSummary();
    }

    private void bindProfile(View view, UserProfileResponse profile) {
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
        TextView tvUserTag = view.findViewById(R.id.tvUserTag);
        ImageView ivAvatar = view.findViewById(R.id.ivAvatar);

        tvUserName.setText(isBlank(profile.getFullName()) ? "Sinh vien UniEats" : profile.getFullName());

        String emailOrPhone = !isBlank(profile.getEmail()) ? profile.getEmail() : profile.getPhone();
        tvUserEmail.setText(isBlank(emailOrPhone) ? "Chua co thong tin lien he" : emailOrPhone);

        String building = isBlank(profile.getBuildingName()) ? "Chua chon toa nha" : profile.getBuildingName();
        tvUserTag.setText(formatRole(profile.getRole()) + " - " + building);

        if (!isBlank(profile.getAvatarUrl()) && getContext() != null) {
            Glide.with(this)
                    .load(profile.getAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(ivAvatar);
        }
    }

    private void bindSpending(View view, SpendingSummaryResponse summary) {
        TextView tvMonthlySpending = view.findViewById(R.id.tvMonthlySpending);
        tvMonthlySpending.setText(formatPrice(summary.getTotalSpent()));
    }

    private void setupActions(View view) {
        view.findViewById(R.id.rowPaymentMethod).setOnClickListener(v -> showPaymentInfo());
        view.findViewById(R.id.rowMyReviews).setOnClickListener(v -> openHistoryTab());
        view.findViewById(R.id.rowSupport).setOnClickListener(v -> showSupportInfo());
        view.findViewById(R.id.btnEditAvatar).setOnClickListener(v -> showAvatarComingSoon());
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
    }

    private void showPaymentInfo() {
        ToastUtils.info(getContext(), "Thanh toan khi nhan hang");
    }

    private void showSupportInfo() {
        if (getContext() == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Ho tro")
                .setMessage("Vui long lien he quay ho tro UniEats neu don hang hoac tai khoan gap van de.")
                .setPositiveButton("Da hieu", null)
                .show();
    }

    private void showAvatarComingSoon() {
        ToastUtils.info(getContext(), "Chinh sua anh dai dien chua duoc ho tro");
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
                .setTitle("Dang xuat")
                .setMessage("Ban muon dang xuat khoi tai khoan nay?")
                .setNegativeButton("Huy", null)
                .setPositiveButton("Dang xuat", (dialog, which) -> logout())
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
            return "Sinh vien";
        }
        if (isBlank(role)) {
            return "Sinh vien";
        }
        return role;
    }

    private String defaultDisplayName(String role) {
        return "STUDENT".equalsIgnoreCase(role) ? "Sinh vien UniEats" : "Nguoi dung UniEats";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "d";
    }
}
