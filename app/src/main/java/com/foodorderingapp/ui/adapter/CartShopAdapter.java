package com.foodorderingapp.ui.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.CartItemResponse;
import com.foodorderingapp.model.response.ShopCartResponse;
import com.foodorderingapp.ui.adapter.CartFoodAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartShopAdapter extends RecyclerView.Adapter<CartShopAdapter.ShopVH> {

    private final List<ShopCartResponse> shops = new ArrayList<>();
    private OnCheckoutClickListener checkoutClickListener;

    public interface OnCheckoutClickListener {
        void onCheckoutClick();
    }

    public void setOnCheckoutClickListener(OnCheckoutClickListener listener) {
        this.checkoutClickListener = listener;
    }

    public void submitList(List<ShopCartResponse> newShops) {
        shops.clear();
        if (newShops != null) shops.addAll(newShops);
        notifyDataSetChanged();
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

        CartFoodAdapter foodAdapter = new CartFoodAdapter();
        holder.rvFoods.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvFoods.setAdapter(foodAdapter);
        holder.rvFoods.setNestedScrollingEnabled(false);

        foodAdapter.submitList(shop.getItems());

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
            if (checkoutClickListener != null) {
                checkoutClickListener.onCheckoutClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    static class ShopVH extends RecyclerView.ViewHolder {
        TextView tvShopName, tvItemCount, tvShopTotal;
        View btnCheckoutShop;
        RecyclerView rvFoods;

        ShopVH(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvCartShopName);
            tvItemCount = itemView.findViewById(R.id.tvShopItemCount);
            tvShopTotal = itemView.findViewById(R.id.tvShopTotal);
            btnCheckoutShop = itemView.findViewById(R.id.btnCheckoutShop);
            rvFoods = itemView.findViewById(R.id.rvCartFoods);
        }
    }
}
