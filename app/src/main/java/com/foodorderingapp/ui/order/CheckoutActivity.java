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
import com.foodorderingapp.model.response.VoucherResponse;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.CartViewModel;
import com.foodorderingapp.viewmodel.OrderViewModel;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
    private MaterialButton btnConfirmOrder;

    private final List<BuildingResponse> buildingOptions = new ArrayList<>();
    private final List<VoucherResponse> voucherOptions = new ArrayList<>();
    private String shopId;
    private ShopCartResponse checkoutShop;
    private BuildingResponse selectedBuilding;
    private VoucherResponse appliedVoucher;
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

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        observeData();

        cartViewModel.loadCart();
        orderViewModel.loadBuildings();
        orderViewModel.loadVouchers(shopId);
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
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        findViewById(R.id.btnApplyVoucher).setOnClickListener(v -> applyVoucherFromInput(true));
        btnConfirmOrder.setOnClickListener(v -> confirmCheckout());
    }

    private void setupInputs() {
        setupDropdown(acBuilding);
        setupDropdown(acVoucher);

        acBuilding.setOnItemClickListener((parent, view, position, id) ->
                selectedBuilding = (BuildingResponse) parent.getItemAtPosition(position));

        acVoucher.setOnItemClickListener((parent, view, position, id) -> {
            Object selected = parent.getItemAtPosition(position);
            if (selected instanceof VoucherResponse) {
                applyVoucher((VoucherResponse) selected, true);
            }
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
                    clearAppliedVoucher();
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
        orderViewModel.getVouchers().observe(this, this::bindVouchers);
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

    private void bindVouchers(List<VoucherResponse> vouchers) {
        voucherOptions.clear();
        if (vouchers != null) {
            voucherOptions.addAll(vouchers);
        }
        acVoucher.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                voucherOptions
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
                appliedVoucher != null ? appliedVoucher.getCode() : null
        );
    }

    private boolean applyVoucherFromInput(boolean showSuccessMessage) {
        String value = acVoucher.getText().toString().trim();
        if (value.isEmpty()) {
            clearAppliedVoucher();
            return true;
        }

        if (appliedVoucher != null && sameText(appliedVoucherCode, value)) {
            return true;
        }

        VoucherResponse voucher = findVoucher(value);
        if (voucher == null) {
            clearAppliedVoucher();
            ToastUtils.error(this, "Mã giảm giá không hợp lệ");
            return false;
        }
        return applyVoucher(voucher, showSuccessMessage);
    }

    private boolean applyVoucher(VoucherResponse voucher, boolean showSuccessMessage) {
        VoucherCalculation calculation = calculateVoucher(voucher);
        if (!calculation.valid) {
            clearAppliedVoucher();
            ToastUtils.error(this, calculation.message);
            return false;
        }

        appliedVoucher = voucher;
        appliedVoucherCode = voucher.getCode();
        acVoucher.setText(voucher.getCode(), false);
        acVoucher.setSelection(acVoucher.getText().length());
        bindVoucherPreview(voucher, calculation);
        if (showSuccessMessage) {
            ToastUtils.success(this, "Đã áp dụng mã giảm giá");
        }
        return true;
    }

    private void refreshAppliedVoucher() {
        if (appliedVoucher == null) {
            return;
        }
        VoucherCalculation calculation = calculateVoucher(appliedVoucher);
        if (calculation.valid) {
            bindVoucherPreview(appliedVoucher, calculation);
        } else {
            clearAppliedVoucher();
        }
    }

    private void bindVoucherPreview(VoucherResponse voucher, VoucherCalculation calculation) {
        layoutVoucherPreview.setVisibility(View.VISIBLE);
        tvVoucherApplied.setText("Đã áp dụng: " + voucher.getDisplayText());
        tvDiscount.setText("Giảm giá: -" + formatPrice(calculation.discount));
        tvTotal.setText("Tổng thanh toán: " + formatPrice(calculation.totalAfterDiscount));
    }

    private void clearAppliedVoucher() {
        appliedVoucher = null;
        appliedVoucherCode = null;
        layoutVoucherPreview.setVisibility(View.GONE);
    }

    private VoucherResponse findVoucher(String value) {
        for (VoucherResponse voucher : voucherOptions) {
            if (voucher != null && (sameText(voucher.getCode(), value)
                    || sameText(voucher.getDisplayText(), value))) {
                return voucher;
            }
        }
        return null;
    }

    private VoucherCalculation calculateVoucher(VoucherResponse voucher) {
        if (checkoutShop == null) {
            return VoucherCalculation.invalid("Đơn hàng chưa sẵn sàng");
        }

        double subtotal = calculateSubtotal();
        double applicableTotal = calculateApplicableTotal(voucher);
        if (applicableTotal <= 0D) {
            return VoucherCalculation.invalid("Mã giảm giá không áp dụng cho món trong giỏ");
        }

        double minOrder = decimalValue(voucher.getMinOrderValue());
        if (applicableTotal < minOrder) {
            return VoucherCalculation.invalid("Đơn hàng chưa đạt giá trị tối thiểu");
        }

        double discountValue = decimalValue(voucher.getDiscountValue());
        double discount;
        if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = applicableTotal * discountValue / 100D;
            double maxDiscount = decimalValue(voucher.getMaxDiscountValue());
            if (maxDiscount > 0D) {
                discount = Math.min(discount, maxDiscount);
            }
        } else {
            discount = discountValue;
        }

        discount = Math.min(discount, subtotal);
        return VoucherCalculation.valid(discount, subtotal - discount);
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

    private double calculateApplicableTotal(VoucherResponse voucher) {
        if (!"SPECIFIC_FOODS".equalsIgnoreCase(voucher.getApplyType())) {
            return calculateSubtotal();
        }

        double total = 0D;
        for (CartItemResponse item : checkoutShop.getItems()) {
            if (item != null && voucherAppliesToFood(voucher, item.getFoodId())) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    private boolean voucherAppliesToFood(VoucherResponse voucher, String foodId) {
        if (voucher.getFoodIds() == null || foodId == null) {
            return false;
        }
        for (UUID id : voucher.getFoodIds()) {
            if (id != null && sameText(id.toString(), foodId)) {
                return true;
            }
        }
        return false;
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

    private double decimalValue(BigDecimal value) {
        return value == null ? 0D : value.doubleValue();
    }

    private String formatPrice(double price) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(price) + "đ";
    }

    private boolean sameText(String first, String second) {
        return first != null && second != null
                && first.trim().equalsIgnoreCase(second.trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class VoucherCalculation {
        final boolean valid;
        final String message;
        final double discount;
        final double totalAfterDiscount;

        private VoucherCalculation(boolean valid, String message, double discount,
                                   double totalAfterDiscount) {
            this.valid = valid;
            this.message = message;
            this.discount = discount;
            this.totalAfterDiscount = totalAfterDiscount;
        }

        static VoucherCalculation valid(double discount, double totalAfterDiscount) {
            return new VoucherCalculation(true, null, discount, totalAfterDiscount);
        }

        static VoucherCalculation invalid(String message) {
            return new VoucherCalculation(false, message, 0D, 0D);
        }
    }
}
