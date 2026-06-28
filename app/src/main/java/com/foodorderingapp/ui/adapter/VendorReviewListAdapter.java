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
    private final java.util.Set<String> collapsedReviewIds = new java.util.HashSet<>();

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

        // Bind SharedPreferences multiple reply data
        List<String> replies = getRepliesForReview(holder.itemView.getContext(), review.getId());
        holder.layoutRepliesContainer.removeAllViews();

        boolean isVendor = "VENDOR".equalsIgnoreCase(com.foodorderingapp.utils.TokenManager.getInstance().getRole());

        if (!replies.isEmpty()) {
            holder.layoutShopReply.setVisibility(View.VISIBLE);
            holder.btnReplyReview.setVisibility(View.GONE);

            boolean isCollapsed = collapsedReviewIds.contains(review.getId());
            if (isCollapsed) {
                holder.layoutRepliesContainer.setVisibility(View.GONE);
                holder.btnCollapseReply.setText("Mở rộng (" + replies.size() + ")");
            } else {
                holder.layoutRepliesContainer.setVisibility(View.VISIBLE);
                holder.btnCollapseReply.setText("Thu gọn");
                for (int i = 0; i < replies.size(); i++) {
                    View replyView = createReplyItemView(holder.itemView.getContext(), review.getId(), replies.get(i), i, position, isVendor);
                    holder.layoutRepliesContainer.addView(replyView);
                }
            }
        } else {
            holder.layoutShopReply.setVisibility(View.GONE);
            holder.btnReplyReview.setVisibility(isVendor ? View.VISIBLE : View.GONE);
        }

        if (!isVendor) {
            holder.btnReplyReview.setVisibility(View.GONE);
            holder.btnReplyMore.setVisibility(View.GONE);
        }

        holder.btnReplyReview.setOnClickListener(v -> {
            showAddReplyDialog(holder.itemView.getContext(), review.getId(), position);
        });

        holder.btnReplyMore.setOnClickListener(v -> {
            showAddReplyDialog(holder.itemView.getContext(), review.getId(), position);
        });

        holder.btnCollapseReply.setOnClickListener(v -> {
            boolean isCollapsed = collapsedReviewIds.contains(review.getId());
            if (isCollapsed) {
                collapsedReviewIds.remove(review.getId());
            } else {
                collapsedReviewIds.add(review.getId());
            }
            notifyItemChanged(position);
        });
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

    private List<String> getRepliesForReview(android.content.Context context, String reviewId) {
        List<String> list = new ArrayList<>();
        android.content.SharedPreferences prefs = context.getSharedPreferences("vendor_reviews_reply", android.content.Context.MODE_PRIVATE);
        int count = prefs.getInt("reply_count_" + reviewId, 0);

        // Backward compatibility: migrate old single reply
        String oldReply = prefs.getString("reply_" + reviewId, null);
        if (oldReply != null && !oldReply.trim().isEmpty() && count == 0) {
            list.add(oldReply);
            prefs.edit()
                 .putInt("reply_count_" + reviewId, 1)
                 .putString("reply_" + reviewId + "_0", oldReply)
                 .remove("reply_" + reviewId)
                 .apply();
            count = 1;
        }

        for (int i = 0; i < count; i++) {
            String r = prefs.getString("reply_" + reviewId + "_" + i, null);
            if (r != null) {
                list.add(r);
            }
        }
        return list;
    }

    private void addReplyForReview(android.content.Context context, String reviewId, String replyText) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("vendor_reviews_reply", android.content.Context.MODE_PRIVATE);
        int count = prefs.getInt("reply_count_" + reviewId, 0);
        prefs.edit()
             .putString("reply_" + reviewId + "_" + count, replyText)
             .putInt("reply_count_" + reviewId, count + 1)
             .apply();
    }

    private void updateReplyForReview(android.content.Context context, String reviewId, int index, String replyText) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("vendor_reviews_reply", android.content.Context.MODE_PRIVATE);
        prefs.edit()
             .putString("reply_" + reviewId + "_" + index, replyText)
             .apply();
    }

    private void deleteReplyForReview(android.content.Context context, String reviewId, int index) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("vendor_reviews_reply", android.content.Context.MODE_PRIVATE);
        int count = prefs.getInt("reply_count_" + reviewId, 0);
        if (index < 0 || index >= count) return;

        android.content.SharedPreferences.Editor editor = prefs.edit();
        for (int i = index; i < count - 1; i++) {
            String nextReply = prefs.getString("reply_" + reviewId + "_" + (i + 1), null);
            editor.putString("reply_" + reviewId + "_" + i, nextReply);
        }
        editor.remove("reply_" + reviewId + "_" + (count - 1));
        editor.putInt("reply_count_" + reviewId, count - 1);
        editor.apply();
    }

    private View createReplyItemView(android.content.Context context, String reviewId, String replyText, int index, int position, boolean isVendor) {
        android.widget.LinearLayout itemLayout = new android.widget.LinearLayout(context);
        itemLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        itemLayout.setPadding(0, 8, 0, 8);

        if (index > 0) {
            View divider = new View(context);
            android.widget.LinearLayout.LayoutParams divParams = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 2);
            divParams.topMargin = 4;
            divParams.bottomMargin = 8;
            divider.setLayoutParams(divParams);
            divider.setBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_divider));
            itemLayout.addView(divider);
        }

        android.widget.LinearLayout headerLayout = new android.widget.LinearLayout(context);
        headerLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvIndex = new TextView(context);
        tvIndex.setText("#" + (index + 1));
        tvIndex.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));
        tvIndex.setTextSize(11);
        tvIndex.setTypeface(null, android.graphics.Typeface.BOLD);

        android.widget.LinearLayout.LayoutParams indexParams = new android.widget.LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tvIndex.setLayoutParams(indexParams);
        headerLayout.addView(tvIndex);

        if (isVendor) {
            TextView btnEdit = new TextView(context);
            btnEdit.setText("Sửa");
            btnEdit.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));
            btnEdit.setTextSize(11);
            btnEdit.setPadding(12, 6, 12, 6);
            btnEdit.setClickable(true);
            btnEdit.setFocusable(true);
            btnEdit.setOnClickListener(v -> {
                showEditReplyDialog(context, reviewId, index, replyText, position);
            });
            headerLayout.addView(btnEdit);

            TextView btnDelete = new TextView(context);
            btnDelete.setText("Xóa");
            btnDelete.setTextColor(android.graphics.Color.parseColor("#FF4D4D"));
            btnDelete.setTextSize(11);
            btnDelete.setPadding(12, 6, 12, 6);
            btnDelete.setClickable(true);
            btnDelete.setFocusable(true);
            btnDelete.setOnClickListener(v -> {
                androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Xóa phản hồi? ⚠️")
                    .setMessage("Bạn có chắc chắn muốn xóa phản hồi #" + (index + 1) + " này?")
                    .setPositiveButton("Xóa", (d, which) -> {
                        deleteReplyForReview(context, reviewId, index);
                        notifyItemChanged(position);
                        android.widget.Toast.makeText(context, "Đã xóa phản hồi", android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", (d, which) -> d.dismiss())
                    .create();
                dialog.show();
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(
                    android.graphics.Color.parseColor("#FF4D4D"));
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));
            });
            headerLayout.addView(btnDelete);
        }

        itemLayout.addView(headerLayout);

        TextView tvContent = new TextView(context);
        tvContent.setText(replyText);
        tvContent.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_primary));
        tvContent.setTextSize(13);
        android.widget.LinearLayout.LayoutParams contentParams = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentParams.topMargin = 4;
        tvContent.setLayoutParams(contentParams);
        itemLayout.addView(tvContent);

        return itemLayout;
    }

    private void showAddReplyDialog(android.content.Context context, String reviewId, int position) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Viết phản hồi ✍️");

        final android.widget.EditText etInput = new android.widget.EditText(context);
        etInput.setHint("Nhập phản hồi của bạn...");
        etInput.setPadding(36, 32, 36, 32);
        etInput.setTextSize(15);
        etInput.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_primary));
        etInput.setHintTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));
        etInput.setBackgroundResource(R.drawable.bg_vendor_reply);

        android.widget.FrameLayout container = new android.widget.FrameLayout(context);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 56;
        params.rightMargin = 56;
        params.topMargin = 24;
        params.bottomMargin = 16;
        etInput.setLayoutParams(params);
        container.addView(etInput);
        builder.setView(container);

        builder.setPositiveButton("Gửi", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(
                androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_orange));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(
                androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String replyText = etInput.getText().toString().trim();
            if (replyText.isEmpty()) {
                android.widget.Toast.makeText(context, "Nội dung phản hồi không được để trống", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            addReplyForReview(context, reviewId, replyText);
            collapsedReviewIds.remove(reviewId);
            notifyItemChanged(position);
            android.widget.Toast.makeText(context, "Đã gửi phản hồi", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void showEditReplyDialog(android.content.Context context, String reviewId, int index, String initialText, int position) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Chỉnh sửa phản hồi #" + (index + 1) + " ✍️");

        final android.widget.EditText etInput = new android.widget.EditText(context);
        etInput.setHint("Nhập phản hồi...");
        etInput.setText(initialText);
        etInput.setPadding(36, 32, 36, 32);
        etInput.setTextSize(15);
        etInput.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_primary));
        etInput.setHintTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));
        etInput.setBackgroundResource(R.drawable.bg_vendor_reply);

        android.widget.FrameLayout container = new android.widget.FrameLayout(context);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 56;
        params.rightMargin = 56;
        params.topMargin = 24;
        params.bottomMargin = 16;
        etInput.setLayoutParams(params);
        container.addView(etInput);
        builder.setView(container);

        builder.setPositiveButton("Cập nhật", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(
                androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_orange));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(
                androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String replyText = etInput.getText().toString().trim();
            if (replyText.isEmpty()) {
                android.widget.Toast.makeText(context, "Nội dung phản hồi không được để trống", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            updateReplyForReview(context, reviewId, index, replyText);
            notifyItemChanged(position);
            android.widget.Toast.makeText(context, "Đã cập nhật phản hồi", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvRating;
        TextView tvComment;
        TextView tvDate;
        View layoutShopReply;
        android.widget.LinearLayout layoutRepliesContainer;
        TextView btnReplyReview;
        TextView btnCollapseReply;
        TextView btnReplyMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVendorReviewerName);
            tvRating = itemView.findViewById(R.id.tvVendorReviewRating);
            tvComment = itemView.findViewById(R.id.tvVendorReviewComment);
            tvDate = itemView.findViewById(R.id.tvVendorReviewDate);
            layoutShopReply = itemView.findViewById(R.id.layoutShopReply);
            layoutRepliesContainer = itemView.findViewById(R.id.layoutRepliesContainer);
            btnReplyReview = itemView.findViewById(R.id.btnReplyReview);
            btnCollapseReply = itemView.findViewById(R.id.btnCollapseReply);
            btnReplyMore = itemView.findViewById(R.id.btnReplyMore);
        }
    }
}
