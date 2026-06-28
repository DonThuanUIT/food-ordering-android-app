package com.foodorderingapp.ui.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.CartItemResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
public class CartFoodAdapter extends RecyclerView.Adapter<CartFoodAdapter.FoodVH> {

    private final List<CartItemResponse> items = new ArrayList<>();
    private OnQuantityChangeListener quantityChangeListener;
    private OnDeleteClickListener deleteClickListener;

    public CartFoodAdapter() {
        setHasStableIds(true);
    }

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
        List<CartItemResponse> oldItems = new ArrayList<>(items);
        List<CartItemResponse> nextItems = new ArrayList<>();
        if (newItems != null) {
            nextItems.addAll(newItems);
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldItems.size();
            }

            @Override
            public int getNewListSize() {
                return nextItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(
                        oldItems.get(oldItemPosition).getId(),
                        nextItems.get(newItemPosition).getId()
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                CartItemResponse oldItem = oldItems.get(oldItemPosition);
                CartItemResponse newItem = nextItems.get(newItemPosition);
                return Objects.equals(oldItem.getFoodId(), newItem.getFoodId())
                        && Objects.equals(oldItem.getFoodName(), newItem.getFoodName())
                        && Objects.equals(oldItem.getFoodImageUrl(), newItem.getFoodImageUrl())
                        && Double.compare(oldItem.getPrice(), newItem.getPrice()) == 0
                        && oldItem.getQuantity() == newItem.getQuantity()
                        && Objects.equals(oldItem.getNote(), newItem.getNote());
            }
        });

        items.clear();
        items.addAll(nextItems);
        diffResult.dispatchUpdatesTo(this);
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
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION || quantityChangeListener == null) {
                return;
            }

            CartItemResponse currentItem = items.get(adapterPosition);
            if (currentItem.getQuantity() > 1) {
                quantityChangeListener.onQuantityChange(currentItem, currentItem.getQuantity() - 1);
            }
        });
        holder.btnPlus.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION || quantityChangeListener == null) {
                return;
            }

            CartItemResponse currentItem = items.get(adapterPosition);
            quantityChangeListener.onQuantityChange(currentItem, currentItem.getQuantity() + 1);
        });
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && deleteClickListener != null) {
                deleteClickListener.onDeleteClick(items.get(adapterPosition));
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

    @Override
    public long getItemId(int position) {
        String id = items.get(position).getId();
        if (id == null) {
            id = items.get(position).getFoodId();
        }
        return id != null ? id.hashCode() : position;
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
