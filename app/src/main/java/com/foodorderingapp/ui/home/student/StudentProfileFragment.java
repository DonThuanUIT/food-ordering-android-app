package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.model.request.UpdateProfileRequest;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.SpendingSummaryResponse;
import com.foodorderingapp.model.response.StudentReviewResponse;
import com.foodorderingapp.model.response.UserProfileResponse;
import com.foodorderingapp.ui.adapter.StudentReviewAdapter;
import com.foodorderingapp.ui.auth.LoginActivity;
import com.foodorderingapp.utils.TokenManager;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.StudentProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentProfileFragment extends Fragment {
    private final List<BuildingResponse> buildingOptions = new ArrayList<>();
    private final DateTimeFormatter apiDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private StudentProfileViewModel viewModel;
    private UserProfileResponse currentProfile;
    private BottomSheetDialog editProfileDialog;
    private BottomSheetDialog reviewsDialog;
    private StudentReviewAdapter reviewAdapter;
    private TextView tvReviewsEmpty;
    private LocalDate spendingFrom;
    private LocalDate spendingTo;

    public StudentProfileFragment() {}

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
        loadRemoteProfile(view);
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
        tvUserEmail.setText(isBlank(phone) ? "Chua co so dien thoai" : phone);
        tvUserTag.setText("Vai tro: " + formatRole(role));
        tvMonthlySpending.setText("0d");
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

        tvUserName.setText(isBlank(profile.getFullName()) ? "Sinh vien UniEats" : profile.getFullName());

        String emailOrPhone = !isBlank(profile.getEmail()) ? profile.getEmail() : profile.getPhone();
        tvUserEmail.setText(isBlank(emailOrPhone) ? "Chua co thong tin lien he" : emailOrPhone);

        String building = isBlank(profile.getBuildingName()) ? "Chua chon toa nha" : profile.getBuildingName();
        tvUserTag.setText(formatRole(profile.getRole()) + " - " + building);

        if (!isBlank(profile.getAvatarUrl()) && getContext() != null) {
            Glide.with(this)
                    .load(profile.getAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(ivAvatar);
        }
    }

    private void bindSpending(View view, SpendingSummaryResponse summary) {
        TextView tvMonthlySpending = view.findViewById(R.id.tvMonthlySpending);
        tvMonthlySpending.setText(formatPrice(summary.getTotalSpent()));
        bindSpendingSummaryStats(view, summary);
        bindSpendingChart(view, summary.getBreakdown());
        bindSpendingBreakdown(view, summary.getBreakdown());
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
        view.findViewById(R.id.rowPaymentMethod).setOnClickListener(v -> showPaymentInfo());
        view.findViewById(R.id.rowMyReviews).setOnClickListener(v -> showMyReviewsSheet());
        view.findViewById(R.id.rowSupport).setOnClickListener(v -> showSupportInfo());
        view.findViewById(R.id.btnEditAvatar).setOnClickListener(v -> showEditProfileSheet());
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
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
        int backgroundColor = requireContext().getColor(selected ? R.color.profile_spending_accent : R.color.white);
        int strokeColor = requireContext().getColor(selected ? R.color.profile_spending_accent : R.color.profile_spending_border);
        int textColor = requireContext().getColor(selected ? R.color.white : R.color.profile_spending_accent_dark);

        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setStrokeWidth(dp(selected ? 0 : 1));
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
            tvBreakdownTitle.setText(isSelectedRangeWithinOneWeek()
                    ? "Chi tiết tuần đã chọn"
                    : "Chi tiết theo tuần");
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

    private void bindSpendingSummaryStats(View view, SpendingSummaryResponse summary) {
        TextView tvPeriodCount = view.findViewById(R.id.tvSpendingPeriodCount);
        TextView tvAverage = view.findViewById(R.id.tvSpendingAverage);
        TextView tvPeak = view.findViewById(R.id.tvSpendingPeak);

        List<SpendingSummaryResponse.SpendingBreakdown> breakdown = summary.getBreakdown();
        int count = breakdown == null ? 0 : breakdown.size();
        double peak = 0;
        if (breakdown != null) {
            for (SpendingSummaryResponse.SpendingBreakdown item : breakdown) {
                peak = Math.max(peak, item.getTotal());
            }
        }

        double average = count == 0 ? 0 : summary.getTotalSpent() / count;
        tvPeriodCount.setText(String.valueOf(count));
        tvAverage.setText(formatCompactPrice(average));
        tvPeak.setText(formatCompactPrice(peak));
    }

    private void bindSpendingChart(View view, List<SpendingSummaryResponse.SpendingBreakdown> breakdown) {
        LinearLayout chart = view.findViewById(R.id.llSpendingChart);
        chart.removeAllViews();

        int dataSize = breakdown == null ? 0 : breakdown.size();
        int visibleBars = dataSize == 0 ? 4 : Math.min(6, dataSize);
        int startIndex = Math.max(0, dataSize - visibleBars);
        double maxValue = 0;
        for (int i = startIndex; i < dataSize; i++) {
            maxValue = Math.max(maxValue, breakdown.get(i).getTotal());
        }

        for (int i = 0; i < visibleBars; i++) {
            int sourceIndex = startIndex + i;
            boolean hasData = sourceIndex < dataSize;
            double value = hasData ? breakdown.get(sourceIndex).getTotal() : 0;
            int height = maxValue <= 0 ? 16 : (int) Math.max(16, Math.round(80 * (value / maxValue)));

            LinearLayout column = new LinearLayout(requireContext());
            column.setGravity(android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.BOTTOM);
            column.setOrientation(LinearLayout.VERTICAL);

            View bar = new View(requireContext());
            bar.setBackground(topRoundedBackground(requireContext().getColor(
                    value > 0 ? R.color.profile_chart_bar_main : R.color.profile_chart_bar_empty
            ), 6));

            TextView label = new TextView(requireContext());
            label.setText(hasData ? shortPeriod(breakdown.get(sourceIndex).getPeriod(), i + 1) : "--");
            label.setTextColor(requireContext().getColor(value > 0 ? R.color.profile_spending_accent_dark : R.color.text_gray));
            label.setTextSize(11);
            label.setGravity(android.view.Gravity.CENTER);
            label.setMaxLines(1);

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dp(28), dp(height));
            column.addView(bar, barParams);

            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            labelParams.topMargin = dp(8);
            column.addView(label, labelParams);

            chart.addView(column, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        }
    }

    private void bindSpendingBreakdown(View view, List<SpendingSummaryResponse.SpendingBreakdown> breakdown) {
        LinearLayout container = view.findViewById(R.id.llSpendingBreakdown);
        container.removeAllViews();

        if (breakdown == null || breakdown.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Chưa có chi tiêu trong khoảng này");
            empty.setTextColor(requireContext().getColor(R.color.text_secondary));
            empty.setTextSize(13);
            empty.setPadding(0, dp(8), 0, dp(8));
            container.addView(empty);
            return;
        }

        double maxValue = 0;
        for (SpendingSummaryResponse.SpendingBreakdown item : breakdown) {
            maxValue = Math.max(maxValue, item.getTotal());
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
            bullet.setBackground(roundedBackground(requireContext().getColor(R.color.profile_spending_accent), 4));
            LinearLayout.LayoutParams bulletParams = new LinearLayout.LayoutParams(dp(8), dp(8));
            bulletParams.rightMargin = dp(8);
            row.addView(bullet, bulletParams);

            TextView period = new TextView(requireContext());
            period.setText(formatPeriod(item.getPeriod()));
            period.setTextColor(requireContext().getColor(R.color.text_secondary));
            period.setTextSize(13);

            TextView total = new TextView(requireContext());
            total.setText(formatPrice(item.getTotal()));
            total.setTextColor(requireContext().getColor(R.color.text_primary));
            total.setTextSize(13);
            total.setTypeface(total.getTypeface(), android.graphics.Typeface.BOLD);
            total.setGravity(android.view.Gravity.END);

            row.addView(period, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            row.addView(total, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            itemContainer.addView(row);

            LinearLayout track = new LinearLayout(requireContext());
            track.setOrientation(LinearLayout.HORIZONTAL);
            track.setBackground(roundedBackground(requireContext().getColor(R.color.profile_chart_bar_empty), 8));

            float percent = maxValue <= 0 ? 0f : (float) (item.getTotal() / maxValue);
            if (percent > 0f) {
                View fill = new View(requireContext());
                fill.setBackground(roundedBackground(requireContext().getColor(R.color.profile_chart_bar_main), 8));
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

    private void showPaymentInfo() {
        ToastUtils.info(getContext(), "Thanh toan khi nhan hang");
    }

    private void showSupportInfo() {
        if (getContext() == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Ho tro")
                .setMessage("Vui long lien he quay ho tro UniEats neu don hang hoac tai khoan gap van de.")
                .setPositiveButton("Da hieu", null)
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
        tvReviewsEmpty.setText("Dang tai danh gia...");
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
        tvReviewsEmpty.setText(reviews == null ? "Khong tai duoc danh gia" : "Ban chua co danh gia nao");

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
        EditText etAvatarUrl = content.findViewById(R.id.etProfileAvatarUrl);
        Spinner spinnerBuilding = content.findViewById(R.id.spinnerProfileBuilding);
        View btnSave = content.findViewById(R.id.btnSaveProfile);
        View btnCancel = content.findViewById(R.id.btnCancelEditProfile);

        etFullName.setText(nonNull(currentProfile.getFullName()));
        etEmail.setText(nonNull(currentProfile.getEmail()));
        etAvatarUrl.setText(nonNull(currentProfile.getAvatarUrl()));

        String[] selectedBuildingId = {currentProfile.getBuildingId()};
        bindBuildingSpinner(spinnerBuilding, selectedBuildingId);

        btnCancel.setOnClickListener(v -> editProfileDialog.dismiss());
        btnSave.setOnClickListener(v -> saveProfile(
                etFullName.getText().toString(),
                etEmail.getText().toString(),
                selectedBuildingId[0],
                etAvatarUrl.getText().toString()
        ));

        editProfileDialog.show();
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
                .setTitle("Dang xuat")
                .setMessage("Ban muon dang xuat khoi tai khoan nay?")
                .setNegativeButton("Huy", null)
                .setPositiveButton("Dang xuat", (dialog, which) -> logout())
                .show();
    }

    private void logout() {
        TokenManager.getInstance().clearTokens();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String formatRole(String role) {
        if ("STUDENT".equalsIgnoreCase(role)) {
            return "Sinh vien";
        }
        if (isBlank(role)) {
            return "Sinh vien";
        }
        return role;
    }

    private String defaultDisplayName(String role) {
        return "STUDENT".equalsIgnoreCase(role) ? "Sinh vien UniEats" : "Nguoi dung UniEats";
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
        return clampPeriodToSelectedRange(dates)[0].format(shortDateFormatter);
    }

    private String formatPeriod(String period) {
        LocalDate[] dates = parsePeriod(period);
        if (dates == null) {
            return isBlank(period) ? "Không rõ thời gian" : period;
        }
        LocalDate[] clippedDates = clampPeriodToSelectedRange(dates);
        return "Tuần " + clippedDates[0].format(displayDateFormatter)
                + " - " + clippedDates[1].format(displayDateFormatter);
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

    private boolean isSelectedRangeWithinOneWeek() {
        if (spendingFrom == null || spendingTo == null) {
            return false;
        }
        return spendingTo.toEpochDay() - spendingFrom.toEpochDay() < 7;
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

    private GradientDrawable topRoundedBackground(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        float r = dp(radiusDp);
        drawable.setCornerRadii(new float[]{r, r, r, r, 0f, 0f, 0f, 0f});
        return drawable;
    }

    private String formatCompactPrice(double price) {
        if (price >= 1_000_000_000D) {
            return formatOneDecimal(price / 1_000_000_000D) + "Bđ";
        }
        if (price >= 1_000_000D) {
            return formatOneDecimal(price / 1_000_000D) + "Mđ";
        }
        if (price >= 1_000D) {
            return formatOneDecimal(price / 1_000D) + "Kđ";
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
        return formatter.format(price) + "d";
    }
}
