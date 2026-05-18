package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.FoodExploreResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;
public class FoodExploreAdapter extends RecyclerView.Adapter<FoodExploreAdapter.FoodViewHolder> {

    private final List<FoodExploreResponse> foods = new ArrayList<>();

    public void submitList(List<FoodExploreResponse> newFoods) {
        foods.clear();
        if (newFoods != null) {
            foods.addAll(newFoods);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_explore, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodExploreResponse food = foods.get(position);

        holder.tvFoodName.setText(nullToDefault(food.getFoodName(), "Chưa có tên món"));
        holder.tvShopName.setText(nullToDefault(food.getShopName(), "Chưa có tên quán"));
        holder.tvFoodPrice.setText(formatPrice(food.getPrice()));
        Glide.with(holder.itemView.getContext())
                .load(food.getFoodImageUrl())
                .placeholder(R.drawable.logo_food)
                .error(R.drawable.logo_food)
                .into(holder.ivFoodImage);
        holder.btnAddToCart.setOnClickListener(v -> {
            if (addToCartClickListener != null) {
                addToCartClickListener.onAddToCart(food);
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
        TextView tvFoodName;
        TextView tvShopName;
        TextView tvFoodPrice;
        ImageButton btnAddToCart;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }

    //xuly sk theem vaof gio hang
    public interface OnAddToCartClickListener {
        void onAddToCart(FoodExploreResponse food);
    }
    private OnAddToCartClickListener addToCartClickListener;
    public void setOnAddToCartClickListener(OnAddToCartClickListener listener) {
        this.addToCartClickListener = listener;
    }
}