package com.foodorderingapp.ui.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.utils.ImageUrlUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveOrderAdapter extends RecyclerView.Adapter<ActiveOrderAdapter.OrderViewHolder> {

    public interface OnTrackClickListener {
        void onTrackClick(OrderResponse order);
    }

    public interface OnContactShopClickListener {
        void onContactShopClick(OrderResponse order);
    }

    public interface OnCancelOrderClickListener {
        void onCancelOrderClick(OrderResponse order);
    }

    private static final int COLOR_DARK = Color.parseColor("#1A1D26");
    private static final int COLOR_ORANGE = Color.parseColor("#FF7118");
    private static final int COLOR_BROWN = Color.parseColor("#9D3900");
    private static final int COLOR_MUTED = Color.parseColor("#A7AFBD");
    private static final int COLOR_TEXT_MUTED = Color.parseColor("#55607A");

    private final List<OrderResponse> orders = new ArrayList<>();
    private final OnTrackClickListener trackClickListener;
    private final OnContactShopClickListener contactShopClickListener;
    private final OnCancelOrderClickListener cancelOrderClickListener;

    public ActiveOrderAdapter(OnTrackClickListener trackClickListener) {
        this(trackClickListener, null, null);
    }

    public ActiveOrderAdapter(OnTrackClickListener trackClickListener,
                              OnContactShopClickListener contactShopClickListener) {
        this(trackClickListener, contactShopClickListener, null);
    }

    public ActiveOrderAdapter(OnTrackClickListener trackClickListener,
                              OnContactShopClickListener contactShopClickListener,
                              OnCancelOrderClickListener cancelOrderClickListener) {
        this.trackClickListener = trackClickListener;
        this.contactShopClickListener = contactShopClickListener;
        this.cancelOrderClickListener = cancelOrderClickListener;
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
        holder.tvOrderLocation.setText(formatLocation(order.getBuilding()));
        holder.tvOrderSummaryItems.setText(formatSummaryItems(order.getDetails()));
        bindOrderImage(holder.ivOrderImage, order.getDetails());
        bindShipping(holder.tvOrderShipping, order);
        bindDiscount(holder.tvOrderDiscount, order);
        holder.tvOrderTotal.setText(formatPrice(order.getTotalPrice()));
        holder.btnContactShop.setOnClickListener(v -> {
            if (contactShopClickListener != null) {
                contactShopClickListener.onContactShopClick(order);
            }
        });

        if ("PENDING".equalsIgnoreCase(order.getStatus())) {
            holder.btnCancelOrder.setVisibility(View.VISIBLE);
            holder.btnCancelOrder.setOnClickListener(v -> {
                if (cancelOrderClickListener != null) {
                    cancelOrderClickListener.onCancelOrderClick(order);
                }
            });
        } else {
            holder.btnCancelOrder.setVisibility(View.GONE);
            holder.btnCancelOrder.setOnClickListener(null);
        }

        if ("DELIVERING".equals(order.getStatus())) {
            holder.btnTrackDelivery.setVisibility(View.VISIBLE);
            holder.btnTrackDelivery.setOnClickListener(v -> {
                if (trackClickListener != null) {
                    trackClickListener.onTrackClick(order);
                }
            });
        } else {
            holder.btnTrackDelivery.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private void bindTimeline(OrderViewHolder holder, String status) {
        int currentStep = getCurrentStep(status);
        android.content.Context context = holder.itemView.getContext();
        int colorOrange = androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_orange);
        int colorMuted = androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_border);

        bindStep(holder.tvStepOneIcon, holder.tvStepOneTitle, holder.tvStepOneSubtitle,
                "Chờ xác nhận", currentStep, 1, "Đơn đã được tạo");
        bindStep(holder.tvStepTwoIcon, holder.tvStepTwoTitle, holder.tvStepTwoSubtitle,
                "Đã xác nhận", currentStep, 2, "Quán đã nhận đơn");
        bindStep(holder.tvStepThreeIcon, holder.tvStepThreeTitle, holder.tvStepThreeSubtitle,
                "Đang giao", currentStep, 3, "Đơn đang trên đường giao");

        if (currentStep > 1) {
            holder.lineStepOne.setBackgroundColor(colorOrange);
        } else {
            holder.lineStepOne.setBackgroundColor(colorMuted);
        }

        if (currentStep > 2) {
            holder.lineStepTwo.setBackgroundColor(colorOrange);
        } else {
            holder.lineStepTwo.setBackgroundColor(colorMuted);
        }
    }

    private void bindStep(ImageView icon, TextView title, TextView subtitle,
                          String titleText, int currentStep, int step, String activeText) {
        title.setText(titleText);
        android.content.Context context = title.getContext();
        int colorDark = androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_primary);
        int colorOrange = androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_orange);
        int colorMuted = androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary);
        int colorTextMuted = androidx.core.content.ContextCompat.getColor(context, R.color.vendor_dark_text_secondary);

        if (currentStep > step) {
            icon.setImageResource(R.drawable.ic_step_active);
            icon.setColorFilter(colorOrange, PorterDuff.Mode.SRC_IN);
            title.setTextColor(colorDark);
            subtitle.setTextColor(colorTextMuted);
            subtitle.setText("Đã hoàn tất bước này");
        } else if (currentStep == step) {
            icon.setImageResource(R.drawable.ic_step_active);
            icon.setColorFilter(colorOrange, PorterDuff.Mode.SRC_IN);
            title.setTextColor(colorOrange);
            subtitle.setTextColor(colorOrange);
            subtitle.setText(activeText);
        } else {
            icon.setImageResource(R.drawable.ic_step_inactive);
            icon.setColorFilter(colorMuted, PorterDuff.Mode.SRC_IN);
            title.setTextColor(colorMuted);
            subtitle.setTextColor(colorMuted);
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

    private void bindOrderImage(ImageView imageView, List<OrderDetailResponse> details) {
        String imageUrl = findFirstImageUrl(details);
        Glide.with(imageView.getContext())
                .load(ImageUrlUtils.resolveImageUrl(imageUrl))
                .placeholder(R.drawable.logo_food)
                .error(R.drawable.logo_food)
                .centerCrop()
                .into(imageView);
    }

    private String findFirstImageUrl(List<OrderDetailResponse> details) {
        if (details == null) {
            return null;
        }
        for (OrderDetailResponse detail : details) {
            if (detail != null && detail.getImageUrl() != null
                    && !detail.getImageUrl().trim().isEmpty()) {
                return detail.getImageUrl();
            }
        }
        return null;
    }

    private void bindShipping(TextView view, OrderResponse order) {
        if (view == null) return;
        double shipping = calculateShippingFee(order);
        view.setText("Phí vận chuyển: " + formatPrice(shipping));
        view.setVisibility(View.VISIBLE);
    }

    private double calculateShippingFee(OrderResponse order) {
        if (order.getBuildingLatitude() == null || order.getBuildingLongitude() == null
                || order.getShopLatitude() == null || order.getShopLongitude() == null
                || order.getShopLatitude() == 0.0 || order.getShopLongitude() == 0.0) {
            return 5000.0; // Fallback to 5k
        }
        float[] results = new float[1];
        try {
            android.location.Location.distanceBetween(
                    order.getShopLatitude(), order.getShopLongitude(),
                    order.getBuildingLatitude(), order.getBuildingLongitude(),
                    results
            );
            double distKm = results[0] / 1000.0;
            return Math.round((distKm * 5000.0) / 1000.0) * 1000.0;
        } catch (Exception e) {
            return 5000.0;
        }
    }

    private String formatLocation(String building) {
        return "Tòa nhận: " + nullToDefault(building, "Chưa chọn");
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
        ImageView tvStepOneIcon;
        TextView tvStepOneTitle;
        TextView tvStepOneSubtitle;
        ImageView tvStepTwoIcon;
        TextView tvStepTwoTitle;
        TextView tvStepTwoSubtitle;
        ImageView tvStepThreeIcon;
        TextView tvStepThreeTitle;
        TextView tvStepThreeSubtitle;
        TextView tvOrderShopName;
        TextView tvOrderNumber;
        TextView tvOrderLocation;
        ImageView ivOrderImage;
        TextView tvOrderSummaryItems;
        TextView tvOrderShipping;
        TextView tvOrderDiscount;
        TextView tvOrderTotal;
        View btnContactShop;
        View btnCancelOrder;
        View btnTrackDelivery;
        View lineStepOne;
        View lineStepTwo;

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
            ivOrderImage = itemView.findViewById(R.id.ivActiveOrderImage);
            tvOrderSummaryItems = itemView.findViewById(R.id.tvOrderSummaryItems);
            tvOrderShipping = itemView.findViewById(R.id.tvOrderShipping);
            tvOrderDiscount = itemView.findViewById(R.id.tvOrderDiscount);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnContactShop = itemView.findViewById(R.id.btnContactShop);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            btnTrackDelivery = itemView.findViewById(R.id.btnTrackDelivery);
            lineStepOne = itemView.findViewById(R.id.lineStepOne);
            lineStepTwo = itemView.findViewById(R.id.lineStepTwo);
        }
    }
}
