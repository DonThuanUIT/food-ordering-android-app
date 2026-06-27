package com.foodorderingapp.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.CategoryResponse;
import com.foodorderingapp.utils.CategoryIconHelper;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class VendorCategoryAdapter extends RecyclerView.Adapter<VendorCategoryAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryId, String categoryName);
    }

    public interface OnCategoryLongClickListener {
        void onCategoryLongClick(CategoryResponse category);
    }

    private final List<CategoryResponse> categories = new ArrayList<>();
    private String selectedCategoryId = "ALL";
    private OnCategoryClickListener clickListener;
    private OnCategoryLongClickListener longClickListener;

    public void setData(List<CategoryResponse> newCategories) {
        categories.clear();
        if (newCategories != null) {
            categories.addAll(newCategories);
        }
        notifyDataSetChanged();
    }

    public void setSelectedCategoryId(String selectedId) {
        this.selectedCategoryId = selectedId != null ? selectedId : "ALL";
        notifyDataSetChanged();
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnCategoryLongClickListener(OnCategoryLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendor_category_circle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        
        // Index 0 is always "Tất cả" (ALL)
        String id;
        String displayName;
        String emoji;
        CategoryResponse realCategory = null;

        if (position == 0) {
            id = "ALL";
            displayName = "Tất cả";
            emoji = CategoryIconHelper.getEmojiForDisplay("Tất cả");
        } else {
            realCategory = categories.get(position - 1);
            id = realCategory.getId().toString();
            displayName = CategoryIconHelper.getNameForDisplay(realCategory.getName());
            emoji = CategoryIconHelper.getEmojiForDisplay(realCategory.getName());
        }

        holder.tvEmoji.setText(emoji);
        holder.tvName.setText(displayName);

        boolean isSelected = selectedCategoryId.equalsIgnoreCase(id);

        if (isSelected) {
            holder.cardContainer.setCardBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.vendor_dark_orange)));
            holder.cardContainer.setStrokeColor(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.vendor_dark_orange)));
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.vendor_dark_orange));
            holder.viewIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.cardContainer.setCardBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.vendor_dark_card)));
            holder.cardContainer.setStrokeColor(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.vendor_dark_border)));
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));
            holder.viewIndicator.setVisibility(View.INVISIBLE);
        }

        final String finalId = id;
        final String finalName = displayName;
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCategoryClick(finalId, finalName);
            }
        });

        if (realCategory != null) {
            final CategoryResponse finalRealCategory = realCategory;
            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onCategoryLongClick(finalRealCategory);
                    return true;
                }
                return false;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        // Categories list + 1 for "Tất cả"
        return categories.size() + 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardContainer;
        TextView tvEmoji;
        TextView tvName;
        View viewIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_emoji_container);
            tvEmoji = itemView.findViewById(R.id.tv_category_emoji);
            tvName = itemView.findViewById(R.id.tv_category_name);
            viewIndicator = itemView.findViewById(R.id.view_selected_indicator);
        }
    }
}
