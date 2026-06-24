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
    private String currentCategory = "Tất cả";
    private String currentStatusFilter = "Tất cả";

    public interface OnFoodActionProvider {
        void onStatusChanged(FoodResponse food, boolean isAvailable);
        void onAddNewItemClick();
        void onFoodImageClick(FoodResponse food);
        void onFoodLongClick(FoodResponse food);
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
        this.currentCategory = category != null ? category : "Tất cả";
        applyFilters();
    }

    public void setStatusFilter(String statusFilter) {
        this.currentStatusFilter = statusFilter != null ? statusFilter : "Tất cả";
        applyFilters();
    }

    public void updateFoodAvailability(java.util.UUID foodId, boolean isAvailable) {
        FoodResponse targetFood = null;
        for (FoodResponse f : originalList) {
            if (f.getId() != null && f.getId().equals(foodId)) {
                f.setIsAvailable(isAvailable);
                targetFood = f;
                break;
            }
        }
        if (targetFood == null) return;
        
        // Check if it matches filters
        String cleanCategory = currentCategory.split(" \\(")[0];
        boolean matchesCategory = cleanCategory.equals("All") || cleanCategory.equals("Tất cả") ||
                (targetFood.getCategoryName() != null && targetFood.getCategoryName().equalsIgnoreCase(cleanCategory));
                
        boolean matchesStatus = true;
        if ("Sẵn có".equalsIgnoreCase(currentStatusFilter)) {
            matchesStatus = isAvailable;
        } else if ("Hết món".equalsIgnoreCase(currentStatusFilter)) {
            matchesStatus = !isAvailable;
        }
        
        if (matchesCategory && matchesStatus) {
            int index = -1;
            for (int i = 0; i < filteredList.size(); i++) {
                if (filteredList.get(i).getId() != null && filteredList.get(i).getId().equals(foodId)) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                notifyItemChanged(index);
            } else {
                applyFilters();
            }
        } else {
            applyFilters();
        }
    }

    private void applyFilters() {
        filteredList.clear();
        for (FoodResponse item : originalList) {
            String name = item.getName() != null ? item.getName().toLowerCase(Locale.ROOT) : "";
            boolean matchesQuery = currentQuery.isEmpty() || name.contains(currentQuery.toLowerCase(Locale.ROOT));
            
            // Lọc theo text hiển thị trên Tab (ví dụ: "Burgers (8)" -> "Burgers")
            String cleanCategory = currentCategory.split(" \\(")[0];
            boolean matchesCategory = cleanCategory.equals("All") || cleanCategory.equals("Tất cả") ||
                    (item.getCategoryName() != null && item.getCategoryName().equalsIgnoreCase(cleanCategory));

            boolean isAvailable = item.getIsAvailable() != null ? item.getIsAvailable() : true;
            boolean matchesStatus = true;
            if ("Sẵn có".equalsIgnoreCase(currentStatusFilter)) {
                matchesStatus = isAvailable;
            } else if ("Hết món".equalsIgnoreCase(currentStatusFilter)) {
                matchesStatus = !isAvailable;
            }

            if (matchesQuery && matchesCategory && matchesStatus) {
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
        TextView tvSoldOut, tvName, tvPrice, tvStatus, tvDescription, tvBadge;
        SwitchCompat switchAvailability;
        View btnAddStock;

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
            tvBadge = itemView.findViewById(R.id.tv_badge_label);
            btnAddStock = itemView.findViewById(R.id.btn_add_stock);
        }

        void bind(FoodResponse food) {
            tvName.setText(food.getName());
            if (food.getPrice() != null) {
                tvPrice.setText(String.format(new Locale("vi", "VN"), "%,dđ", food.getPrice().longValue()));
            } else {
                tvPrice.setText("0đ");
            }
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

            // Set overlay badge
            if (tvBadge != null) {
                int hash = food.getName() != null ? Math.abs(food.getName().hashCode()) : 0;
                if (hash % 3 == 0) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText("Yêu thích");
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_orange);
                } else if (hash % 3 == 1) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText("Bán chạy");
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_green);
                } else {
                    tvBadge.setVisibility(View.GONE);
                }
            }

            if (isAvailable) {
                imgFood.setAlpha(1.0f);
                viewOverlay.setVisibility(View.GONE);
                tvSoldOut.setVisibility(View.GONE);
                tvStatus.setText("● Còn hàng");
                tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
                
                switchAvailability.setVisibility(View.VISIBLE);
                if (btnAddStock != null) btnAddStock.setVisibility(View.GONE);
                
                switchAvailability.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#FFCCBC"))); // Light orange track
                switchAvailability.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#F46E26")));  // Orange thumb
            } else {
                imgFood.setAlpha(0.3f); 
                viewOverlay.setVisibility(View.VISIBLE);
                tvSoldOut.setVisibility(View.VISIBLE);
                tvStatus.setText("● Hết hàng");
                tvStatus.setTextColor(Color.parseColor("#D32F2F")); // Red
                
                switchAvailability.setVisibility(View.GONE);
                if (btnAddStock != null) btnAddStock.setVisibility(View.VISIBLE);
            }

            switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed() && actionProvider != null) {
                    actionProvider.onStatusChanged(food, isChecked);
                }
            });

            if (btnAddStock != null) {
                btnAddStock.setOnClickListener(v -> {
                    if (actionProvider != null) {
                        actionProvider.onStatusChanged(food, true); // toggle to available
                    }
                });
            }

            itemView.setOnLongClickListener(v -> {
                if (actionProvider != null) {
                    actionProvider.onFoodLongClick(food);
                }
                return true;
            });
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {
        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
