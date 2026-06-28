package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.foodorderingapp.model.response.OrderResponse;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderVH> {
    private final List<OrderResponse> orders = new ArrayList<>();
    private boolean showReviewAction;
    private OnReviewClickListener reviewClickListener;
    private OnHideClickListener hideClickListener;
    private String pendingHideOrderId;

    public interface OnReviewClickListener {
        void onReviewClick(OrderResponse order);
    }

    public interface OnHideClickListener {
        void onHideClick(OrderResponse order);
    }

    public void setShowReviewAction(boolean showReviewAction) {
        this.showReviewAction = showReviewAction;
        notifyDataSetChanged();
    }

    public void setOnReviewClickListener(OnReviewClickListener listener) {
        this.reviewClickListener = listener;
    }

    public void setOnHideClickListener(OnHideClickListener listener) {
        this.hideClickListener = listener;
    }

    public void setPendingHideOrderId(String orderId) {
        pendingHideOrderId = orderId;
        notifyDataSetChanged();
    }

    public void submitList(List<OrderResponse> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        if (pendingHideOrderId != null && !containsOrderId(pendingHideOrderId)) {
            pendingHideOrderId = null;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderVH holder, int position) {
        OrderResponse order = orders.get(position);

        holder.tvShopName.setText(nullToDefault(order.getShopName(), "Đơn hàng"));
        holder.tvStatus.setText(formatStatus(order.getStatus()));
        holder.tvTotal.setText(formatPrice(order.getTotalPrice()));
        holder.tvCreatedAt.setText(formatDateTime(displayDateTimeForHistory(order)));
        holder.tvAddress.setText(buildAddress(order));
        holder.tvDetails.setText(buildDetails(order.getDetails()));
        bindDiscount(holder.tvDiscount, order);

        String cancelReason = order.getCancelReason();
        if (cancelReason != null && !cancelReason.trim().isEmpty()) {
            holder.tvCancelReason.setVisibility(View.VISIBLE);
            holder.tvCancelReason.setText("Lý do hủy: " + cancelReason);
        } else {
            holder.tvCancelReason.setVisibility(View.GONE);
        }

        boolean canReview = showReviewAction 
                && ("COMPLETED".equalsIgnoreCase(order.getStatus()) || "RECEIVED".equalsIgnoreCase(order.getStatus()))
                && !order.isReviewed();
        boolean canShowHideAction = hideClickListener != null
                && order.getId() != null
                && order.getId().equals(pendingHideOrderId);
        holder.layoutActions.setVisibility(canReview || canShowHideAction ? View.VISIBLE : View.GONE);
        holder.btnReview.setVisibility(canReview ? View.VISIBLE : View.GONE);
        holder.btnReview.setOnClickListener(v -> {
            if (reviewClickListener != null) {
                reviewClickListener.onReviewClick(order);
            }
        });
        holder.btnHide.setVisibility(canShowHideAction ? View.VISIBLE : View.GONE);
        holder.btnHide.setOnClickListener(v -> {
            if (hideClickListener != null) {
                hideClickListener.onHideClick(order);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (hideClickListener == null) {
                return false;
            }
            if (order.getId() == null || order.getId().trim().isEmpty()) {
                return false;
            }
            setPendingHideOrderId(order.getId());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private String buildDetails(List<OrderDetailResponse> details) {
        if (details == null || details.isEmpty()) {
            return "Chưa có chi tiết món";
        }

        StringBuilder builder = new StringBuilder();
        for (OrderDetailResponse detail : details) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(detail.getQuantity())
                    .append(" x ")
                    .append(nullToDefault(detail.getFoodName(), "Món ăn"))
                    .append(" - ")
                    .append(formatPrice(detail.getPrice()));
        }
        return builder.toString();
    }

    private String buildAddress(OrderResponse order) {
        String building = nullToDefault(order.getBuilding(), "").trim();
        if (building.isEmpty()) {
            return "Chưa chọn tòa nhận";
        }
        return "Tòa nhận: " + building;
    }

    private void bindDiscount(TextView view, OrderResponse order) {
        if (order.getDiscountAmount() <= 0) {
            view.setVisibility(View.GONE);
            return;
        }
        String code = order.getVoucherCode();
        String prefix = code == null || code.trim().isEmpty() ? "Giam gia" : "Voucher " + code;
        view.setVisibility(View.VISIBLE);
        view.setText(prefix + ": -" + formatPrice(order.getDiscountAmount()));
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "Không rõ";
        }

        switch (status.toUpperCase(Locale.ROOT)) {
            case "PENDING":
                return "Chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "DELIVERING":
                return "Đang giao";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            case "REJECTED":
                return "Bị từ chối";
            case "FAILED":
                return "Thất bại";
            case "RECEIVED":
                return "Đã nhận";
            default:
                return status;
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    private String displayDateTimeForHistory(OrderResponse order) {
        if (order == null) {
            return "";
        }
        if (("COMPLETED".equalsIgnoreCase(order.getStatus()) || "RECEIVED".equalsIgnoreCase(order.getStatus()))
                && order.getCompletedAt() != null
                && !order.getCompletedAt().trim().isEmpty()) {
            return order.getCompletedAt();
        }
        return order.getCreatedAt();
    }

    private String formatDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String normalized = value.trim();
        int dotIndex = normalized.indexOf('.');
        if (dotIndex > 0) {
            normalized = normalized.substring(0, dotIndex);
        }

        try {
            return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault()));
        } catch (DateTimeParseException ignored) {
            return value;
        }
    }

    private String nullToDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private boolean containsOrderId(String orderId) {
        for (OrderResponse order : orders) {
            if (orderId.equals(order.getId())) {
                return true;
            }
        }
        return false;
    }

    static class OrderVH extends RecyclerView.ViewHolder {
        TextView tvShopName;
        TextView tvStatus;
        TextView tvTotal;
        TextView tvCreatedAt;
        TextView tvAddress;
        TextView tvDetails;
        TextView tvCancelReason;
        TextView tvDiscount;
        View layoutActions;
        MaterialButton btnReview;
        MaterialButton btnHide;

        OrderVH(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvOrderShopName);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvCreatedAt = itemView.findViewById(R.id.tvOrderCreatedAt);
            tvAddress = itemView.findViewById(R.id.tvOrderAddress);
            tvDetails = itemView.findViewById(R.id.tvOrderDetails);
            tvCancelReason = itemView.findViewById(R.id.tvOrderCancelReason);
            tvDiscount = itemView.findViewById(R.id.tvOrderDiscount);
            layoutActions = itemView.findViewById(R.id.layoutOrderActions);
            btnReview = itemView.findViewById(R.id.btnReviewOrder);
            btnHide = itemView.findViewById(R.id.btnHideOrder);
        }
    }
}
