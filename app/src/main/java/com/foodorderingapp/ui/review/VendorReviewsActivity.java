package com.foodorderingapp.ui.review;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.model.response.ReviewResponse;
import com.foodorderingapp.ui.adapter.VendorFoodRatingAdapter;
import com.foodorderingapp.ui.adapter.VendorReviewListAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorReviewsActivity extends AppCompatActivity {

    private TextView tvShopRatingValue;
    private RatingBar ratingBarShop;
    private TabLayout tabLayout;
    private RecyclerView rvReviewsList;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private String shopId;
    private VendorReviewListAdapter reviewAdapter;
    private VendorFoodRatingAdapter foodRatingAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_reviews);

        shopId = getIntent().getStringExtra("SHOP_ID");
        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin cửa hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupToolbar();
        setupRecyclerViews();
        setupTabLayout();

        loadShopOverallRating();
        loadDataForTab(0); // Load Shop reviews by default
    }

    private void bindViews() {
        tvShopRatingValue = findViewById(R.id.tvVendorShopRatingValue);
        ratingBarShop = findViewById(R.id.ratingBarVendorShop);
        tabLayout = findViewById(R.id.tabLayoutVendorReviews);
        rvReviewsList = findViewById(R.id.rvVendorReviewsList);
        progressBar = findViewById(R.id.pbVendorReviews);
        tvEmpty = findViewById(R.id.tvVendorReviewsEmpty);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarVendorReviews);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Phản hồi & Đánh giá");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        rvReviewsList.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new VendorReviewListAdapter();
        foodRatingAdapter = new VendorFoodRatingAdapter();
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadDataForTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadShopOverallRating() {
        ApiClient.getApiService().getShopRating(shopId).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(@NonNull Call<Double> call, @NonNull Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double rating = response.body();
                    tvShopRatingValue.setText(String.format(Locale.getDefault(), "%.1f", rating));
                    ratingBarShop.setRating((float) rating);
                } else {
                    tvShopRatingValue.setText("0.0");
                    ratingBarShop.setRating(0f);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Double> call, @NonNull Throwable t) {
                tvShopRatingValue.setText("0.0");
                ratingBarShop.setRating(0f);
            }
        });
    }

    private void loadDataForTab(int tabIndex) {
        showLoading(true);
        if (tabIndex == 0) {
            // Tab Cửa hàng
            rvReviewsList.setAdapter(reviewAdapter);
            ApiClient.getApiService().getShopReviews(shopId).enqueue(new Callback<List<ReviewResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<ReviewResponse>> call, @NonNull Response<List<ReviewResponse>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<ReviewResponse> list = response.body();
                        reviewAdapter.submitList(list);
                        showEmpty(list.isEmpty(), "Chưa có đánh giá nào về dịch vụ của quán");
                    } else {
                        showEmpty(true, "Không thể tải đánh giá cửa hàng");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<ReviewResponse>> call, @NonNull Throwable t) {
                    showLoading(false);
                    showEmpty(true, "Lỗi mạng: " + t.getMessage());
                }
            });
        } else if (tabIndex == 1) {
            // Tab Món ăn
            rvReviewsList.setAdapter(foodRatingAdapter);
            UUID uuidShop = UUID.fromString(shopId);
            ApiClient.getApiService().getAllFoods(uuidShop, null, 0, 100).enqueue(new Callback<PageResponse<FoodResponse>>() {
                @Override
                public void onResponse(@NonNull Call<PageResponse<FoodResponse>> call, @NonNull Response<PageResponse<FoodResponse>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().getContent() != null) {
                        List<FoodResponse> list = response.body().getContent();
                        foodRatingAdapter.submitList(list);
                        showEmpty(list.isEmpty(), "Cửa hàng chưa thiết lập món ăn nào");
                    } else {
                        showEmpty(true, "Không thể tải danh sách món ăn");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PageResponse<FoodResponse>> call, @NonNull Throwable t) {
                    showLoading(false);
                    showEmpty(true, "Lỗi mạng: " + t.getMessage());
                }
            });
        } else if (tabIndex == 2) {
            // Tab Vận chuyển (Order Reviews)
            rvReviewsList.setAdapter(reviewAdapter);
            ApiClient.getApiService().getDeliveryReviews(shopId).enqueue(new Callback<List<ReviewResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<ReviewResponse>> call, @NonNull Response<List<ReviewResponse>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<ReviewResponse> list = response.body();
                        reviewAdapter.submitList(list);
                        showEmpty(list.isEmpty(), "Chưa có đánh giá nào về khâu vận chuyển");
                    } else {
                        showEmpty(true, "Không thể tải đánh giá vận chuyển");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<ReviewResponse>> call, @NonNull Throwable t) {
                    showLoading(false);
                    showEmpty(true, "Lỗi mạng: " + t.getMessage());
                }
            });
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            tvEmpty.setVisibility(View.GONE);
            rvReviewsList.setVisibility(View.GONE);
        } else {
            rvReviewsList.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty(boolean empty, String message) {
        if (empty) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(message);
            rvReviewsList.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvReviewsList.setVisibility(View.VISIBLE);
        }
    }
}
