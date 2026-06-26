package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.CartItemResponse;
import com.foodorderingapp.model.response.DropOffPointResponse;
import com.foodorderingapp.model.response.ShopCartResponse;
import com.foodorderingapp.model.response.UserProfileResponse;
import com.foodorderingapp.model.response.VoucherResponse;
import com.foodorderingapp.ui.adapter.CartShopAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.CartViewModel;
import com.foodorderingapp.viewmodel.OrderViewModel;
import com.foodorderingapp.viewmodel.StudentProfileViewModel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class StudentCartFragment extends Fragment {

    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private StudentProfileViewModel studentProfileViewModel;
    private CartShopAdapter cartShopAdapter;
    private RecyclerView rvCartShops;
    private TextView tvCartEmpty;
    private TextView btnEditDeliveryInfo;
    private LinearLayout layoutVoucherPreview;
    private TextView tvVoucherApplied;
    private TextView tvOrderSubtotal;
    private TextView tvOrderDiscount;
    private TextView tvOrderTotalAfterDiscount;
    private AutoCompleteTextView acBuilding;
    private AutoCompleteTextView acDropOff;
    private AutoCompleteTextView acVoucherCode;
    private final List<ShopCartResponse> cartShops = new ArrayList<>();
    private final List<BuildingResponse> buildingOptions = new ArrayList<>();
    private final List<DropOffPointResponse> dropOffOptions = new ArrayList<>();
    private final List<VoucherResponse> voucherOptions = new ArrayList<>();
    private BuildingResponse selectedBuilding;
    private DropOffPointResponse selectedDropOffPoint;
    private UserProfileResponse deliveryProfile;
    private VoucherResponse appliedVoucher;
    private String voucherShopId;
    private String appliedVoucherShopId;
    private String appliedVoucherCode;
    private boolean hasCartItems = false;
    private boolean deliveryEditEnabled = false;
    private boolean hasPrefilledDelivery = false;

    public StudentCartFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCartShops = view.findViewById(R.id.rvCartShops);
        acBuilding = view.findViewById(R.id.etCheckoutBuilding);
        acDropOff = view.findViewById(R.id.etCheckoutDropOff);
        acVoucherCode = view.findViewById(R.id.etVoucherCode);
        btnEditDeliveryInfo = view.findViewById(R.id.btnEditDeliveryInfo);
        layoutVoucherPreview = view.findViewById(R.id.layoutVoucherPreview);
        tvVoucherApplied = view.findViewById(R.id.tvVoucherApplied);
        tvOrderSubtotal = view.findViewById(R.id.tvOrderSubtotal);
        tvOrderDiscount = view.findViewById(R.id.tvOrderDiscount);
        tvOrderTotalAfterDiscount = view.findViewById(R.id.tvOrderTotalAfterDiscount);
        Button btnCheckout = view.findViewById(R.id.btnCheckout);
        tvCartEmpty = view.findViewById(R.id.tvCartEmpty);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        studentProfileViewModel = new ViewModelProvider(this).get(StudentProfileViewModel.class);

        setupCheckoutDropdowns();
        setupCartList();
        setupDeliveryEditing();
        observeCart();
        observeCheckoutSupportData();
        observeDeliveryProfile();
        observeActions();

        btnCheckout.setOnClickListener(v -> checkout());
        orderViewModel.loadBuildings();
        studentProfileViewModel.loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cartViewModel != null) {
            cartViewModel.loadCart();
        }
    }

    private void setupCartList() {
        cartShopAdapter = new CartShopAdapter();
        cartShopAdapter.setOnQuantityChangeListener((item, newQuantity) ->
                cartViewModel.updateCartItemQuantity(item.getId(), newQuantity)
        );
        cartShopAdapter.setOnDeleteClickListener(this::confirmDeleteCartItem);
        cartShopAdapter.setOnClearShopCartListener(this::confirmClearShopCart);
        cartShopAdapter.setOnCheckoutClickListener(this::checkout);

        rvCartShops.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartShops.setAdapter(cartShopAdapter);
        rvCartShops.setNestedScrollingEnabled(false);
    }

    private void setupCheckoutDropdowns() {
        setupDropdown(acBuilding);
        setupDropdown(acDropOff);
        setupVoucherInput();

        acBuilding.setOnItemClickListener((parent, view, position, id) -> {
            selectedBuilding = (BuildingResponse) parent.getItemAtPosition(position);
            selectedDropOffPoint = null;
            acDropOff.setText("");
            bindDropOffPoints(null);
            if (selectedBuilding != null && selectedBuilding.getId() != null) {
                orderViewModel.loadDropOffPoints(selectedBuilding.getId());
            }
        });

        acDropOff.setOnItemClickListener((parent, view, position, id) ->
                selectedDropOffPoint = (DropOffPointResponse) parent.getItemAtPosition(position)
        );
    }

    private void setupDropdown(AutoCompleteTextView view) {
        view.setThreshold(0);
        view.setOnClickListener(v -> {
            if (view.isFocusable()) {
                view.showDropDown();
            }
        });
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && view.isFocusable()) {
                view.showDropDown();
            }
        });
    }

    private void setupVoucherInput() {
        acVoucherCode.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterPressed = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            if (actionId == EditorInfo.IME_ACTION_DONE || enterPressed) {
                applyVoucherFromInput();
                return true;
            }
            return false;
        });

        acVoucherCode.addTextChangedListener(new TextWatcher() {
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

    private void setupDeliveryEditing() {
        setDeliveryEditEnabled(false);
        btnEditDeliveryInfo.setOnClickListener(v -> setDeliveryEditEnabled(!deliveryEditEnabled));
    }

    private void setDeliveryEditEnabled(boolean enabled) {
        deliveryEditEnabled = enabled;
        btnEditDeliveryInfo.setText(enabled ? "Xong" : "Sửa");
        setAddressFieldEditable(acBuilding, enabled);
        setAddressFieldEditable(acDropOff, enabled);
    }

    private void setAddressFieldEditable(AutoCompleteTextView field, boolean enabled) {
        field.setFocusable(enabled);
        field.setFocusableInTouchMode(enabled);
        field.setCursorVisible(enabled);
        field.setClickable(enabled);
        field.setLongClickable(enabled);
    }

    private void observeCart() {
        cartViewModel.getCartData().observe(getViewLifecycleOwner(), cart -> {
            if (cart == null) {
                showEmpty("Khong tai duoc gio hang");
                ToastUtils.error(getContext(), "Khong tai duoc gio hang");
                return;
            }

            cartShops.clear();
            if (cart.getShops() != null) {
                cartShops.addAll(cart.getShops());
            }

            hasCartItems = !cartShops.isEmpty();
            cartShopAdapter.submitList(cartShops);
            if (hasCartItems) {
                showList();
                if (cartShops.size() == 1) {
                    loadVouchersForShop(cartShops.get(0));
                    updateVoucherPreviewIfApplied();
                } else {
                    voucherShopId = null;
                    acVoucherCode.setText("");
                    bindVouchers(null);
                    clearAppliedVoucher(false);
                }
            } else {
                showEmpty("Gio hang dang trong");
                voucherShopId = null;
                acVoucherCode.setText("");
                bindVouchers(null);
                clearAppliedVoucher(false);
            }
        });
    }

    private void observeCheckoutSupportData() {
        orderViewModel.getBuildings().observe(getViewLifecycleOwner(), this::bindBuildings);
        orderViewModel.getDropOffPoints().observe(getViewLifecycleOwner(), this::bindDropOffPoints);
        orderViewModel.getVouchers().observe(getViewLifecycleOwner(), this::bindVouchers);
        orderViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                ToastUtils.info(getContext(), message);
            }
        });
    }

    private void observeDeliveryProfile() {
        studentProfileViewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            deliveryProfile = profile;
            prefillDeliveryFromProfile();
        });
    }

    private void observeActions() {
        orderViewModel.getCheckoutResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                ToastUtils.success(getContext(), "Dat hang thanh cong");
                clearAppliedVoucher(true);
                cartViewModel.loadCart();
                openOrdersTab();
            } else if (success != null) {
                ToastUtils.error(getContext(), "Dat hang that bai");
            }
        });

        cartViewModel.getUpdateQuantityResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                cartViewModel.loadCart();
            } else if (success != null) {
                ToastUtils.error(getContext(), "Khong cap nhat duoc so luong");
            }
        });

        cartViewModel.getDeleteItemResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                ToastUtils.success(getContext(), "Da xoa mon khoi gio");
                cartViewModel.loadCart();
            } else if (success != null) {
                ToastUtils.error(getContext(), "Khong the xoa mon");
            }
        });

        cartViewModel.getClearShopResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                ToastUtils.success(getContext(), "Da xoa gio cua quan");
                cartViewModel.loadCart();
            } else if (success != null) {
                ToastUtils.error(getContext(), "Khong the xoa gio cua quan");
            }
        });
    }

    private void bindBuildings(List<BuildingResponse> buildings) {
        buildingOptions.clear();
        if (buildings != null) {
            buildingOptions.addAll(buildings);
        }
        ArrayAdapter<BuildingResponse> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                buildingOptions
        );
        acBuilding.setAdapter(adapter);
        prefillDeliveryFromProfile();
    }

    private void bindDropOffPoints(List<DropOffPointResponse> dropOffPoints) {
        dropOffOptions.clear();
        if (dropOffPoints != null) {
            dropOffOptions.addAll(dropOffPoints);
        }
        ArrayAdapter<DropOffPointResponse> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                dropOffOptions
        );
        acDropOff.setAdapter(adapter);
    }

    private void bindVouchers(List<VoucherResponse> vouchers) {
        voucherOptions.clear();
        if (vouchers != null) {
            voucherOptions.addAll(vouchers);
        }
        updateVoucherPreviewIfApplied();
    }

    private void prefillDeliveryFromProfile() {
        if (hasPrefilledDelivery || deliveryEditEnabled || deliveryProfile == null) {
            return;
        }

        BuildingResponse profileBuilding = findBuildingById(deliveryProfile.getBuildingId());
        if (profileBuilding == null) {
            profileBuilding = findBuilding(deliveryProfile.getBuildingName());
        }

        if (profileBuilding != null) {
            selectedBuilding = profileBuilding;
            acBuilding.setText(profileBuilding.getName(), false);
            if (profileBuilding.getId() != null) {
                orderViewModel.loadDropOffPoints(profileBuilding.getId());
            }
            hasPrefilledDelivery = true;
        } else if (!isBlank(deliveryProfile.getBuildingName())) {
            acBuilding.setText(deliveryProfile.getBuildingName(), false);
            hasPrefilledDelivery = true;
        }
    }

    private void loadVouchersForShop(ShopCartResponse shop) {
        if (shop == null || shop.getShopId() == null) {
            voucherShopId = null;
            bindVouchers(null);
            return;
        }
        if (voucherShopId != null && !sameText(voucherShopId, shop.getShopId())) {
            clearAppliedVoucher(false);
        }
        voucherShopId = shop.getShopId();
        orderViewModel.loadVouchers(shop.getShopId());
    }

    private void checkout() {
        if (!hasCartItems) {
            ToastUtils.info(getContext(), "Gio hang dang trong");
            return;
        }

        if (cartShops.size() != 1) {
            ToastUtils.info(getContext(), "Vui long thanh toan tung quan trong gio hang");
            return;
        }

        checkout(cartShops.get(0));
    }

    private void checkout(ShopCartResponse shop) {
        if (shop == null || shop.getShopId() == null || shop.getShopId().trim().isEmpty()) {
            ToastUtils.error(getContext(), "Khong xac dinh duoc quan can thanh toan");
            return;
        }

        List<String> cartItemIds = collectCartItemIds(shop);
        if (cartItemIds.isEmpty()) {
            ToastUtils.info(getContext(), "Gio cua quan nay dang trong");
            return;
        }

        String building = acBuilding.getText().toString().trim();
        String dropOff = acDropOff.getText().toString().trim();

        if (building.isEmpty()) {
            ToastUtils.error(getContext(), "Vui long chon toa nha");
            return;
        }

        BuildingResponse buildingMatch = findBuilding(building);

        if (buildingMatch == null || buildingMatch.getId() == null) {
            ToastUtils.error(getContext(), "Vui long chon toa nha trong danh sach");
            return;
        }

        DropOffPointResponse dropOffMatch = null;
        if (!dropOff.isEmpty()) {
            dropOffMatch = findDropOffPoint(dropOff);
        }

        if (!dropOff.isEmpty() && (dropOffMatch == null || dropOffMatch.getId() == null)) {
            ToastUtils.error(getContext(), "Vui long chon diem nhan hang trong danh sach");
            return;
        }

        String voucherText = acVoucherCode.getText().toString().trim();
        String voucherCode = null;
        if (!voucherText.isEmpty()) {
            if (appliedVoucher == null || !sameText(appliedVoucherCode, voucherText)) {
                ToastUtils.info(getContext(), "Bam Enter de ap dung ma giam gia truoc khi dat hang");
                return;
            }
            if (!sameText(appliedVoucherShopId, shop.getShopId())) {
                ToastUtils.info(getContext(), "Ma giam gia khong ap dung cho quan dang thanh toan");
                return;
            }
            voucherCode = appliedVoucher.getCode();
        }

        orderViewModel.checkout(
                shop.getShopId(),
                cartItemIds,
                buildingMatch.getId(),
                dropOffMatch != null ? dropOffMatch.getId() : null,
                voucherCode
        );
    }

    private List<String> collectCartItemIds(ShopCartResponse shop) {
        List<String> ids = new ArrayList<>();
        if (shop.getItems() == null) {
            return ids;
        }
        for (CartItemResponse item : shop.getItems()) {
            if (item != null && item.getId() != null && !item.getId().trim().isEmpty()) {
                ids.add(item.getId());
            }
        }
        return ids;
    }

    private void applyVoucherFromInput() {
        String code = acVoucherCode.getText().toString().trim();
        if (code.isEmpty()) {
            clearAppliedVoucher(false);
            return;
        }

        ShopCartResponse shop = getSingleCheckoutShop();
        if (shop == null) {
            ToastUtils.info(getContext(), "Chi ap dung ma giam gia khi gio hang co mot quan");
            return;
        }

        if (!sameText(voucherShopId, shop.getShopId())) {
            ToastUtils.info(getContext(), "Dang tai ma giam gia cua quan");
            return;
        }

        VoucherResponse voucher = findVoucherByCode(code);
        if (voucher == null) {
            clearAppliedVoucher(false);
            ToastUtils.error(getContext(), "Ma giam gia khong hop le");
            return;
        }

        VoucherCalculation calculation = calculateVoucher(shop, voucher);
        if (!calculation.valid) {
            clearAppliedVoucher(false);
            ToastUtils.error(getContext(), calculation.message);
            return;
        }

        appliedVoucher = voucher;
        appliedVoucherShopId = shop.getShopId();
        appliedVoucherCode = voucher.getCode();
        acVoucherCode.setText(voucher.getCode());
        acVoucherCode.setSelection(acVoucherCode.getText().length());
        bindVoucherPreview(voucher, calculation);
    }

    private void updateVoucherPreviewIfApplied() {
        if (appliedVoucher == null || appliedVoucherShopId == null) {
            return;
        }

        ShopCartResponse shop = findCartShopById(appliedVoucherShopId);
        if (shop == null) {
            clearAppliedVoucher(false);
            return;
        }

        VoucherCalculation calculation = calculateVoucher(shop, appliedVoucher);
        if (!calculation.valid) {
            clearAppliedVoucher(false);
            return;
        }
        bindVoucherPreview(appliedVoucher, calculation);
    }

    private void bindVoucherPreview(VoucherResponse voucher, VoucherCalculation calculation) {
        layoutVoucherPreview.setVisibility(View.VISIBLE);
        tvVoucherApplied.setText("Đã áp dụng: " + voucher.getDisplayText());
        tvOrderSubtotal.setText("Tạm tính: " + formatPrice(calculation.subtotal));
        tvOrderDiscount.setText("Giảm giá: -" + formatPrice(calculation.discount));
        tvOrderTotalAfterDiscount.setText("Cần thanh toán: " + formatPrice(calculation.totalAfterDiscount));
    }

    private void clearAppliedVoucher(boolean clearInput) {
        appliedVoucher = null;
        appliedVoucherShopId = null;
        appliedVoucherCode = null;
        if (layoutVoucherPreview != null) {
            layoutVoucherPreview.setVisibility(View.GONE);
        }
        if (clearInput && acVoucherCode != null) {
            acVoucherCode.setText("");
        }
    }

    private VoucherResponse findVoucherByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (VoucherResponse voucher : voucherOptions) {
            if (voucher != null && sameText(voucher.getCode(), code.trim())) {
                return voucher;
            }
        }
        return null;
    }

    private VoucherCalculation calculateVoucher(ShopCartResponse shop, VoucherResponse voucher) {
        double subtotal = calculateShopSubtotal(shop);
        double applicableTotal = calculateApplicableTotal(shop, voucher);
        if (applicableTotal <= 0D) {
            return VoucherCalculation.invalid("Ma giam gia khong ap dung cho mon trong gio");
        }

        double minOrder = decimalValue(voucher.getMinOrderValue());
        if (applicableTotal < minOrder) {
            return VoucherCalculation.invalid("Don hang chua dat gia tri toi thieu");
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
        return VoucherCalculation.valid(subtotal, discount, subtotal - discount);
    }

    private double calculateShopSubtotal(ShopCartResponse shop) {
        double total = 0D;
        if (shop == null || shop.getItems() == null) {
            return total;
        }
        for (CartItemResponse item : shop.getItems()) {
            if (item != null) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    private double calculateApplicableTotal(ShopCartResponse shop, VoucherResponse voucher) {
        if (!"SPECIFIC_FOODS".equalsIgnoreCase(voucher.getApplyType())) {
            return calculateShopSubtotal(shop);
        }

        double total = 0D;
        if (shop == null || shop.getItems() == null) {
            return total;
        }
        for (CartItemResponse item : shop.getItems()) {
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

    private ShopCartResponse getSingleCheckoutShop() {
        return cartShops.size() == 1 ? cartShops.get(0) : null;
    }

    private ShopCartResponse findCartShopById(String shopId) {
        for (ShopCartResponse shop : cartShops) {
            if (shop != null && sameText(shop.getShopId(), shopId)) {
                return shop;
            }
        }
        return null;
    }

    private BuildingResponse findBuilding(String name) {
        if (selectedBuilding != null && sameText(selectedBuilding.getName(), name)) {
            return selectedBuilding;
        }
        for (BuildingResponse building : buildingOptions) {
            if (sameText(building.getName(), name)) {
                return building;
            }
        }
        return null;
    }

    private BuildingResponse findBuildingById(String id) {
        if (id == null) {
            return null;
        }
        for (BuildingResponse building : buildingOptions) {
            if (sameText(building.getId(), id)) {
                return building;
            }
        }
        return null;
    }

    private DropOffPointResponse findDropOffPoint(String name) {
        if (selectedDropOffPoint != null && sameText(selectedDropOffPoint.getName(), name)) {
            return selectedDropOffPoint;
        }
        for (DropOffPointResponse dropOffPoint : dropOffOptions) {
            if (sameText(dropOffPoint.getName(), name)) {
                return dropOffPoint;
            }
        }
        return null;
    }

    private String resolveVoucherCode(String value) {
        if (value.isEmpty()) {
            return null;
        }
        for (VoucherResponse voucher : voucherOptions) {
            if (sameText(voucher.getDisplayText(), value) || sameText(voucher.getCode(), value)) {
                return voucher.getCode();
            }
        }
        int dashIndex = value.indexOf(" - ");
        return dashIndex > 0 ? value.substring(0, dashIndex).trim() : value;
    }

    private boolean sameText(String first, String second) {
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private double decimalValue(BigDecimal value) {
        return value == null ? 0D : value.doubleValue();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

    private void confirmDeleteCartItem(CartItemResponse item) {
        if (getContext() == null || item == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Xoa mon")
                .setMessage("Ban muon xoa mon nay khoi gio hang?")
                .setNegativeButton("Huy", null)
                .setPositiveButton("Xoa", (dialog, which) -> cartViewModel.deleteCartItem(item.getId()))
                .show();
    }

    private void confirmClearShopCart(ShopCartResponse shop) {
        if (getContext() == null || shop == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Xoa gio cua quan")
                .setMessage("Ban muon xoa tat ca mon cua quan nay?")
                .setNegativeButton("Huy", null)
                .setPositiveButton("Xoa", (dialog, which) -> cartViewModel.clearShopCart(shop.getShopId()))
                .show();
    }

    private void showList() {
        rvCartShops.setVisibility(View.VISIBLE);
        tvCartEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        rvCartShops.setVisibility(View.GONE);
        tvCartEmpty.setVisibility(View.VISIBLE);
        tvCartEmpty.setText(message);
    }

    private void openOrdersTab() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("USER_ROLE", "STUDENT");
        intent.putExtra("OPEN_TAB", "ORDERS");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private static class VoucherCalculation {
        final boolean valid;
        final String message;
        final double subtotal;
        final double discount;
        final double totalAfterDiscount;

        private VoucherCalculation(boolean valid, String message, double subtotal,
                                   double discount, double totalAfterDiscount) {
            this.valid = valid;
            this.message = message;
            this.subtotal = subtotal;
            this.discount = discount;
            this.totalAfterDiscount = totalAfterDiscount;
        }

        static VoucherCalculation valid(double subtotal, double discount, double totalAfterDiscount) {
            return new VoucherCalculation(true, null, subtotal, discount, totalAfterDiscount);
        }

        static VoucherCalculation invalid(String message) {
            return new VoucherCalculation(false, message, 0D, 0D, 0D);
        }
    }
}
