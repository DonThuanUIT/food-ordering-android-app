package com.foodorderingapp.ui.voucher;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.VoucherCreateRequest;
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.model.response.VoucherResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherFormActivity extends AppCompatActivity {

    private TextView tvTitleHeader;
    private EditText etCode;
    private EditText etTitle;
    private RadioGroup rgDiscountType;
    private RadioButton rbPercentage;
    private RadioButton rbFixedAmount;
    private TextView tvDiscountValueLabel;
    private EditText etDiscountValue;
    private EditText etMinOrder;
    private LinearLayout layoutMaxDiscount;
    private EditText etMaxDiscount;
    
    private View btnPickStart;
    private TextView tvStartDateDisplay;
    private View btnPickEnd;
    private TextView tvEndDateDisplay;
    
    private RadioGroup rgApplyType;
    private RadioButton rbAllMenu;
    private RadioButton rbSpecificFoods;
    private LinearLayout layoutFoodsSelector;
    private Button btnSelectFoods;
    private TextView tvSelectedFoodsCount;
    
    private SwitchCompat switchActiveForm;
    private Button btnSaveVoucher;

    private UUID shopId;
    private UUID voucherId; // Null if create mode
    
    // Dates internally stored as ISO strings
    private String startIsoStr = "";
    private String endIsoStr = "";
    
    // Selected foods tracking
    private List<FoodResponse> allShopFoods = new ArrayList<>();
    private final List<UUID> selectedFoodIds = new ArrayList<>();
    private boolean isFoodsLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_form);

        // Bind Views
        tvTitleHeader = findViewById(R.id.tv_title);
        etCode = findViewById(R.id.et_code);
        etTitle = findViewById(R.id.et_title);
        rgDiscountType = findViewById(R.id.rg_discount_type);
        rbPercentage = findViewById(R.id.rb_percentage);
        rbFixedAmount = findViewById(R.id.rb_fixed_amount);
        tvDiscountValueLabel = findViewById(R.id.tv_discount_value_label);
        etDiscountValue = findViewById(R.id.et_discount_value);
        etMinOrder = findViewById(R.id.et_min_order);
        layoutMaxDiscount = findViewById(R.id.layout_max_discount);
        etMaxDiscount = findViewById(R.id.et_max_discount);
        
        btnPickStart = findViewById(R.id.btn_pick_start);
        tvStartDateDisplay = findViewById(R.id.tv_start_date);
        btnPickEnd = findViewById(R.id.btn_pick_end);
        tvEndDateDisplay = findViewById(R.id.tv_end_date);
        
        rgApplyType = findViewById(R.id.rg_apply_type);
        rbAllMenu = findViewById(R.id.rb_all_menu);
        rbSpecificFoods = findViewById(R.id.rb_specific_foods);
        layoutFoodsSelector = findViewById(R.id.layout_foods_selector);
        btnSelectFoods = findViewById(R.id.btn_select_foods);
        tvSelectedFoodsCount = findViewById(R.id.tv_selected_foods_count);
        
        switchActiveForm = findViewById(R.id.switch_active_form);
        btnSaveVoucher = findViewById(R.id.btn_save_voucher);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Parse arguments
        String shopIdStr = getIntent().getStringExtra("SHOP_ID");
        if (shopIdStr != null) {
            shopId = UUID.fromString(shopIdStr);
        } else {
            Toast.makeText(this, "Thiếu ID cửa hàng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String voucherIdStr = getIntent().getStringExtra("VOUCHER_ID");
        if (voucherIdStr != null) {
            voucherId = UUID.fromString(voucherIdStr);
            tvTitleHeader.setText("Cập nhật Voucher");
        } else {
            tvTitleHeader.setText("Tạo Voucher Mới");
        }

        setupFormListeners();
        
        // If edit mode, populate data
        if (voucherId != null) {
            populateDataFromIntent();
        }

        // Fetch foods beforehand so multi-select works seamlessly
        fetchShopFoods();
    }

    private void setupFormListeners() {
        // Discount Type listener
        rgDiscountType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_percentage) {
                layoutMaxDiscount.setVisibility(View.VISIBLE);
                tvDiscountValueLabel.setText("Giá trị giảm giá (%) (*)");
                etDiscountValue.setHint("Ví dụ: 10 (cho 10%)");
            } else {
                layoutMaxDiscount.setVisibility(View.GONE);
                etMaxDiscount.setText("");
                tvDiscountValueLabel.setText("Giá trị giảm giá (đ) (*)");
                etDiscountValue.setHint("Ví dụ: 20000 (cho 20,000đ)");
            }
        });

        // Apply Type listener
        rgApplyType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_specific_foods) {
                layoutFoodsSelector.setVisibility(View.VISIBLE);
            } else {
                layoutFoodsSelector.setVisibility(View.GONE);
            }
        });

        // Date pickers
        btnPickStart.setOnClickListener(v -> showDateTimePicker(true));
        btnPickEnd.setOnClickListener(v -> showDateTimePicker(false));

        // Foods Select Button
        btnSelectFoods.setOnClickListener(v -> {
            if (!isFoodsLoaded) {
                Toast.makeText(this, "Đang tải danh sách món ăn, vui lòng đợi...", Toast.LENGTH_SHORT).show();
                fetchShopFoods();
            } else if (allShopFoods.isEmpty()) {
                Toast.makeText(this, "Cửa hàng không có món ăn nào để lựa chọn", Toast.LENGTH_SHORT).show();
            } else {
                showFoodsMultiSelectDialog();
            }
        });

        // Save Click
        btnSaveVoucher.setOnClickListener(v -> saveVoucher());
    }

    private void populateDataFromIntent() {
        etCode.setText(getIntent().getStringExtra("CODE"));
        etTitle.setText(getIntent().getStringExtra("TITLE"));
        
        String discountType = getIntent().getStringExtra("DISCOUNT_TYPE");
        if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
            rbPercentage.setChecked(true);
            layoutMaxDiscount.setVisibility(View.VISIBLE);
        } else {
            rbFixedAmount.setChecked(true);
            layoutMaxDiscount.setVisibility(View.GONE);
        }
        
        etDiscountValue.setText(getIntent().getStringExtra("DISCOUNT_VALUE"));
        
        String minOrder = getIntent().getStringExtra("MIN_ORDER_VALUE");
        if (minOrder != null && !minOrder.equals("0.00") && !minOrder.equals("0")) {
            etMinOrder.setText(minOrder);
        }
        
        String maxDiscount = getIntent().getStringExtra("MAX_DISCOUNT_VALUE");
        if (maxDiscount != null && !maxDiscount.isEmpty()) {
            etMaxDiscount.setText(maxDiscount);
        }

        String applyType = getIntent().getStringExtra("APPLY_TYPE");
        if ("SPECIFIC_FOODS".equalsIgnoreCase(applyType)) {
            rbSpecificFoods.setChecked(true);
            layoutFoodsSelector.setVisibility(View.VISIBLE);
        } else {
            rbAllMenu.setChecked(true);
            layoutFoodsSelector.setVisibility(View.GONE);
        }

        startIsoStr = getIntent().getStringExtra("START_DATE");
        endIsoStr = getIntent().getStringExtra("END_DATE");
        
        tvStartDateDisplay.setText(formatIsoDateTimeToDisplay(startIsoStr));
        tvEndDateDisplay.setText(formatIsoDateTimeToDisplay(endIsoStr));
        
        switchActiveForm.setChecked(getIntent().getBooleanExtra("IS_ACTIVE", true));

        // Populate selected food IDs
        ArrayList<String> foodIdsStrList = getIntent().getStringArrayListExtra("FOOD_IDS");
        if (foodIdsStrList != null) {
            for (String idStr : foodIdsStrList) {
                selectedFoodIds.add(UUID.fromString(idStr));
            }
            updateSelectedFoodsCountText();
        }
    }

    private void fetchShopFoods() {
        if (shopId == null) return;
        ApiClient.getApiService().getAllFoods(shopId, null).enqueue(new Callback<List<FoodResponse>>() {
            @Override
            public void onResponse(Call<List<FoodResponse>> call, Response<List<FoodResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allShopFoods = response.body();
                    isFoodsLoaded = true;
                    // Update preview names if needed
                    updateSelectedFoodsCountText();
                }
            }

            @Override
            public void onFailure(Call<List<FoodResponse>> call, Throwable t) {
                // Fail silently, retry on select click
                isFoodsLoaded = false;
            }
        });
    }

    private void showDateTimePicker(final boolean isStartDate) {
        final Calendar currentCalendar = Calendar.getInstance();
        
        // If editing/re-picking, parse current values as base
        String existingIso = isStartDate ? startIsoStr : endIsoStr;
        if (existingIso != null && !existingIso.isEmpty()) {
            try {
                String clean = existingIso;
                if (clean.contains(".")) {
                    clean = clean.substring(0, clean.indexOf("."));
                }
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date d = parser.parse(clean);
                if (d != null) {
                    currentCalendar.setTime(d);
                }
            } catch (Exception ignored) {}
        }

        // 1. Date Picker
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            final Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(Calendar.YEAR, year);
            selectedCal.set(Calendar.MONTH, month);
            selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // 2. Time Picker
            new TimePickerDialog(VoucherFormActivity.this, (timeView, hourOfDay, minute) -> {
                selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCal.set(Calendar.MINUTE, minute);
                selectedCal.set(Calendar.SECOND, 0);

                SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                SimpleDateFormat displayFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                
                String isoStr = isoFormatter.format(selectedCal.getTime());
                String displayStr = displayFormatter.format(selectedCal.getTime());
                
                if (isStartDate) {
                    startIsoStr = isoStr;
                    tvStartDateDisplay.setText(displayStr);
                } else {
                    endIsoStr = isoStr;
                    tvEndDateDisplay.setText(displayStr);
                }
            }, currentCalendar.get(Calendar.HOUR_OF_DAY), currentCalendar.get(Calendar.MINUTE), true).show();

        }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showFoodsMultiSelectDialog() {
        if (allShopFoods == null || allShopFoods.isEmpty()) return;

        int size = allShopFoods.size();
        CharSequence[] items = new CharSequence[size];
        boolean[] checkedItems = new boolean[size];

        for (int i = 0; i < size; i++) {
            FoodResponse food = allShopFoods.get(i);
            items[i] = food.getName() + " (" + formatCurrency(food.getPrice()) + ")";
            checkedItems[i] = selectedFoodIds.contains(food.getId());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn các món ăn áp dụng");
        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
            UUID id = allShopFoods.get(which).getId();
            if (isChecked) {
                if (!selectedFoodIds.contains(id)) {
                    selectedFoodIds.add(id);
                }
            } else {
                selectedFoodIds.remove(id);
            }
        });

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            updateSelectedFoodsCountText();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateSelectedFoodsCountText() {
        if (selectedFoodIds.isEmpty()) {
            tvSelectedFoodsCount.setText("Chưa chọn món nào");
            tvSelectedFoodsCount.setTextColor(getResources().getColor(R.color.status_error));
        } else {
            // Find selected food names for preview
            StringBuilder sb = new StringBuilder();
            sb.append("Đã chọn ").append(selectedFoodIds.size()).append(" món ăn:\n");
            
            int count = 0;
            for (FoodResponse food : allShopFoods) {
                if (selectedFoodIds.contains(food.getId())) {
                    if (count > 0) sb.append(", ");
                    sb.append(food.getName());
                    count++;
                }
            }
            tvSelectedFoodsCount.setText(sb.toString());
            tvSelectedFoodsCount.setTextColor(getResources().getColor(R.color.brand_orange_dark));
        }
    }

    private void saveVoucher() {
        String code = etCode.getText().toString().trim().toUpperCase();
        String title = etTitle.getText().toString().trim();
        
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã Voucher!", Toast.LENGTH_SHORT).show();
            etCode.requestFocus();
            return;
        }

        if (code.contains(" ")) {
            Toast.makeText(this, "Mã Voucher không được chứa khoảng trắng!", Toast.LENGTH_SHORT).show();
            etCode.requestFocus();
            return;
        }

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề hiển thị!", Toast.LENGTH_SHORT).show();
            etTitle.requestFocus();
            return;
        }

        String discountType = rbPercentage.isChecked() ? "PERCENTAGE" : "FIXED_AMOUNT";
        
        String discountValStr = etDiscountValue.getText().toString().trim();
        if (discountValStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập giá trị giảm giá!", Toast.LENGTH_SHORT).show();
            etDiscountValue.requestFocus();
            return;
        }

        BigDecimal discountVal;
        try {
            discountVal = new BigDecimal(discountValStr);
        } catch (Exception e) {
            Toast.makeText(this, "Giá trị giảm giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            etDiscountValue.requestFocus();
            return;
        }

        if (discountVal.compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(this, "Giá trị giảm giá phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
            etDiscountValue.requestFocus();
            return;
        }

        if ("PERCENTAGE".equals(discountType) && discountVal.compareTo(new BigDecimal(100)) > 0) {
            Toast.makeText(this, "Phần trăm giảm giá tối đa là 100%!", Toast.LENGTH_SHORT).show();
            etDiscountValue.requestFocus();
            return;
        }

        BigDecimal minOrderVal = BigDecimal.ZERO;
        String minOrderStr = etMinOrder.getText().toString().trim();
        if (!minOrderStr.isEmpty()) {
            try {
                minOrderVal = new BigDecimal(minOrderStr);
            } catch (Exception e) {
                Toast.makeText(this, "Giá trị đơn tối thiểu không hợp lệ!", Toast.LENGTH_SHORT).show();
                etMinOrder.requestFocus();
                return;
            }
        }

        BigDecimal maxDiscountVal = null;
        if ("PERCENTAGE".equals(discountType)) {
            String maxDiscountStr = etMaxDiscount.getText().toString().trim();
            if (!maxDiscountStr.isEmpty()) {
                try {
                    maxDiscountVal = new BigDecimal(maxDiscountStr);
                } catch (Exception e) {
                    Toast.makeText(this, "Mức giảm giá tối đa không hợp lệ!", Toast.LENGTH_SHORT).show();
                    etMaxDiscount.requestFocus();
                    return;
                }
            }
        }

        if (startIsoStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày bắt đầu!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endIsoStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày kết thúc!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Compare start and end dates
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date start = parser.parse(startIsoStr.replace(".000", ""));
            Date end = parser.parse(endIsoStr.replace(".000", ""));
            if (start != null && end != null && end.before(start)) {
                Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu!", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception ignored) {}

        String applyType = rbAllMenu.isChecked() ? "ALL_MENU" : "SPECIFIC_FOODS";
        if ("SPECIFIC_FOODS".equals(applyType) && selectedFoodIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một món ăn được áp dụng!", Toast.LENGTH_LONG).show();
            return;
        }

        VoucherCreateRequest request = new VoucherCreateRequest();
        request.setCode(code);
        request.setTitle(title);
        request.setDiscountType(discountType);
        request.setDiscountValue(discountVal);
        request.setMinOrderValue(minOrderVal);
        request.setMaxDiscountValue(maxDiscountVal);
        request.setApplyType(applyType);
        request.setStartDate(startIsoStr);
        request.setEndDate(endIsoStr);
        request.setFoodIds("SPECIFIC_FOODS".equals(applyType) ? selectedFoodIds : null);

        btnSaveVoucher.setEnabled(false);
        
        if (voucherId == null) {
            // Create mode
            ApiClient.getApiService().createVoucher(shopId, request).enqueue(new Callback<VoucherResponse>() {
                @Override
                public void onResponse(Call<VoucherResponse> call, Response<VoucherResponse> response) {
                    btnSaveVoucher.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(VoucherFormActivity.this, "Đã tạo voucher mới thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(VoucherFormActivity.this, "Lỗi backend: " + getErrorMsg(response), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<VoucherResponse> call, Throwable t) {
                    btnSaveVoucher.setEnabled(true);
                    Toast.makeText(VoucherFormActivity.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Edit mode
            ApiClient.getApiService().updateVoucher(shopId, voucherId, request).enqueue(new Callback<VoucherResponse>() {
                @Override
                public void onResponse(Call<VoucherResponse> call, Response<VoucherResponse> response) {
                    btnSaveVoucher.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(VoucherFormActivity.this, "Đã cập nhật voucher thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(VoucherFormActivity.this, "Lỗi backend: " + getErrorMsg(response), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<VoucherResponse> call, Throwable t) {
                    btnSaveVoucher.setEnabled(true);
                    Toast.makeText(VoucherFormActivity.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getErrorMsg(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                if (errorJson.contains("\"message\":")) {
                    int startIdx = errorJson.indexOf("\"message\":\"") + 11;
                    int endIdx = errorJson.indexOf("\"", startIdx);
                    return errorJson.substring(startIdx, endIdx);
                }
                return errorJson;
            }
        } catch (Exception ignored) {}
        return "Mã lỗi " + response.code();
    }

    private String formatIsoDateTimeToDisplay(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) return "Chọn thời gian";
        try {
            String clean = isoDateTime;
            if (clean.contains(".")) {
                clean = clean.substring(0, clean.indexOf("."));
            }
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            Date date = parser.parse(clean);
            return date != null ? formatter.format(date) : "Chọn thời gian";
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0đ";
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(value) + "đ";
    }
}
