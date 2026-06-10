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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.ui.adapter.OrderAdapter;
import com.foodorderingapp.viewmodel.OrderViewModel;

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
        orderAdapter.setOnReviewClickListener(this::showReviewDialog);
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
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        orderViewModel.loadOrderHistory();
    }

    private void showReviewDialog(OrderResponse order) {
        if (getContext() == null) {
            return;
        }

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 10, padding, 0);

        RatingBar ratingBar = new RatingBar(getContext());
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1f);
        ratingBar.setRating(5f);
        container.addView(ratingBar);

        EditText commentInput = new EditText(getContext());
        commentInput.setHint("Nhận xét của bạn");
        commentInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        commentInput.setMinLines(3);
        container.addView(commentInput);

        new AlertDialog.Builder(getContext())
                .setTitle("Đánh giá " + order.getShopName())
                .setView(container)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    int rating = Math.round(ratingBar.getRating());
                    if (rating <= 0) {
                        Toast.makeText(getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    orderViewModel.createReview(order.getId(), rating, commentInput.getText().toString().trim());
                })
                .show();
    }
}
