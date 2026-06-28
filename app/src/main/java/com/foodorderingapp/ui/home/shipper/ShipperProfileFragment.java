package com.foodorderingapp.ui.home.shipper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.UpdateProfileRequest;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.model.response.ReviewResponse;
import com.foodorderingapp.model.response.UserProfileResponse;
import com.foodorderingapp.ui.auth.LoginActivity;
import com.foodorderingapp.utils.ImageUrlUtils;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.utils.TokenManager;
import com.foodorderingapp.viewmodel.UploadImageViewModel;
import com.foodorderingapp.viewmodel.ViewModelFactory;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.res.ColorStateList;
import android.graphics.Color;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class ShipperProfileFragment extends Fragment {

    private static final String TAG = "ShipperProfileFragment";
    private static final String PREFS_NAME = "shipper_settings";
    private static final String KEY_IS_ONLINE = "is_online";

    private ShapeableImageView ivAvatar;
    private ImageButton btnEditAvatar;
    private TextView tvUserName, tvUserPhone, tvUserTag;
    private MaterialSwitch switchActiveStatus;
    private TextView tvActiveStatusText;
    private TextView tvStatOrders, tvStatEarnings, tvStatRating;
    private View btnEditProfile, btnLogout;

    private UserProfileResponse currentProfile;
    private UploadImageViewModel uploadImageViewModel;
    private ActivityResultLauncher<Intent> avatarPickerLauncher;
    private BottomSheetDialog editProfileDialog;
    private ImageView editAvatarPreview;
    private MaterialButton btnSaveProfileDlg;
    private String selectedAvatarUrl;

    public ShipperProfileFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        uploadImageViewModel = new ViewModelProvider(this, factory).get(UploadImageViewModel.class);
        avatarPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            previewSelectedAvatar(selectedImageUri);
                            uploadImageViewModel.clearUploadResult();
                            uploadImageViewModel.uploadImage(selectedImageUri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shipper_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Views
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btnEditAvatar = view.findViewById(R.id.btnEditAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserPhone = view.findViewById(R.id.tvUserPhone);
        tvUserTag = view.findViewById(R.id.tvUserTag);
        switchActiveStatus = view.findViewById(R.id.switchActiveStatus);
        tvActiveStatusText = view.findViewById(R.id.tvActiveStatusText);
        tvStatOrders = view.findViewById(R.id.tvStatOrders);
        tvStatEarnings = view.findViewById(R.id.tvStatEarnings);
        tvStatRating = view.findViewById(R.id.tvStatRating);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Setup Observers
        observeAvatarUpload();

        // Setup Active Status Switch
        setupActiveStatus();

        // Load Session info initially
        bindSessionInfo();

        // Setup click listeners
        btnEditAvatar.setOnClickListener(v -> openAvatarPicker());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> confirmLogout());

        // Load dynamic data from Server
        loadData();
    }

    private void bindSessionInfo() {
        TokenManager tokenManager = TokenManager.getInstance();
        String fullName = tokenManager.getFullName();
        String phone = tokenManager.getPhone();

        tvUserName.setText(fullName == null || fullName.trim().isEmpty() ? "Tài xế UniEats" : fullName);
        tvUserPhone.setText(phone == null || phone.trim().isEmpty() ? "Chưa có số điện thoại" : phone);
    }

    private void setupActiveStatus() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isOnline = prefs.getBoolean(KEY_IS_ONLINE, true);

        switchActiveStatus.setChecked(isOnline);
        updateActiveStatusUI(isOnline);

        switchActiveStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_IS_ONLINE, isChecked).apply();
            updateActiveStatusUI(isChecked);
        });
    }

    private void updateActiveStatusUI(boolean isOnline) {
        if (isOnline) {
            tvActiveStatusText.setText("Đang sẵn sàng nhận đơn");
            tvActiveStatusText.setTextColor(ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981")));
        } else {
            tvActiveStatusText.setText("Đang nghỉ ngơi");
            tvActiveStatusText.setTextColor(ColorStateList.valueOf(android.graphics.Color.parseColor("#718096")));
        }
    }

    private void loadData() {
        // Fetch profile
        ApiClient.getApiService().getMyProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    tvUserName.setText(currentProfile.getFullName());
                    tvUserPhone.setText(currentProfile.getPhone());
                    if (currentProfile.getAvatarUrl() != null && !currentProfile.getAvatarUrl().trim().isEmpty()) {
                        Glide.with(ShipperProfileFragment.this)
                                .load(ImageUrlUtils.resolveImageUrl(currentProfile.getAvatarUrl()))
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(ivAvatar);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi tải profile: ", t);
            }
        });

        // Fetch Order History for stats and earnings
        ApiClient.getApiService().getShipperOrderHistory().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderResponse>> call, @NonNull Response<List<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderResponse> orders = response.body();
                    processStatsAndEarnings(orders);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrderResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi tải lịch sử đơn hàng: ", t);
            }
        });
    }

    private void processStatsAndEarnings(List<OrderResponse> orders) {
        int completedCount = 0;
        double totalEarnings = 0.0;
        Set<String> shopIds = new HashSet<>();

        for (OrderResponse order : orders) {
            if ("COMPLETED".equalsIgnoreCase(order.getStatus())) {
                completedCount++;
                double distanceKm = calculateOrderDistance(order);
                totalEarnings += distanceKm * 5000.0; // 5k per 1km
                if (order.getShopId() != null) {
                    shopIds.add(order.getShopId());
                }
            }
        }

        tvStatOrders.setText(String.valueOf(completedCount));
        tvStatEarnings.setText(formatPrice(totalEarnings));

        // Setup chart for shipper earnings
        setupEarningsChart(getView(), orders);

        // Fetch Delivery reviews from unique shopIds to compute average rating
        if (!shopIds.isEmpty()) {
            calculateAverageDeliveryRating(new ArrayList<>(shopIds));
        } else {
            tvStatRating.setText("5.0 ★");
        }
    }

    private double calculateOrderDistance(OrderResponse order) {
        if (order.getShopLatitude() == null || order.getShopLongitude() == null ||
            order.getBuildingLatitude() == null || order.getBuildingLongitude() == null) {
            return 1.0; // Fallback distance if coordinates are missing
        }
        double distMeters = calculateDistance(
                order.getShopLatitude(), order.getShopLongitude(),
                order.getBuildingLatitude(), order.getBuildingLongitude()
        );
        return distMeters / 1000.0; // Convert to km
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        try {
            android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results);
            return results[0];
        } catch (Exception e) {
            double earthRadius = 6371000; // meters
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                       Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                       Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return earthRadius * c;
        }
    }

    private void calculateAverageDeliveryRating(List<String> shopIds) {
        final List<Integer> ratingsList = new ArrayList<>();
        final int[] requestsLeft = {shopIds.size()};

        for (String shopId : shopIds) {
            ApiClient.getApiService().getDeliveryReviews(shopId).enqueue(new Callback<List<ReviewResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<ReviewResponse>> call, @NonNull Response<List<ReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (ReviewResponse rev : response.body()) {
                            if (rev.getRating() != null) {
                                ratingsList.add(rev.getRating());
                            }
                        }
                    }
                    checkAndCalculate();
                }

                @Override
                public void onFailure(@NonNull Call<List<ReviewResponse>> call, @NonNull Throwable t) {
                    checkAndCalculate();
                }

                private void checkAndCalculate() {
                    requestsLeft[0]--;
                    if (requestsLeft[0] == 0) {
                        updateRatingUI(ratingsList);
                    }
                }
            });
        }
    }

    private void setupEarningsChart(View view, List<OrderResponse> orders) {
        if (view == null) return;
        BarChart chart = view.findViewById(R.id.chartShipperEarnings);
        if (chart == null) return;

        boolean isDark = false;
        if (getContext() != null) {
            SharedPreferences appPrefs = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            isDark = appPrefs.getBoolean("app_dark_mode", false);
        }
        int textColor = isDark ? Color.parseColor("#FFFFFF") : Color.parseColor("#64748B");
        int gridColor = isDark ? Color.parseColor("#2D2D2D") : Color.parseColor("#E2E8F0");

        java.time.LocalDate today = java.time.LocalDate.now();
        List<java.time.LocalDate> last7Days = new ArrayList<>();
        java.util.Map<java.time.LocalDate, Double> earningsMap = new java.util.HashMap<>();

        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            last7Days.add(d);
            earningsMap.put(d, 0.0);
        }

        if (orders != null) {
            for (OrderResponse order : orders) {
                if ("COMPLETED".equalsIgnoreCase(order.getStatus())) {
                    String dateStr = order.getCompletedAt() != null ? order.getCompletedAt() : order.getCreatedAt();
                    java.time.LocalDate orderDate = parseOrderDate(dateStr);
                    if (orderDate != null && earningsMap.containsKey(orderDate)) {
                        double distanceKm = calculateOrderDistance(order);
                        double earnings = distanceKm * 5000.0;
                        earningsMap.put(orderDate, earningsMap.get(orderDate) + earnings);
                    }
                }
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        java.time.format.DateTimeFormatter labelFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault());

        for (int i = 0; i < last7Days.size(); i++) {
            java.time.LocalDate d = last7Days.get(i);
            float value = earningsMap.get(d).floatValue();
            entries.add(new BarEntry(i, value));
            labels.add(d.format(labelFormatter));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Thu nhập (đ)");
        dataSet.setColor(Color.parseColor("#FF7A21")); // Orange
        dataSet.setValueTextColor(textColor);
        dataSet.setValueTextSize(9f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                if (barEntry.getY() == 0) return "";
                return String.format(Locale.getDefault(), "%.0fđ", barEntry.getY());
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        chart.setData(barData);

        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(textColor);
        
        com.github.mikephil.charting.components.XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textColor);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < labels.size()) {
                    return labels.get(idx);
                }
                return "";
            }
        });

        com.github.mikephil.charting.components.YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(gridColor);
        leftAxis.setTextColor(textColor);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "0đ";
                return String.format(Locale.getDefault(), "%.0fk", value / 1000f);
            }
        });

        chart.animateY(800);
        chart.invalidate();
    }

    private java.time.LocalDate parseOrderDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            String clean = dateStr.trim();
            int dotIndex = clean.indexOf('.');
            if (dotIndex > 0) {
                clean = clean.substring(0, dotIndex);
            }
            return java.time.LocalDateTime.parse(clean, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private void updateRatingUI(List<Integer> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            tvStatRating.setText("5.0 ★");
            return;
        }
        double sum = 0.0;
        for (int r : ratings) {
            sum += r;
        }
        double avg = sum / ratings.size();
        tvStatRating.setText(String.format(Locale.getDefault(), "%.1f ★", avg));
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    private void openAvatarPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        avatarPickerLauncher.launch(intent);
    }

    private void previewSelectedAvatar(Uri uri) {
        if (editAvatarPreview != null) {
            editAvatarPreview.setPadding(0, 0, 0, 0);
            Glide.with(this).load(uri).into(editAvatarPreview);
        }
    }

    private void observeAvatarUpload() {
        uploadImageViewModel.getUploadSuccessUrl().observe(getViewLifecycleOwner(), successUrl -> {
            if (successUrl != null) {
                selectedAvatarUrl = successUrl;
                if (editAvatarPreview != null) {
                    Glide.with(ShipperProfileFragment.this)
                            .load(ImageUrlUtils.resolveImageUrl(selectedAvatarUrl))
                            .into(editAvatarPreview);
                }
                // Save directly to profile
                saveAvatarUrlToServer(selectedAvatarUrl);
            }
        });

        uploadImageViewModel.getUploadError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                ToastUtils.error(getContext(), "Không thể tải ảnh lên: " + errorMsg);
            }
        });
    }

    private void saveAvatarUrlToServer(String avatarUrl) {
        if (currentProfile == null) return;
        UpdateProfileRequest request = new UpdateProfileRequest(
                currentProfile.getFullName(),
                currentProfile.getEmail(),
                currentProfile.getBuildingId(),
                avatarUrl
        );
        ApiClient.getApiService().updateMyProfile(request).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    ToastUtils.success(getContext(), "Cập nhật ảnh đại diện thành công!");
                    Glide.with(ShipperProfileFragment.this)
                            .load(ImageUrlUtils.resolveImageUrl(currentProfile.getAvatarUrl()))
                            .into(ivAvatar);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                ToastUtils.error(getContext(), "Lỗi cập nhật ảnh đại diện");
            }
        });
    }

    private void showEditProfileDialog() {
        if (currentProfile == null) {
            ToastUtils.info(getContext(), "Vui lòng đợi tải thông tin");
            return;
        }

        editProfileDialog = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        editProfileDialog.setContentView(content);

        EditText etFullName = content.findViewById(R.id.etProfileFullName);
        EditText etEmail = content.findViewById(R.id.etProfileEmail);
        editAvatarPreview = content.findViewById(R.id.imgProfileAvatarPreview);
        MaterialButton btnChooseAvatarDlg = content.findViewById(R.id.btnChooseProfileAvatar);
        btnSaveProfileDlg = content.findViewById(R.id.btnSaveProfile);
        View btnCancel = content.findViewById(R.id.btnCancelEditProfile);
        View layoutBuilding = content.findViewById(R.id.spinnerProfileBuilding);
        View labelBuilding = content.findViewById(R.id.spinnerProfileBuilding).getParent() instanceof ViewGroup ?
                ((ViewGroup) content.findViewById(R.id.spinnerProfileBuilding).getParent()).findViewById(R.id.spinnerProfileBuilding) : null;

        // Hide building selection for Shipper
        if (layoutBuilding != null) {
            layoutBuilding.setVisibility(View.GONE);
        }
        // Try to hide building spinner labels if any
        if (content.findViewById(R.id.spinnerProfileBuilding) != null) {
            content.findViewById(R.id.spinnerProfileBuilding).setVisibility(View.GONE);
        }

        etFullName.setText(currentProfile.getFullName());
        etEmail.setText(currentProfile.getEmail());
        selectedAvatarUrl = currentProfile.getAvatarUrl();

        if (selectedAvatarUrl != null && !selectedAvatarUrl.trim().isEmpty()) {
            editAvatarPreview.setPadding(0, 0, 0, 0);
            Glide.with(this)
                    .load(ImageUrlUtils.resolveImageUrl(selectedAvatarUrl))
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(editAvatarPreview);
        }

        btnChooseAvatarDlg.setOnClickListener(v -> openAvatarPicker());
        btnCancel.setOnClickListener(v -> editProfileDialog.dismiss());
        btnSaveProfileDlg.setOnClickListener(v -> saveProfileInfo(
                etFullName.getText().toString(),
                etEmail.getText().toString()
        ));

        editProfileDialog.setOnDismissListener(dialog -> {
            editAvatarPreview = null;
        });

        editProfileDialog.show();
    }

    private void saveProfileInfo(String fullName, String email) {
        if (fullName == null || fullName.trim().isEmpty()) {
            ToastUtils.error(getContext(), "Vui lòng nhập họ tên");
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(
                fullName.trim(),
                email != null ? email.trim() : "",
                currentProfile != null ? currentProfile.getBuildingId() : null,
                selectedAvatarUrl
        );

        if (btnSaveProfileDlg != null) {
            btnSaveProfileDlg.setEnabled(false);
        }
        ApiClient.getApiService().updateMyProfile(request).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if (editProfileDialog != null) {
                    editProfileDialog.dismiss();
                }
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    tvUserName.setText(currentProfile.getFullName());
                    tvUserPhone.setText(currentProfile.getPhone());
                    ToastUtils.success(getContext(), "Cập nhật thông tin thành công!");
                } else {
                    ToastUtils.error(getContext(), "Không thể lưu thông tin!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                if (editProfileDialog != null) {
                    editProfileDialog.dismiss();
                }
                ToastUtils.error(getContext(), "Lỗi kết nối máy chủ");
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn muốn đăng xuất khỏi tài khoản này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        String fcmToken = TokenManager.getInstance().getFcmToken();
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            finishLogout();
            return;
        }

        ApiClient.getApiService().removeDeviceToken(fcmToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                finishLogout();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                finishLogout();
            }
        });
    }

    private void finishLogout() {
        TokenManager.getInstance().clearTokens();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
