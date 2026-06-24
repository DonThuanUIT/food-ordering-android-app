package com.foodorderingapp.ui.home.vendor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.databinding.FragmentVendorStatsBinding;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.model.response.TopProductData;
import com.foodorderingapp.model.response.TrendData;
import com.foodorderingapp.model.response.VendorDashboardResponse;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import com.google.android.material.datepicker.MaterialDatePicker;
import androidx.core.util.Pair;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorStatsFragment extends Fragment {

    private FragmentVendorStatsBinding binding;
    private UUID currentShopId;
    private TopProductAdapter topProductAdapter;
    private LocalDateTime customStartDate = null;
    private LocalDateTime customEndDate = null;
    private int lastSelectedFilterPosition = 0;

    public VendorStatsFragment() {
        // Required empty public constructor
    }

    public static VendorStatsFragment newInstance() {
        return new VendorStatsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVendorStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilterSpinner();
        setupSwipeRefresh();

        // Load data
        fetchShopInfoAndLoadStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        binding.rvTopProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        topProductAdapter = new TopProductAdapter();
        binding.rvTopProducts.setAdapter(topProductAdapter);
    }

    private void setupFilterSpinner() {
        String[] filters = {"30 ngày gần đây", "7 ngày gần đây", "Hôm nay", "Chọn khoảng ngày..."};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_vendor_spinner, filters);
        adapter.setDropDownViewResource(R.layout.item_vendor_spinner_dropdown);
        binding.spinnerFilter.setAdapter(adapter);

        binding.spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3) {
                    showCustomDateRangePicker();
                } else {
                    lastSelectedFilterPosition = position;
                    if (currentShopId != null) {
                        loadDashboardStats(false);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showCustomDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Chọn khoảng ngày");
        
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.show(getChildFragmentManager(), "date_range_picker");
        
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                java.time.Instant startInstant = java.time.Instant.ofEpochMilli(selection.first);
                java.time.Instant endInstant = java.time.Instant.ofEpochMilli(selection.second);
                
                customStartDate = LocalDateTime.ofInstant(startInstant, java.time.ZoneId.systemDefault())
                        .truncatedTo(ChronoUnit.DAYS);
                customEndDate = LocalDateTime.ofInstant(endInstant, java.time.ZoneId.systemDefault())
                        .withHour(23).withMinute(59).withSecond(59);
                
                lastSelectedFilterPosition = 3;
                if (currentShopId != null) {
                    loadDashboardStats(false);
                }
            } else {
                binding.spinnerFilter.setSelection(lastSelectedFilterPosition);
            }
        });
        
        picker.addOnCancelListener(dialog -> {
            binding.spinnerFilter.setSelection(lastSelectedFilterPosition);
        });
        
        picker.addOnDismissListener(dialog -> {
            if (binding.spinnerFilter.getSelectedItemPosition() == 3 && (customStartDate == null || customEndDate == null)) {
                binding.spinnerFilter.setSelection(lastSelectedFilterPosition);
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (currentShopId == null) {
                fetchShopInfoAndLoadStats();
            } else {
                loadDashboardStats(true);
            }
        });
    }

    private void fetchShopInfoAndLoadStats() {
        binding.swipeRefresh.setRefreshing(true);
        ApiClient.getApiService().getVendorShops().enqueue(new Callback<List<ShopResponse>>() {
            @Override
            public void onResponse(Call<List<ShopResponse>> call, Response<List<ShopResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    ShopResponse shop = response.body().get(0);
                    binding.tvShopName.setText(shop.getName());
                    String idStr = shop.getId();
                    if (idStr != null) {
                        currentShopId = UUID.fromString(idStr);
                        loadDashboardStats(false);
                    } else {
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), "Không tìm thấy ID quán ăn", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), "Không thể tải thông tin cửa hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShopResponse>> call, Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDashboardStats(boolean isRefresh) {
        if (currentShopId == null) {
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        if (!isRefresh) {
            binding.swipeRefresh.setRefreshing(true);
        }

        // Calculate date range based on filter
        int selectedIndex = binding.spinnerFilter.getSelectedItemPosition();
        LocalDateTime start;
        LocalDateTime end;

        if (selectedIndex == 3) { // Custom date range
            if (customStartDate != null && customEndDate != null) {
                start = customStartDate;
                end = customEndDate;
            } else {
                start = LocalDateTime.now().minusDays(30);
                end = LocalDateTime.now();
            }
        } else if (selectedIndex == 1) { // 7 days
            start = LocalDateTime.now().minusDays(7);
            end = LocalDateTime.now();
            customStartDate = null;
            customEndDate = null;
        } else if (selectedIndex == 2) { // Today
            start = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
            end = LocalDateTime.now();
            customStartDate = null;
            customEndDate = null;
        } else { // 30 days (default)
            start = LocalDateTime.now().minusDays(30);
            end = LocalDateTime.now();
            customStartDate = null;
            customEndDate = null;
        }

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateRangeText = start.format(displayFormatter) + " - " + end.format(displayFormatter);
        binding.tvDateRange.setText(dateRangeText);
        binding.tvDateRange.setVisibility(View.VISIBLE);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String startDateStr = start.format(formatter);
        String endDateStr = end.format(formatter);

        ApiClient.getApiService().getDashboardStats(currentShopId, startDateStr, endDateStr)
                .enqueue(new Callback<VendorDashboardResponse>() {
                    @Override
                    public void onResponse(Call<VendorDashboardResponse> call, Response<VendorDashboardResponse> response) {
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            displayDashboardData(response.body());
                        } else {
                            Toast.makeText(getContext(), "Không thể tải số liệu thống kê", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<VendorDashboardResponse> call, Throwable t) {
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayDashboardData(VendorDashboardResponse data) {
        BigDecimal revenue = data.getTotalRevenue() != null ? data.getTotalRevenue() : BigDecimal.ZERO;
        binding.tvTotalRevenue.setText(formatCurrency(revenue));
        displayGrowthMetric(data.getRevenueGrowth(), binding.tvRevenueGrowth);

        Long totalOrders = data.getTotalOrders() != null ? data.getTotalOrders() : 0L;
        binding.tvTotalOrders.setText(String.valueOf(totalOrders));
        displayGrowthMetric(data.getOrderCountGrowth(), binding.tvOrdersGrowth);

        Double rate = data.getCompletionRate() != null ? data.getCompletionRate() : 0.0;
        binding.tvCompletionRate.setText(String.format(Locale.US, "%.1f%%", rate));
        displayGrowthMetric(data.getCompletionRateGrowth(), binding.tvCompletionRateGrowth);

        BigDecimal avgValue = data.getAverageOrderValue() != null ? data.getAverageOrderValue() : BigDecimal.ZERO;
        binding.tvAverageOrderValue.setText(formatCurrency(avgValue));
        displayGrowthMetric(data.getAverageOrderValueGrowth(), binding.tvAovGrowth);

        // Draw Line Charts
        setupRevenueChart(data.getOrderTrends());
        setupOrderChart(data.getOrderTrends());

        // Draw Pie Chart
        setupPieChart(data.getOrderStatusBreakdown());

        // Populate RecyclerView
        topProductAdapter.submitList(data.getTopSellingProducts());
    }

    private void displayGrowthMetric(Double growth, android.widget.TextView tvGrowth) {
        if (growth == null) {
            tvGrowth.setVisibility(View.GONE);
            return;
        }
        tvGrowth.setVisibility(View.VISIBLE);
        if (growth > 0) {
            tvGrowth.setText(String.format(Locale.US, "▲ +%.1f%% so với kỳ trước", growth));
            tvGrowth.setTextColor(Color.parseColor("#48BB78")); // Green
        } else if (growth < 0) {
            tvGrowth.setText(String.format(Locale.US, "▼ %.1f%% so với kỳ trước", growth));
            tvGrowth.setTextColor(Color.parseColor("#E53E3E")); // Red
        } else {
            tvGrowth.setText("0.0% so với kỳ trước");
            tvGrowth.setTextColor(Color.parseColor("#718096")); // Gray
        }
    }

    private String formatCurrency(BigDecimal value) {
        return String.format(Locale.US, "%,dđ", value.longValue());
    }

    private void setupRevenueChart(List<TrendData> trends) {
        if (trends == null || trends.isEmpty()) {
            binding.chartRevenueTrend.clear();
            return;
        }

        List<Entry> revenueEntries = new ArrayList<>();
        final List<String> dates = new ArrayList<>();

        for (int i = 0; i < trends.size(); i++) {
            TrendData trend = trends.get(i);
            revenueEntries.add(new Entry(i, trend.getRevenue().floatValue()));
            
            String dateStr = trend.getDate();
            if (dateStr != null && dateStr.length() >= 10) {
                dates.add(dateStr.substring(5)); // take MM-dd
            } else {
                dates.add(dateStr);
            }
        }

        LineDataSet set = new LineDataSet(revenueEntries, "Doanh thu (đ)");
        set.setColor(Color.parseColor("#F46E26")); // vendor_dark_orange
        set.setCircleColor(Color.parseColor("#F46E26"));
        set.setLineWidth(2.5f);
        set.setCircleRadius(4f);
        set.setDrawCircleHole(false);
        set.setDrawValues(false); // Clean design: hide values on line points
        set.setDrawFilled(true);
        set.setFillColor(Color.parseColor("#22F46E26")); // light orange fill (alpha 13%)
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(set);
        binding.chartRevenueTrend.setData(lineData);

        // Styling Line Chart
        binding.chartRevenueTrend.getDescription().setEnabled(false);
        
        XAxis xAxis = binding.chartRevenueTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false); // Clean: hide vertical grid lines
        xAxis.setTextColor(Color.parseColor("#8A7D79"));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < dates.size()) {
                    return dates.get(idx);
                }
                return "";
            }
        });
        
        binding.chartRevenueTrend.getAxisRight().setEnabled(false);
        
        binding.chartRevenueTrend.getAxisLeft().setAxisMinimum(0f);
        
        float maxRevenue = 0f;
        for (Entry entry : revenueEntries) {
            maxRevenue = Math.max(maxRevenue, entry.getY());
        }
        if (maxRevenue == 0f) {
            binding.chartRevenueTrend.getAxisLeft().setAxisMaximum(10000f);
        } else {
            binding.chartRevenueTrend.getAxisLeft().resetAxisMaximum();
        }

        binding.chartRevenueTrend.getAxisLeft().setDrawGridLines(true);
        binding.chartRevenueTrend.getAxisLeft().setGridColor(Color.parseColor("#382C29")); // vendor_dark_divider
        binding.chartRevenueTrend.getAxisLeft().setTextColor(Color.parseColor("#8A7D79"));
        binding.chartRevenueTrend.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) {
                    return String.format(Locale.US, "%.1fM", value / 1000000.0);
                } else if (value >= 1000) {
                    return String.format(Locale.US, "%.0fK", value / 1000.0);
                }
                return String.format(Locale.US, "%.0f", value);
            }
        });

        binding.chartRevenueTrend.getLegend().setTextColor(Color.parseColor("#FFFFFF"));
        binding.chartRevenueTrend.animateY(800);
        binding.chartRevenueTrend.invalidate();
    }

    private void setupOrderChart(List<TrendData> trends) {
        if (trends == null || trends.isEmpty()) {
            binding.chartOrderTrend.clear();
            return;
        }

        List<Entry> orderEntries = new ArrayList<>();
        final List<String> dates = new ArrayList<>();

        for (int i = 0; i < trends.size(); i++) {
            TrendData trend = trends.get(i);
            orderEntries.add(new Entry(i, trend.getOrderCount().floatValue()));
            
            String dateStr = trend.getDate();
            if (dateStr != null && dateStr.length() >= 10) {
                dates.add(dateStr.substring(5)); // take MM-dd
            } else {
                dates.add(dateStr);
            }
        }

        LineDataSet set = new LineDataSet(orderEntries, "Số đơn hàng");
        set.setColor(Color.parseColor("#5299FF")); // Lighter Blue
        set.setCircleColor(Color.parseColor("#5299FF"));
        set.setLineWidth(2.5f);
        set.setCircleRadius(4f);
        set.setDrawCircleHole(false);
        set.setDrawValues(false); // Clean design: hide values on line points
        set.setDrawFilled(true);
        set.setFillColor(Color.parseColor("#225299FF")); // light blue fill (alpha 13%)
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(set);
        binding.chartOrderTrend.setData(lineData);

        // Styling Line Chart
        binding.chartOrderTrend.getDescription().setEnabled(false);
        
        XAxis xAxis = binding.chartOrderTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false); // Clean: hide vertical grid lines
        xAxis.setTextColor(Color.parseColor("#8A7D79"));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < dates.size()) {
                    return dates.get(idx);
                }
                return "";
            }
        });
        
        binding.chartOrderTrend.getAxisRight().setEnabled(false);
        
        binding.chartOrderTrend.getAxisLeft().setAxisMinimum(0f);
        binding.chartOrderTrend.getAxisLeft().setGranularity(1f);

        float maxOrders = 0f;
        for (Entry entry : orderEntries) {
            maxOrders = Math.max(maxOrders, entry.getY());
        }
        if (maxOrders == 0f) {
            binding.chartOrderTrend.getAxisLeft().setAxisMaximum(10f);
        } else {
            binding.chartOrderTrend.getAxisLeft().resetAxisMaximum();
        }

        binding.chartOrderTrend.getAxisLeft().setDrawGridLines(true);
        binding.chartOrderTrend.getAxisLeft().setGridColor(Color.parseColor("#382C29")); // vendor_dark_divider
        binding.chartOrderTrend.getAxisLeft().setTextColor(Color.parseColor("#8A7D79"));
        binding.chartOrderTrend.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.0f đơn", value);
            }
        });

        binding.chartOrderTrend.getLegend().setTextColor(Color.parseColor("#FFFFFF"));
        binding.chartOrderTrend.animateY(800);
        binding.chartOrderTrend.invalidate();
    }

    private void setupPieChart(Map<String, Long> statusMap) {
        if (statusMap == null || statusMap.isEmpty()) {
            binding.chartStatusBreakdown.clear();
            return;
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Map status names to display names and set status specific colors
        for (Map.Entry<String, Long> entry : statusMap.entrySet()) {
            if (entry.getValue() > 0) {
                String label;
                int color;
                switch (entry.getKey().toUpperCase()) {
                    case "COMPLETED":
                        label = "Hoàn thành";
                        color = Color.parseColor("#34C759"); // status_green
                        break;
                    case "CANCELLED":
                        label = "Đã hủy";
                        color = Color.parseColor("#FF3B30"); // status_red
                        break;
                    case "DELIVERING":
                        label = "Đang giao";
                        color = Color.parseColor("#FF9500"); // status_orange
                        break;
                    case "PENDING":
                        label = "Chờ duyệt";
                        color = Color.parseColor("#8E8E93"); // status_gray
                        break;
                    case "PREPARING":
                        label = "Chuẩn bị";
                        color = Color.parseColor("#FF7A21"); // brand_orange
                        break;
                    default:
                        label = entry.getKey();
                        color = Color.LTGRAY;
                        break;
                }
                pieEntries.add(new PieEntry(entry.getValue().floatValue(), label));
                colors.add(color);
            }
        }

        if (pieEntries.isEmpty()) {
            binding.chartStatusBreakdown.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(binding.chartStatusBreakdown));

        PieData pieData = new PieData(dataSet);
        binding.chartStatusBreakdown.setData(pieData);
        binding.chartStatusBreakdown.setUsePercentValues(true);
        binding.chartStatusBreakdown.getDescription().setEnabled(false);
        binding.chartStatusBreakdown.setDrawHoleEnabled(true);
        binding.chartStatusBreakdown.setHoleColor(Color.parseColor("#281F1C")); // vendor_dark_card
        binding.chartStatusBreakdown.setHoleRadius(40f);
        binding.chartStatusBreakdown.setTransparentCircleRadius(45f);
        binding.chartStatusBreakdown.setCenterText("Đơn hàng");
        binding.chartStatusBreakdown.setCenterTextSize(14f);
        binding.chartStatusBreakdown.setCenterTextColor(Color.WHITE);
        binding.chartStatusBreakdown.setDrawEntryLabels(false);

        Legend l = binding.chartStatusBreakdown.getLegend();
        l.setTextColor(Color.WHITE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);

        binding.chartStatusBreakdown.animateY(800);
        binding.chartStatusBreakdown.invalidate();
    }

    // RecyclerView Adapter for Top Selling Products
    private static class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.ViewHolder> {

        private final List<TopProductData> list = new ArrayList<>();

        public void submitList(List<TopProductData> newList) {
            list.clear();
            if (newList != null) {
                list.addAll(newList);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TopProductData item = list.get(position);
            holder.tvFoodName.setText(item.getFoodName());
            holder.tvQuantitySold.setText(String.valueOf(item.getQuantitySold()));
            holder.tvRevenue.setText(String.format(Locale.US, "%,dđ", item.getRevenue()));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView tvFoodName;
            android.widget.TextView tvQuantitySold;
            android.widget.TextView tvRevenue;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvFoodName = itemView.findViewById(R.id.tv_food_name);
                tvQuantitySold = itemView.findViewById(R.id.tv_quantity_sold);
                tvRevenue = itemView.findViewById(R.id.tv_revenue);
            }
        }
    }
}