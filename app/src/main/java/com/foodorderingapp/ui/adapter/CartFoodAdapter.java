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
    private OnQuantityChangeListener quantityChangeListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnQuantityChangeListener {
        void onQuantityChange(CartItemResponse item, int newQuantity);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(CartItemResponse item);
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

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
        holder.btnMinus.setEnabled(item.getQuantity() > 1);
        holder.btnMinus.setAlpha(item.getQuantity() > 1 ? 1f : 0.35f);
        holder.btnMinus.setOnClickListener(v -> {
            if (quantityChangeListener != null && item.getQuantity() > 1) {
                quantityChangeListener.onQuantityChange(item, item.getQuantity() - 1);
            }
        });
        holder.btnPlus.setOnClickListener(v -> {
            if (quantityChangeListener != null) {
                quantityChangeListener.onQuantityChange(item, item.getQuantity() + 1);
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(item);
            }
        });
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
        ImageView btnMinus, btnPlus, btnDelete, ivFoodImage;

        FoodVH(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivCartFoodImage);
            tvName = itemView.findViewById(R.id.tvCartFoodName);
            tvPrice = itemView.findViewById(R.id.tvCartFoodPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDeleteCartItem);
        }
    }
}
