package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.foodorderingapp.model.response.VoucherResponse;
import com.foodorderingapp.ui.adapter.CartShopAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.CartViewModel;
import com.foodorderingapp.viewmodel.OrderViewModel;

import java.util.ArrayList;
import java.util.List;

public class StudentCartFragment extends Fragment {

    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private CartShopAdapter cartShopAdapter;
    private RecyclerView rvCartShops;
    private TextView tvCartEmpty;
    private AutoCompleteTextView acBuilding;
    private AutoCompleteTextView acDropOff;
    private AutoCompleteTextView acVoucherCode;
    private final List<BuildingResponse> buildingOptions = new ArrayList<>();
    private final List<DropOffPointResponse> dropOffOptions = new ArrayList<>();
    private final List<VoucherResponse> voucherOptions = new ArrayList<>();
    private BuildingResponse selectedBuilding;
    private DropOffPointResponse selectedDropOffPoint;
    private boolean hasCartItems = false;

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
        Button btnCheckout = view.findViewById(R.id.btnCheckout);
        tvCartEmpty = view.findViewById(R.id.tvCartEmpty);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        setupCheckoutDropdowns();
        setupCartList();
        observeCart();
        observeCheckoutSupportData();
        observeActions();

        btnCheckout.setOnClickListener(v -> checkout());
        orderViewModel.loadBuildings();
        cartViewModel.loadCart();
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
        setupDropdown(acVoucherCode);

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
        view.setOnClickListener(v -> view.showDropDown());
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                view.showDropDown();
            }
        });
    }

    private void observeCart() {
        cartViewModel.getCartData().observe(getViewLifecycleOwner(), cart -> {
            if (cart == null) {
                showEmpty("Khong tai duoc gio hang");
                ToastUtils.error(getContext(), "Khong tai duoc gio hang");
                return;
            }

            hasCartItems = cart.getShops() != null && !cart.getShops().isEmpty();
            cartShopAdapter.submitList(cart.getShops());
            if (hasCartItems) {
                showList();
                loadVouchersForFirstShop(cart.getShops());
            } else {
                showEmpty("Gio hang dang trong");
                bindVouchers(null);
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

    private void observeActions() {
        orderViewModel.getCheckoutResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                ToastUtils.success(getContext(), "Dat hang thanh cong");
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
        ArrayAdapter<VoucherResponse> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                voucherOptions
        );
        acVoucherCode.setAdapter(adapter);
    }

    private void loadVouchersForFirstShop(List<ShopCartResponse> shops) {
        if (shops == null || shops.isEmpty() || shops.get(0).getShopId() == null) {
            bindVouchers(null);
            return;
        }
        orderViewModel.loadVouchers(shops.get(0).getShopId());
    }

    private void checkout() {
        if (!hasCartItems) {
            ToastUtils.info(getContext(), "Gio hang dang trong");
            return;
        }

        String building = acBuilding.getText().toString().trim();
        String dropOff = acDropOff.getText().toString().trim();

        if (building.isEmpty()) {
            ToastUtils.error(getContext(), "Vui long chon hoac nhap toa nha");
            return;
        }

        if (dropOff.isEmpty()) {
            ToastUtils.error(getContext(), "Vui long chon hoac nhap diem nhan hang");
            return;
        }

        BuildingResponse buildingMatch = findBuilding(building);
        DropOffPointResponse dropOffMatch = findDropOffPoint(dropOff);
        String voucherCode = resolveVoucherCode(acVoucherCode.getText().toString().trim());

        orderViewModel.checkout(
                building,
                dropOff,
                buildingMatch != null ? buildingMatch.getId() : null,
                dropOffMatch != null ? dropOffMatch.getId() : null,
                voucherCode
        );
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
}
