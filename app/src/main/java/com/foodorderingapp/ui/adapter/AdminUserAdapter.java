package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.AdminUserResponse;

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

    private int roleBackground(String role) {
        if ("VENDOR".equalsIgnoreCase(role)) {
            return R.drawable.bg_admin_role_vendor;
        }
        return R.drawable.bg_admin_role_student;
    }

    private String formatRole(String role) {
        if ("VENDOR".equalsIgnoreCase(role)) {
            return "Vendor";
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "Admin";
        }
        return "Student";
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

    static class AdminUserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvEmail;
        TextView tvRole;
        Switch swActive;

        AdminUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAdminUserName);
            tvEmail = itemView.findViewById(R.id.tvAdminUserEmail);
            tvRole = itemView.findViewById(R.id.tvAdminUserRole);
            swActive = itemView.findViewById(R.id.swAdminUserActive);
        }
    }
}
