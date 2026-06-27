package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.ui.adapter.ActiveOrderAdapter;
import com.foodorderingapp.ui.chat.ChatActivity;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.OrderViewModel;

public class StudentOrdersFragment extends Fragment {

    private ActiveOrderAdapter activeOrderAdapter;
    private RecyclerView rvActiveOrders;
    private TextView tvActiveOrdersEmpty;
    private OrderViewModel orderViewModel;

    public StudentOrdersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvActiveOrders = view.findViewById(R.id.rvActiveOrders);
        tvActiveOrdersEmpty = view.findViewById(R.id.tvActiveOrdersEmpty);

        activeOrderAdapter = new ActiveOrderAdapter(
                this::openOrderTracking,
                this::openShopChat,
                this::showCancelOrderDialog
        );
        rvActiveOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvActiveOrders.setAdapter(activeOrderAdapter);
        rvActiveOrders.setNestedScrollingEnabled(false);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderViewModel.getActiveOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders == null) {
                activeOrderAdapter.submitList(null);
                showActiveOrdersEmpty("Không tải được đơn đang xử lý");
                return;
            }

            activeOrderAdapter.submitList(orders);
            if (orders.isEmpty()) {
                showActiveOrdersEmpty("Chưa có đơn đang xử lý");
            } else {
                showActiveOrdersList();
            }
        });
        orderViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                ToastUtils.info(getContext(), message);
            }
        });
        orderViewModel.getCancelOrderResult().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                orderViewModel.loadActiveOrders();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (orderViewModel != null) {
            orderViewModel.loadActiveOrders();
        }
    }

    private void showActiveOrdersList() {
        rvActiveOrders.setVisibility(View.VISIBLE);
        tvActiveOrdersEmpty.setVisibility(View.GONE);
    }

    private void showActiveOrdersEmpty(String message) {
        rvActiveOrders.setVisibility(View.GONE);
        tvActiveOrdersEmpty.setVisibility(View.VISIBLE);
        tvActiveOrdersEmpty.setText(message);
    }

    private void openOrderTracking(OrderResponse order) {
        if (order == null || order.getId() == null) {
            return;
        }

        Intent intent = new Intent(getContext(), com.foodorderingapp.ui.order.OrderTrackingActivity.class);
        intent.putExtra("ORDER_ID", order.getId());
        intent.putExtra("SHOP_NAME", order.getShopName());
        intent.putExtra("SHOP_ADDRESS", order.getShopAddress());
        intent.putExtra("SHOP_LATITUDE", order.getShopLatitude() != null ? order.getShopLatitude() : 0.0);
        intent.putExtra("SHOP_LONGITUDE", order.getShopLongitude() != null ? order.getShopLongitude() : 0.0);
        intent.putExtra("BUILDING_NAME", order.getBuilding());
        intent.putExtra("BUILDING_LATITUDE", order.getBuildingLatitude() != null ? order.getBuildingLatitude() : 0.0);
        intent.putExtra("BUILDING_LONGITUDE", order.getBuildingLongitude() != null ? order.getBuildingLongitude() : 0.0);
        intent.putExtra("ORDER_STATUS", order.getStatus());
        intent.putExtra("SHIPPER_NAME", order.getShipperName());
        intent.putExtra("SHIPPER_PHONE", order.getShipperPhone());
        startActivity(intent);
    }

    private void openShopChat(OrderResponse order) {
        if (order == null || isBlank(order.getShopId())) {
            ToastUtils.error(getContext(), "Không tìm thấy quán để nhắn tin");
            return;
        }

        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_SHOP_ID, order.getShopId());
        intent.putExtra(ChatActivity.EXTRA_SHOP_NAME, order.getShopName());
        startActivity(intent);
    }

    private void showCancelOrderDialog(OrderResponse order) {
        if (order == null || isBlank(order.getId())) {
            return;
        }

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            ToastUtils.info(getContext(), "Chỉ có thể hủy đơn đang chờ xác nhận");
            return;
        }

        EditText reasonInput = new EditText(requireContext());
        reasonInput.setHint("Lý do hủy đơn");
        reasonInput.setMinLines(2);
        reasonInput.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        reasonInput.setPadding(padding, padding / 2, padding, padding / 2);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Hủy đơn hàng?")
                .setMessage("Đơn đang chờ xác nhận sẽ được chuyển sang đã hủy.")
                .setView(reasonInput)
                .setNegativeButton("Giữ đơn", null)
                .setPositiveButton("Hủy đơn", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String reason = reasonInput.getText() == null
                            ? ""
                            : reasonInput.getText().toString().trim();
                    if (reason.isEmpty()) {
                        reasonInput.setError("Vui lòng nhập lý do hủy");
                        reasonInput.requestFocus();
                        return;
                    }

                    orderViewModel.cancelPendingOrder(order.getId(), reason);
                    dialog.dismiss();
                }));

        dialog.show();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
