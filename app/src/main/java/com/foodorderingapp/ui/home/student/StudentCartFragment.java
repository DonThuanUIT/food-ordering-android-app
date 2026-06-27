package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.CartItemResponse;
import com.foodorderingapp.model.response.ShopCartResponse;
import com.foodorderingapp.ui.adapter.CartShopAdapter;
import com.foodorderingapp.ui.order.CheckoutActivity;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.CartViewModel;

import java.util.ArrayList;
import java.util.List;

public class StudentCartFragment extends Fragment {

    private CartViewModel cartViewModel;
    private CartShopAdapter cartShopAdapter;
    private RecyclerView rvCartShops;
    private TextView tvCartEmpty;
    private final List<ShopCartResponse> cartShops = new ArrayList<>();

    public StudentCartFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCartShops = view.findViewById(R.id.rvCartShops);
        tvCartEmpty = view.findViewById(R.id.tvCartEmpty);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setupCartList();
        observeCart();
        observeActions();
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
                cartViewModel.updateCartItemQuantity(item.getId(), newQuantity));
        cartShopAdapter.setOnDeleteClickListener(this::confirmDeleteCartItem);
        cartShopAdapter.setOnClearShopCartListener(this::confirmClearShopCart);
        cartShopAdapter.setOnCheckoutClickListener(this::openCheckoutDetails);

        rvCartShops.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartShops.setAdapter(cartShopAdapter);
        rvCartShops.setNestedScrollingEnabled(false);
    }

    private void observeCart() {
        cartViewModel.getCartData().observe(getViewLifecycleOwner(), cart -> {
            if (cart == null) {
                showEmpty("Không tải được giỏ hàng");
                ToastUtils.error(getContext(), "Không tải được giỏ hàng");
                return;
            }

            cartShops.clear();
            if (cart.getShops() != null) {
                cartShops.addAll(cart.getShops());
            }
            cartShopAdapter.submitList(cartShops);

            if (cartShops.isEmpty()) {
                showEmpty("Giỏ hàng đang trống");
            } else {
                showList();
            }
        });
    }

    private void observeActions() {
        cartViewModel.getUpdateQuantityResult().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                cartViewModel.loadCart();
            } else if (success != null) {
                ToastUtils.error(getContext(), "Không cập nhật được số lượng");
            }
        });

        cartViewModel.getDeleteItemResult().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.success(getContext(), "Đã xóa món khỏi giỏ");
                cartViewModel.loadCart();
            } else if (success != null) {
                ToastUtils.error(getContext(), "Không thể xóa món");
            }
        });

        cartViewModel.getClearShopResult().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.success(getContext(), "Đã xóa giỏ của quán");
                cartViewModel.loadCart();
            } else if (success != null) {
                ToastUtils.error(getContext(), "Không thể xóa giỏ của quán");
            }
        });
    }

    private void openCheckoutDetails(ShopCartResponse shop) {
        if (shop == null || isBlank(shop.getShopId()) || shop.getItems() == null
                || shop.getItems().isEmpty()) {
            ToastUtils.info(getContext(), "Giỏ của quán này đang trống");
            return;
        }

        Intent intent = new Intent(requireContext(), CheckoutActivity.class);
        intent.putExtra(CheckoutActivity.EXTRA_SHOP_ID, shop.getShopId());
        startActivity(intent);
    }

    private void confirmDeleteCartItem(CartItemResponse item) {
        if (getContext() == null || item == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa món")
                .setMessage("Bạn muốn xóa món này khỏi giỏ hàng?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        cartViewModel.deleteCartItem(item.getId()))
                .show();
    }

    private void confirmClearShopCart(ShopCartResponse shop) {
        if (getContext() == null || shop == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa giỏ của quán")
                .setMessage("Bạn muốn xóa tất cả món của quán này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        cartViewModel.clearShopCart(shop.getShopId()))
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
