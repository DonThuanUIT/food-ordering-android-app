package com.foodorderingapp.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.FoodResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AiMessageAdapter extends RecyclerView.Adapter<AiMessageAdapter.ViewHolder> {

    public interface OnRecommendationClickListener {
        void onAddToCart(FoodResponse food);
        void onFoodClick(FoodResponse food);
    }

    private final List<AiChatMessage> messages;
    private final OnRecommendationClickListener listener;

    public AiMessageAdapter(List<AiChatMessage> messages, OnRecommendationClickListener listener) {
        this.messages = messages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AiChatMessage msg = messages.get(position);
        holder.bind(msg, listener);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View layoutUser;
        private final View layoutAi;
        private final TextView tvUserContent;
        private final TextView tvAiContent;
        private final LinearLayout layoutFoodCards;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUser = itemView.findViewById(R.id.layout_user_message);
            layoutAi = itemView.findViewById(R.id.layout_ai_message);
            tvUserContent = itemView.findViewById(R.id.tv_user_content);
            tvAiContent = itemView.findViewById(R.id.tv_ai_content);
            layoutFoodCards = itemView.findViewById(R.id.layout_ai_food_cards);
        }

        public void bind(AiChatMessage msg, OnRecommendationClickListener listener) {
            if (msg.isUser()) {
                layoutUser.setVisibility(View.VISIBLE);
                layoutAi.setVisibility(View.GONE);
                tvUserContent.setText(msg.getText());
            } else {
                layoutUser.setVisibility(View.GONE);
                layoutAi.setVisibility(View.VISIBLE);
                tvAiContent.setText(msg.getText());

                // Clear previously added food card views
                layoutFoodCards.removeAllViews();

                List<FoodResponse> recommendations = msg.getRecommendations();
                if (recommendations != null && !recommendations.isEmpty()) {
                    LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                    for (FoodResponse food : recommendations) {
                        View cardView = inflater.inflate(R.layout.item_food_explore, layoutFoodCards, false);

                        ImageView ivFoodImage = cardView.findViewById(R.id.ivFoodImage);
                        TextView tvFoodName = cardView.findViewById(R.id.tvFoodName);
                        TextView tvShopName = cardView.findViewById(R.id.tvShopName);
                        TextView tvFoodPrice = cardView.findViewById(R.id.tvFoodPrice);
                        ImageButton btnAddToCart = cardView.findViewById(R.id.btnAddToCart);

                        // Load image
                        Glide.with(itemView.getContext())
                                .load(food.getImageUrl())
                                .placeholder(R.drawable.logo_food)
                                .error(R.drawable.logo_food)
                                .into(ivFoodImage);

                        tvFoodName.setText(food.getName());
                        tvShopName.setText(food.getCategoryName() != null ? food.getCategoryName() : "Món ngon");

                        // Format price
                        BigDecimal price = food.getPrice();
                        if (price != null) {
                            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                            tvFoodPrice.setText(currencyFormat.format(price));
                        } else {
                            tvFoodPrice.setText("0đ");
                        }

                        // Set action clicks
                        btnAddToCart.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onAddToCart(food);
                            }
                        });

                        cardView.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onFoodClick(food);
                            }
                        });

                        layoutFoodCards.addView(cardView);
                    }
                }
            }
        }
    }
}
