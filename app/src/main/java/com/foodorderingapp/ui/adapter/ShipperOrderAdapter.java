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

public class ShipperOrderAdapter extends RecyclerView.Adapter<ShipperOrderAdapter.ShipperViewHolder> {

    public interface OnOrderActionListener {
        void onClaim(OrderResponse order);
        void onDeliver(OrderResponse order);
    }

    private final List<OrderResponse> orders = new ArrayList<>();
    private final int mode; // 0: Available, 1: Active, 2: Completed
    private final OnOrderActionListener listener;

    public ShipperOrderAdapter(int mode, OnOrderActionListener listener) {
        this.mode = mode;
        this.listener = listener;
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
    public ShipperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shipper_order, parent, false);
        return new ShipperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShipperViewHolder holder, int position) {
        OrderResponse order = orders.get(position);

        holder.tvShopName.setText(nullToDefault(order.getShopName(), "Quán ăn"));
        holder.tvOrderNumber.setText("Đơn hàng #" + shortOrderId(order.getId()));
        holder.tvCustomer.setText("Khách: " + nullToDefault(order.getCustomerName(), "N/A") + " - " + nullToDefault(order.getCustomerPhone(), "N/A"));
        holder.tvLocation.setText(nullToDefault(order.getBuilding(), "") + " - " + nullToDefault(order.getDropOff(), ""));
        holder.tvSummaryItems.setText(formatSummaryItems(order.getDetails()));
        holder.tvTotal.setText(formatPrice(order.getTotalPrice()));
        
        holder.tvStatus.setText(formatStatusText(order.getStatus()));

        if (mode == 0) {
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Nhận đơn");
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onClaim(order);
            });
        } else if (mode == 1) {
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Bản đồ & Giao hàng");
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onDeliver(order);
            });
        } else {
            holder.btnAction.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private String formatStatusText(String status) {
        if (status == null) return "Chờ xử lý";
        switch (status.toUpperCase(Locale.ROOT)) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "DELIVERING": return "Đang giao";
            case "COMPLETED": return "Đã hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }

    private String formatSummaryItems(List<OrderDetailResponse> details) {
        if (details == null || details.isEmpty()) {
            return "Chưa có chi tiết món";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < details.size(); i++) {
            OrderDetailResponse detail = details.get(i);
            if (i > 0) builder.append('\n');
            builder.append(detail.getQuantity()).append("x ").append(nullToDefault(detail.getFoodName(), "Món"));
        }
        return builder.toString();
    }

    private String shortOrderId(String id) {
        if (id == null || id.isEmpty()) return "000000";
        String comp = id.replace("-", "");
        return comp.length() > 6 ? comp.substring(0, 6) : comp;
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    private String nullToDefault(String value, String defValue) {
        return (value == null || value.trim().isEmpty()) ? defValue : value;
    }

    static class ShipperViewHolder extends RecyclerView.ViewHolder {
        TextView tvShopName, tvOrderStatus, tvOrderNumber, tvCustomer, tvLocation, tvSummaryItems, tvTotal, tvStatus;
        MaterialButton btnAction;

        ShipperViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvOrderShopName);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvCustomer = itemView.findViewById(R.id.tvOrderCustomer);
            tvLocation = itemView.findViewById(R.id.tvOrderLocation);
            tvSummaryItems = itemView.findViewById(R.id.tvOrderSummaryItems);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnAction = itemView.findViewById(R.id.btnOrderAction);
        }
    }
}
