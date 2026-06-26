package com.foodorderingapp.ui.home.shipper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
            intent.putExtra("SHOP_NAME", order.getShopName());
            intent.putExtra("SHOP_ADDRESS", order.getShopAddress());
            intent.putExtra("SHOP_LATITUDE", order.getShopLatitude() != null ? order.getShopLatitude() : 0.0);
            intent.putExtra("SHOP_LONGITUDE", order.getShopLongitude() != null ? order.getShopLongitude() : 0.0);
            intent.putExtra("CUSTOMER_NAME", order.getCustomerName());
            intent.putExtra("BUILDING_NAME", order.getBuilding());
            intent.putExtra("BUILDING_LATITUDE", order.getBuildingLatitude() != null ? order.getBuildingLatitude() : 0.0);
            intent.putExtra("BUILDING_LONGITUDE", order.getBuildingLongitude() != null ? order.getBuildingLongitude() : 0.0);
            intent.putExtra("DROP_OFF", order.getDropOff());
            intent.putExtra("ORDER_STATUS", order.getStatus());
            startActivity(intent);
        }
    }
}
