package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.ShopDetailResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        
        StringBuilder descBuilder = new StringBuilder();
        if (food.getDescription() != null && !food.getDescription().trim().isEmpty()) {
            descBuilder.append(food.getDescription());
        }
        if (food.getCuisine() != null && !food.getCuisine().trim().isEmpty()) {
            if (descBuilder.length() > 0) descBuilder.append(" | ");
            descBuilder.append("🌐 ").append(food.getCuisine());
        }
        if (food.getTags() != null && !food.getTags().isEmpty()) {
            if (descBuilder.length() > 0) descBuilder.append(" | ");
            descBuilder.append("🏷️ ").append(android.text.TextUtils.join(", ", food.getTags()));
        }
        holder.tvDescription.setText(descBuilder.length() > 0 ? descBuilder.toString() : "Chưa có mô tả");
        
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

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvName;
        TextView tvDescription;
        TextView tvPrice;
        ImageView btnAddFood;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivMenuFoodImage);
            tvName = itemView.findViewById(R.id.tvMenuFoodName);
            tvDescription = itemView.findViewById(R.id.tvMenuFoodDescription);
            tvPrice = itemView.findViewById(R.id.tvMenuFoodPrice);
            btnAddFood = itemView.findViewById(R.id.btnAddFood);
        }
    }
}
