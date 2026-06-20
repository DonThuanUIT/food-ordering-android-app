package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.model.request.UpdateProfileRequest;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.SpendingSummaryResponse;
import com.foodorderingapp.model.response.UserProfileResponse;
import com.foodorderingapp.ui.auth.LoginActivity;
import com.foodorderingapp.utils.TokenManager;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.StudentProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentProfileFragment extends Fragment {
    private final List<BuildingResponse> buildingOptions = new ArrayList<>();
    private StudentProfileViewModel viewModel;
    private UserProfileResponse currentProfile;
    private BottomSheetDialog editProfileDialog;

    public StudentProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StudentProfileViewModel.class);
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
        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                currentProfile = profile;
                bindProfile(view, profile);
                TokenManager.getInstance().saveUserSession(
                        profile.getPhone(),
                        profile.getRole(),
                        profile.getFullName()
                );
            }
        });
        viewModel.getSpendingSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                bindSpending(view, summary);
            }
        });
        viewModel.getBuildings().observe(getViewLifecycleOwner(), buildings -> {
            buildingOptions.clear();
            if (buildings != null) {
                buildingOptions.addAll(buildings);
            }
        });
        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                return;
            }
            if (result) {
                ToastUtils.success(getContext(), "Đã cập nhật hồ sơ");
                if (editProfileDialog != null) {
                    editProfileDialog.dismiss();
                    editProfileDialog = null;
                }
            } else {
                ToastUtils.error(getContext(), "Không thể cập nhật hồ sơ");
            }
            viewModel.clearUpdateResult();
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                ToastUtils.info(getContext(), message);
            }
        });

        viewModel.loadProfile();
        viewModel.loadSpendingSummary();
        viewModel.loadBuildings();
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
        view.findViewById(R.id.rowEditProfile).setOnClickListener(v -> showEditProfileSheet());
        view.findViewById(R.id.rowPaymentMethod).setOnClickListener(v -> showPaymentInfo());
        view.findViewById(R.id.rowMyReviews).setOnClickListener(v -> openHistoryTab());
        view.findViewById(R.id.rowSupport).setOnClickListener(v -> showSupportInfo());
        view.findViewById(R.id.btnEditAvatar).setOnClickListener(v -> showEditProfileSheet());
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

    private void showEditProfileSheet() {
        if (getContext() == null) {
            return;
        }
        if (currentProfile == null) {
            ToastUtils.info(getContext(), "Đang tải hồ sơ");
            viewModel.loadProfile();
            return;
        }

        editProfileDialog = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null);
        editProfileDialog.setContentView(content);

        EditText etFullName = content.findViewById(R.id.etProfileFullName);
        EditText etEmail = content.findViewById(R.id.etProfileEmail);
        EditText etAvatarUrl = content.findViewById(R.id.etProfileAvatarUrl);
        Spinner spinnerBuilding = content.findViewById(R.id.spinnerProfileBuilding);
        View btnSave = content.findViewById(R.id.btnSaveProfile);
        View btnCancel = content.findViewById(R.id.btnCancelEditProfile);

        etFullName.setText(nonNull(currentProfile.getFullName()));
        etEmail.setText(nonNull(currentProfile.getEmail()));
        etAvatarUrl.setText(nonNull(currentProfile.getAvatarUrl()));

        String[] selectedBuildingId = {currentProfile.getBuildingId()};
        bindBuildingSpinner(spinnerBuilding, selectedBuildingId);

        btnCancel.setOnClickListener(v -> editProfileDialog.dismiss());
        btnSave.setOnClickListener(v -> saveProfile(
                etFullName.getText().toString(),
                etEmail.getText().toString(),
                selectedBuildingId[0],
                etAvatarUrl.getText().toString()
        ));

        editProfileDialog.show();
    }

    private void bindBuildingSpinner(Spinner spinner, String[] selectedBuildingId) {
        List<String> labels = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        labels.add("Giữ nguyên tòa nhà");
        ids.add(currentProfile.getBuildingId());

        int selectedIndex = 0;
        for (BuildingResponse building : buildingOptions) {
            if (building == null || isBlank(building.getId())) {
                continue;
            }
            labels.add(isBlank(building.getName()) ? "Tòa nhà" : building.getName());
            ids.add(building.getId());
            if (!isBlank(currentProfile.getBuildingId())
                    && currentProfile.getBuildingId().equals(building.getId())) {
                selectedIndex = labels.size() - 1;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selectedIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBuildingId[0] = ids.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void saveProfile(String fullName, String email, String buildingId, String avatarUrl) {
        String cleanName = normalize(fullName);
        String cleanEmail = normalize(email);
        String cleanAvatarUrl = normalize(avatarUrl);

        if (isBlank(cleanName)) {
            ToastUtils.error(getContext(), "Vui lòng nhập họ tên");
            return;
        }
        if (!isBlank(cleanEmail) && !cleanEmail.contains("@")) {
            ToastUtils.error(getContext(), "Email không hợp lệ");
            return;
        }

        viewModel.updateProfile(new UpdateProfileRequest(
                cleanName,
                cleanEmail,
                buildingId,
                cleanAvatarUrl
        ));
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

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String nonNull(String value) {
        return value == null ? "" : value;
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "d";
    }
}
