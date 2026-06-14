package com.foodorderingapp.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.foodorderingapp.model.response.OrderResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveOrderAdapter extends RecyclerView.Adapter<ActiveOrderAdapter.OrderViewHolder> {

    private static final int COLOR_DARK = Color.parseColor("#1A1D26");
    private static final int COLOR_ORANGE = Color.parseColor("#FF7118");
    private static final int COLOR_BROWN = Color.parseColor("#9D3900");
    private static final int COLOR_MUTED = Color.parseColor("#A7AFBD");
    private static final int COLOR_TEXT_MUTED = Color.parseColor("#55607A");

    private final List<OrderResponse> orders = new ArrayList<>();

    public void submitList(List<OrderResponse> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderResponse order = orders.get(position);

        bindTimeline(holder, order.getStatus());
        holder.tvOrderShopName.setText(nullToDefault(order.getShopName(), "Quán"));
        holder.tvOrderNumber.setText("Đơn hàng #" + shortOrderId(order.getId()));
        holder.tvOrderLocation.setText(formatLocation(order.getBuilding(), order.getDropOff()));
        holder.tvOrderSummaryItems.setText(formatSummaryItems(order.getDetails()));
        holder.tvOrderTotal.setText(formatPrice(order.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private void bindTimeline(OrderViewHolder holder, String status) {
        int currentStep = getCurrentStep(status);

        bindStep(holder.tvStepOneIcon, holder.tvStepOneTitle, holder.tvStepOneSubtitle,
                "Chờ xác nhận", currentStep, 1, "Đơn đã được tạo");
        bindStep(holder.tvStepTwoIcon, holder.tvStepTwoTitle, holder.tvStepTwoSubtitle,
                "Đã xác nhận", currentStep, 2, "Quán đã nhận đơn");
        bindStep(holder.tvStepThreeIcon, holder.tvStepThreeTitle, holder.tvStepThreeSubtitle,
                "Đang giao", currentStep, 3, "Đơn đang trên đường giao");
    }

    private void bindStep(TextView icon, TextView title, TextView subtitle,
                          String titleText, int currentStep, int step, String activeText) {
        title.setText(titleText);

        if (currentStep > step) {
            icon.setText("✓");
            icon.setTextColor(COLOR_ORANGE);
            title.setTextColor(COLOR_DARK);
            subtitle.setTextColor(COLOR_TEXT_MUTED);
            subtitle.setText("Đã hoàn tất bước này");
        } else if (currentStep == step) {
            icon.setText("●");
            icon.setTextColor(step == 3 ? COLOR_BROWN : COLOR_ORANGE);
            title.setTextColor(step == 3 ? COLOR_BROWN : COLOR_DARK);
            subtitle.setTextColor(step == 3 ? COLOR_BROWN : COLOR_TEXT_MUTED);
            subtitle.setText(activeText);
        } else {
            icon.setText("○");
            icon.setTextColor(COLOR_MUTED);
            title.setTextColor(COLOR_MUTED);
            subtitle.setTextColor(COLOR_MUTED);
            subtitle.setText("Chờ cập nhật");
        }
    }

    private int getCurrentStep(String status) {
        if (status == null) {
            return 1;
        }

        switch (status.toUpperCase(Locale.ROOT)) {
            case "CONFIRMED":
                return 2;
            case "DELIVERING":
            case "RECEIVED":
            case "COMPLETED":
                return 3;
            case "PENDING":
            default:
                return 1;
        }
    }

    private String formatSummaryItems(List<OrderDetailResponse> details) {
        if (details == null || details.isEmpty()) {
            return "Chưa có chi tiết món";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < details.size(); i++) {
            OrderDetailResponse detail = details.get(i);
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(detail.getQuantity())
                    .append("x ")
                    .append(nullToDefault(detail.getFoodName(), "Món"))
                    .append("    ")
                    .append(formatPrice(detail.getPrice() * detail.getQuantity()));
        }
        return builder.toString();
    }

    private String formatLocation(String building, String dropOff) {
        String safeBuilding = nullToDefault(building, "Chưa có tòa nhà");
        String safeDropOff = nullToDefault(dropOff, "Chưa có điểm nhận");
        return safeBuilding + " - " + safeDropOff;
    }

    private String shortOrderId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return "000000";
        }
        String compactId = id.replace("-", "");
        if (compactId.length() <= 6) {
            return compactId;
        }
        return compactId.substring(0, 6);
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepOneIcon;
        TextView tvStepOneTitle;
        TextView tvStepOneSubtitle;
        TextView tvStepTwoIcon;
        TextView tvStepTwoTitle;
        TextView tvStepTwoSubtitle;
        TextView tvStepThreeIcon;
        TextView tvStepThreeTitle;
        TextView tvStepThreeSubtitle;
        TextView tvOrderShopName;
        TextView tvOrderNumber;
        TextView tvOrderLocation;
        TextView tvOrderSummaryItems;
        TextView tvOrderTotal;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepOneIcon = itemView.findViewById(R.id.tvStepOneIcon);
            tvStepOneTitle = itemView.findViewById(R.id.tvStepOneTitle);
            tvStepOneSubtitle = itemView.findViewById(R.id.tvStepOneSubtitle);
            tvStepTwoIcon = itemView.findViewById(R.id.tvStepTwoIcon);
            tvStepTwoTitle = itemView.findViewById(R.id.tvStepTwoTitle);
            tvStepTwoSubtitle = itemView.findViewById(R.id.tvStepTwoSubtitle);
            tvStepThreeIcon = itemView.findViewById(R.id.tvStepThreeIcon);
            tvStepThreeTitle = itemView.findViewById(R.id.tvStepThreeTitle);
            tvStepThreeSubtitle = itemView.findViewById(R.id.tvStepThreeSubtitle);
            tvOrderShopName = itemView.findViewById(R.id.tvOrderShopName);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderLocation = itemView.findViewById(R.id.tvOrderLocation);
            tvOrderSummaryItems = itemView.findViewById(R.id.tvOrderSummaryItems);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}
