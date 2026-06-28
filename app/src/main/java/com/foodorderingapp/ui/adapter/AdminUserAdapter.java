package com.foodorderingapp.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.AdminUserResponse;
import com.foodorderingapp.utils.ImageUrlUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.AdminUserViewHolder> {

    private final List<AdminUserResponse> users = new ArrayList<>();
    private OnUserActiveChangeListener listener;

    public interface OnUserActiveChangeListener {
        void onUserActiveChanged(AdminUserResponse user, boolean isActive);
    }

    public void setOnUserActiveChangeListener(OnUserActiveChangeListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AdminUserResponse> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    public void appendList(List<AdminUserResponse> newUsers) {
        if (newUsers == null || newUsers.isEmpty()) {
            return;
        }
        int start = users.size();
        users.addAll(newUsers);
        notifyItemRangeInserted(start, newUsers.size());
    }

    @NonNull
    @Override
    public AdminUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new AdminUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUserViewHolder holder, int position) {
        AdminUserResponse user = users.get(position);
        boolean isActive = !user.isLocked();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        holder.tvName.setText(nullToDefault(user.getFullName(), "Người dùng"));
        holder.tvEmail.setText(firstNonBlank(user.getEmail(), user.getPhone(), "Chưa cập nhật"));
        holder.tvRole.setText(formatRole(user.getRole()));
        holder.tvRole.setBackgroundResource(roleBackground(user.getRole()));
        bindAvatar(holder, user.getAvatarUrl());

        holder.swActive.setOnCheckedChangeListener(null);
        holder.swActive.setChecked(isActive);
        holder.swActive.setEnabled(!isAdmin);
        holder.swActive.setAlpha(isAdmin ? 0.45f : 1f);
        holder.swActive.setOnCheckedChangeListener((buttonView, checked) -> {
            if (listener != null && checked != isActive) {
                listener.onUserActiveChanged(user, checked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void bindAvatar(AdminUserViewHolder holder, String avatarUrl) {
        String resolvedAvatarUrl = ImageUrlUtils.resolveImageUrl(avatarUrl);
        if (resolvedAvatarUrl == null) {
            Glide.with(holder.itemView).clear(holder.ivAvatar);
            holder.ivAvatar.setPadding(dp(holder.itemView, 12), dp(holder.itemView, 12),
                    dp(holder.itemView, 12), dp(holder.itemView, 12));
            holder.ivAvatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.ivAvatar.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.vendor_dark_text_secondary)));
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
            return;
        }

        holder.ivAvatar.setPadding(0, 0, 0, 0);
        holder.ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.ivAvatar.setImageTintList(null);
        Glide.with(holder.itemView)
                .load(resolvedAvatarUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(holder.ivAvatar);
    }

    private int roleBackground(String role) {
        if ("VENDOR".equalsIgnoreCase(role)) {
            return R.drawable.bg_admin_role_vendor;
        }
        return R.drawable.bg_admin_role_student;
    }

    private String formatRole(String role) {
        if ("VENDOR".equalsIgnoreCase(role)) {
            return "Người bán";
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "Quản trị viên";
        }
        return "Sinh viên";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }

    static class AdminUserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvEmail;
        TextView tvRole;
        Switch swActive;

        AdminUserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAdminUserAvatar);
            tvName = itemView.findViewById(R.id.tvAdminUserName);
            tvEmail = itemView.findViewById(R.id.tvAdminUserEmail);
            tvRole = itemView.findViewById(R.id.tvAdminUserRole);
            swActive = itemView.findViewById(R.id.swAdminUserActive);
        }
    }
}
