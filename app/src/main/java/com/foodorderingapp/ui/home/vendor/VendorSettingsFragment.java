package com.foodorderingapp.ui.home.vendor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.ShopUpdateRequest;
import com.foodorderingapp.model.response.ShopResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorSettingsFragment extends Fragment {

    private ImageView imgShopCover;
    private ImageView imgShopLogo;
    private TextView tvShopName;
    private TextView tvShopDescription;

    private SwitchCompat switchOperational;
    private TextView tvOperationalStatus;
    private CardView cardStatusIconBg;
    private ImageView ivStatusIcon;

    private TextView btnUpdateHours;
    private TextView tvHoursMonFri;
    private TextView tvHoursSat;
    private TextView tvHoursSun;

    private Button btnManageLeads;
    private Button btnViewStatements;
    private CheckBox checkboxOrderAlerts;
    private CheckBox checkboxPromotions;
    private CheckBox checkboxTurboMode;
    private Button btnDeactivate;

    private View btnEditCover;
    private View btnEditLogo;

    private SharedPreferences sharedPreferences;
    private UUID currentShopId;
    private ShopResponse currentShopData;

    private final CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            toggleShopStatusOnServer(isChecked);
        }
    };

    public VendorSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences("vendor_settings_pref", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_settings, container, false);

        // Bind views
        imgShopCover = view.findViewById(R.id.img_shop_cover);
        imgShopLogo = view.findViewById(R.id.img_shop_logo);
        tvShopName = view.findViewById(R.id.tv_shop_name);
        tvShopDescription = view.findViewById(R.id.tv_shop_description);

        switchOperational = view.findViewById(R.id.switch_operational);
        tvOperationalStatus = view.findViewById(R.id.tv_operational_status);
        cardStatusIconBg = view.findViewById(R.id.card_status_icon_bg);
        ivStatusIcon = view.findViewById(R.id.iv_status_icon);

        btnUpdateHours = view.findViewById(R.id.btn_update_hours);
        tvHoursMonFri = view.findViewById(R.id.tv_hours_mon_fri);
        tvHoursSat = view.findViewById(R.id.tv_hours_sat);
        tvHoursSun = view.findViewById(R.id.tv_hours_sun);

        btnManageLeads = view.findViewById(R.id.btn_manage_leads);
        btnViewStatements = view.findViewById(R.id.btn_view_statements);
        checkboxOrderAlerts = view.findViewById(R.id.checkbox_order_alerts);
        checkboxPromotions = view.findViewById(R.id.checkbox_promotions);
        checkboxTurboMode = view.findViewById(R.id.checkbox_turbo_mode);
        btnDeactivate = view.findViewById(R.id.btn_deactivate);

        btnEditCover = view.findViewById(R.id.btn_edit_cover);
        btnEditLogo = view.findViewById(R.id.btn_edit_logo);

        setupClickListeners();
        loadSavedPreferences();
        fetchShopInfo();

        return view;
    }

    private void setupClickListeners() {
        // Edit cover and logo click
        View.OnClickListener photoEditListener = v -> {
            Toast.makeText(getContext(), "Tính năng chọn ảnh sẽ sớm ra mắt!", Toast.LENGTH_SHORT).show();
        };
        if (btnEditCover != null) btnEditCover.setOnClickListener(photoEditListener);
        if (btnEditLogo != null) btnEditLogo.setOnClickListener(photoEditListener);

        // Profile Details Click (opens edit profile dialog)
        View.OnClickListener profileEditListener = v -> {
            if (currentShopData != null) {
                showEditProfileDialog(currentShopData);
            } else {
                Toast.makeText(getContext(), "Đang tải dữ liệu cửa hàng...", Toast.LENGTH_SHORT).show();
            }
        };
        if (tvShopName != null) tvShopName.setOnClickListener(profileEditListener);
        if (tvShopDescription != null) tvShopDescription.setOnClickListener(profileEditListener);

        // Preference checkboxes listeners
        checkboxOrderAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("order_alerts", isChecked).apply();
            Toast.makeText(getContext(), isChecked ? "Đã bật thông báo đơn hàng" : "Đã tắt thông báo đơn hàng", Toast.LENGTH_SHORT).show();
        });
        checkboxPromotions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("promo_alerts", isChecked).apply();
            Toast.makeText(getContext(), isChecked ? "Đã bật khuyến mãi" : "Đã tắt khuyến mãi", Toast.LENGTH_SHORT).show();
        });
        checkboxTurboMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("turbo_mode", isChecked).apply();
            Toast.makeText(getContext(), isChecked ? "Đã kích hoạt Chế độ Turbo" : "Đã tắt Chế độ Turbo", Toast.LENGTH_SHORT).show();
        });

        // Business Hour Update button click (opens edit profile dialog)
        btnUpdateHours.setOnClickListener(profileEditListener);

        // Manage Leads click
        btnManageLeads.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuyển hướng đến Quản lý khách hàng tiềm năng", Toast.LENGTH_SHORT).show();
        });

        // View Statements click
        btnViewStatements.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Hiển thị lịch sử sao kê thanh toán", Toast.LENGTH_SHORT).show();
        });

        // Deactivate button click with warning alert dialog
        btnDeactivate.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Vô hiệu hóa Cửa hàng?")
                .setMessage("Bạn có chắc chắn muốn tạm thời vô hiệu hóa cửa hàng? Cửa hàng của bạn sẽ bị ẩn khỏi mọi kết quả tìm kiếm trên DormDash.")
                .setPositiveButton("Vô hiệu hóa", (dialog, which) -> {
                    Toast.makeText(getContext(), "Cửa hàng đã bị vô hiệu hóa tạm thời!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Hủy bỏ", (dialog, which) -> dialog.dismiss())
                .show();
        });
    }

    private void loadSavedPreferences() {
        checkboxOrderAlerts.setChecked(sharedPreferences.getBoolean("order_alerts", true));
        checkboxPromotions.setChecked(sharedPreferences.getBoolean("promo_alerts", false));
        checkboxTurboMode.setChecked(sharedPreferences.getBoolean("turbo_mode", true));
    }

    private void updateOperationalStatusUI(boolean isOpen) {
        if (isOpen) {
            tvOperationalStatus.setText("Open");
            tvOperationalStatus.setTextColor(Color.parseColor("#319795")); // Active Teal
            cardStatusIconBg.setCardBackgroundColor(Color.parseColor("#E6FFFA")); // Light Teal background
            ivStatusIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#319795")));
        } else {
            tvOperationalStatus.setText("Closed");
            tvOperationalStatus.setTextColor(Color.parseColor("#718096")); // Gray text
            cardStatusIconBg.setCardBackgroundColor(Color.parseColor("#EDF2F7")); // Light Gray background
            ivStatusIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#718096")));
        }
    }

    private void fetchShopInfo() {
        ApiClient.getApiService().getVendorShops().enqueue(new Callback<List<ShopResponse>>() {
            @Override
            public void onResponse(Call<List<ShopResponse>> call, Response<List<ShopResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    bindShopData(response.body().get(0));
                }
            }

            @Override
            public void onFailure(Call<List<ShopResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không thể tải thông tin cửa hàng", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void bindShopData(ShopResponse shop) {
        currentShopData = shop;
        currentShopId = shop.getId();

        if (tvShopName != null) {
            tvShopName.setText(shop.getName());
        }
        if (tvShopDescription != null) {
            tvShopDescription.setText(shop.getDescription() != null ? shop.getDescription() : "Chưa có mô tả cửa hàng");
        }

        // Format and show operating hours
        String openStr = formatTime12Hour(shop.getOpenTime());
        String closeStr = formatTime12Hour(shop.getCloseTime());
        String formattedHours = openStr + " - " + closeStr;

        if (tvHoursMonFri != null) tvHoursMonFri.setText(formattedHours);
        if (tvHoursSat != null) tvHoursSat.setText(formattedHours);
        if (tvHoursSun != null) tvHoursSun.setText(formattedHours);

        // Bind switch toggle status programmatically without triggering listener recursion
        boolean isOpen = shop.getIsActive() != null ? shop.getIsActive() : true;
        switchOperational.setOnCheckedChangeListener(null);
        switchOperational.setChecked(isOpen);
        switchOperational.setOnCheckedChangeListener(switchListener);
        updateOperationalStatusUI(isOpen);

        // Load static logo fallback
        if (getContext() != null && imgShopLogo != null) {
            Glide.with(getContext())
                .load(R.drawable.logo_food)
                .placeholder(R.drawable.logo_food)
                .into(imgShopLogo);
        }
    }

    private String formatTime12Hour(String timeStr) {
        if (timeStr == null || !timeStr.contains(":")) return "08:00 AM";
        try {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int min = Integer.parseInt(parts[1]);
            String ampm = hour >= 12 ? "PM" : "AM";
            int h = hour % 12;
            if (h == 0) h = 12;
            return String.format(java.util.Locale.US, "%02d:%02d %s", h, min, ampm);
        } catch (Exception e) {
            return timeStr;
        }
    }

    private void toggleShopStatusOnServer(boolean isActive) {
        if (currentShopId == null) return;
        Map<String, Boolean> body = new HashMap<>();
        body.put("isActive", isActive);

        ApiClient.getApiService().toggleShopStatus(currentShopId, body).enqueue(new Callback<ShopResponse>() {
            @Override
            public void onResponse(Call<ShopResponse> call, Response<ShopResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindShopData(response.body());
                    String status = isActive ? "Open" : "Closed";
                    Toast.makeText(getContext(), "Cửa hàng đã chuyển sang trạng thái: " + status, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Không thể cập nhật trạng thái hoạt động", Toast.LENGTH_SHORT).show();
                    revertSwitchUI(!isActive);
                }
            }

            @Override
            public void onFailure(Call<ShopResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi mạng, không thể cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
                revertSwitchUI(!isActive);
            }
        });
    }

    private void revertSwitchUI(boolean targetState) {
        switchOperational.setOnCheckedChangeListener(null);
        switchOperational.setChecked(targetState);
        switchOperational.setOnCheckedChangeListener(switchListener);
        updateOperationalStatusUI(targetState);
    }

    private void showEditProfileDialog(ShopResponse shop) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Shop Profile");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final android.widget.EditText etName = new android.widget.EditText(requireContext());
        etName.setHint("Shop Name");
        etName.setText(shop.getName());
        layout.addView(etName);

        final android.widget.EditText etDesc = new android.widget.EditText(requireContext());
        etDesc.setHint("Description");
        etDesc.setText(shop.getDescription() != null ? shop.getDescription() : "");
        layout.addView(etDesc);

        final android.widget.EditText etAddress = new android.widget.EditText(requireContext());
        etAddress.setHint("Address");
        etAddress.setText(shop.getAddress() != null ? shop.getAddress() : "");
        layout.addView(etAddress);

        final android.widget.EditText etOpen = new android.widget.EditText(requireContext());
        etOpen.setHint("Opening Time (HH:mm)");
        etOpen.setText(shop.getOpenTime() != null ? shop.getOpenTime().substring(0, 5) : "08:00");
        layout.addView(etOpen);

        final android.widget.EditText etClose = new android.widget.EditText(requireContext());
        etClose.setHint("Closing Time (HH:mm)");
        etClose.setText(shop.getCloseTime() != null ? shop.getCloseTime().substring(0, 5) : "22:00");
        layout.addView(etClose);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String addr = etAddress.getText().toString().trim();
            String open = etOpen.getText().toString().trim();
            String close = etClose.getText().toString().trim();

            if (name.isEmpty() || open.isEmpty() || close.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
                return;
            }

            ShopUpdateRequest req = new ShopUpdateRequest(name, addr, desc, open, close);
            saveShopProfile(req);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void saveShopProfile(ShopUpdateRequest req) {
        if (currentShopId == null) return;
        ApiClient.getApiService().updateShopProfile(currentShopId, req).enqueue(new Callback<ShopResponse>() {
            @Override
            public void onResponse(Call<ShopResponse> call, Response<ShopResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Đã cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    bindShopData(response.body());
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShopResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}