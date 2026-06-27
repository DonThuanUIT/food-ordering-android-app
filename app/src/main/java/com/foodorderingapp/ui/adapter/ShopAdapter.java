package com.foodorderingapp.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.ShopResponse;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private final List<ShopResponse> shops = new ArrayList<>();
    private OnShopClickListener onShopClickListener;

    public interface OnShopClickListener {
        void onShopClick(ShopResponse shop);
    }

    public void setOnShopClickListener(OnShopClickListener listener) {
        this.onShopClickListener = listener;
    }

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
        String imageUrl = firstNonBlank(shop.getLogoUrl(), shop.getCoverUrl());
        if (imageUrl == null) {
            holder.ivShopImage.setPadding(dp(holder.itemView, 22), dp(holder.itemView, 22), dp(holder.itemView, 22), dp(holder.itemView, 22));
            holder.ivShopImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.ivShopImage.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF7A21")));
            holder.ivShopImage.setImageResource(R.drawable.ic_store_outline);
        } else {
            holder.ivShopImage.setPadding(0, 0, 0, 0);
            holder.ivShopImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.ivShopImage.setImageTintList(null);
            Glide.with(holder.itemView)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_store_outline)
                    .error(R.drawable.ic_store_outline)
                    .into(holder.ivShopImage);
        }

        String openTime = nullToDefault(shop.getOpenTime(), "--:--");
        String closeTime = nullToDefault(shop.getCloseTime(), "--:--");
        holder.tvShopTime.setText(openTime + " - " + closeTime);

        boolean manuallyOpen = shop.getIsOpen() != null
                ? shop.getIsOpen()
                : "OPENING".equalsIgnoreCase(shop.getDisplayStatus())
                || "OPEN".equalsIgnoreCase(shop.getDisplayStatus());
        boolean isOpening = manuallyOpen && isWithinOpeningHours(shop.getOpenTime(), shop.getCloseTime());
        if (isOpening) {
            holder.tvShopStatus.setText("Đang mở");
            holder.tvShopStatus.setTextColor(Color.parseColor("#FF7A21"));
        } else {
            holder.tvShopStatus.setText("Đang đóng");
            holder.tvShopStatus.setTextColor(Color.parseColor("#777777"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (onShopClickListener != null) {
                onShopClickListener.onShopClick(shop);
            }
        });
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

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }
        if (second != null && !second.trim().isEmpty()) {
            return second;
        }
        return null;
    }

    private boolean isWithinOpeningHours(String openTime, String closeTime) {
        if (openTime == null || openTime.trim().isEmpty()
                || closeTime == null || closeTime.trim().isEmpty()) {
            return false;
        }

        try {
            LocalTime open = LocalTime.parse(openTime.trim());
            LocalTime close = LocalTime.parse(closeTime.trim());
            LocalTime now = LocalTime.now();

            if (open.equals(close)) {
                return true;
            }
            if (close.isAfter(open)) {
                return !now.isBefore(open) && !now.isAfter(close);
            }
            return !now.isBefore(open) || !now.isAfter(close);
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    private int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName;
        TextView tvShopDescription;
        TextView tvShopAddress;
        TextView tvShopTime;
        TextView tvShopStatus;
        ImageView ivShopImage;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvShopDescription = itemView.findViewById(R.id.tvShopDescription);
            tvShopAddress = itemView.findViewById(R.id.tvShopAddress);
            tvShopTime = itemView.findViewById(R.id.tvShopTime);
            tvShopStatus = itemView.findViewById(R.id.tvShopStatus);
            ivShopImage = itemView.findViewById(R.id.ivShopImage);
        }
    }
}
