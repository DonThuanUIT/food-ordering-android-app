package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.ReviewReplyRequest;
import com.foodorderingapp.model.response.ReviewReplyResponse;
import com.foodorderingapp.model.response.ReviewResponse;
import com.foodorderingapp.utils.TokenManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentReviewListAdapter extends RecyclerView.Adapter<StudentReviewListAdapter.ViewHolder> {

    public static class StudentReviewWrapper {
        private final ReviewResponse review;
        private final String type; // "SHOP", "FOOD", "DELIVERY"
        private final String targetName; // Shop name or Food name
        private final String subTitle; // E.g., "Đơn hàng #1234"

        public StudentReviewWrapper(ReviewResponse review, String type, String targetName, String subTitle) {
            this.review = review;
            this.type = type;
            this.targetName = targetName;
            this.subTitle = subTitle;
        }

        public ReviewResponse getReview() { return review; }
        public String getType() { return type; }
        public String getTargetName() { return targetName; }
        public String getSubTitle() { return subTitle; }
    }

    private final List<StudentReviewWrapper> list = new ArrayList<>();
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Set<String> collapsedReviewIds = new HashSet<>();

    // Cache loaded replies from backend
    private final Map<String, List<ReviewReplyResponse>> repliesCache = new HashMap<>();

    public void submitList(List<StudentReviewWrapper> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
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
        StudentReviewWrapper wrapper = list.get(position);
        ReviewResponse review = wrapper.getReview();
        String rId = review.getId();

        // Display targeted title
        holder.tvName.setText(wrapper.getTargetName());
        holder.tvRating.setText(buildStars(review.getRating()));
        holder.tvComment.setText(review.getComment() != null && !review.getComment().trim().isEmpty() 
                ? review.getComment() : "Không có bình luận");
        
        String subtitleText = wrapper.getSubTitle() + " • " + formatDate(review.getCreatedAt());
        holder.tvDate.setText(subtitleText);

        // Fetch replies from backend if not cached
        if (!repliesCache.containsKey(rId)) {
            repliesCache.put(rId, new ArrayList<>()); // Placeholder to prevent duplicate calls
            ApiClient.getApiService().getReviewReplies(rId).enqueue(new Callback<List<ReviewReplyResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<ReviewReplyResponse>> call, @NonNull Response<List<ReviewReplyResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        repliesCache.put(rId, response.body());
                        notifyItemChanged(position);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<ReviewReplyResponse>> call, @NonNull Throwable t) {}
            });
        }

        List<ReviewReplyResponse> replies = repliesCache.get(rId);
        holder.layoutRepliesContainer.removeAllViews();

        if (replies != null && !replies.isEmpty()) {
            holder.layoutShopReply.setVisibility(View.VISIBLE);
            holder.btnReplyReview.setVisibility(View.GONE);

            boolean isCollapsed = collapsedReviewIds.contains(rId);
            if (isCollapsed) {
                holder.layoutRepliesContainer.setVisibility(View.GONE);
                holder.btnCollapseReply.setText("Mở rộng (" + replies.size() + ")");
            } else {
                holder.layoutRepliesContainer.setVisibility(View.VISIBLE);
                holder.btnCollapseReply.setText("Thu gọn");
                for (int i = 0; i < replies.size(); i++) {
                    View replyView = createReplyItemView(holder.itemView.getContext(), rId, replies.get(i), i, position);
                    holder.layoutRepliesContainer.addView(replyView);
                }
            }
        } else {
            holder.layoutShopReply.setVisibility(View.GONE);
            holder.btnReplyReview.setVisibility(View.VISIBLE);
        }

        holder.btnReplyReview.setOnClickListener(v -> showAddReplyDialog(holder.itemView.getContext(), rId, wrapper.getType(), position));
        holder.btnReplyMore.setOnClickListener(v -> showAddReplyDialog(holder.itemView.getContext(), rId, wrapper.getType(), position));

        holder.btnCollapseReply.setOnClickListener(v -> {
            boolean isCollapsed = collapsedReviewIds.contains(rId);
            if (isCollapsed) {
                collapsedReviewIds.remove(rId);
            } else {
                collapsedReviewIds.add(rId);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
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

    private View createReplyItemView(android.content.Context context, String reviewId, ReviewReplyResponse reply, int index, int position) {
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
        String senderRole = reply.getSenderRole();
        String currentUserName = TokenManager.getInstance().getFullName();
        boolean isOwner = currentUserName != null && currentUserName.equalsIgnoreCase(reply.getSenderName());

        if (isOwner) {
            tvIndex.setText("#" + (index + 1) + " - Bạn (Khách hàng) 👤");
        } else if ("STUDENT".equalsIgnoreCase(senderRole) || "ROLE_STUDENT".equalsIgnoreCase(senderRole)) {
            tvIndex.setText("#" + (index + 1) + " - Khách hàng (" + reply.getSenderName() + ") 👤");
        } else {
            tvIndex.setText("#" + (index + 1) + " - Cửa hàng 🏪");
        }
        tvIndex.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));
        tvIndex.setTextSize(11);
        tvIndex.setTypeface(null, android.graphics.Typeface.BOLD);

        android.widget.LinearLayout.LayoutParams indexParams = new android.widget.LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tvIndex.setLayoutParams(indexParams);
        headerLayout.addView(tvIndex);

        // Edit/Delete replies owned by the current logged-in user
        if (isOwner) {
            TextView btnEdit = new TextView(context);
            btnEdit.setText("Sửa");
            btnEdit.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));
            btnEdit.setTextSize(11);
            btnEdit.setPadding(12, 6, 12, 6);
            btnEdit.setClickable(true);
            btnEdit.setFocusable(true);
            btnEdit.setOnClickListener(v -> showEditReplyDialog(context, reviewId, reply.getId(), reply.getReplyText(), position));
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
                        ApiClient.getApiService().deleteReviewReply(reply.getId()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                if (response.isSuccessful()) {
                                    repliesCache.remove(reviewId);
                                    notifyItemChanged(position);
                                    android.widget.Toast.makeText(context, "Đã xóa phản hồi", android.widget.Toast.LENGTH_SHORT).show();
                                } else {
                                    android.widget.Toast.makeText(context, "Không thể xóa phản hồi", android.widget.Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                android.widget.Toast.makeText(context, "Lỗi kết nối", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Hủy", (d, which) -> d.dismiss())
                    .create();
                dialog.show();
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.parseColor("#FF4D4D"));
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));
            });
            headerLayout.addView(btnDelete);
        }

        itemLayout.addView(headerLayout);

        TextView tvContent = new TextView(context);
        tvContent.setText(reply.getReplyText());
        tvContent.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_primary));
        tvContent.setTextSize(13);
        android.widget.LinearLayout.LayoutParams contentParams = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentParams.topMargin = 4;
        tvContent.setLayoutParams(contentParams);
        itemLayout.addView(tvContent);

        return itemLayout;
    }

    private void showAddReplyDialog(android.content.Context context, String reviewId, String reviewType, int position) {
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
                androidx.core.content.ContextCompat.getColor(context, R.color.brand_orange));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(
                androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String replyText = etInput.getText().toString().trim();
            if (replyText.isEmpty()) {
                android.widget.Toast.makeText(context, "Nội dung phản hồi không được để trống", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();

            ReviewReplyRequest request = new ReviewReplyRequest(replyText, reviewType);
            ApiClient.getApiService().createReviewReply(reviewId, request).enqueue(new Callback<ReviewReplyResponse>() {
                @Override
                public void onResponse(@NonNull Call<ReviewReplyResponse> call, @NonNull Response<ReviewReplyResponse> response) {
                    if (response.isSuccessful()) {
                        repliesCache.remove(reviewId);
                        collapsedReviewIds.remove(reviewId);
                        notifyItemChanged(position);
                        android.widget.Toast.makeText(context, "Đã gửi phản hồi", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        android.widget.Toast.makeText(context, "Không thể gửi phản hồi", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ReviewReplyResponse> call, @NonNull Throwable t) {
                    android.widget.Toast.makeText(context, "Lỗi kết nối", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showEditReplyDialog(android.content.Context context, String reviewId, String replyId, String initialText, int position) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Chỉnh sửa phản hồi ✍️");

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
                androidx.core.content.ContextCompat.getColor(context, R.color.brand_orange));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(
                androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary));

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String replyText = etInput.getText().toString().trim();
            if (replyText.isEmpty()) {
                android.widget.Toast.makeText(context, "Nội dung phản hồi không được để trống", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();

            ReviewReplyRequest request = new ReviewReplyRequest(replyText);
            ApiClient.getApiService().updateReviewReply(replyId, request).enqueue(new Callback<ReviewReplyResponse>() {
                @Override
                public void onResponse(@NonNull Call<ReviewReplyResponse> call, @NonNull Response<ReviewReplyResponse> response) {
                    if (response.isSuccessful()) {
                        repliesCache.remove(reviewId);
                        notifyItemChanged(position);
                        android.widget.Toast.makeText(context, "Đã cập nhật phản hồi", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        android.widget.Toast.makeText(context, "Không thể cập nhật phản hồi", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ReviewReplyResponse> call, @NonNull Throwable t) {
                    android.widget.Toast.makeText(context, "Lỗi kết nối", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvRating;
        public TextView tvComment;
        public TextView tvDate;
        public View layoutShopReply;
        public android.widget.LinearLayout layoutRepliesContainer;
        public TextView btnReplyReview;
        public TextView btnCollapseReply;
        public TextView btnReplyMore;

        public ViewHolder(@NonNull View itemView) {
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
