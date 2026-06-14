package com.foodorderingapp.ui.shop;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.foodorderingapp.R;
import com.foodorderingapp.viewmodel.ShopViewModel;

public class ShopDetailActivity extends AppCompatActivity {

    private TextView tvShopName;
    private TextView tvShopAddress;
    private TextView tvShopDescription;
    private TextView tvShopStatus;
    private TextView tvShopTime;
    private ImageView ivShopStatusIcon;
    private LinearLayout layoutShopStatus;
    private Button btnViewMenu;

    private ShopViewModel shopViewModel;
    private String shopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        shopId = getIntent().getStringExtra("SHOP_ID");
        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy quán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        bindClickListeners();
        showInitialShopInfo();

        shopViewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        observeShopDetail();
        shopViewModel.loadShopDetail(shopId);
    }

    private void bindViews() {
        tvShopName = findViewById(R.id.tvShopDetailName);
        tvShopAddress = findViewById(R.id.tvShopDetailAddress);
        tvShopDescription = findViewById(R.id.tvShopDetailDescription);
        tvShopStatus = findViewById(R.id.tvShopStatus);
        tvShopTime = findViewById(R.id.tvShopTime);
        ivShopStatusIcon = findViewById(R.id.ivShopStatusIcon);
        layoutShopStatus = findViewById(R.id.layoutShopStatus);
        btnViewMenu = findViewById(R.id.btnViewMenu);
    }

    private void bindClickListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnFavorite = findViewById(R.id.btnFavorite);

        btnBack.setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            Toast.makeText(this, v.isSelected() ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
        });

        btnViewMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, ShopMenuActivity.class);
            intent.putExtra("SHOP_ID", shopId);
            intent.putExtra("SHOP_NAME", tvShopName.getText().toString());
            startActivity(intent);
        });
    }

    private void showInitialShopInfo() {
        String name = getIntent().getStringExtra("SHOP_NAME");
        String address = getIntent().getStringExtra("SHOP_ADDRESS");
        String description = getIntent().getStringExtra("SHOP_DESCRIPTION");
        String openTime = getIntent().getStringExtra("SHOP_OPEN_TIME");
        String closeTime = getIntent().getStringExtra("SHOP_CLOSE_TIME");
        String displayStatus = getIntent().getStringExtra("SHOP_DISPLAY_STATUS");

        tvShopName.setText(nullToDefault(name, "Chưa có tên quán"));
        tvShopAddress.setText(nullToDefault(address, "Đang tải địa chỉ..."));
        tvShopDescription.setText(nullToDefault(description, "Chưa có mô tả"));
        tvShopTime.setText(formatTimeRange(openTime, closeTime));
        bindStatus(displayStatus);
    }

    private void observeShopDetail() {
        shopViewModel.getShopDetail().observe(this, detail -> {
            if (detail == null) {
                Toast.makeText(this, "Không tải được chi tiết quán", Toast.LENGTH_SHORT).show();
                return;
            }

            tvShopName.setText(nullToDefault(detail.getName(), tvShopName.getText().toString()));
            tvShopAddress.setText(nullToDefault(detail.getAddress(), "Chưa cập nhật địa chỉ"));
            tvShopDescription.setText(nullToDefault(detail.getDescription(), tvShopDescription.getText().toString()));
        });
    }

    private void bindStatus(String displayStatus) {
        boolean opening = "OPENING".equalsIgnoreCase(displayStatus)
                || "OPEN".equalsIgnoreCase(displayStatus)
                || displayStatus == null
                || displayStatus.trim().isEmpty();

        int statusColor = opening ? Color.parseColor("#00A843") : Color.parseColor("#777777");
        tvShopStatus.setText(opening ? "Đang mở cửa" : "Đang đóng cửa");
        tvShopStatus.setTextColor(statusColor);
        ivShopStatusIcon.setImageTintList(ColorStateList.valueOf(statusColor));
        layoutShopStatus.setBackgroundResource(opening
                ? R.drawable.bg_shop_detail_status_open
                : R.drawable.bg_shop_detail_status_closed);
    }

    private String formatTimeRange(String openTime, String closeTime) {
        return nullToDefault(openTime, "--:--") + " - " + nullToDefault(closeTime, "--:--");
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}
