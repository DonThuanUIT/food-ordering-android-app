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
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.model.response.ReviewResponse;
import com.foodorderingapp.model.response.StudentReviewResponse;
import com.foodorderingapp.ui.adapter.StudentReviewListAdapter;
import com.foodorderingapp.ui.adapter.StudentReviewListAdapter.StudentReviewWrapper;
import com.foodorderingapp.utils.TokenManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentReviewsActivity extends AppCompatActivity {

    private TextView tvOverallRating;
    private RatingBar ratingBarOverall;
    private TextView tvTotalReviews;
    private TabLayout tabLayout;
    private RecyclerView rvReviewsList;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private StudentReviewListAdapter adapter;

    private final List<StudentReviewWrapper> shopReviews = new ArrayList<>();
    private final List<StudentReviewWrapper> foodReviews = new ArrayList<>();
    private final List<StudentReviewWrapper> deliveryReviews = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_reviews);

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupTabLayout();

        loadAllStudentReviews();
    }

    private void bindViews() {
        tvOverallRating = findViewById(R.id.tvStudentOverallRating);
        ratingBarOverall = findViewById(R.id.ratingBarStudent);
        tvTotalReviews = findViewById(R.id.tvStudentTotalReviews);
        tabLayout = findViewById(R.id.tabLayoutStudentReviews);
        rvReviewsList = findViewById(R.id.rvStudentReviewsList);
        progressBar = findViewById(R.id.pbStudentReviews);
        tvEmpty = findViewById(R.id.tvStudentReviewsEmpty);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarStudentReviews);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đánh giá & Phản hồi");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvReviewsList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentReviewListAdapter();
        rvReviewsList.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                displayDataForTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show, String message) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        tvEmpty.setText(message);
    }

    private void loadAllStudentReviews() {
        showLoading(true);
        shopReviews.clear();
        foodReviews.clear();
        deliveryReviews.clear();

        String currentStudentName = TokenManager.getInstance().getFullName();
        if (currentStudentName == null || currentStudentName.isEmpty()) {
            // Fallback if full name is missing in session
            currentStudentName = "Tôi";
        }
        final String studentName = currentStudentName;

        // Fetch Order History and Delivery Reviews in parallel
        ApiClient.getApiService().getOrderHistory().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderResponse>> call, @NonNull Response<List<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderResponse> orders = response.body();
                    fetchRemainingReviews(orders, studentName);
                } else {
                    showLoading(false);
                    Toast.makeText(StudentReviewsActivity.this, "Không thể tải lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrderResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(StudentReviewsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRemainingReviews(List<OrderResponse> orders, String studentName) {
        // Map order details for easy name matching
        Map<String, String> shopIdToNameMap = new HashMap<>();
        Map<String, String> foodNames = new HashMap<>();
        Map<String, String> foodShopNames = new HashMap<>();
        Set<String> uniqueShopIds = new HashSet<>();
        Set<String> uniqueFoodIds = new HashSet<>();

        for (OrderResponse order : orders) {
            if (order.getShopId() != null) {
                shopIdToNameMap.put(order.getShopId(), order.getShopName());
                uniqueShopIds.add(order.getShopId());

                if (order.getDetails() != null) {
                    for (OrderDetailResponse detail : order.getDetails()) {
                        if (detail.getFoodId() != null) {
                            uniqueFoodIds.add(detail.getFoodId());
                            foodNames.put(detail.getFoodId(), detail.getFoodName());
                            foodShopNames.put(detail.getFoodId(), order.getShopName());
                        }
                    }
                }
            }
        }

        // Calculate total tasks to synchronize
        int totalTasks = 1 + uniqueShopIds.size() + uniqueFoodIds.size();
        AtomicInteger completedTasks = new AtomicInteger(0);

        Runnable onTaskCompleted = () -> {
            int current = completedTasks.incrementAndGet();
            if (current >= totalTasks) {
                runOnUiThread(() -> {
                    showLoading(false);
                    updateOverallStats();
                    displayDataForTab(tabLayout.getSelectedTabPosition());
                });
            }
        };

        // Task 1: Load Delivery Reviews
        ApiClient.getApiService().getMyReviews().enqueue(new Callback<List<StudentReviewResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<StudentReviewResponse>> call, @NonNull Response<List<StudentReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (StudentReviewResponse sr : response.body()) {
                        // Map StudentReviewResponse to ReviewResponse structure
                        ReviewResponse review = new ReviewResponse();
                        review.setId(sr.getId());
                        review.setRating(sr.getRating());
                        review.setComment(sr.getComment());
                        review.setCreatedAt(sr.getCreatedAt());

                        String shopName = sr.getShopName() != null ? sr.getShopName() : "Cửa hàng";
                        String orderIdShort = sr.getOrderId() != null ? sr.getOrderId().substring(0, Math.min(8, sr.getOrderId().length())) : "";

                        deliveryReviews.add(new StudentReviewWrapper(review, "DELIVERY", "Giao hàng - " + shopName, "Đơn hàng #" + orderIdShort));
                    }
                }
                onTaskCompleted.run();
            }

            @Override
            public void onFailure(@NonNull Call<List<StudentReviewResponse>> call, @NonNull Throwable t) {
                onTaskCompleted.run();
            }
        });

        // Task 2: Fetch Shop Reviews
        for (String shopId : uniqueShopIds) {
            final String finalShopId = shopId;
            ApiClient.getApiService().getShopReviews(shopId).enqueue(new Callback<List<ReviewResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<ReviewResponse>> call, @NonNull Response<List<ReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String shopName = shopIdToNameMap.get(finalShopId);
                        if (shopName == null) shopName = "Cửa hàng";
                        for (ReviewResponse r : response.body()) {
                            if (r.getUser() != null && studentName.equalsIgnoreCase(r.getUser().getFullName())) {
                                shopReviews.add(new StudentReviewWrapper(r, "SHOP", "Cửa hàng - " + shopName, "Đánh giá dịch vụ"));
                            }
                        }
                    }
                    onTaskCompleted.run();
                }

                @Override
                public void onFailure(@NonNull Call<List<ReviewResponse>> call, @NonNull Throwable t) {
                    onTaskCompleted.run();
                }
            });
        }

        // Task 3: Fetch Food Reviews
        for (String foodId : uniqueFoodIds) {
            final String finalFoodId = foodId;
            ApiClient.getApiService().getFoodReviews(foodId).enqueue(new Callback<List<ReviewResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<ReviewResponse>> call, @NonNull Response<List<ReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String fName = foodNames.get(finalFoodId);
                        String sName = foodShopNames.get(finalFoodId);
                        if (fName == null) fName = "Món ăn";
                        if (sName == null) sName = "Cửa hàng";
                        for (ReviewResponse r : response.body()) {
                            if (r.getUser() != null && studentName.equalsIgnoreCase(r.getUser().getFullName())) {
                                foodReviews.add(new StudentReviewWrapper(r, "FOOD", "Món ăn - " + fName, "Cửa hàng: " + sName));
                            }
                        }
                    }
                    onTaskCompleted.run();
                }

                @Override
                public void onFailure(@NonNull Call<List<ReviewResponse>> call, @NonNull Throwable t) {
                    onTaskCompleted.run();
                }
            });
        }
    }

    private void updateOverallStats() {
        int total = shopReviews.size() + foodReviews.size() + deliveryReviews.size();
        double sum = 0;
        for (StudentReviewWrapper wrapper : shopReviews) {
            sum += wrapper.getReview().getRating() != null ? wrapper.getReview().getRating() : 5;
        }
        for (StudentReviewWrapper wrapper : foodReviews) {
            sum += wrapper.getReview().getRating() != null ? wrapper.getReview().getRating() : 5;
        }
        for (StudentReviewWrapper wrapper : deliveryReviews) {
            sum += wrapper.getReview().getRating() != null ? wrapper.getReview().getRating() : 5;
        }

        double avg = total > 0 ? sum / total : 5.0;

        tvOverallRating.setText(String.format(Locale.getDefault(), "%.1f", avg));
        ratingBarOverall.setRating((float) avg);
        tvTotalReviews.setText("Tổng số: " + total + " đánh giá");
    }

    private void displayDataForTab(int tabIndex) {
        if (tabIndex == 0) {
            adapter.submitList(shopReviews);
            showEmpty(shopReviews.isEmpty(), "Bạn chưa gửi đánh giá cửa hàng nào");
        } else if (tabIndex == 1) {
            adapter.submitList(foodReviews);
            showEmpty(foodReviews.isEmpty(), "Bạn chưa gửi đánh giá món ăn nào");
        } else {
            adapter.submitList(deliveryReviews);
            showEmpty(deliveryReviews.isEmpty(), "Bạn chưa gửi đánh giá vận chuyển nào");
        }
    }
}
