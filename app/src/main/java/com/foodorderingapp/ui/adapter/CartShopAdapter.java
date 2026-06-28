package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.CartItemResponse;
import com.foodorderingapp.model.response.ShopCartResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CartShopAdapter extends RecyclerView.Adapter<CartShopAdapter.ShopVH> {

    private final List<ShopCartResponse> shops = new ArrayList<>();
    private OnCheckoutClickListener checkoutClickListener;
    private OnClearShopCartListener clearShopCartListener;
    private CartFoodAdapter.OnQuantityChangeListener quantityChangeListener;
    private CartFoodAdapter.OnDeleteClickListener deleteClickListener;

    public CartShopAdapter() {
        setHasStableIds(true);
    }

    public interface OnCheckoutClickListener {
        void onCheckoutClick(ShopCartResponse shop);
    }

    public interface OnClearShopCartListener {
        void onClearShopCart(ShopCartResponse shop);
    }

    public void setOnCheckoutClickListener(OnCheckoutClickListener listener) {
        this.checkoutClickListener = listener;
    }

    public void setOnClearShopCartListener(OnClearShopCartListener listener) {
        this.clearShopCartListener = listener;
    }

    public void setOnQuantityChangeListener(CartFoodAdapter.OnQuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }

    public void setOnDeleteClickListener(CartFoodAdapter.OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void submitList(List<ShopCartResponse> newShops) {
        List<ShopCartResponse> oldShops = new ArrayList<>(shops);
        List<ShopCartResponse> nextShops = new ArrayList<>();
        if (newShops != null) {
            nextShops.addAll(newShops);
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldShops.size();
            }

            @Override
            public int getNewListSize() {
                return nextShops.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(
                        oldShops.get(oldItemPosition).getShopId(),
                        nextShops.get(newItemPosition).getShopId()
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return areShopContentsTheSame(
                        oldShops.get(oldItemPosition),
                        nextShops.get(newItemPosition)
                );
            }
        });

        shops.clear();
        shops.addAll(nextShops);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ShopVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_shop, parent, false);
        return new ShopVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopVH holder, int position) {
        ShopCartResponse shop = shops.get(position);

        holder.tvShopName.setText(shop.getShopName());

        holder.foodAdapter.setOnQuantityChangeListener(quantityChangeListener);
        holder.foodAdapter.setOnDeleteClickListener(deleteClickListener);
        holder.foodAdapter.submitList(shop.getItems());

        int totalItems = 0;
        double totalPrice = 0;

        if (shop.getItems() != null) {
            for (CartItemResponse item : shop.getItems()) {
                totalItems += item.getQuantity();
                totalPrice += item.getPrice() * item.getQuantity();
            }
        }

        holder.tvItemCount.setText("Tổng cộng (" + totalItems + " món):");
        holder.tvShopTotal.setText(formatPrice(totalPrice));
        holder.btnCheckoutShop.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && checkoutClickListener != null) {
                checkoutClickListener.onCheckoutClick(shops.get(adapterPosition));
            }
        });
        holder.btnDeleteShopCart.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && clearShopCartListener != null) {
                clearShopCartListener.onClearShopCart(shops.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    @Override
    public long getItemId(int position) {
        String id = shops.get(position).getShopId();
        if (id == null) {
            id = shops.get(position).getShopName();
        }
        return id != null ? id.hashCode() : position;
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    private boolean areShopContentsTheSame(ShopCartResponse oldShop, ShopCartResponse newShop) {
        return Objects.equals(oldShop.getShopName(), newShop.getShopName())
                && areCartItemsTheSame(oldShop.getItems(), newShop.getItems());
    }

    private boolean areCartItemsTheSame(List<CartItemResponse> oldItems,
                                        List<CartItemResponse> newItems) {
        if (oldItems == newItems) {
            return true;
        }
        if (oldItems == null || newItems == null || oldItems.size() != newItems.size()) {
            return false;
        }

        for (int i = 0; i < oldItems.size(); i++) {
            CartItemResponse oldItem = oldItems.get(i);
            CartItemResponse newItem = newItems.get(i);
            if (!Objects.equals(oldItem.getId(), newItem.getId())
                    || !Objects.equals(oldItem.getFoodId(), newItem.getFoodId())
                    || !Objects.equals(oldItem.getFoodName(), newItem.getFoodName())
                    || !Objects.equals(oldItem.getFoodImageUrl(), newItem.getFoodImageUrl())
                    || Double.compare(oldItem.getPrice(), newItem.getPrice()) != 0
                    || oldItem.getQuantity() != newItem.getQuantity()
                    || !Objects.equals(oldItem.getNote(), newItem.getNote())) {
                return false;
            }
        }
        return true;
    }

    static class ShopVH extends RecyclerView.ViewHolder {
        TextView tvShopName;
        TextView tvItemCount;
        TextView tvShopTotal;
        View btnCheckoutShop;
        View btnDeleteShopCart;
        RecyclerView rvFoods;
        CartFoodAdapter foodAdapter;

        ShopVH(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvCartShopName);
            tvItemCount = itemView.findViewById(R.id.tvShopItemCount);
            tvShopTotal = itemView.findViewById(R.id.tvShopTotal);
            btnCheckoutShop = itemView.findViewById(R.id.btnCheckoutShop);
            btnDeleteShopCart = itemView.findViewById(R.id.btnDeleteShopCart);
            rvFoods = itemView.findViewById(R.id.rvCartFoods);
            foodAdapter = new CartFoodAdapter();
            rvFoods.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvFoods.setAdapter(foodAdapter);
            rvFoods.setNestedScrollingEnabled(false);
            rvFoods.setItemAnimator(null);
        }
    }
}
