package com.foodorderingapp.ui.adapter;

import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.FoodResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FOOD = 1;
    private static final int TYPE_ADD_CARD = 2;

    private List<FoodResponse> originalList;
    private final List<FoodResponse> filteredList;
    private final OnFoodActionProvider actionProvider;

    private String currentQuery = "";
    private String currentCategory = "All";

    public interface OnFoodActionProvider {
        void onStatusChanged(FoodResponse food, boolean isAvailable);
        void onAddNewItemClick();
        void onFoodImageClick(FoodResponse food);
    }

    public FoodAdapter(List<FoodResponse> foodList, OnFoodActionProvider actionProvider) {
        this.originalList = foodList != null ? foodList : new ArrayList<>();
        this.filteredList = new ArrayList<>(originalList);
        this.actionProvider = actionProvider;
        applyFilters();
    }

    public void updateData(List<FoodResponse> newList) {
        this.originalList = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        java.util.Collections.sort(this.originalList, (f1, f2) -> {
            if (f1.getName() == null) return 1;
            if (f2.getName() == null) return -1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        });
        applyFilters();
    }

    public void filter(String query) {
        this.currentQuery = query != null ? query : "";
        applyFilters();
    }

    public void setCategoryFilter(String category) {
        this.currentCategory = category != null ? category : "All";
        applyFilters();
    }

    private void applyFilters() {
        filteredList.clear();
        for (FoodResponse item : originalList) {
            String name = item.getName() != null ? item.getName().toLowerCase(Locale.ROOT) : "";
            boolean matchesQuery = currentQuery.isEmpty() || name.contains(currentQuery.toLowerCase(Locale.ROOT));
            
            // Lọc theo text hiển thị trên Tab (ví dụ: "Burgers (8)" -> "Burgers")
            String cleanCategory = currentCategory.split(" \\(")[0];
            boolean matchesCategory = cleanCategory.equals("All") ||
                    (item.getCategoryName() != null && item.getCategoryName().equalsIgnoreCase(cleanCategory));

            if (matchesQuery && matchesCategory) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == filteredList.size()) {
            return TYPE_ADD_CARD;
        }
        return TYPE_FOOD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD_CARD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_food_card, parent, false);
            return new AddViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vendor_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FoodViewHolder) {
            ((FoodViewHolder) holder).bind(filteredList.get(position));
        } else if (holder instanceof AddViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                if (actionProvider != null) actionProvider.onAddNewItemClick();
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size() + 1;
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        View viewOverlay;
        TextView tvSoldOut, tvName, tvPrice, tvStatus, tvDescription;
        SwitchCompat switchAvailability;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.img_food);
            viewOverlay = itemView.findViewById(R.id.view_sold_out_overlay);
            tvSoldOut = itemView.findViewById(R.id.tv_sold_out_label);
            tvName = itemView.findViewById(R.id.tv_food_name);
            tvPrice = itemView.findViewById(R.id.tv_food_price);
            tvStatus = itemView.findViewById(R.id.tv_status_text);
            tvDescription = itemView.findViewById(R.id.tv_food_description);
            switchAvailability = itemView.findViewById(R.id.switch_available);
        }

        void bind(FoodResponse food) {
            tvName.setText(food.getName());
            tvPrice.setText(String.format(Locale.US, "$%.2f", food.getPrice()));
            if (tvDescription != null) {
                tvDescription.setText(food.getDescription());
            }

            Glide.with(itemView.getContext())
                    .load(food.getImageUrl())
                    .placeholder(R.drawable.logo_food)
                    .error(R.drawable.logo_food)
                    .into(imgFood);

            boolean isAvailable = food.getIsAvailable() != null ? food.getIsAvailable() : true;
            
            switchAvailability.setOnCheckedChangeListener(null);
            switchAvailability.setChecked(isAvailable);

            imgFood.setOnClickListener(v -> {
                if (actionProvider != null) {
                    actionProvider.onFoodImageClick(food);
                }
            });

            if (isAvailable) {
                imgFood.setAlpha(1.0f);
                viewOverlay.setVisibility(View.GONE);
                tvSoldOut.setVisibility(View.GONE);
                tvStatus.setText("In Stock");
                tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
                
                switchAvailability.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#81C784"))); // Light green track
                switchAvailability.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));  // Dark green thumb
            } else {
                imgFood.setAlpha(0.3f); 
                viewOverlay.setVisibility(View.VISIBLE);
                tvSoldOut.setVisibility(View.VISIBLE);
                tvStatus.setText("Out of Stock");
                tvStatus.setTextColor(Color.parseColor("#D32F2F")); // Red
                
                switchAvailability.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0"))); // Light gray track
                switchAvailability.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));  // Gray thumb
            }

            switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed() && actionProvider != null) {
                    actionProvider.onStatusChanged(food, isChecked);
                }
            });
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {
        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
