package com.foodorderingapp.ui.home.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.AdminOverviewResponse;
import com.foodorderingapp.ui.widget.AdminOrderChartView;
import com.foodorderingapp.viewmodel.AdminViewModel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminOverviewFragment extends Fragment {

    private final Locale vietnameseLocale = new Locale("vi", "VN");
    private AdminViewModel viewModel;
    private TextView tvUsersValue;
    private TextView tvShopsValue;
    private TextView tvPendingValue;
    private TextView tvRevenueValue;
    private AdminOrderChartView dailyChart;

    public AdminOverviewFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        tvUsersValue = view.findViewById(R.id.tvAdminUsersValue);
        tvShopsValue = view.findViewById(R.id.tvAdminShopsValue);
        tvPendingValue = view.findViewById(R.id.tvAdminPendingValue);
        tvRevenueValue = view.findViewById(R.id.tvAdminRevenueValue);
        dailyChart = view.findViewById(R.id.adminDailyChart);

        viewModel.getOverview().observe(getViewLifecycleOwner(), this::bindOverview);
        viewModel.loadOverview();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadOverview();
        }
    }

    private void bindOverview(AdminOverviewResponse overview) {
        if (overview == null) {
            tvUsersValue.setText("--");
            tvShopsValue.setText("--");
            tvPendingValue.setText("--");
            tvRevenueValue.setText("--");
            dailyChart.setData(null);
            return;
        }

        tvUsersValue.setText(formatNumber(overview.getTotalUsers()));
        tvShopsValue.setText(formatNumber(overview.getTotalShops()));
        tvPendingValue.setText(formatNumber(overview.getPendingShops()));
        tvRevenueValue.setText(formatCurrency(overview.getTotalSystemRevenue()));
        dailyChart.setData(overview.getDailyOrders());
    }

    private String formatNumber(long value) {
        return NumberFormat.getNumberInstance(vietnameseLocale).format(value);
    }

    private String formatCurrency(BigDecimal value) {
        return formatNumber(value == null ? 0L : value.longValue()) + " VND";
    }

}
