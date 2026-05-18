package com.foodorderingapp.ui.home.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class StudentOrdersFragment extends Fragment {

    private CartViewModel cartViewModel;
    private CartShopAdapter cartShopAdapter;

    public StudentOrdersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvCartShops = view.findViewById(R.id.rvCartShops);

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

            cartShopAdapter.submitList(cart.getShops());
        });

        cartViewModel.loadCart();
    }
}