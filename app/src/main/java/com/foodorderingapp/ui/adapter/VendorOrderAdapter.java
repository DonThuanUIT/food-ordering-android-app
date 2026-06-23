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
        private final TextView tvOrderRoom;
        private final TextView tvOrderDelivery;
        private final TextView tvOrderCustomer;
        private final LinearLayout layoutOrderItems;
        private final TextView tvOrderTotal;
        private final TextView tvCancelReason;
        private final Button btnActionCancel;
        private final Button btnActionAccept;
        private final Button btnActionDeliver;
        private final Button btnActionComplete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderTime = itemView.findViewById(R.id.tv_order_time);
            tvOrderRoom = itemView.findViewById(R.id.tv_order_room);
            tvOrderDelivery = itemView.findViewById(R.id.tv_order_delivery);
            tvOrderCustomer = itemView.findViewById(R.id.tv_order_customer);
            layoutOrderItems = itemView.findViewById(R.id.layout_order_items);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            tvCancelReason = itemView.findViewById(R.id.tv_cancel_reason);
            btnActionCancel = itemView.findViewById(R.id.btn_action_cancel);
            btnActionAccept = itemView.findViewById(R.id.btn_action_accept);
            btnActionDeliver = itemView.findViewById(R.id.btn_action_deliver);
            btnActionComplete = itemView.findViewById(R.id.btn_action_complete);
        }

        public void bind(OrderResponse order) {
            // 1. Order ID Display
            String shortId = order.getId();
            if (shortId != null && shortId.length() > 6) {
                shortId = shortId.substring(shortId.length() - 6);
            }
            tvOrderId.setText("Đơn hàng #" + shortId);

            // 2. Status Badge Styling
            String status = order.getStatus();
            String readableStatus = status;
            if ("PENDING".equalsIgnoreCase(status)) readableStatus = "Chờ xử lý";
            else if ("CONFIRMED".equalsIgnoreCase(status)) readableStatus = "Đã xác nhận";
            else if ("DELIVERING".equalsIgnoreCase(status)) readableStatus = "Đang giao";
            else if ("COMPLETED".equalsIgnoreCase(status)) readableStatus = "Hoàn thành";
            else if ("CANCELLED".equalsIgnoreCase(status)) readableStatus = "Đã hủy";
            tvOrderStatus.setText(readableStatus);
            int badgeColor = Color.parseColor("#E53935"); // Default Red
            if ("PENDING".equalsIgnoreCase(status)) {
                badgeColor = Color.parseColor("#E53935"); // Red
            } else if ("CONFIRMED".equalsIgnoreCase(status)) {
                badgeColor = Color.parseColor("#3182CE"); // Blue
            } else if ("DELIVERING".equalsIgnoreCase(status)) {
                badgeColor = Color.parseColor("#DD6B20"); // Orange
            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                badgeColor = Color.parseColor("#38A169"); // Green
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                badgeColor = Color.parseColor("#718096"); // Grey
            }
            tvOrderStatus.setBackgroundTintList(ColorStateList.valueOf(badgeColor));

            // 3. Time Display
            tvOrderTime.setText(formatDateTimeShort(order.getCreatedAt()));

            // 4. Delivery Details
            tvOrderRoom.setText(order.getDropOff() != null ? order.getDropOff() : "Phòng chưa rõ");
            tvOrderDelivery.setText("Giao đến: " + (order.getBuilding() != null ? order.getBuilding() : "Tòa nhà chưa rõ"));
            tvOrderCustomer.setText("Khách: " + (order.getCustomerName() != null ? order.getCustomerName() : "Không tên") 
                    + " (" + (order.getCustomerPhone() != null ? order.getCustomerPhone() : "") + ")");

            // 5. Build Items List dynamically
            layoutOrderItems.removeAllViews();
            if (order.getDetails() != null) {
                for (OrderDetailResponse detail : order.getDetails()) {
                    RelativeLayout itemRow = new RelativeLayout(itemView.getContext());
                    itemRow.setLayoutParams(new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    itemRow.setPadding(0, 6, 0, 6);

                    TextView tvName = new TextView(itemView.getContext());
                    tvName.setText(detail.getQuantity() + "x " + detail.getFoodName());
                    tvName.setTextColor(Color.parseColor("#1A202C"));
                    tvName.setTextSize(13);
                    RelativeLayout.LayoutParams lpLeft = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpLeft.addRule(RelativeLayout.ALIGN_PARENT_START);
                    tvName.setLayoutParams(lpLeft);

                    TextView tvPrice = new TextView(itemView.getContext());
                    tvPrice.setText(formatCurrency(detail.getPrice() * detail.getQuantity()));
                    tvPrice.setTextColor(Color.parseColor("#718096"));
                    tvPrice.setTextSize(13);
                    RelativeLayout.LayoutParams lpRight = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpRight.addRule(RelativeLayout.ALIGN_PARENT_END);
                    tvPrice.setLayoutParams(lpRight);

                    itemRow.addView(tvName);
                    itemRow.addView(tvPrice);
                    layoutOrderItems.addView(itemRow);
                }
            }

            // 6. Total Payment
            tvOrderTotal.setText(formatCurrency(order.getTotalPrice()));

            // 7. Cancel Reason Display
            if ("CANCELLED".equalsIgnoreCase(status)) {
                tvCancelReason.setVisibility(View.VISIBLE);
                tvCancelReason.setText("Lý do hủy: " + (order.getCancelReason() != null ? order.getCancelReason() : "Không có lý do"));
            } else {
                tvCancelReason.setVisibility(View.GONE);
            }

            // 8. Action Buttons Flow
            btnActionCancel.setVisibility(View.GONE);
            btnActionAccept.setVisibility(View.GONE);
            btnActionDeliver.setVisibility(View.GONE);
            btnActionComplete.setVisibility(View.GONE);

            if ("PENDING".equalsIgnoreCase(status)) {
                btnActionCancel.setVisibility(View.VISIBLE);
                btnActionAccept.setVisibility(View.VISIBLE);
            } else if ("CONFIRMED".equalsIgnoreCase(status)) {
                btnActionCancel.setVisibility(View.VISIBLE);
                btnActionDeliver.setVisibility(View.VISIBLE);
            } else if ("DELIVERING".equalsIgnoreCase(status)) {
                btnActionCancel.setVisibility(View.VISIBLE);
                btnActionComplete.setVisibility(View.VISIBLE);
            }

            // 9. Click Listeners
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

            itemView.setOnClickListener(v -> {
                if (actionHandler != null) actionHandler.onOrderClicked(order);
            });
        }

        private String formatDateTimeShort(String isoDateTime) {
            if (isoDateTime == null || isoDateTime.isEmpty()) return "";
            try {
                String clean = isoDateTime;
                if (clean.contains(".")) {
                    clean = clean.substring(0, clean.indexOf("."));
                }
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM HH:mm", Locale.US);
                Date date = parser.parse(clean);
                return formatter.format(date);
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
