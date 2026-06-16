package com.foodorderingapp.ui.home.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.CartItemResponse;
import com.foodorderingapp.model.response.ShopCartResponse;
import com.foodorderingapp.ui.adapter.CartShopAdapter;
import com.foodorderingapp.viewmodel.CartViewModel;
import com.foodorderingapp.viewmodel.OrderViewModel;

public class StudentCartFragment extends Fragment {

    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private CartShopAdapter cartShopAdapter;
    private RecyclerView rvCartShops;
    private TextView tvCartEmpty;
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
        EditText etBuilding = view.findViewById(R.id.etCheckoutBuilding);
        EditText etDropOff = view.findViewById(R.id.etCheckoutDropOff);
        Button btnCheckout = view.findViewById(R.id.btnCheckout);
        tvCartEmpty = view.findViewById(R.id.tvCartEmpty);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        cartShopAdapter = new CartShopAdapter();
        cartShopAdapter.setOnQuantityChangeListener((item, newQuantity) ->
                cartViewModel.updateCartItemQuantity(item.getId(), newQuantity)
        );
        cartShopAdapter.setOnDeleteClickListener(this::confirmDeleteCartItem);
        cartShopAdapter.setOnClearShopCartListener(this::confirmClearShopCart);
        cartShopAdapter.setOnCheckoutClickListener(() -> checkout(etBuilding, etDropOff));

        rvCartShops.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartShops.setAdapter(cartShopAdapter);
        rvCartShops.setNestedScrollingEnabled(false);

        cartViewModel.getCartData().observe(getViewLifecycleOwner(), cart -> {
            if (cart == null) {
                showEmpty("Không tải được giỏ hàng");
                Toast.makeText(getContext(), "Không tải được giỏ hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            hasCartItems = cart.getShops() != null && !cart.getShops().isEmpty();
            cartShopAdapter.submitList(cart.getShops());
            if (hasCartItems) {
                showList();
            } else {
                showEmpty("Giỏ hàng đang trống");
            }
        });

        orderViewModel.getCheckoutResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
                cartViewModel.loadCart();
                openOrdersTab();
            } else if (success != null) {
                Toast.makeText(getContext(), "Đặt hàng thất bại", Toast.LENGTH_SHORT).show();
            }
        });

        cartViewModel.getUpdateQuantityResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                cartViewModel.loadCart();
            } else if (success != null) {
                Toast.makeText(getContext(), "Không cập nhật được số lượng", Toast.LENGTH_SHORT).show();
            }
        });

        cartViewModel.getDeleteItemResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Đã xóa món khỏi giỏ", Toast.LENGTH_SHORT).show();
                cartViewModel.loadCart();
            } else if (success != null) {
                Toast.makeText(getContext(), "Không thể xóa món", Toast.LENGTH_SHORT).show();
            }
        });

        cartViewModel.getClearShopResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Đã xóa giỏ của quán", Toast.LENGTH_SHORT).show();
                cartViewModel.loadCart();
            } else if (success != null) {
                Toast.makeText(getContext(), "Không thể xóa giỏ của quán", Toast.LENGTH_SHORT).show();
            }
        });

        btnCheckout.setOnClickListener(v -> checkout(etBuilding, etDropOff));
        cartViewModel.loadCart();
    }

    private void checkout(EditText etBuilding, EditText etDropOff) {
        if (!hasCartItems) {
            Toast.makeText(getContext(), "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String building = etBuilding.getText().toString().trim();
        String dropOff = etDropOff.getText().toString().trim();

        if (building.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tòa nhà", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dropOff.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập điểm nhận hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        orderViewModel.checkout(building, dropOff);
    }

    private void confirmDeleteCartItem(CartItemResponse item) {
        if (getContext() == null || item == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa món")
                .setMessage("Bạn muốn xóa món này khỏi giỏ hàng?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> cartViewModel.deleteCartItem(item.getId()))
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
                .setPositiveButton("Xóa", (dialog, which) -> cartViewModel.clearShopCart(shop.getShopId()))
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
