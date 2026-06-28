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
    private TextView tvShopRating;
    private LinearLayout layoutShopRating;

    private ShopViewModel shopViewModel;
    private String shopId;
    private String currentOpenTime;
    private String currentCloseTime;

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
        tvShopRating = findViewById(R.id.tvShopRating);
        layoutShopRating = findViewById(R.id.layoutShopRating);
    }

    private void bindClickListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnFavorite = findViewById(R.id.btnFavorite);

        btnBack.setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                ToastUtils.success(this, "Đã thêm vào yêu thích");
            } else {
                ToastUtils.info(this, "Đã bỏ yêu thích");
            }
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

        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton btnShopAiAssistant = findViewById(R.id.btn_shop_ai_assistant);
        btnShopAiAssistant.setVisibility(isStudent ? android.view.View.VISIBLE : android.view.View.GONE);
        btnShopAiAssistant.setOnClickListener(v -> {
            Intent intent = new Intent(this, AiRecommendationActivity.class);
            intent.putExtra(AiRecommendationActivity.EXTRA_SHOP_ID, shopId);
            startActivity(intent);
        });

        layoutShopRating.setOnClickListener(v -> showReviewsDialog());
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
        bindStatus(displayStatus);
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
            bindStatus(detail.getIsOpen());
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

    private void showReviewsDialog() {
        if (shopId == null || shopId.isEmpty()) {
            return;
        }

        View content = LayoutInflater.from(this).inflate(R.layout.dialog_student_reviews, null);
        TextView tvTitle = content.findViewById(R.id.tvReviewsTitle);
        if (tvTitle != null) {
            tvTitle.setText("Đánh giá của quán");
        }

        RecyclerView rvReviews = content.findViewById(R.id.rvStudentReviews);
        TextView tvEmpty = content.findViewById(R.id.tvStudentReviewsEmpty);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        ReviewAdapter reviewAdapter = new ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(content);
        dialog.show();

        ApiClient.getApiService().getShopReviews(shopId).enqueue(new Callback<List<ReviewResponse>>() {
            @Override
            public void onResponse(Call<List<ReviewResponse>> call, Response<List<ReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReviewResponse> list = response.body();
                    reviewAdapter.submitList(list);
                    boolean isEmpty = list.isEmpty();
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                        if (isEmpty) {
                            tvEmpty.setText("Chưa có đánh giá nào");
                        }
                    }
                } else {
                    if (tvEmpty != null) {
                        tvEmpty.setText("Không tải được đánh giá");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ReviewResponse>> call, Throwable t) {
                if (tvEmpty != null) {
                    tvEmpty.setText("Lỗi mạng: " + t.getMessage());
                }
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

    private boolean isClosedStatus(String displayStatus) {
        if (displayStatus == null || displayStatus.trim().isEmpty()) {
            return false;
        }
        String normalized = displayStatus.trim().toUpperCase(Locale.ROOT);
        return normalized.contains("CLOSED")
                || normalized.contains("DONG")
                || normalized.contains("ĐÓNG")
                || normalized.contains("ÄÃ“NG");
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
