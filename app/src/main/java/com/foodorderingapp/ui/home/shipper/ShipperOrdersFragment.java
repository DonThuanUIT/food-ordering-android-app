package com.foodorderingapp.ui.home.shipper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.ui.adapter.ShipperOrderAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.OrderViewModel;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ShipperOrdersFragment extends Fragment implements ShipperOrderAdapter.OnOrderActionListener {

    private ChipGroup chipGroup;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;

    private OrderViewModel viewModel;
    private ShipperOrderAdapter adapter;
    private int currentMode = 0; // 0: Available, 1: Active, 2: Completed

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shipper_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroup = view.findViewById(R.id.chip_group_shipper);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_shipper);
        rvOrders = view.findViewById(R.id.rv_shipper_orders);
        layoutEmpty = view.findViewById(R.id.layout_empty_shipper_orders);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefresh.setOnRefreshListener(this::loadData);

        viewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        setupObservers();
        setupListeners();
        
        // Load initial data
        updateAdapterMode(0);
    }

    private void setupListeners() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_shipper_available) {
                updateAdapterMode(0);
            } else if (checkedId == R.id.chip_shipper_active) {
                updateAdapterMode(1);
            } else if (checkedId == R.id.chip_shipper_completed) {
                updateAdapterMode(2);
            }
        });
    }

    private void updateAdapterMode(int mode) {
        currentMode = mode;
        adapter = new ShipperOrderAdapter(mode, this);
        rvOrders.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        if (currentMode == 0) {
            viewModel.loadShipperAvailableOrders();
        } else if (currentMode == 1) {
            viewModel.loadShipperActiveOrders();
        } else {
            viewModel.loadShipperOrderHistory();
        }
    }

    private void setupObservers() {
        viewModel.getShipperAvailableOrders().observe(getViewLifecycleOwner(), orders -> {
            if (currentMode == 0) {
                handleOrdersResult(orders);
            }
        });

        viewModel.getShipperActiveOrders().observe(getViewLifecycleOwner(), orders -> {
            if (currentMode == 1) {
                handleOrdersResult(orders);
            }
        });

        viewModel.getShipperOrderHistory().observe(getViewLifecycleOwner(), orders -> {
            if (currentMode == 2) {
                handleOrdersResult(orders);
            }
        });

        viewModel.getClaimResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success) {
                    ToastUtils.success(getContext(), "Nhận đơn thành công!");
                    // Switch to active deliveries tab
                    chipGroup.check(R.id.chip_shipper_active);
                } else {
                    ToastUtils.error(getContext(), "Không thể nhận đơn!");
                }
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                ToastUtils.info(getContext(), msg);
            }
        });
    }

    private void handleOrdersResult(List<OrderResponse> orders) {
        swipeRefresh.setRefreshing(false);
        if (orders == null || orders.isEmpty()) {
            adapter.submitList(null);
            layoutEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            adapter.submitList(orders);
            layoutEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClaim(OrderResponse order) {
        if (order != null && order.getId() != null) {
            viewModel.claimOrder(order.getId());
        }
    }

    @Override
    public void onDeliver(OrderResponse order) {
        if (order != null && order.getId() != null) {
            Intent intent = new Intent(getContext(), com.foodorderingapp.ui.home.shipper.ShipperDeliveryMapActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            intent.putExtra("SHOP_ID", order.getShopId());
            intent.putExtra("SHOP_NAME", order.getShopName());
            intent.putExtra("SHOP_ADDRESS", order.getShopAddress());
            intent.putExtra("SHOP_LATITUDE", order.getShopLatitude() != null ? order.getShopLatitude() : 0.0);
            intent.putExtra("SHOP_LONGITUDE", order.getShopLongitude() != null ? order.getShopLongitude() : 0.0);
            intent.putExtra("CUSTOMER_NAME", order.getCustomerName());
            intent.putExtra("BUILDING_NAME", order.getBuilding());
            intent.putExtra("BUILDING_LATITUDE", order.getBuildingLatitude() != null ? order.getBuildingLatitude() : 0.0);
            intent.putExtra("BUILDING_LONGITUDE", order.getBuildingLongitude() != null ? order.getBuildingLongitude() : 0.0);
            intent.putExtra("ORDER_STATUS", order.getStatus());
            startActivity(intent);
        }
    }

    @Override
    public void onContactVendor(OrderResponse order) {
        if (order == null || order.getShopId() == null) {
            ToastUtils.error(getContext(), "Không tìm thấy thông tin cửa hàng");
            return;
        }
        Intent intent = new Intent(requireContext(), com.foodorderingapp.ui.chat.ChatActivity.class);
        intent.putExtra(com.foodorderingapp.ui.chat.ChatActivity.EXTRA_SHOP_ID, order.getShopId());
        intent.putExtra(com.foodorderingapp.ui.chat.ChatActivity.EXTRA_SHOP_NAME, order.getShopName());
        intent.putExtra(com.foodorderingapp.ui.chat.ChatActivity.EXTRA_PEER_NAME,
                order.getShopName() == null || order.getShopName().trim().isEmpty() ? "Cửa hàng" : order.getShopName());
        startActivity(intent);
    }

    @Override
    public void onDelete(OrderResponse order) {
        if (order != null && order.getId() != null) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận xóa lịch sử? ⚠️")
                    .setMessage("Bạn có chắc muốn ẩn đơn hàng này khỏi lịch sử giao hàng của bạn?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        com.foodorderingapp.data.remote.api.ApiClient.getApiService().hideOrderForShipper(order.getId()).enqueue(new retrofit2.Callback<OrderResponse>() {
                            @Override
                            public void onResponse(@NonNull retrofit2.Call<OrderResponse> call, @NonNull retrofit2.Response<OrderResponse> response) {
                                if (response.isSuccessful()) {
                                    loadData();
                                    ToastUtils.success(getContext(), "Đã xóa đơn hàng khỏi lịch sử!");
                                } else {
                                    ToastUtils.error(getContext(), "Không thể xóa đơn hàng!");
                                }
                            }

                            @Override
                            public void onFailure(@NonNull retrofit2.Call<OrderResponse> call, @NonNull Throwable t) {
                                ToastUtils.error(getContext(), "Lỗi kết nối: " + t.getMessage());
                            }
                        });
                    })
                    .setNegativeButton("Quay lại", null)
                    .show();
        }
    }

    @Override
    public void onOrderClicked(OrderResponse order) {
        if (order != null) {
            showOrderDetailDialog(order);
        }
    }

    private void showOrderDetailDialog(OrderResponse order) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
                new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_shipper_order_detail, null);

        TextView tvOrderId = view.findViewById(R.id.tv_detail_order_id);
        TextView tvOrderStatus = view.findViewById(R.id.tv_detail_order_status);
        TextView tvCustomerName = view.findViewById(R.id.tv_detail_customer_name);
        TextView tvCustomerPhone = view.findViewById(R.id.tv_detail_customer_phone);
        View btnChatCustomer = view.findViewById(R.id.btn_chat_customer);
        TextView tvBuilding = view.findViewById(R.id.tv_detail_building);
        TextView tvShopName = view.findViewById(R.id.tv_detail_shop_name);
        TextView tvShopAddress = view.findViewById(R.id.tv_detail_shop_address);
        View btnChatShop = view.findViewById(R.id.btn_chat_shop);
        LinearLayout layoutItems = view.findViewById(R.id.layout_detail_order_items);
        TextView tvSubtotal = view.findViewById(R.id.tv_detail_subtotal);
        TextView tvShipping = view.findViewById(R.id.tv_detail_shipping);
        View layoutDiscount = view.findViewById(R.id.layout_detail_discount);
        TextView tvDiscountLabel = view.findViewById(R.id.tv_detail_discount_label);
        TextView tvDiscountValue = view.findViewById(R.id.tv_detail_discount_value);
        TextView tvTotal = view.findViewById(R.id.tv_detail_total);
        View btnClose = view.findViewById(R.id.btn_close_detail);

        // Bind basic info
        String shortId = order.getId();
        if (shortId != null && shortId.length() > 6) {
            shortId = shortId.substring(0, 6);
        }
        if (tvOrderId != null) tvOrderId.setText("Đơn hàng #" + shortId);

        String status = order.getStatus();
        if (tvOrderStatus != null) {
            tvOrderStatus.setText(formatStatusText(status));
            int badgeColor = android.graphics.Color.parseColor("#718096"); // Default Grey
            if ("PENDING".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status)) {
                badgeColor = android.graphics.Color.parseColor("#F46E26"); // Orange
            } else if ("DELIVERING".equalsIgnoreCase(status)) {
                badgeColor = android.graphics.Color.parseColor("#382C29"); // Dark Cacao Grey
            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                badgeColor = android.graphics.Color.parseColor("#38A169"); // Green
            }
            tvOrderStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(badgeColor));
        }

        if (tvCustomerName != null) tvCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "Không tên");
        if (tvCustomerPhone != null) {
            String phone = order.getCustomerPhone() != null ? order.getCustomerPhone() : "Chưa có SĐT";
            tvCustomerPhone.setText("📞 " + phone);
            if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
                tvCustomerPhone.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(android.net.Uri.parse("tel:" + order.getCustomerPhone()));
                    startActivity(intent);
                });
            }
        }

        if (btnChatCustomer != null) {
            btnChatCustomer.setOnClickListener(v -> {
                dialog.dismiss();
                if (order.getId() != null) {
                    Intent intent = new Intent(requireContext(), com.foodorderingapp.ui.chat.ChatActivity.class);
                    intent.putExtra(com.foodorderingapp.ui.chat.ChatActivity.EXTRA_ORDER_ID, order.getId());
                    intent.putExtra(com.foodorderingapp.ui.chat.ChatActivity.EXTRA_PEER_NAME,
                            order.getCustomerName() == null || order.getCustomerName().trim().isEmpty() ? "Sinh viên" : order.getCustomerName());
                    startActivity(intent);
                }
            });
        }

        if (tvBuilding != null) tvBuilding.setText("Giao đến: " + (order.getBuilding() != null ? order.getBuilding() : "Chưa rõ tòa nhà"));

        // Shop Info
        if (tvShopName != null) tvShopName.setText(order.getShopName() != null ? order.getShopName() : "Cửa hàng");
        if (tvShopAddress != null) tvShopAddress.setText(order.getShopAddress() != null ? order.getShopAddress() : "Địa chỉ lấy hàng");

        if (btnChatShop != null) {
            btnChatShop.setOnClickListener(v -> {
                dialog.dismiss();
                onContactVendor(order);
            });
        }

        // Populate items
        if (layoutItems != null) {
            layoutItems.removeAllViews();
            if (order.getDetails() != null) {
                for (com.foodorderingapp.model.response.OrderDetailResponse detail : order.getDetails()) {
                    RelativeLayout itemRow = new RelativeLayout(requireContext());
                    itemRow.setLayoutParams(new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    itemRow.setPadding(0, 6, 0, 6);

                    TextView tvName = new TextView(requireContext());
                    tvName.setText(detail.getQuantity() + "x " + detail.getFoodName());
                    tvName.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vendor_dark_text_primary));
                    tvName.setTextSize(13);
                    RelativeLayout.LayoutParams lpLeft = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpLeft.addRule(RelativeLayout.ALIGN_PARENT_START);
                    tvName.setLayoutParams(lpLeft);

                    TextView tvPrice = new TextView(requireContext());
                    tvPrice.setText(formatPrice(detail.getPrice() * detail.getQuantity()));
                    tvPrice.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vendor_dark_text_secondary));
                    tvPrice.setTextSize(13);
                    RelativeLayout.LayoutParams lpRight = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpRight.addRule(RelativeLayout.ALIGN_PARENT_END);
                    tvPrice.setLayoutParams(lpRight);

                    itemRow.addView(tvName);
                    itemRow.addView(tvPrice);
                    layoutItems.addView(itemRow);
                }
            }
        }

        // Pricing summary
        double itemSubtotal = 0.0;
        if (order.getDetails() != null) {
            for (com.foodorderingapp.model.response.OrderDetailResponse detail : order.getDetails()) {
                itemSubtotal += detail.getPrice() * detail.getQuantity();
            }
        }
        if (tvSubtotal != null) tvSubtotal.setText(formatPrice(itemSubtotal));

        double shippingFee = calculateShippingFee(order);
        if (tvShipping != null) tvShipping.setText(formatPrice(shippingFee));

        if (order.getDiscountAmount() > 0) {
            if (layoutDiscount != null) layoutDiscount.setVisibility(View.VISIBLE);
            if (tvDiscountLabel != null) {
                String codeInfo = order.getVoucherCode() != null ? " (" + order.getVoucherCode() + ")" : "";
                tvDiscountLabel.setText("Khuyến mãi" + codeInfo);
            }
            if (tvDiscountValue != null) tvDiscountValue.setText("-" + formatPrice(order.getDiscountAmount()));
        } else {
            if (layoutDiscount != null) layoutDiscount.setVisibility(View.GONE);
        }

        if (tvTotal != null) tvTotal.setText(formatPrice(getShipperTotalPrice(order)));

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.setContentView(view);
        dialog.show();
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

    private String formatStatusText(String status) {
        if (status == null) return "Chờ xử lý";
        switch (status.toUpperCase(java.util.Locale.ROOT)) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "DELIVERING": return "Đang giao";
            case "COMPLETED": return "Đã hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }

    private String formatPrice(double price) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    private double getShipperTotalPrice(OrderResponse order) {
        double subtotal = 0.0;
        if (order.getDetails() != null) {
            for (com.foodorderingapp.model.response.OrderDetailResponse detail : order.getDetails()) {
                subtotal += detail.getPrice() * detail.getQuantity();
            }
        }
        double foodTotal = subtotal - order.getDiscountAmount();
        double shippingFee = calculateShippingFee(order);
        if (order.getTotalPrice() >= (foodTotal + shippingFee - 100)) {
            return order.getTotalPrice();
        }
        return order.getTotalPrice() + shippingFee;
    }
}
