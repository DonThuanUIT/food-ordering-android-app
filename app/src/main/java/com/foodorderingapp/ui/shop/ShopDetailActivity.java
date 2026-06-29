package com.foodorderingapp.ui.shop;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.ui.chat.ChatActivity;
import com.foodorderingapp.ui.chat.AiRecommendationActivity;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.utils.TokenManager;
import com.foodorderingapp.viewmodel.ShopViewModel;

import android.view.LayoutInflater;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.ReviewResponse;
import com.foodorderingapp.ui.adapter.ReviewAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class ShopDetailActivity extends AppCompatActivity {

    private TextView tvShopName;
    private TextView tvShopAddress;
    private TextView tvShopDescription;
    private TextView tvShopStatus;
    private TextView tvShopTime;
    private ImageView ivShopHero;
    private ImageView ivShopStatusIcon;
    private LinearLayout layoutShopStatus;
    private Button btnViewMenu;
    private Button btnChatShop;
    private ImageButton btnFavorite;
    private TextView tvShopRating;
    private LinearLayout layoutShopRating;

    private ShopViewModel shopViewModel;
    private String shopId;
    private String currentOpenTime;
    private String currentCloseTime;
    private boolean favoriteUpdating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        shopId = getIntent().getStringExtra("SHOP_ID");
        if (shopId == null || shopId.isEmpty()) {
            ToastUtils.error(this, "Không tìm thấy quán");
            finish();
            return;
        }

        bindViews();
        bindClickListeners();
        showInitialShopInfo();
        loadShopRating();
        loadFavoriteStatus();

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
        ivShopHero = findViewById(R.id.ivShopHero);
        ivShopStatusIcon = findViewById(R.id.ivShopStatusIcon);
        layoutShopStatus = findViewById(R.id.layoutShopStatus);
        btnViewMenu = findViewById(R.id.btnViewMenu);
        btnChatShop = findViewById(R.id.btnChatShop);
        btnFavorite = findViewById(R.id.btnFavorite);
        tvShopRating = findViewById(R.id.tvShopRating);
        layoutShopRating = findViewById(R.id.layoutShopRating);
        btnFavorite = findViewById(R.id.btnFavorite);
    }

    private void bindClickListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> {
            boolean isStudent = "STUDENT".equalsIgnoreCase(TokenManager.getInstance().getRole());
            if (!isStudent || favoriteUpdating || shopId == null || shopId.isEmpty()) {
                return;
            }
            favoriteUpdating = true;
            btnFavorite.setEnabled(false);
            ApiClient.getApiService().toggleFavoriteShop(shopId).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    favoriteUpdating = false;
                    btnFavorite.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        boolean isFav = response.body();
                        btnFavorite.setSelected(isFav);
                        if (isFav) {
                            ToastUtils.success(ShopDetailActivity.this, "Đã thêm vào yêu thích");
                        } else {
                            ToastUtils.info(ShopDetailActivity.this, "Đã bỏ yêu thích");
                        }
                    } else {
                        ToastUtils.error(ShopDetailActivity.this, "Không thể cập nhật yêu thích");
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    favoriteUpdating = false;
                    btnFavorite.setEnabled(true);
                    ToastUtils.error(ShopDetailActivity.this, "Lỗi mạng: " + t.getMessage());
                }
            });
        });

        btnViewMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, ShopMenuActivity.class);
            intent.putExtra("SHOP_ID", shopId);
            intent.putExtra("SHOP_NAME", tvShopName.getText().toString());
            startActivity(intent);
        });

        boolean isStudent = "STUDENT".equalsIgnoreCase(TokenManager.getInstance().getRole());
        btnChatShop.setVisibility(isStudent ? android.view.View.VISIBLE : android.view.View.GONE);
        btnChatShop.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_SHOP_ID, shopId);
            intent.putExtra(ChatActivity.EXTRA_SHOP_NAME, tvShopName.getText().toString());
            startActivity(intent);
        });



        layoutShopRating.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.foodorderingapp.ui.review.VendorReviewsActivity.class);
            intent.putExtra("SHOP_ID", shopId);
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
        currentOpenTime = openTime;
        currentCloseTime = closeTime;

        tvShopName.setText(nullToDefault(name, "Chưa có tên quán"));
        tvShopAddress.setText(nullToDefault(address, "Đang tải địa chỉ..."));
        tvShopDescription.setText(nullToDefault(description, "Chưa có mô tả"));
        tvShopTime.setText(formatTimeRange(openTime, closeTime));
        if (getIntent().hasExtra("SHOP_CURRENTLY_OPEN")) {
            bindOpeningStatus(getIntent().getBooleanExtra("SHOP_CURRENTLY_OPEN", false));
        } else {
            bindStatus(displayStatus);
        }
    }

    private void observeShopDetail() {
        shopViewModel.getShopDetail().observe(this, detail -> {
            if (detail == null) {
                ToastUtils.error(this, "Không tải được chi tiết quán");
                finish();
                return;
            }

            tvShopName.setText(nullToDefault(detail.getName(), tvShopName.getText().toString()));
            tvShopAddress.setText(nullToDefault(detail.getAddress(), "Chưa cập nhật địa chỉ"));
            tvShopDescription.setText(nullToDefault(detail.getDescription(), tvShopDescription.getText().toString()));
            currentOpenTime = detail.getOpenTime();
            currentCloseTime = detail.getCloseTime();
            tvShopTime.setText(formatTimeRange(currentOpenTime, currentCloseTime));
            if (detail.getCurrentlyOpen() != null) {
                bindOpeningStatus(detail.getCurrentlyOpen());
            } else {
                bindStatus(detail.getIsOpen());
            }
            bindHeroImage(detail.getCoverUrl(), detail.getLogoUrl());
        });
    }

    private void loadShopRating() {
        ApiClient.getApiService().getShopRating(shopId).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double rating = response.body();
                    tvShopRating.setText(String.format(Locale.getDefault(), "%.1f", rating));
                } else {
                    tvShopRating.setText("N/A");
                }
            }

            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                tvShopRating.setText("N/A");
            }
        });
    }

    private void loadFavoriteStatus() {
        boolean isStudent = "STUDENT".equalsIgnoreCase(TokenManager.getInstance().getRole());
        if (!isStudent) {
            btnFavorite.setVisibility(View.GONE);
            return;
        }
        btnFavorite.setVisibility(View.VISIBLE);
        ApiClient.getApiService().isFavoriteShop(shopId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    btnFavorite.setSelected(response.body());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                // ignore
            }
        });
    }



    private void bindHeroImage(String coverUrl, String logoUrl) {
        String imageUrl = coverUrl != null && !coverUrl.trim().isEmpty() ? coverUrl : logoUrl;
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.logo_food)
                .error(R.drawable.logo_food)
                .into(ivShopHero);
    }

    private void bindStatus(Boolean isOpen) {
        if (isOpen == null) {
            return;
        }
        bindStatus(isOpen, currentOpenTime, currentCloseTime);
    }

    private void bindStatus(String displayStatus) {
        boolean manuallyOpen = !isClosedStatus(displayStatus);
        bindStatus(manuallyOpen, currentOpenTime, currentCloseTime);
    }

    private void bindStatus(boolean manuallyOpen, String openTime, String closeTime) {
        boolean opening = manuallyOpen && isWithinOpeningHours(openTime, closeTime);
        int statusColor = opening ? Color.parseColor("#00A843") : Color.parseColor("#777777");
        tvShopStatus.setText(opening ? "Đang mở cửa" : "Đang đóng cửa");
        tvShopStatus.setTextColor(statusColor);
        ivShopStatusIcon.setImageTintList(ColorStateList.valueOf(statusColor));
        layoutShopStatus.setBackgroundResource(opening
                ? R.drawable.bg_shop_detail_status_open
                : R.drawable.bg_shop_detail_status_closed);
    }

    private void bindOpeningStatus(boolean opening) {
        int statusColor = opening ? Color.parseColor("#00A843") : Color.parseColor("#777777");
        tvShopStatus.setTextColor(statusColor);
        tvShopStatus.setText(opening ? "Đang mở cửa" : "Đang đóng cửa");
        ivShopStatusIcon.setImageTintList(ColorStateList.valueOf(statusColor));
        layoutShopStatus.setBackgroundResource(opening
                ? R.drawable.bg_shop_detail_status_open
                : R.drawable.bg_shop_detail_status_closed);
    }

    private boolean isClosedStatus(String displayStatus) {
        if (displayStatus == null || displayStatus.trim().isEmpty()) {
            return false;
        }
        String normalized = displayStatus.trim().toUpperCase(Locale.ROOT);
        return normalized.contains("CLOSED")
                || normalized.contains("DONG")
                || normalized.contains("ĐÓNG");
    }

    private boolean isWithinOpeningHours(String openTime, String closeTime) {
        if (openTime == null || openTime.trim().isEmpty()
                || closeTime == null || closeTime.trim().isEmpty()) {
            return true;
        }

        try {
            LocalTime open = LocalTime.parse(openTime.trim());
            LocalTime close = LocalTime.parse(closeTime.trim());
            LocalTime now = LocalTime.now();

            if (open.equals(close)) {
                return true;
            }
            if (close.isAfter(open)) {
                return !now.isBefore(open) && !now.isAfter(close);
            }
            return !now.isBefore(open) || !now.isAfter(close);
        } catch (DateTimeParseException exception) {
            return true;
        }
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
