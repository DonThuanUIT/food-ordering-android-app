package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.ReviewResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class VendorReviewListAdapter extends RecyclerView.Adapter<VendorReviewListAdapter.ViewHolder> {

    private final List<ReviewResponse> reviews = new ArrayList<>();
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void submitList(List<ReviewResponse> newReviews) {
        reviews.clear();
        if (newReviews != null) {
            reviews.addAll(newReviews);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vendor_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewResponse review = reviews.get(position);

        String name = (review.getUser() != null && review.getUser().getFullName() != null)
                ? review.getUser().getFullName()
                : "Người dùng ẩn danh";
        holder.tvName.setText(name);
        holder.tvRating.setText(buildStars(review.getRating()));
        holder.tvComment.setText(review.getComment() != null ? review.getComment() : "Không có bình luận");
        holder.tvDate.setText(formatDate(review.getCreatedAt()));
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
        for (int i = safeRating; i < 5; i++) {
            builder.append("☆");
        }
        return builder.toString() + " " + safeRating + "/5";
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvRating;
        TextView tvComment;
        TextView tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVendorReviewerName);
            tvRating = itemView.findViewById(R.id.tvVendorReviewRating);
            tvComment = itemView.findViewById(R.id.tvVendorReviewComment);
            tvDate = itemView.findViewById(R.id.tvVendorReviewDate);
        }
    }
}
