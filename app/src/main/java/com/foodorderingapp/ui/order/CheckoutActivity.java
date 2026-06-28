package com.foodorderingapp.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.CartItemResponse;
import com.foodorderingapp.model.response.ShopCartResponse;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.CartViewModel;
import com.foodorderingapp.viewmodel.OrderViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_SHOP_ID = "CHECKOUT_SHOP_ID";

    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private AutoCompleteTextView acBuilding;
    private AutoCompleteTextView acVoucher;
    private TextView tvShopName;
    private TextView tvItemCount;
    private TextView tvSubtotal;
    private View layoutVoucherPreview;
    private TextView tvVoucherApplied;
    private TextView tvDiscount;
    private TextView tvTotal;
    private MaterialButton btnApplyVoucher;
    private MaterialButton btnConfirmOrder;

    private TextView tvSummarySubtotal;
    private TextView tvSummaryShipping;
    private TextView tvSummaryTotal;
    private Double shopLat = null;
    private Double shopLng = null;

    private final List<BuildingResponse> buildingOptions = new ArrayList<>();
    private String shopId;
    private ShopCartResponse checkoutShop;
    private BuildingResponse selectedBuilding;
    private String appliedVoucherCode;
    private boolean submitting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        shopId = getIntent().getStringExtra(EXTRA_SHOP_ID);
        if (isBlank(shopId)) {
            ToastUtils.error(this, "Không xác định được quán cần đặt hàng");
            finish();
            return;
        }

        bindViews();
        setupInputs();
        loadShopCoordinates();

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        observeData();

        cartViewModel.loadCart();
        orderViewModel.loadBuildings();
    }

    private void bindViews() {
        findViewById(R.id.btnCheckoutBack).setOnClickListener(v -> finish());
        acBuilding = findViewById(R.id.etCheckoutBuilding);
        acVoucher = findViewById(R.id.etCheckoutVoucher);
        tvShopName = findViewById(R.id.tvCheckoutShopName);
        tvItemCount = findViewById(R.id.tvCheckoutItemCount);
        tvSubtotal = findViewById(R.id.tvCheckoutSubtotal);
        layoutVoucherPreview = findViewById(R.id.layoutCheckoutVoucherPreview);
        tvVoucherApplied = findViewById(R.id.tvCheckoutVoucherApplied);
        tvDiscount = findViewById(R.id.tvCheckoutDiscount);
        tvTotal = findViewById(R.id.tvCheckoutTotal);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        tvSummarySubtotal = findViewById(R.id.tvCheckoutSummarySubtotal);
        tvSummaryShipping = findViewById(R.id.tvCheckoutSummaryShipping);
        tvSummaryTotal = findViewById(R.id.tvCheckoutSummaryTotal);

        btnApplyVoucher.setOnClickListener(v -> handleVoucherButtonClick());
        btnConfirmOrder.setOnClickListener(v -> confirmCheckout());
    }

    private void setupInputs() {
        setupDropdown(acBuilding);

        acBuilding.setOnItemClickListener((parent, view, position, id) -> {
            selectedBuilding = (BuildingResponse) parent.getItemAtPosition(position);
            updatePriceCalculation();
        });

        acVoucher.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterPressed = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            if (actionId == EditorInfo.IME_ACTION_DONE || enterPressed) {
                applyVoucherFromInput(true);
                return true;
            }
            return false;
        });

        acVoucher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (appliedVoucherCode != null && !sameText(appliedVoucherCode, s.toString())) {
                    clearAppliedVoucher(false);
                }
            }
        });
    }

    private void setupDropdown(AutoCompleteTextView view) {
        view.setThreshold(0);
        view.setOnClickListener(v -> view.showDropDown());
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                view.showDropDown();
            }
        });
    }

    private void observeData() {
        cartViewModel.getCartData().observe(this, cart -> {
            if (cart == null || cart.getShops() == null) {
                ToastUtils.error(this, "Không tải được giỏ hàng");
                return;
            }

            checkoutShop = findShop(cart.getShops(), shopId);
            if (checkoutShop == null || checkoutShop.getItems() == null
                    || checkoutShop.getItems().isEmpty()) {
                ToastUtils.info(this, "Giỏ của quán này không còn món");
                finish();
                return;
            }

            bindOrderSummary();
            btnConfirmOrder.setEnabled(!submitting);
            refreshAppliedVoucher();
        });

        orderViewModel.getBuildings().observe(this, this::bindBuildings);
        orderViewModel.getMessage().observe(this, message -> {
            if (!isBlank(message)) {
                ToastUtils.info(this, message);
            }
        });

        orderViewModel.getCheckoutResult().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.success(this, "Đặt hàng thành công");
                openOrdersTab();
            } else if (success != null) {
                setSubmitting(false);
            }
        });
    }

    private void bindBuildings(List<BuildingResponse> buildings) {
        buildingOptions.clear();
        if (buildings != null) {
            buildingOptions.addAll(buildings);
        }
        acBuilding.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                buildingOptions
        ));
    }

    private void bindOrderSummary() {
        int totalItems = 0;
        for (CartItemResponse item : checkoutShop.getItems()) {
            if (item != null) {
                totalItems += item.getQuantity();
            }
        }
        tvShopName.setText(isBlank(checkoutShop.getShopName())
                ? "Quán ăn" : checkoutShop.getShopName());
        tvItemCount.setText(totalItems + " món");
        tvSubtotal.setText(formatPrice(calculateSubtotal()));
        updatePriceCalculation();
    }

    private void confirmCheckout() {
        if (submitting || checkoutShop == null) {
            return;
        }

        if (selectedBuilding == null || isBlank(selectedBuilding.getId())) {
            ToastUtils.error(this, "Vui lòng chọn tòa nhận");
            acBuilding.requestFocus();
            acBuilding.showDropDown();
            return;
        }

        if (!applyVoucherFromInput(false)) {
            return;
        }

        List<String> cartItemIds = collectCartItemIds();
        if (cartItemIds.isEmpty()) {
            ToastUtils.info(this, "Giỏ của quán này đang trống");
            return;
        }

        setSubmitting(true);
        orderViewModel.checkout(
                shopId,
                cartItemIds,
                selectedBuilding.getId(),
                appliedVoucherCode
        );
    }

    private void handleVoucherButtonClick() {
        String value = normalizeVoucherCode(acVoucher.getText().toString());
        if (!isBlank(appliedVoucherCode) && sameText(appliedVoucherCode, value)) {
            clearAppliedVoucher(true);
            ToastUtils.info(this, "Đã bỏ mã giảm giá");
            return;
        }
        applyVoucherFromInput(true);
    }

    private boolean applyVoucherFromInput(boolean showSuccessMessage) {
        String value = normalizeVoucherCode(acVoucher.getText().toString());
        if (value.isEmpty()) {
            clearAppliedVoucher(false);
            if (showSuccessMessage) {
                ToastUtils.error(this, "Vui lòng nhập mã giảm giá");
            }
            return true;
        }

        if (sameText(appliedVoucherCode, value)) {
            return true;
        }

        appliedVoucherCode = value;
        acVoucher.setText(value, false);
        acVoucher.setSelection(acVoucher.getText().length());
        bindVoucherPreview(value);
        if (showSuccessMessage) {
            ToastUtils.success(this, "Đã nhập mã giảm giá");
        }
        return true;
    }

    private void refreshAppliedVoucher() {
        if (isBlank(appliedVoucherCode)) {
            return;
        }
        bindVoucherPreview(appliedVoucherCode);
    }

    private void bindVoucherPreview(String voucherCode) {
        layoutVoucherPreview.setVisibility(View.VISIBLE);
        tvVoucherApplied.setText("Mã giảm giá: " + voucherCode);
        tvDiscount.setText("Mã sẽ được kiểm tra khi xác nhận đặt hàng");
        tvTotal.setText("Tạm tính: " + formatPrice(calculateSubtotal()));
        btnApplyVoucher.setText("Bỏ mã");
    }

    private void clearAppliedVoucher(boolean clearInput) {
        appliedVoucherCode = null;
        layoutVoucherPreview.setVisibility(View.GONE);
        btnApplyVoucher.setText("Áp dụng");
        if (clearInput) {
            acVoucher.setText("", false);
        }
    }

    private double calculateSubtotal() {
        double total = 0D;
        if (checkoutShop == null || checkoutShop.getItems() == null) {
            return total;
        }
        for (CartItemResponse item : checkoutShop.getItems()) {
            if (item != null) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    private List<String> collectCartItemIds() {
        List<String> ids = new ArrayList<>();
        for (CartItemResponse item : checkoutShop.getItems()) {
            if (item != null && !isBlank(item.getId())) {
                ids.add(item.getId());
            }
        }
        return ids;
    }

    private ShopCartResponse findShop(List<ShopCartResponse> shops, String targetShopId) {
        for (ShopCartResponse shop : shops) {
            if (shop != null && sameText(shop.getShopId(), targetShopId)) {
                return shop;
            }
        }
        return null;
    }

    private void setSubmitting(boolean value) {
        submitting = value;
        btnConfirmOrder.setEnabled(!value && checkoutShop != null);
        btnConfirmOrder.setText(value ? "Đang đặt hàng..." : "Xác nhận đặt hàng");
    }

    private void openOrdersTab() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ROLE", "STUDENT");
        intent.putExtra("OPEN_TAB", "ORDERS");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String formatPrice(double price) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(price) + "đ";
    }

    private String normalizeVoucherCode(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean sameText(String first, String second) {
        return first != null && second != null
                && first.trim().equalsIgnoreCase(second.trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void updatePriceCalculation() {
        double subtotal = calculateSubtotal();
        double shipping = calculateShippingFee();
        double total = subtotal + shipping;

        if (tvSummarySubtotal != null) {
            tvSummarySubtotal.setText(formatPrice(subtotal));
        }
        if (tvSummaryShipping != null) {
            tvSummaryShipping.setText(formatPrice(shipping));
        }
        if (tvSummaryTotal != null) {
            tvSummaryTotal.setText(formatPrice(total));
        }
    }

    private double calculateShippingFee() {
        if (selectedBuilding == null || selectedBuilding.getLatitude() == null || selectedBuilding.getLongitude() == null
                || shopLat == null || shopLng == null || shopLat == 0.0 || shopLng == 0.0) {
            return 5000.0; // Fallback to 5k
        }
        double distMeters = calculateDistance(shopLat, shopLng, selectedBuilding.getLatitude(), selectedBuilding.getLongitude());
        double distKm = distMeters / 1000.0;
        return distKm * 5000.0; // 5k per 1km
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        try {
            android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results);
            return results[0];
        } catch (Exception e) {
            double earthRadius = 6371000; // meters
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                       Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                       Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return earthRadius * c;
        }
    }

    private void loadShopCoordinates() {
        com.foodorderingapp.data.remote.api.ApiClient.getApiService().getShopDetail(shopId).enqueue(
                new retrofit2.Callback<com.foodorderingapp.model.response.ShopDetailResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.foodorderingapp.model.response.ShopDetailResponse> call,
                                   retrofit2.Response<com.foodorderingapp.model.response.ShopDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shopLat = response.body().getLatitude();
                    shopLng = response.body().getLongitude();
                    updatePriceCalculation();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.foodorderingapp.model.response.ShopDetailResponse> call, Throwable t) {
                // Fail silently, fallback values will be used
            }
        });
    }
}
