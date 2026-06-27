package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.StudentReviewResponse;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentReviewAdapter extends RecyclerView.Adapter<StudentReviewAdapter.ReviewViewHolder> {
    private final List<StudentReviewResponse> reviews = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void submitList(List<StudentReviewResponse> newReviews) {
        reviews.clear();
        if (newReviews != null) {
            reviews.addAll(newReviews);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        StudentReviewResponse review = reviews.get(position);
        holder.tvShopName.setText(defaultText(review.getShopName(), "Quán ăn"));
        holder.tvRating.setText(buildStars(review.getRating()));
        holder.tvComment.setText(defaultText(review.getComment(), "Chưa có bình luận"));
        holder.tvDate.setText(formatDate(review.getCreatedAt()));
        holder.tvTotal.setText(formatPrice(review.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    private String buildStars(Integer rating) {
        int safeRating = rating == null ? 0 : Math.max(0, Math.min(5, rating));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < safeRating; i++) {
            builder.append("★");
        }
        return builder.length() == 0 ? "Chưa đánh giá" : builder + " " + safeRating + "/5";
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        try {
            return LocalDateTime.parse(value, inputFormatter).format(outputFormatter);
        } catch (DateTimeParseException ignored) {
            return value;
        }
    }

    private String formatPrice(Double value) {
        if (value == null) {
            return "";
        }
        return currencyFormat.format(value);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName;
        TextView tvRating;
        TextView tvComment;
        TextView tvDate;
        TextView tvTotal;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvReviewShopName);
            tvRating = itemView.findViewById(R.id.tvReviewRating);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            tvTotal = itemView.findViewById(R.id.tvReviewTotal);
        }
    }
}
