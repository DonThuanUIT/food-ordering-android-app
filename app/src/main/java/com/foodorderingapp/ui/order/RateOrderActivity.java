package com.foodorderingapp.ui.order;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.ReviewSubmitRequest;
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RateOrderActivity extends AppCompatActivity {

    private RatingBar ratingOrder;
    private EditText etCommentOrder;
    private RatingBar ratingShop;
    private EditText etCommentShop;
    private RecyclerView rvFoodRatings;
    private MaterialButton btnSubmit;

    private String orderId;
    private final List<OrderDetailResponse> foodItems = new ArrayList<>();
    private FoodRatingAdapter adapter;

    // Store ratings and comments for foods
    private final Map<String, Integer> foodRatingsMap = new HashMap<>();
    private final Map<String, String> foodCommentsMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_order);

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupToolbar();
        setupRecyclerView();
        loadOrderDetails();
    }

    private void bindViews() {
        ratingOrder = findViewById(R.id.rating_bar_order);
        etCommentOrder = findViewById(R.id.et_comment_order);
        ratingShop = findViewById(R.id.rating_bar_shop);
        etCommentShop = findViewById(R.id.et_comment_shop);
        rvFoodRatings = findViewById(R.id.rv_food_ratings);
        btnSubmit = findViewById(R.id.btn_submit_review);

        btnSubmit.setOnClickListener(v -> submitReviews());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvFoodRatings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodRatingAdapter(foodItems);
        rvFoodRatings.setAdapter(adapter);
    }

    private void loadOrderDetails() {
        ArrayList<String> foodIds = getIntent().getStringArrayListExtra("FOOD_IDS");
        ArrayList<String> foodNames = getIntent().getStringArrayListExtra("FOOD_NAMES");
        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("FOOD_IMAGES");

        if (foodIds != null && foodNames != null && foodIds.size() == foodNames.size()) {
            for (int i = 0; i < foodIds.size(); i++) {
                OrderDetailResponse item = new OrderDetailResponse();
                item.setFoodId(foodIds.get(i));
                item.setFoodName(foodNames.get(i));
                if (imageUrls != null && i < imageUrls.size()) {
                    item.setImageUrl(imageUrls.get(i));
                }
                foodItems.add(item);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void submitReviews() {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang gửi...");

        ReviewSubmitRequest request = new ReviewSubmitRequest();
        request.setOrderRating((int) ratingOrder.getRating());
        request.setOrderComment(etCommentOrder.getText().toString().trim());

        request.setShopRating((int) ratingShop.getRating());
        request.setShopComment(etCommentShop.getText().toString().trim());

        List<ReviewSubmitRequest.FoodReviewItem> foodReviews = new ArrayList<>();
        for (OrderDetailResponse item : foodItems) {
            String foodId = item.getFoodId();
            if (foodId != null) {
                int rating = foodRatingsMap.containsKey(foodId) ? foodRatingsMap.get(foodId) : 5;
                String comment = foodCommentsMap.containsKey(foodId) ? foodCommentsMap.get(foodId) : "";
                foodReviews.add(new ReviewSubmitRequest.FoodReviewItem(foodId, rating, comment));
            }
        }
        request.setFoodReviews(foodReviews);

        ApiClient.getApiService().createReview(orderId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Gửi đánh giá");
                if (response.isSuccessful()) {
                    Toast.makeText(RateOrderActivity.this, "Đánh giá đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RateOrderActivity.this, "Gửi đánh giá thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Gửi đánh giá");
                Toast.makeText(RateOrderActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class FoodRatingAdapter extends RecyclerView.Adapter<FoodRatingAdapter.ViewHolder> {
        private final List<OrderDetailResponse> items;

        FoodRatingAdapter(List<OrderDetailResponse> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_rating, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OrderDetailResponse item = items.get(position);
            holder.tvName.setText(item.getFoodName());

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_chef_cook)
                        .into(holder.imgFood);
            } else {
                holder.imgFood.setImageResource(R.drawable.ic_chef_cook);
            }

            String foodId = item.getFoodId();
            if (foodId != null) {
                if (!foodRatingsMap.containsKey(foodId)) {
                    foodRatingsMap.put(foodId, 5);
                }
                holder.ratingBar.setRating(foodRatingsMap.get(foodId));

                holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                    foodRatingsMap.put(foodId, (int) rating);
                });

                holder.etComment.setText(foodCommentsMap.containsKey(foodId) ? foodCommentsMap.get(foodId) : "");
                holder.etComment.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        foodCommentsMap.put(foodId, s.toString().trim());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgFood;
            TextView tvName;
            RatingBar ratingBar;
            EditText etComment;

            ViewHolder(View itemView) {
                super(itemView);
                imgFood = itemView.findViewById(R.id.img_food);
                tvName = itemView.findViewById(R.id.tv_food_name);
                ratingBar = itemView.findViewById(R.id.rating_bar_food);
                etComment = itemView.findViewById(R.id.et_comment_food);
            }
        }
    }
}
