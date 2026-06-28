package com.foodorderingapp.ui.home.student;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.UpdateProfileRequest;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.SpendingSummaryResponse;
import com.foodorderingapp.model.response.StudentReviewResponse;
import com.foodorderingapp.model.response.UserProfileResponse;
import com.foodorderingapp.ui.adapter.StudentReviewAdapter;
import com.foodorderingapp.ui.auth.LoginActivity;
import com.foodorderingapp.utils.ImageUrlUtils;
import com.foodorderingapp.utils.TokenManager;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.StudentProfileViewModel;
import com.foodorderingapp.viewmodel.UploadImageViewModel;
import com.foodorderingapp.viewmodel.ViewModelFactory;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentProfileFragment extends Fragment {
    private final List<BuildingResponse> buildingOptions = new ArrayList<>();
    private final DateTimeFormatter apiDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private StudentProfileViewModel viewModel;
    private UploadImageViewModel uploadImageViewModel;
    private ActivityResultLauncher<Intent> avatarPickerLauncher;
    private UserProfileResponse currentProfile;
    private BottomSheetDialog editProfileDialog;
    private BottomSheetDialog reviewsDialog;
    private StudentReviewAdapter reviewAdapter;
    private TextView tvReviewsEmpty;
    private ImageView editAvatarPreview;
    private MaterialButton btnChooseAvatar;
    private MaterialButton btnSaveProfile;
    private String selectedAvatarUrl;
    private LocalDate spendingFrom;
    private LocalDate spendingTo;

    public StudentProfileFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        uploadImageViewModel = new ViewModelProvider(this, factory).get(UploadImageViewModel.class);
        avatarPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            previewSelectedAvatar(selectedImageUri);
                            uploadImageViewModel.clearUploadResult();
                            uploadImageViewModel.uploadImage(selectedImageUri);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StudentProfileViewModel.class);
        bindSessionInfo(view);
        setupSpendingRange(view);
        setupActions(view);
        observeMyReviews();
        observeAvatarUpload();
        loadRemoteProfile(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSpendingForSelectedRange();
    }

    private void bindSessionInfo(View view) {
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
        TextView tvUserTag = view.findViewById(R.id.tvUserTag);
        TextView tvMonthlySpending = view.findViewById(R.id.tvMonthlySpending);

        TokenManager tokenManager = TokenManager.getInstance();
        String role = tokenManager.getRole();
        String phone = tokenManager.getPhone();
        String fullName = tokenManager.getFullName();

        tvUserName.setText(isBlank(fullName) ? defaultDisplayName(role) : fullName);
        tvUserEmail.setText(isBlank(phone) ? "Chưa có số điện thoại" : phone);
        tvUserTag.setText("Vai trò: " + formatRole(role));
        tvMonthlySpending.setText("0đ");
    }

    private void loadRemoteProfile(View view) {
        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                currentProfile = profile;
                bindProfile(view, profile);
                TokenManager.getInstance().saveUserSession(
                        profile.getPhone(),
                        profile.getRole(),
                        profile.getFullName()
                );
            }
        });
        viewModel.getSpendingSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
        bindSpending(view, summary);
    }
        });
        viewModel.getBuildings().observe(getViewLifecycleOwner(), buildings -> {
            buildingOptions.clear();
            if (buildings != null) {
                buildingOptions.addAll(buildings);
            }
        });
        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                return;
            }
            if (result) {
                ToastUtils.success(getContext(), "Đã cập nhật hồ sơ");
                if (editProfileDialog != null) {
                    editProfileDialog.dismiss();
                    editProfileDialog = null;
                }
            } else {
                ToastUtils.error(getContext(), "Không thể cập nhật hồ sơ");
            }
            viewModel.clearUpdateResult();
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                ToastUtils.info(getContext(), message);
            }
        });

        viewModel.loadProfile();
        loadSpendingForSelectedRange();
        viewModel.loadBuildings();
    }

    private void bindProfile(View view, UserProfileResponse profile) {
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
        TextView tvUserTag = view.findViewById(R.id.tvUserTag);
        ImageView ivAvatar = view.findViewById(R.id.ivAvatar);

        tvUserName.setText(isBlank(profile.getFullName()) ? "Sinh viên UniEats" : profile.getFullName());

        String emailOrPhone = !isBlank(profile.getEmail()) ? profile.getEmail() : profile.getPhone();
        tvUserEmail.setText(isBlank(emailOrPhone) ? "Chưa có thông tin liên hệ" : emailOrPhone);

        String building = isBlank(profile.getBuildingName()) ? "Chưa chọn tòa nhà" : profile.getBuildingName();
        tvUserTag.setText(formatRole(profile.getRole()) + " - " + building);

        String avatarUrl = ImageUrlUtils.resolveImageUrl(profile.getAvatarUrl());
        if (!isBlank(avatarUrl) && getContext() != null) {
            ivAvatar.setPadding(0, 0, 0, 0);
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(ivAvatar);
        } else {
            ivAvatar.setPadding(dp(18), dp(18), dp(18), dp(18));
            ivAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    private void bindSpending(View view, SpendingSummaryResponse summary) {
        TextView tvMonthlySpending = view.findViewById(R.id.tvMonthlySpending);
        tvMonthlySpending.setText(formatPrice(summary.getTotalSpent()));
        List<SpendingSummaryResponse.SpendingBreakdown> displayBreakdown = normalizeSpendingBreakdown(summary);
        bindSpendingSummaryStats(view, summary, displayBreakdown);
        bindSpendingChart(view, displayBreakdown);
        bindSpendingBarChart(view, displayBreakdown);
        bindSpendingPieChart(view, displayBreakdown);
        bindSpendingBreakdown(view, buildWeeklySpendingBreakdown(displayBreakdown));
    }

    private void setupActions(View view) {
        view.findViewById(R.id.btnSpendingThisWeek).setOnClickListener(v -> {
            setCurrentWeekRange();
            updateSpendingRangeControls(view);
            loadSpendingForSelectedRange();
        });
        view.findViewById(R.id.btnSpendingThisMonth).setOnClickListener(v -> {
            setCurrentMonthRange();
            updateSpendingRangeControls(view);
            loadSpendingForSelectedRange();
        });
        view.findViewById(R.id.btnSpendingFrom).setOnClickListener(v -> pickSpendingDate(true, view));
        view.findViewById(R.id.btnSpendingTo).setOnClickListener(v -> pickSpendingDate(false, view));
        view.findViewById(R.id.btnSpendingRefresh).setOnClickListener(v -> {
            updateSpendingRangeControls(view);
            loadSpendingForSelectedRange();
        });
        view.findViewById(R.id.rowEditProfile).setOnClickListener(v -> showEditProfileSheet());
        view.findViewById(R.id.rowMyReviews).setOnClickListener(v -> showMyReviewsSheet());
        view.findViewById(R.id.rowSupport).setOnClickListener(v -> showSupportInfo());
        view.findViewById(R.id.btnEditAvatar).setOnClickListener(v -> showEditProfileSheet());
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
    }

    private void observeAvatarUpload() {
        uploadImageViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            if (btnChooseAvatar != null) {
                btnChooseAvatar.setEnabled(!loading);
                btnChooseAvatar.setText(loading ? "Đang upload ảnh..." : "Chọn ảnh từ thư viện");
            }
            if (btnSaveProfile != null) {
                btnSaveProfile.setEnabled(!loading);
            }
        });

        uploadImageViewModel.getUploadSuccessUrl().observe(getViewLifecycleOwner(), url -> {
            if (isBlank(url)) {
                return;
            }
            selectedAvatarUrl = url;
            String previewUrl = ImageUrlUtils.resolveImageUrl(url);
            if (editAvatarPreview != null && getContext() != null) {
                Glide.with(this)
                        .load(previewUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(editAvatarPreview);
            }
            ToastUtils.success(getContext(), "Đã upload ảnh đại diện");
        });

        uploadImageViewModel.getUploadError().observe(getViewLifecycleOwner(), error -> {
            if (!isBlank(error)) {
                ToastUtils.error(getContext(), "Không upload được ảnh đại diện");
            }
        });
    }

    private void previewSelectedAvatar(Uri uri) {
        if (editAvatarPreview != null && getContext() != null) {
            editAvatarPreview.setPadding(0, 0, 0, 0);
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(editAvatarPreview);
        }
    }

    private void openAvatarPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        avatarPickerLauncher.launch(intent);
    }

    private void setupSpendingRange(View view) {
        setCurrentMonthRange();
        updateSpendingRangeControls(view);
    }

    private void setCurrentMonthRange() {
        YearMonth currentMonth = YearMonth.now();
        spendingFrom = currentMonth.atDay(1);
        spendingTo = LocalDate.now();
    }

    private void setCurrentWeekRange() {
        LocalDate today = LocalDate.now();
        spendingFrom = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        spendingTo = today;
    }

    private void updateQuickRangeState(View view) {
        MaterialButton weekButton = view.findViewById(R.id.btnSpendingThisWeek);
        MaterialButton monthButton = view.findViewById(R.id.btnSpendingThisMonth);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate monthStart = YearMonth.now().atDay(1);

        setQuickButtonState(weekButton, matchesSelectedRange(weekStart, today));
        setQuickButtonState(monthButton, matchesSelectedRange(monthStart, today));
    }

    private void setQuickButtonState(MaterialButton button, boolean selected) {
        int backgroundColor = requireContext().getColor(selected ? R.color.vendor_dark_orange : R.color.vendor_dark_card);
        int strokeColor = requireContext().getColor(selected ? R.color.vendor_dark_orange : R.color.vendor_dark_border);
        int textColor = requireContext().getColor(selected ? R.color.white : R.color.vendor_dark_text_secondary);

        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setStrokeWidth(dp(1));
        button.setTextColor(textColor);
    }

    private boolean matchesSelectedRange(LocalDate from, LocalDate to) {
        return spendingFrom != null
                && spendingTo != null
                && spendingFrom.equals(from)
                && spendingTo.equals(to);
    }

    private void pickSpendingDate(boolean pickFrom, View rootView) {
        LocalDate initialDate = pickFrom ? spendingFrom : spendingTo;
        if (initialDate == null) {
            initialDate = LocalDate.now();
        }

        new DatePickerDialog(
                requireContext(),
                (picker, year, month, dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    if (pickFrom) {
                        spendingFrom = selectedDate;
                        if (spendingTo != null && spendingFrom.isAfter(spendingTo)) {
                            spendingTo = spendingFrom;
                        }
                    } else {
                        spendingTo = selectedDate;
                        if (spendingFrom != null && spendingTo.isBefore(spendingFrom)) {
                            spendingFrom = spendingTo;
                        }
                    }
                    updateSpendingRangeControls(rootView);
                    loadSpendingForSelectedRange();
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        ).show();
    }

    private void updateSpendingRangeControls(View view) {
        TextView tvRange = view.findViewById(R.id.tvSpendingRange);
        TextView btnFrom = view.findViewById(R.id.btnSpendingFrom);
        TextView btnTo = view.findViewById(R.id.btnSpendingTo);
        TextView tvBreakdownTitle = view.findViewById(R.id.tvSpendingBreakdownTitle);

        String fromText = spendingFrom == null ? "--/--/----" : spendingFrom.format(displayDateFormatter);
        String toText = spendingTo == null ? "--/--/----" : spendingTo.format(displayDateFormatter);
        tvRange.setText("Từ " + fromText + " đến " + toText);
        btnFrom.setText(fromText);
        btnTo.setText(toText);

        if (tvBreakdownTitle != null) {
            tvBreakdownTitle.setText("Chi tiết theo tuần");
        }
        updateQuickRangeState(view);
    }

    private void loadSpendingForSelectedRange() {
        if (viewModel == null || spendingFrom == null || spendingTo == null) {
            return;
        }
        viewModel.loadSpendingSummary(
                spendingFrom.format(apiDateFormatter),
                spendingTo.format(apiDateFormatter)
        );
    }

    private List<SpendingSummaryResponse.SpendingBreakdown> normalizeSpendingBreakdown(SpendingSummaryResponse summary) {
        List<SpendingSummaryResponse.SpendingBreakdown> result = new ArrayList<>();
        if (summary == null) {
            return result;
        }

        List<SpendingSummaryResponse.SpendingBreakdown> source = summary.getBreakdown();
        if (source != null) {
            for (SpendingSummaryResponse.SpendingBreakdown item : source) {
                if (item != null) {
                    result.add(item);
                }
            }
        }

        result.sort((left, right) -> {
            LocalDate leftDate = periodSortDate(left.getPeriod());
            LocalDate rightDate = periodSortDate(right.getPeriod());
            return leftDate.compareTo(rightDate);
        });

        if (result.isEmpty() && summary.getTotalSpent() > 0) {
            SpendingSummaryResponse.SpendingBreakdown fallback = new SpendingSummaryResponse.SpendingBreakdown();
            fallback.setPeriod(fallbackSpendingPeriod());
            fallback.setTotal(summary.getTotalSpent());
            result.add(fallback);
        }

        return result;
    }

    private String fallbackSpendingPeriod() {
        LocalDate labelDate = spendingTo != null ? spendingTo : LocalDate.now();
        return labelDate + " - " + labelDate;
    }

    private LocalDate periodSortDate(String period) {
        LocalDate[] dates = parsePeriod(period);
        if (dates == null) {
            return LocalDate.MAX;
        }
        return isSelectedRangeDaily() ? clampPeriodToSelectedRange(dates)[1] : clampPeriodToSelectedRange(dates)[0];
    }

    private void bindSpendingSummaryStats(View view, SpendingSummaryResponse summary, List<SpendingSummaryResponse.SpendingBreakdown> breakdown) {
        TextView tvPeriodCount = view.findViewById(R.id.tvSpendingPeriodCount);
        TextView tvAverage = view.findViewById(R.id.tvSpendingAverage);
        TextView tvPeak = view.findViewById(R.id.tvSpendingPeak);

        int count = 0;
        double peak = 0;
        if (breakdown != null) {
            for (SpendingSummaryResponse.SpendingBreakdown item : breakdown) {
                if (item.getTotal() <= 0) {
                    continue;
                }
                count++;
                peak = Math.max(peak, item.getTotal());
            }
        }

        double average = count == 0 ? 0 : summary.getTotalSpent() / count;
        tvPeriodCount.setText(String.valueOf(count));
        tvAverage.setText(formatCompactPrice(average));
        tvPeak.setText(formatCompactPrice(peak));
    }

    private void bindSpendingChart(View view, List<SpendingSummaryResponse.SpendingBreakdown> breakdown) {
        LineChart chart = view.findViewById(R.id.chartSpendingTrend);
        if (chart == null) {
            return;
        }

        if (breakdown == null || breakdown.isEmpty()) {
            chart.clear();
            chart.setNoDataText("Chưa có chi tiêu trong khoảng này");
            chart.setNoDataTextColor(Color.parseColor("#8A7D79"));
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < breakdown.size(); i++) {
            SpendingSummaryResponse.SpendingBreakdown item = breakdown.get(i);
            if (item.getTotal() <= 0) {
                continue;
            }
            int chartIndex = entries.size();
            entries.add(new Entry(chartIndex, (float) item.getTotal()));
            labels.add(shortPeriod(item.getPeriod(), i + 1));
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("ChÆ°a cÃ³ chi tiÃªu trong khoáº£ng nÃ y");
            chart.setNoDataTextColor(Color.parseColor("#8A7D79"));
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Chi tiêu (đ)");
        dataSet.setColor(Color.parseColor("#F46E26"));
        dataSet.setCircleColor(Color.parseColor("#F46E26"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#22F46E26"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.parseColor("#5F514D"));
        dataSet.setValueTextSize(9f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return formatCompactPrice(entry.getY());
            }
        });

        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setExtraLeftOffset(10f);
        chart.setExtraRightOffset(8f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size(), true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setLabelRotationAngle(labels.size() > 4 ? -35f : 0f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#8A7D79"));
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);
        if (entries.size() == 1) {
            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(0.5f);
        } else {
            xAxis.setAxisMinimum(-0.2f);
            xAxis.setAxisMaximum(entries.size() - 0.8f);
        }
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#382C29"));
        chart.getAxisLeft().setTextColor(Color.parseColor("#8A7D79"));
        chart.getAxisLeft().setXOffset(10f);
        chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatCompactPrice(value);
            }
        });

        chart.getLegend().setTextColor(Color.parseColor("#5F514D"));
        chart.animateY(800);
        chart.invalidate();
    }

    private void bindSpendingBarChart(View view, List<SpendingSummaryResponse.SpendingBreakdown> breakdown) {
        BarChart chart = view.findViewById(R.id.chartSpendingPeriod);
        if (chart == null) {
            return;
        }

        if (breakdown == null || breakdown.isEmpty()) {
            chart.clear();
            chart.setNoDataText("Chưa có chi tiêu trong khoảng này");
            chart.setNoDataTextColor(Color.parseColor("#8A7D79"));
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < breakdown.size(); i++) {
            SpendingSummaryResponse.SpendingBreakdown item = breakdown.get(i);
            if (item.getTotal() <= 0) {
                continue;
            }
            int chartIndex = entries.size();
            entries.add(new BarEntry(chartIndex, (float) item.getTotal()));
            labels.add(shortPeriod(item.getPeriod(), i + 1));
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("ChÆ°a cÃ³ chi tiÃªu trong khoáº£ng nÃ y");
            chart.setNoDataTextColor(Color.parseColor("#8A7D79"));
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu theo kỳ");
        dataSet.setColor(Color.parseColor("#F46E26"));
        dataSet.setValueTextColor(Color.parseColor("#5F514D"));
        dataSet.setValueTextSize(9f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return formatCompactPrice(barEntry.getY());
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.48f);
        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setFitBars(true);
        chart.setExtraLeftOffset(10f);
        chart.setExtraRightOffset(8f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size(), true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setLabelRotationAngle(labels.size() > 4 ? -35f : 0f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#8A7D79"));
        if (entries.size() == 1) {
            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(0.5f);
        } else {
            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum(entries.size() - 0.5f);
        }
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#382C29"));
        chart.getAxisLeft().setTextColor(Color.parseColor("#8A7D79"));
        chart.getAxisLeft().setXOffset(10f);
        chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatCompactPrice(value);
            }
        });

        chart.getLegend().setTextColor(Color.parseColor("#5F514D"));
        chart.animateY(800);
        chart.invalidate();
    }

    private void bindSpendingPieChart(View view, List<SpendingSummaryResponse.SpendingBreakdown> breakdown) {
        PieChart chart = view.findViewById(R.id.chartSpendingBreakdown);
        if (chart == null) {
            return;
        }

        if (breakdown == null || breakdown.isEmpty()) {
            chart.clear();
            chart.setNoDataText("Chưa có chi tiêu trong khoảng này");
            chart.setNoDataTextColor(Color.parseColor("#8A7D79"));
            return;
        }

        int[] palette = {
                Color.parseColor("#F46E26"),
                Color.parseColor("#5299FF"),
                Color.parseColor("#34C759"),
                Color.parseColor("#FF9500"),
                Color.parseColor("#A855F7"),
                Color.parseColor("#EF4444")
        };
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int colorIndex = 0;
        Map<String, Double> weeklyTotals = buildWeeklySpendingTotals(breakdown);

        for (Map.Entry<String, Double> entry : weeklyTotals.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            entries.add(new PieEntry(entry.getValue().floatValue(), formatShortWeeklyPeriod(entry.getKey())));
            colors.add(palette[colorIndex % palette.length]);
            colorIndex++;
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("Chưa có chi tiêu trong khoảng này");
            chart.setNoDataTextColor(Color.parseColor("#8A7D79"));
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.parseColor("#5F514D"));
        dataSet.setValueFormatter(new PercentFormatter(chart));

        chart.setData(new PieData(dataSet));
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.parseColor("#281F1C"));
        chart.setHoleRadius(40f);
        chart.setTransparentCircleRadius(45f);
        chart.setCenterText("Chi tiêu");
        chart.setCenterTextSize(14f);
        chart.setCenterTextColor(Color.parseColor("#F46E26"));
        chart.setDrawEntryLabels(false);
        chart.setEntryLabelColor(Color.parseColor("#5F514D"));
        chart.setEntryLabelTextSize(10f);
        chart.setExtraBottomOffset(18f);
        chart.setExtraLeftOffset(8f);
        chart.setExtraRightOffset(8f);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.parseColor("#5F514D"));
        legend.setTextSize(12f);
        legend.setFormSize(10f);
        legend.setXEntrySpace(12f);
        legend.setYEntrySpace(6f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);

        chart.animateY(800);
        chart.invalidate();
    }

    private Map<String, Double> buildWeeklySpendingTotals(List<SpendingSummaryResponse.SpendingBreakdown> breakdown) {
        Map<String, Double> weeklyTotals = new LinkedHashMap<>();
        if (breakdown == null) {
            return weeklyTotals;
        }

        for (SpendingSummaryResponse.SpendingBreakdown item : breakdown) {
            if (item == null || item.getTotal() <= 0) {
                continue;
            }

            LocalDate[] dates = parsePeriod(item.getPeriod());
            if (dates == null) {
                weeklyTotals.merge(item.getPeriod(), item.getTotal(), Double::sum);
                continue;
            }

            LocalDate spendingDate = clampPeriodToSelectedRange(dates)[1];
            LocalDate weekStart = spendingDate.minusDays(spendingDate.getDayOfWeek().getValue() - 1L);
            LocalDate weekEnd = weekStart.plusDays(6);
            if (spendingFrom != null && weekStart.isBefore(spendingFrom)) {
                weekStart = spendingFrom;
            }
            if (spendingTo != null && weekEnd.isAfter(spendingTo)) {
                weekEnd = spendingTo;
            }

            String label = weekStart + " - " + weekEnd;
            weeklyTotals.merge(label, item.getTotal(), Double::sum);
        }

        return weeklyTotals;
    }

    private List<SpendingSummaryResponse.SpendingBreakdown> buildWeeklySpendingBreakdown(
            List<SpendingSummaryResponse.SpendingBreakdown> breakdown
    ) {
        List<SpendingSummaryResponse.SpendingBreakdown> weeklyBreakdown = new ArrayList<>();
        for (Map.Entry<String, Double> entry : buildWeeklySpendingTotals(breakdown).entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            SpendingSummaryResponse.SpendingBreakdown item = new SpendingSummaryResponse.SpendingBreakdown();
            item.setPeriod(entry.getKey());
            item.setTotal(entry.getValue());
            weeklyBreakdown.add(item);
        }
        return weeklyBreakdown;
    }

    private void bindSpendingBreakdown(View view, List<SpendingSummaryResponse.SpendingBreakdown> breakdown) {
        LinearLayout container = view.findViewById(R.id.llSpendingBreakdown);
        container.removeAllViews();

        if (breakdown == null || breakdown.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Chưa có chi tiêu trong khoảng này");
            empty.setTextColor(requireContext().getColor(R.color.vendor_dark_text_secondary));
            empty.setTextSize(13);
            empty.setPadding(0, dp(8), 0, dp(8));
            container.addView(empty);
            return;
        }

        double maxValue = 0;
        double totalSpent = 0;
        for (SpendingSummaryResponse.SpendingBreakdown item : breakdown) {
            maxValue = Math.max(maxValue, item.getTotal());
            totalSpent += item.getTotal();
        }

        for (SpendingSummaryResponse.SpendingBreakdown item : breakdown) {
            LinearLayout itemContainer = new LinearLayout(requireContext());
            itemContainer.setOrientation(LinearLayout.VERTICAL);
            itemContainer.setPadding(0, dp(9), 0, dp(9));

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Add bullet point indicator
            View bullet = new View(requireContext());
            bullet.setBackground(roundedBackground(requireContext().getColor(R.color.vendor_dark_orange), 4));
            LinearLayout.LayoutParams bulletParams = new LinearLayout.LayoutParams(dp(8), dp(8));
            bulletParams.rightMargin = dp(8);
            row.addView(bullet, bulletParams);

            TextView period = new TextView(requireContext());
            period.setText(formatPeriod(item.getPeriod()));
            period.setTextColor(requireContext().getColor(R.color.vendor_dark_text_secondary));
            period.setTextSize(13);

            TextView total = new TextView(requireContext());
            total.setText(formatPrice(item.getTotal()));
            total.setTextColor(requireContext().getColor(R.color.vendor_dark_text_primary));
            total.setTextSize(13);
            total.setTypeface(total.getTypeface(), android.graphics.Typeface.BOLD);
            total.setGravity(android.view.Gravity.END);

            TextView share = new TextView(requireContext());
            share.setText(totalSpent <= 0
                    ? "0%"
                    : String.format(Locale.US, "%.1f%%", item.getTotal() * 100D / totalSpent));
            share.setTextColor(requireContext().getColor(R.color.vendor_dark_orange));
            share.setTextSize(13);
            share.setTypeface(share.getTypeface(), android.graphics.Typeface.BOLD);
            share.setGravity(android.view.Gravity.END);

            row.addView(period, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 5));
            row.addView(total, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3));
            row.addView(share, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
            itemContainer.addView(row);

            LinearLayout track = new LinearLayout(requireContext());
            track.setOrientation(LinearLayout.HORIZONTAL);
            track.setBackground(roundedBackground(requireContext().getColor(R.color.vendor_dark_divider), 8));

            float percent = maxValue <= 0 ? 0f : (float) (item.getTotal() / maxValue);
            if (percent > 0f) {
                View fill = new View(requireContext());
                fill.setBackground(roundedBackground(requireContext().getColor(R.color.vendor_dark_orange), 8));
                track.addView(fill, new LinearLayout.LayoutParams(0, dp(8), Math.max(0.08f, percent)));
            }
            View spacer = new View(requireContext());
            track.addView(spacer, new LinearLayout.LayoutParams(0, dp(8), Math.max(0.001f, 1f - percent)));

            LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(8)
            );
            trackParams.topMargin = dp(8);
            itemContainer.addView(track, trackParams);
            container.addView(itemContainer);
        }
    }

    private void showSupportInfo() {
        if (getContext() == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Hỗ trợ")
                .setMessage("Vui lòng liên hệ quầy hỗ trợ UniEats nếu đơn hàng hoặc tài khoản gặp vấn đề.")
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    private void showMyReviewsSheet() {
        if (getContext() == null) {
            return;
        }

        reviewsDialog = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_student_reviews, null);
        reviewsDialog.setContentView(content);

        RecyclerView rvReviews = content.findViewById(R.id.rvStudentReviews);
        tvReviewsEmpty = content.findViewById(R.id.tvStudentReviewsEmpty);
        reviewAdapter = new StudentReviewAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReviews.setAdapter(reviewAdapter);

        tvReviewsEmpty.setVisibility(View.VISIBLE);
        tvReviewsEmpty.setText("Đang tải đánh giá...");
        rvReviews.setVisibility(View.GONE);

        reviewsDialog.setOnDismissListener(dialog -> {
            reviewsDialog = null;
            reviewAdapter = null;
            tvReviewsEmpty = null;
        });
        reviewsDialog.show();
        viewModel.loadMyReviews();
    }

    private void observeMyReviews() {
        viewModel.getMyReviews().observe(getViewLifecycleOwner(), this::bindMyReviews);
    }

    private void bindMyReviews(List<StudentReviewResponse> reviews) {
        if (reviewAdapter == null || tvReviewsEmpty == null || reviewsDialog == null) {
            return;
        }

        boolean empty = reviews == null || reviews.isEmpty();
        reviewAdapter.submitList(reviews);
        tvReviewsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        tvReviewsEmpty.setText(reviews == null ? "Không tải được đánh giá" : "Bạn chưa có đánh giá nào");

        View rvReviews = reviewsDialog.findViewById(R.id.rvStudentReviews);
        if (rvReviews != null) {
            rvReviews.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private void showEditProfileSheet() {
        if (getContext() == null) {
            return;
        }
        if (currentProfile == null) {
            ToastUtils.info(getContext(), "Đang tải hồ sơ");
            viewModel.loadProfile();
            return;
        }

        editProfileDialog = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null);
        editProfileDialog.setContentView(content);

        EditText etFullName = content.findViewById(R.id.etProfileFullName);
        EditText etEmail = content.findViewById(R.id.etProfileEmail);
        editAvatarPreview = content.findViewById(R.id.imgProfileAvatarPreview);
        btnChooseAvatar = content.findViewById(R.id.btnChooseProfileAvatar);
        btnSaveProfile = content.findViewById(R.id.btnSaveProfile);
        Spinner spinnerBuilding = content.findViewById(R.id.spinnerProfileBuilding);
        View btnCancel = content.findViewById(R.id.btnCancelEditProfile);

        etFullName.setText(nonNull(currentProfile.getFullName()));
        etEmail.setText(nonNull(currentProfile.getEmail()));
        selectedAvatarUrl = normalize(currentProfile.getAvatarUrl());
        bindEditAvatarPreview();

        String[] selectedBuildingId = {currentProfile.getBuildingId()};
        bindBuildingSpinner(spinnerBuilding, selectedBuildingId);

        uploadImageViewModel.clearUploadResult();
        btnChooseAvatar.setOnClickListener(v -> openAvatarPicker());
        btnCancel.setOnClickListener(v -> editProfileDialog.dismiss());
        btnSaveProfile.setOnClickListener(v -> saveProfile(
                etFullName.getText().toString(),
                etEmail.getText().toString(),
                selectedBuildingId[0],
                selectedAvatarUrl
        ));

        editProfileDialog.setOnDismissListener(dialog -> {
            editAvatarPreview = null;
            btnChooseAvatar = null;
            btnSaveProfile = null;
        });
        editProfileDialog.show();
    }

    private void bindEditAvatarPreview() {
        if (editAvatarPreview == null) {
            return;
        }
        if (!isBlank(selectedAvatarUrl) && getContext() != null) {
            editAvatarPreview.setPadding(0, 0, 0, 0);
            Glide.with(this)
                    .load(ImageUrlUtils.resolveImageUrl(selectedAvatarUrl))
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(editAvatarPreview);
        } else {
            editAvatarPreview.setImageResource(R.drawable.ic_profile);
        }
    }

    private void bindBuildingSpinner(Spinner spinner, String[] selectedBuildingId) {
        List<String> labels = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        labels.add("Giữ nguyên tòa nhà");
        ids.add(currentProfile.getBuildingId());

        int selectedIndex = 0;
        for (BuildingResponse building : buildingOptions) {
            if (building == null || isBlank(building.getId())) {
                continue;
            }
            labels.add(isBlank(building.getName()) ? "Tòa nhà" : building.getName());
            ids.add(building.getId());
            if (!isBlank(currentProfile.getBuildingId())
                    && currentProfile.getBuildingId().equals(building.getId())) {
                selectedIndex = labels.size() - 1;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selectedIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBuildingId[0] = ids.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void saveProfile(String fullName, String email, String buildingId, String avatarUrl) {
        String cleanName = normalize(fullName);
        String cleanEmail = normalize(email);
        String cleanAvatarUrl = normalize(avatarUrl);

        if (isBlank(cleanName)) {
            ToastUtils.error(getContext(), "Vui lòng nhập họ tên");
            return;
        }
        if (!isBlank(cleanEmail) && !cleanEmail.contains("@")) {
            ToastUtils.error(getContext(), "Email không hợp lệ");
            return;
        }

        viewModel.updateProfile(new UpdateProfileRequest(
                cleanName,
                cleanEmail,
                buildingId,
                cleanAvatarUrl
        ));
    }

    private void confirmLogout() {
        if (getContext() == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn muốn đăng xuất khỏi tài khoản này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        if (!unregisterDeviceToken()) {
            finishLogout();
        }
    }

    private boolean unregisterDeviceToken() {
        String fcmToken = TokenManager.getInstance().getFcmToken();
        if (isBlank(fcmToken)) {
            return false;
        }

        ApiClient.getApiService().removeDeviceToken(fcmToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                finishLogout();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                finishLogout();
            }
        });
        return true;
    }

    private void finishLogout() {
        TokenManager.getInstance().clearTokens();
        if (getContext() == null) {
            return;
        }
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String formatRole(String role) {
        if ("STUDENT".equalsIgnoreCase(role)) {
            return "Sinh viên";
        }
        if ("SHIPPER".equalsIgnoreCase(role)) {
            return "Shipper";
        }
        if (isBlank(role)) {
            return "Sinh viên";
        }
        return role;
    }

    private String defaultDisplayName(String role) {
        return "STUDENT".equalsIgnoreCase(role) ? "Sinh viên UniEats" : "Người dùng UniEats";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String nonNull(String value) {
        return value == null ? "" : value;
    }

    private String shortPeriod(String period, int fallbackIndex) {
        LocalDate[] dates = parsePeriod(period);
        if (dates == null) {
            return "K" + fallbackIndex;
        }
        LocalDate[] clippedDates = clampPeriodToSelectedRange(dates);
        LocalDate labelDate = isSelectedRangeDaily() ? clippedDates[1] : clippedDates[0];
        return labelDate.format(shortDateFormatter);
    }

    private String formatPeriod(String period) {
        LocalDate[] dates = parsePeriod(period);
        if (dates == null) {
            return isBlank(period) ? "Không rõ thời gian" : period;
        }
        LocalDate[] clippedDates = clampPeriodToSelectedRange(dates);
        if (clippedDates[0].equals(clippedDates[1])) {
            return "Ngày " + clippedDates[0].format(displayDateFormatter);
        }
        return "Tuần " + clippedDates[0].format(displayDateFormatter)
                + " - " + clippedDates[1].format(displayDateFormatter);
    }

    private String formatShortWeeklyPeriod(String period) {
        LocalDate[] dates = parsePeriod(period);
        if (dates == null) {
            return isBlank(period) ? "Tuần không rõ" : period;
        }
        LocalDate[] clippedDates = clampPeriodToSelectedRange(dates);
        return "Tuần " + clippedDates[0].format(shortDateFormatter)
                + " - " + clippedDates[1].format(shortDateFormatter);
    }

    private LocalDate[] clampPeriodToSelectedRange(LocalDate[] dates) {
        LocalDate start = dates[0];
        LocalDate end = dates[1];
        if (spendingFrom != null && start.isBefore(spendingFrom)) {
            start = spendingFrom;
        }
        if (spendingTo != null && end.isAfter(spendingTo)) {
            end = spendingTo;
        }
        if (start.isAfter(end)) {
            return dates;
        }
        return new LocalDate[]{start, end};
    }

    private boolean isSelectedRangeDaily() {
        if (spendingFrom == null || spendingTo == null) {
            return false;
        }
        return spendingTo.toEpochDay() - spendingFrom.toEpochDay() <= 31;
    }

    private LocalDate[] parsePeriod(String period) {
        if (isBlank(period) || !period.contains(" - ")) {
            return null;
        }
        String[] parts = period.split(" - ");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new LocalDate[]{
                    LocalDate.parse(parts[0].trim(), apiDateFormatter),
                    LocalDate.parse(parts[1].trim(), apiDateFormatter)
            };
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundedBackground(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private String formatCompactPrice(double price) {
        if (price >= 1_000_000_000D) {
            return formatOneDecimal(price / 1_000_000_000D) + " tỷ đ";
        }
        if (price >= 1_000_000D) {
            return formatOneDecimal(price / 1_000_000D) + " triệu đ";
        }
        if (price >= 1_000D) {
            return formatOneDecimal(price / 1_000D) + "k đ";
        }
        return formatPrice(price);
    }

    private String formatOneDecimal(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }
}
