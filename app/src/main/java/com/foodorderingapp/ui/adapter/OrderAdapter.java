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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderVH> {
    private final List<OrderResponse> orders = new ArrayList<>();
    private boolean showReviewAction;
    private OnReviewClickListener reviewClickListener;

    public interface OnReviewClickListener {
        void onReviewClick(OrderResponse order);
    }

    public void setShowReviewAction(boolean showReviewAction) {
        this.showReviewAction = showReviewAction;
        notifyDataSetChanged();
    }

    public void setOnReviewClickListener(OnReviewClickListener listener) {
        this.reviewClickListener = listener;
    }

    public void submitList(List<OrderResponse> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
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
        holder.tvCreatedAt.setText(nullToDefault(order.getCreatedAt(), ""));
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
        holder.btnReview.setVisibility(canReview ? View.VISIBLE : View.GONE);
        holder.btnReview.setOnClickListener(v -> {
            if (reviewClickListener != null) {
                reviewClickListener.onReviewClick(order);
            }
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
        String dropOff = nullToDefault(order.getDropOff(), "").trim();

        if (building.isEmpty() && dropOff.isEmpty()) {
            return "Chưa có địa điểm nhận";
        }
        if (building.isEmpty()) {
            return dropOff;
        }
        if (dropOff.isEmpty()) {
            return building;
        }
        return building + " - " + dropOff;
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

    private String nullToDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
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
        MaterialButton btnReview;

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
            btnReview = itemView.findViewById(R.id.btnReviewOrder);
        }
    }
}
