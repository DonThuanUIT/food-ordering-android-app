package com.foodorderingapp.ui.home.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.ui.adapter.ActiveOrderAdapter;
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

        activeOrderAdapter = new ActiveOrderAdapter();
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

        orderViewModel.loadActiveOrders();
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
}
