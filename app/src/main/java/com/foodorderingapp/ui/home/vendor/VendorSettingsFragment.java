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
import com.foodorderingapp.model.response.ShopResponse;

import java.util.List;
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
        // Switch toggle action
        switchOperational.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateOperationalStatusUI(isChecked);
            sharedPreferences.edit().putBoolean("shop_open", isChecked).apply();
            String status = isChecked ? "Mở cửa (Open)" : "Đóng cửa (Closed)";
            Toast.makeText(getContext(), "Trạng thái hoạt động: " + status, Toast.LENGTH_SHORT).show();
        });

        // Edit cover and logo click
        View.OnClickListener photoEditListener = v -> {
            Toast.makeText(getContext(), "Tính năng chọn ảnh sẽ sớm ra mắt!", Toast.LENGTH_SHORT).show();
        };
        if (btnEditCover != null) btnEditCover.setOnClickListener(photoEditListener);
        if (btnEditLogo != null) btnEditLogo.setOnClickListener(photoEditListener);

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

        // Business Hour Update button click
        btnUpdateHours.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng cập nhật giờ làm việc", Toast.LENGTH_SHORT).show();
        });

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
        boolean isShopOpen = sharedPreferences.getBoolean("shop_open", true);
        switchOperational.setChecked(isShopOpen);
        updateOperationalStatusUI(isShopOpen);

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
                    ShopResponse shop = response.body().get(0);
                    currentShopId = shop.getId();

                    if (tvShopName != null) {
                        tvShopName.setText(shop.getName());
                    }
                    if (tvShopDescription != null && shop.getDescription() != null) {
                        tvShopDescription.setText(shop.getDescription());
                    }

                    // Dynamically load logo using Glide if available
                    if (getContext() != null && imgShopLogo != null) {
                        // Normally we load logo image URL, but fallback to sample placeholder if null
                        Glide.with(getContext())
                            .load(R.drawable.logo_food) // Default/fallback resource
                            .placeholder(R.drawable.logo_food)
                            .into(imgShopLogo);
                    }
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
}