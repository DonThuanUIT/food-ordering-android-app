package com.foodorderingapp.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.utils.ImageUrlUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminPendingShopAdapter extends RecyclerView.Adapter<AdminPendingShopAdapter.PendingShopViewHolder> {

    private final List<ShopResponse> shops = new ArrayList<>();
    private OnShopClickListener listener;

    public interface OnShopClickListener {
        void onShopClick(ShopResponse shop);
    }

    public void setOnShopClickListener(OnShopClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ShopResponse> newShops) {
        shops.clear();
        if (newShops != null) {
            shops.addAll(newShops);
        }
        notifyDataSetChanged();
    }

    public void appendList(List<ShopResponse> newShops) {
        if (newShops == null || newShops.isEmpty()) {
            return;
        }
        int start = shops.size();
        shops.addAll(newShops);
        notifyItemRangeInserted(start, newShops.size());
    }

    @NonNull
    @Override
    public PendingShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_pending_shop, parent, false);
        return new PendingShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingShopViewHolder holder, int position) {
        ShopResponse shop = shops.get(position);
        holder.tvName.setText(nullToDefault(shop.getName(), "Chưa có tên quán"));
        holder.tvOwner.setText(buildContactLine(shop));
        holder.tvStatus.setText(nullToDefault(shop.getStatus(), "PENDING"));
        bindShopLogo(holder, shop);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShopClick(shop);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    private void bindShopLogo(PendingShopViewHolder holder, ShopResponse shop) {
        String resolvedImageUrl = ImageUrlUtils.resolveImageUrl(
                firstNonBlank(shop.getLogoUrl(), shop.getCoverUrl()));
        if (resolvedImageUrl == null) {
            Glide.with(holder.itemView).clear(holder.ivLogo);
            holder.ivLogo.setPadding(dp(holder.itemView, 12), dp(holder.itemView, 12),
                    dp(holder.itemView, 12), dp(holder.itemView, 12));
            holder.ivLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.ivLogo.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_orange)));
            holder.ivLogo.setImageResource(R.drawable.ic_store);
            return;
        }

        holder.ivLogo.setPadding(0, 0, 0, 0);
        holder.ivLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.ivLogo.setImageTintList(null);
        Glide.with(holder.itemView)
                .load(resolvedImageUrl)
                .placeholder(R.drawable.ic_store)
                .error(R.drawable.ic_store)
                .circleCrop()
                .into(holder.ivLogo);
    }

    private String buildContactLine(ShopResponse shop) {
        String contact = firstNonBlank(shop.getPhone(), shop.getEmail(), shop.getAddress());
        return "Liên hệ: " + nullToDefault(contact, "Chưa cập nhật");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }

    static class PendingShopViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvName;
        TextView tvOwner;
        TextView tvStatus;

        PendingShopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivAdminPendingShopLogo);
            tvName = itemView.findViewById(R.id.tvAdminPendingShopName);
            tvOwner = itemView.findViewById(R.id.tvAdminPendingShopOwner);
            tvStatus = itemView.findViewById(R.id.tvAdminPendingShopStatus);
        }
    }
}
