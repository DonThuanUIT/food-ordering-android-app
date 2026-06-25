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
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.model.response.ReviewResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorFoodRatingAdapter extends RecyclerView.Adapter<VendorFoodRatingAdapter.ViewHolder> {

    private final List<FoodResponse> foods = new ArrayList<>();

    public void submitList(List<FoodResponse> newList) {
        foods.clear();
        if (newList != null) {
            foods.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vendor_food_rating, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodResponse food = foods.get(position);

        holder.tvName.setText(food.getName() != null ? food.getName() : "Không có tên");

        Glide.with(holder.itemView.getContext())
                .load(food.getImageUrl())
                .placeholder(R.drawable.logo_food)
                .error(R.drawable.logo_food)
                .into(holder.ivImage);

        // Fetch food rating and reviews count dynamically
        holder.tvStars.setText("★ ...");
        holder.tvCount.setText("(...)");

        if (food.getId() != null) {
            String foodIdStr = food.getId().toString();
            ApiClient.getApiService().getFoodReviews(foodIdStr).enqueue(new Callback<List<ReviewResponse>>() {
                @Override
                public void onResponse(Call<List<ReviewResponse>> call, Response<List<ReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ReviewResponse> list = response.body();
                        int count = list.size();
                        double sum = 0;
                        for (ReviewResponse r : list) {
                            sum += r.getRating();
                        }
                        double avg = count > 0 ? sum / count : 0.0;
                        holder.tvStars.setText(String.format(Locale.getDefault(), "★ %.1f", avg));
                        holder.tvCount.setText(String.format(Locale.getDefault(), "(%d đánh giá)", count));
                    } else {
                        holder.tvStars.setText("★ 0.0");
                        holder.tvCount.setText("(0 đánh giá)");
                    }
                }

                @Override
                public void onFailure(Call<List<ReviewResponse>> call, Throwable t) {
                    holder.tvStars.setText("★ N/A");
                    holder.tvCount.setText("(0 đánh giá)");
                }
            });

            // Click to view detailed food reviews
            holder.itemView.setOnClickListener(v -> {
                android.content.Context ctx = holder.itemView.getContext();
                View content = LayoutInflater.from(ctx).inflate(R.layout.dialog_student_reviews, null);
                TextView tvTitle = content.findViewById(R.id.tvReviewsTitle);
                if (tvTitle != null) {
                    tvTitle.setText("Đánh giá món: " + food.getName());
                }

                RecyclerView rvReviews = content.findViewById(R.id.rvStudentReviews);
                TextView tvEmpty = content.findViewById(R.id.tvStudentReviewsEmpty);

                rvReviews.setLayoutManager(new LinearLayoutManager(ctx));
                VendorReviewListAdapter adapter = new VendorReviewListAdapter();
                rvReviews.setAdapter(adapter);

                BottomSheetDialog dialog = new BottomSheetDialog(ctx);
                dialog.setContentView(content);
                dialog.show();

                ApiClient.getApiService().getFoodReviews(foodIdStr).enqueue(new Callback<List<ReviewResponse>>() {
                    @Override
                    public void onResponse(Call<List<ReviewResponse>> call, Response<List<ReviewResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ReviewResponse> list = response.body();
                            adapter.submitList(list);
                            boolean isEmpty = list.isEmpty();
                            if (tvEmpty != null) {
                                tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                                if (isEmpty) {
                                    tvEmpty.setText("Chưa có đánh giá nào cho món này");
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
        }
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvStars;
        TextView tvCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivVendorFoodRatingImage);
            tvName = itemView.findViewById(R.id.tvVendorFoodRatingName);
            tvStars = itemView.findViewById(R.id.tvVendorFoodRatingStars);
            tvCount = itemView.findViewById(R.id.tvVendorFoodRatingCount);
        }
    }
}
