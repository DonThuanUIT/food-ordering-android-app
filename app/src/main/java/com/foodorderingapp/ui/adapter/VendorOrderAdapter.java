package com.foodorderingapp.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.foodorderingapp.model.response.OrderResponse;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VendorOrderAdapter extends RecyclerView.Adapter<VendorOrderAdapter.OrderViewHolder> {

    public interface OrderActionHandler {
        void onAcceptClicked(OrderResponse order);
        void onDeliverClicked(OrderResponse order);
        void onCompleteClicked(OrderResponse order);
        void onCancelClicked(OrderResponse order);
        void onContactStudentClicked(OrderResponse order);
        void onOrderClicked(OrderResponse order);
    }

    private final List<OrderResponse> orders;
    private final OrderActionHandler actionHandler;

    public VendorOrderAdapter(List<OrderResponse> orders, OrderActionHandler actionHandler) {
        this.orders = orders;
        this.actionHandler = actionHandler;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vendor_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateData(List<OrderResponse> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId;
        private final TextView tvOrderStatus;
        private final TextView tvOrderTime;
        private final TextView tvOrderDelivery;
        private final TextView tvOrderCustomer;
        private final LinearLayout layoutOrderItems;
        private final TextView tvOrderTotal;
        private final TextView tvCancelReason;
        private final Button btnActionCancel;
        private final Button btnActionAccept;
        private final Button btnActionDeliver;
        private final Button btnActionComplete;
        private final Button btnContactStudent;
        private final TextView tvOrderItemsCount;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderTime = itemView.findViewById(R.id.tv_order_time);
            tvOrderDelivery = itemView.findViewById(R.id.tv_order_delivery);
            tvOrderCustomer = itemView.findViewById(R.id.tv_order_customer);
            layoutOrderItems = itemView.findViewById(R.id.layout_order_items);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            tvCancelReason = itemView.findViewById(R.id.tv_cancel_reason);
            btnActionCancel = itemView.findViewById(R.id.btn_action_cancel);
            btnActionAccept = itemView.findViewById(R.id.btn_action_accept);
            btnActionDeliver = itemView.findViewById(R.id.btn_action_deliver);
            btnActionComplete = itemView.findViewById(R.id.btn_action_complete);
            btnContactStudent = itemView.findViewById(R.id.btn_contact_student);
            tvOrderItemsCount = itemView.findViewById(R.id.tv_order_items_count);
        }

        public void bind(OrderResponse order) {
            // 1. Order ID Display (Hidden in layout, but set for safety)
            String shortId = order.getId();
            if (shortId != null && shortId.length() > 6) {
                shortId = shortId.substring(shortId.length() - 6);
            }
            if (tvOrderId != null) tvOrderId.setText("Đơn hàng #" + shortId);

            // 2. Status Badge Styling
            String status = order.getStatus();
            String readableStatus = status;
            int badgeColor = Color.parseColor("#718096"); // Default Grey
            if ("PENDING".equalsIgnoreCase(status)) {
                readableStatus = "MỚI";
                badgeColor = Color.parseColor("#F46E26"); // Orange
            } else if ("CONFIRMED".equalsIgnoreCase(status)) {
                readableStatus = "ĐÃ XÁC NHẬN";
                badgeColor = Color.parseColor("#F46E26"); // Orange
            } else if ("DELIVERING".equalsIgnoreCase(status)) {
                readableStatus = "ĐANG GIAO";
                badgeColor = Color.parseColor("#382C29"); // Dark Cacao Grey
            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                readableStatus = "HOÀN THÀNH";
                badgeColor = Color.parseColor("#38A169"); // Green
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                readableStatus = "ĐÃ HỦY";
                badgeColor = Color.parseColor("#718096"); // Grey
            }
            tvOrderStatus.setText(readableStatus);
            tvOrderStatus.setBackgroundTintList(ColorStateList.valueOf(badgeColor));

            // 3. Relative Time Display
            tvOrderTime.setText(getRelativeTime(order.getCreatedAt()));

            // 4. Details Info
            tvOrderCustomer.setText(order.getCustomerName() != null ? order.getCustomerName() : "Không tên");
            tvOrderDelivery.setText("Tòa nhận: " + (order.getBuilding() != null ? order.getBuilding() : "Chưa chọn"));
            
            // 5. Total Items Count
            int totalItems = 0;
            if (order.getDetails() != null) {
                for (OrderDetailResponse detail : order.getDetails()) {
                    totalItems += detail.getQuantity();
                }
            }
            if (tvOrderItemsCount != null) {
                tvOrderItemsCount.setText(String.format(Locale.US, "Món: %02d món", totalItems));
            }

            // 6. Build Items List (Hidden on main card list)
            if (layoutOrderItems != null) {
                layoutOrderItems.setVisibility(View.GONE);
            }

            // 7. Total Payment
            tvOrderTotal.setText(formatCurrency(order.getTotalPrice()));

            // 8. Cancel Reason Display
            if ("CANCELLED".equalsIgnoreCase(status)) {
                tvCancelReason.setVisibility(View.VISIBLE);
                tvCancelReason.setText("Lý do hủy: " + (order.getCancelReason() != null ? order.getCancelReason() : "Không có lý do"));
            } else {
                tvCancelReason.setVisibility(View.GONE);
            }

            // 9. Action Buttons Flow
            btnActionCancel.setVisibility(View.GONE);
            btnActionAccept.setVisibility(View.GONE);
            btnActionDeliver.setVisibility(View.GONE);
            btnActionComplete.setVisibility(View.GONE);

            btnActionDeliver.setText("GIAO HÀNG"); // Reset default

            if ("PENDING".equalsIgnoreCase(status)) {
                btnActionCancel.setVisibility(View.VISIBLE);
                btnActionAccept.setVisibility(View.VISIBLE);
            } else if ("CONFIRMED".equalsIgnoreCase(status)) {
                btnActionCancel.setVisibility(View.VISIBLE);
            } else if ("DELIVERING".equalsIgnoreCase(status)) {
                btnActionDeliver.setVisibility(View.VISIBLE);
                btnActionDeliver.setText("XEM BẢN ĐỒ");
            }

            // 10. Click Listeners
            btnActionAccept.setOnClickListener(v -> {
                if (actionHandler != null) actionHandler.onAcceptClicked(order);
            });

            btnActionDeliver.setOnClickListener(v -> {
                if (actionHandler != null) actionHandler.onDeliverClicked(order);
            });

            btnActionComplete.setOnClickListener(v -> {
                if (actionHandler != null) actionHandler.onCompleteClicked(order);
            });

            btnActionCancel.setOnClickListener(v -> {
                if (actionHandler != null) actionHandler.onCancelClicked(order);
            });

            btnContactStudent.setOnClickListener(v -> {
                if (actionHandler != null) actionHandler.onContactStudentClicked(order);
            });

            itemView.setOnClickListener(v -> {
                if (actionHandler != null) actionHandler.onOrderClicked(order);
            });
        }

        private String getRelativeTime(String isoDateTime) {
            if (isoDateTime == null || isoDateTime.isEmpty()) return "";
            try {
                String clean = isoDateTime;
                if (clean.contains(".")) {
                    clean = clean.substring(0, clean.indexOf("."));
                }
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date date = parser.parse(clean);
                long diffMs = System.currentTimeMillis() - date.getTime();
                long diffMins = diffMs / (60 * 1000);
                if (diffMins < 1) return "Vừa xong";
                if (diffMins < 60) return diffMins + " phút trước";
                long diffHours = diffMins / 60;
                if (diffHours < 24) return diffHours + " giờ trước";
                long diffDays = diffHours / 24;
                return diffDays + " ngày trước";
            } catch (Exception e) {
                return isoDateTime;
            }
        }

        private String formatCurrency(double value) {
            DecimalFormat formatter = new DecimalFormat("#,###");
            return formatter.format(value) + "đ";
        }
    }
}
