package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.ReviewResponse;
import com.foodorderingapp.model.response.ShopDetailResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopMenuFoodAdapter extends RecyclerView.Adapter<ShopMenuFoodAdapter.FoodViewHolder> {

    public interface OnFoodClickListener {
        void onFoodClick(ShopDetailResponse.FoodItem food);
    }

    private final List<ShopDetailResponse.FoodItem> foods = new ArrayList<>();
    private OnFoodClickListener listener;

    public void submitList(List<ShopDetailResponse.FoodItem> newFoods) {
        foods.clear();
        if (newFoods != null) {
            foods.addAll(newFoods);
        }
        notifyDataSetChanged();
    }

    public void setOnFoodClickListener(OnFoodClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_menu_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        ShopDetailResponse.FoodItem food = foods.get(position);

        holder.tvName.setText(nullToDefault(food.getName(), "Chưa có tên món"));
        
        holder.tvDescription.setText(nullToDefault(food.getDescription(), "Chưa có mô tả"));

        StringBuilder tagBuilder = new StringBuilder();
        if (food.getCuisine() != null && !food.getCuisine().trim().isEmpty()) {
            tagBuilder.append("🌐 ").append(food.getCuisine());
        }
        if (food.getTags() != null && !food.getTags().isEmpty()) {
            if (tagBuilder.length() > 0) tagBuilder.append(" | ");
            tagBuilder.append("🏷️ ").append(android.text.TextUtils.join(", ", food.getTags()));
        }
        if (tagBuilder.length() > 0) {
            holder.tvTags.setVisibility(View.VISIBLE);
            holder.tvTags.setText(tagBuilder.toString());
        } else {
            holder.tvTags.setVisibility(View.GONE);
        }
        
        holder.tvPrice.setText(formatPrice(food.getPrice()));

        Glide.with(holder.itemView.getContext())
                .load(food.getImageUrl())
                .placeholder(R.drawable.logo_food)
                .error(R.drawable.logo_food)
                .into(holder.ivFoodImage);

        boolean available = food.isAvailable();
        holder.btnAddFood.setEnabled(available);
        holder.btnAddFood.setAlpha(available ? 1f : 0.45f);
        holder.btnAddFood.setOnClickListener(v -> {
            if (listener != null && food.isAvailable()) {
                listener.onFoodClick(food);
            }
        });

        // Set average star rating of food item and update badge dynamically
        updateBadgeState(holder.tvBadge, food, -1.0);
        holder.tvRating.setText("★ ...");
        if (food.getId() != null) {
            ApiClient.getApiService().getFoodRating(food.getId()).enqueue(new Callback<Double>() {
                @Override
                public void onResponse(Call<Double> call, Response<Double> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        double rating = response.body();
                        holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", rating));
                        updateBadgeState(holder.tvBadge, food, rating);
                    } else {
                        holder.tvRating.setText("★ N/A");
                        updateBadgeState(holder.tvBadge, food, 0.0);
                    }
                }

                @Override
                public void onFailure(Call<Double> call, Throwable t) {
                    holder.tvRating.setText("★ N/A");
                    updateBadgeState(holder.tvBadge, food, 0.0);
                }
            });
        } else {
            holder.tvRating.setText("★ N/A");
            updateBadgeState(holder.tvBadge, food, 0.0);
        }

            holder.tvRating.setOnClickListener(v -> {
                android.content.Context ctx = holder.itemView.getContext();
                View content = LayoutInflater.from(ctx).inflate(R.layout.dialog_student_reviews, null);
                TextView tvTitle = content.findViewById(R.id.tvReviewsTitle);
                if (tvTitle != null) {
                    tvTitle.setText("Đánh giá món: " + food.getName());
                }

                RecyclerView rvReviews = content.findViewById(R.id.rvStudentReviews);
                TextView tvEmpty = content.findViewById(R.id.tvStudentReviewsEmpty);

                rvReviews.setLayoutManager(new LinearLayoutManager(ctx));
                ReviewAdapter reviewAdapter = new ReviewAdapter();
                rvReviews.setAdapter(reviewAdapter);

                BottomSheetDialog dialog = new BottomSheetDialog(ctx);
                dialog.setContentView(content);
                dialog.show();

                ApiClient.getApiService().getFoodReviews(food.getId()).enqueue(new Callback<List<ReviewResponse>>() {
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
            });
        } else {
            holder.tvRating.setText("★ N/A");
            holder.tvRating.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    private void updateBadgeState(TextView tvBadge, ShopDetailResponse.FoodItem food, double rating) {
        if (tvBadge == null) return;

        // 1. Check manual tags
        if (food.getTags() != null) {
            for (String tag : food.getTags()) {
                String cleanTag = tag.trim().toLowerCase();
                if (cleanTag.contains("yêu thích") || cleanTag.contains("favorite")) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText("Yêu thích");
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_orange);
                    return;
                }
                if (cleanTag.contains("bán chạy") || cleanTag.contains("best seller")) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText("Bán chạy");
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_green);
                    return;
                }
            }
        }

        // 2. Check rating
        if (rating >= 4.0) {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText("Yêu thích");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_orange);
            return;
        }

        // 3. Check soldCount
        int soldCount = food.getSoldCount() != null ? food.getSoldCount() : 0;
        if (soldCount >= 30) {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText("Bán chạy");
            tvBadge.setBackgroundResource(R.drawable.bg_badge_green);
            return;
        }

        // 4. Otherwise hide
        tvBadge.setVisibility(View.GONE);
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvName;
        TextView tvDescription;
        TextView tvPrice;
        TextView tvTags;
        TextView tvRating;
        TextView tvBadge;
        ImageView btnAddFood;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivMenuFoodImage);
            tvName = itemView.findViewById(R.id.tvMenuFoodName);
            tvDescription = itemView.findViewById(R.id.tvMenuFoodDescription);
            tvPrice = itemView.findViewById(R.id.tvMenuFoodPrice);
            tvTags = itemView.findViewById(R.id.tvMenuFoodTags);
            tvRating = itemView.findViewById(R.id.tvMenuFoodRating);
            tvBadge = itemView.findViewById(R.id.tv_badge_label);
            btnAddFood = itemView.findViewById(R.id.btnAddFood);
        }
    }
}
