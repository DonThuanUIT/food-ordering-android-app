package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.ShopDetailResponse;
import com.foodorderingapp.utils.CategoryIconHelper;

import java.util.ArrayList;
import java.util.List;

public class ShopMenuTabAdapter extends RecyclerView.Adapter<ShopMenuTabAdapter.TabViewHolder> {

    public interface OnTabClickListener {
        void onTabClick(int position);
    }

    private final List<ShopDetailResponse.CategoryMenu> categories = new ArrayList<>();
    private OnTabClickListener listener;
    private int selectedPosition = 0;

    public void submitList(List<ShopDetailResponse.CategoryMenu> newCategories) {
        categories.clear();
        if (newCategories != null) {
            categories.addAll(newCategories);
        }
        selectedPosition = 0;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

    public void setOnTabClickListener(OnTabClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_menu_tab, parent, false);
        return new TabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        ShopDetailResponse.CategoryMenu category = categories.get(position);
        boolean selected = position == selectedPosition;

        String name = nullToDefault(category.getName(), "Danh mục");
        holder.tvTabName.setText(CategoryIconHelper.getEmojiForDisplay(name));
        holder.tvTabName.setTextColor(selected ? 0xFFFFFFFF : 0xFF563528);
        holder.tvTabName.setBackgroundResource(selected
                ? R.drawable.bg_shop_menu_tab_selected
                : R.drawable.bg_shop_menu_tab_unselected);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTabClick(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    static class TabViewHolder extends RecyclerView.ViewHolder {
        TextView tvTabName;

        TabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTabName = itemView.findViewById(R.id.tvMenuTabName);
        }
    }
}
