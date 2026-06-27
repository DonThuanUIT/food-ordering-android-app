package com.foodorderingapp.ui.home.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.UserProfileResponse;
import com.foodorderingapp.ui.auth.LoginActivity;
import com.foodorderingapp.utils.TokenManager;
import com.foodorderingapp.utils.ToastUtils;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileFragment extends Fragment {

    private TextView tvAdminName;
    private TextView tvAdminPhone;
    private TextView tvAdminEmail;
    private TextView tvAdminRole;
    private MaterialButton btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAdminName = view.findViewById(R.id.tvAdminProfileName);
        tvAdminPhone = view.findViewById(R.id.tvAdminProfilePhone);
        tvAdminEmail = view.findViewById(R.id.tvAdminProfileEmail);
        tvAdminRole = view.findViewById(R.id.tvAdminProfileRole);
        btnLogout = view.findViewById(R.id.btnAdminProfileLogout);

        bindSessionFallback();
        btnLogout.setOnClickListener(v -> confirmLogout());
        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    private void bindSessionFallback() {
        TokenManager tokenManager = TokenManager.getInstance();
        tvAdminName.setText(defaultText(tokenManager.getFullName(), "Quản trị viên UniEats"));
        tvAdminPhone.setText(defaultText(tokenManager.getPhone(), "Chưa có số điện thoại"));
        tvAdminEmail.setText("Chưa có email");
        tvAdminRole.setText("ADMIN");
    }

    private void loadProfile() {
        ApiClient.getApiService().getMyProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    bindProfile(response.body());
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                if (isAdded()) {
                    ToastUtils.info(getContext(), "Không tải được thông tin cá nhân");
                }
            }
        });
    }

    private void bindProfile(UserProfileResponse profile) {
        tvAdminName.setText(defaultText(profile.getFullName(), "Quản trị viên UniEats"));
        tvAdminPhone.setText(defaultText(profile.getPhone(), "Chưa có số điện thoại"));
        tvAdminEmail.setText(defaultText(profile.getEmail(), "Chưa có email"));
        tvAdminRole.setText(defaultText(profile.getRole(), "ADMIN"));
        TokenManager.getInstance().saveUserSession(
                profile.getPhone(),
                profile.getRole(),
                profile.getFullName()
        );
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất?")
                .setMessage("Bạn muốn đăng xuất khỏi tài khoản admin này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        TokenManager.getInstance().clearTokens();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
