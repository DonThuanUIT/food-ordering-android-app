package com.foodorderingapp.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.ShopResponse;

import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private final List<ShopResponse> shops = new ArrayList<>();

    public void submitList(List<ShopResponse> newShops) {
        shops.clear();
        if (newShops != null) {
            shops.addAll(newShops);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        ShopResponse shop = shops.get(position);

        holder.tvShopName.setText(nullToDefault(shop.getName(), "Chưa có tên quán"));
        holder.tvShopDescription.setText(nullToDefault(shop.getDescription(), "Chưa có mô tả"));
        holder.tvShopAddress.setText("Địa chỉ: " + nullToDefault(shop.getAddress(), "Chưa cập nhật"));

        String openTime = nullToDefault(shop.getOpenTime(), "--:--");
        String closeTime = nullToDefault(shop.getCloseTime(), "--:--");
        holder.tvShopTime.setText(openTime + " - " + closeTime);

        if ("OPENING".equalsIgnoreCase(shop.getDisplayStatus())) {
            holder.tvShopStatus.setText("Đang mở");
            holder.tvShopStatus.setTextColor(Color.parseColor("#FF7A21"));
        } else {
            holder.tvShopStatus.setText("Đang đóng");
            holder.tvShopStatus.setTextColor(Color.parseColor("#777777"));
        }
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName;
        TextView tvShopDescription;
        TextView tvShopAddress;
        TextView tvShopTime;
        TextView tvShopStatus;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvShopDescription = itemView.findViewById(R.id.tvShopDescription);
            tvShopAddress = itemView.findViewById(R.id.tvShopAddress);
            tvShopTime = itemView.findViewById(R.id.tvShopTime);
            tvShopStatus = itemView.findViewById(R.id.tvShopStatus);
        }
    }
}