package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.ui.adapter.OrderAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.OrderViewModel;

import android.content.Intent;
import java.util.ArrayList;
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.foodorderingapp.ui.order.RateOrderActivity;

public class StudentHistoryFragment extends Fragment {

    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private TextView tvOrderHistoryEmpty;

    public StudentHistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvOrderHistory = view.findViewById(R.id.rvOrderHistory);
        tvOrderHistoryEmpty = view.findViewById(R.id.tvOrderHistoryEmpty);

        orderAdapter = new OrderAdapter();
        orderAdapter.setShowReviewAction(true);
        orderAdapter.setOnReviewClickListener(this::startRateOrderActivity);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderHistory.setAdapter(orderAdapter);
        rvOrderHistory.setNestedScrollingEnabled(false);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderViewModel.getOrderHistory().observe(getViewLifecycleOwner(), orders -> {
            boolean empty = orders == null || orders.isEmpty();
            tvOrderHistoryEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvOrderHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
            orderAdapter.submitList(orders);
        });
        orderViewModel.getReviewResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                orderViewModel.loadOrderHistory();
            }
        });
        orderViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                ToastUtils.info(getContext(), message);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (orderViewModel != null) {
            orderViewModel.loadOrderHistory();
        }
    }

    private void startRateOrderActivity(OrderResponse order) {
        if (getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), RateOrderActivity.class);
        intent.putExtra("ORDER_ID", order.getId());

        ArrayList<String> foodIds = new ArrayList<>();
        ArrayList<String> foodNames = new ArrayList<>();
        ArrayList<String> imageUrls = new ArrayList<>();

        if (order.getDetails() != null) {
            for (OrderDetailResponse detail : order.getDetails()) {
                if (detail.getFoodId() != null) {
                    foodIds.add(detail.getFoodId());
                    foodNames.add(detail.getFoodName() != null ? detail.getFoodName() : "");
                    imageUrls.add(detail.getImageUrl() != null ? detail.getImageUrl() : "");
                }
            }
        }

        intent.putStringArrayListExtra("FOOD_IDS", foodIds);
        intent.putStringArrayListExtra("FOOD_NAMES", foodNames);
        intent.putStringArrayListExtra("FOOD_IMAGES", imageUrls);

        startActivity(intent);
    }
}
