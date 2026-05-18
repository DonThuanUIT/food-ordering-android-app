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
import com.foodorderingapp.model.response.CartItemResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class CartFoodAdapter extends RecyclerView.Adapter<CartFoodAdapter.FoodVH> {

    private final List<CartItemResponse> items = new ArrayList<>();

    public void submitList(List<CartItemResponse> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_food, parent, false);
        return new FoodVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodVH holder, int position) {
        CartItemResponse item = items.get(position);

        holder.tvName.setText(item.getFoodName());
        holder.tvPrice.setText(formatPrice(item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        Glide.with(holder.itemView.getContext())
                .load(item.getFoodImageUrl())
                .placeholder(R.drawable.logo_food)
                .error(R.drawable.logo_food)
                .into(holder.ivFoodImage);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    static class FoodVH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        ImageView ivFoodImage;

        FoodVH(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivCartFoodImage);
            tvName = itemView.findViewById(R.id.tvCartFoodName);
            tvPrice = itemView.findViewById(R.id.tvCartFoodPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
        }
    }
}
