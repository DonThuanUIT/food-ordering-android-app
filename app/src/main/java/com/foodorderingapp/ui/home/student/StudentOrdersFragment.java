package com.foodorderingapp.ui.home.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.ui.adapter.CartShopAdapter;
import com.foodorderingapp.viewmodel.CartViewModel;
import com.foodorderingapp.viewmodel.OrderViewModel;

public class StudentOrdersFragment extends Fragment {

    private CartViewModel cartViewModel;
    private CartShopAdapter cartShopAdapter;
    private OrderViewModel orderViewModel;

    private boolean hasCartItems = false;
    public StudentOrdersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvCartShops = view.findViewById(R.id.rvCartShops);
        EditText etBuilding = view.findViewById(R.id.etCheckoutBuilding);
        EditText etDropOff = view.findViewById(R.id.etCheckoutDropOff);
        Button btnCheckout = view.findViewById(R.id.btnCheckout);

        cartShopAdapter = new CartShopAdapter();
        rvCartShops.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartShops.setAdapter(cartShopAdapter);
        rvCartShops.setNestedScrollingEnabled(false);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        cartViewModel.getCartData().observe(getViewLifecycleOwner(), cart -> {
            if (cart == null) {
                Toast.makeText(getContext(), "Không tải được giỏ hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            hasCartItems = cart.getShops() != null && !cart.getShops().isEmpty();
            cartShopAdapter.submitList(cart.getShops());
        });

        cartViewModel.loadCart();

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderViewModel.getCheckoutResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
                cartViewModel.loadCart();
            } else {
                Toast.makeText(getContext(), "Đặt hàng thất bại", Toast.LENGTH_SHORT).show();
            }
        });

        btnCheckout.setOnClickListener(v -> {
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
        });
    }
}