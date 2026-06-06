package com.foodorderingapp.ui.home.student;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.foodorderingapp.databinding.FragmentStudentHomeBinding;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;

import com.foodorderingapp.ui.adapter.CartShopAdapter;
import com.foodorderingapp.ui.adapter.ShopAdapter;
import com.foodorderingapp.viewmodel.CartViewModel;
import com.foodorderingapp.viewmodel.ShopViewModel;
import java.util.ArrayList;
import java.util.List;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.ui.adapter.FoodExploreAdapter;
import com.foodorderingapp.viewmodel.FoodViewModel;
import com.foodorderingapp.ui.shop.ShopDetailActivity;

public class StudentHomeFragment extends Fragment {
    private ShopViewModel shopViewModel;
    private ShopAdapter shopAdapter;
    private FragmentStudentHomeBinding binding;
    private FoodViewModel foodViewModel;
    private FoodExploreAdapter foodAdapter;
    private CartViewModel cartViewModel;
    private CartShopAdapter cartShopAdapter;
    public StudentHomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shopViewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        shopAdapter = new ShopAdapter();

        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);
        foodAdapter = new FoodExploreAdapter();

        binding.rvMainHomeList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMainHomeList.setNestedScrollingEnabled(false);
        binding.rvMainHomeList.setAdapter(shopAdapter);

        foodAdapter.setOnFoodClickListener(food -> {
            Intent intent = new Intent(requireContext(), ShopDetailActivity.class);
            intent.putExtra("SHOP_ID", food.getShopId());
            intent.putExtra("SHOP_NAME", food.getShopName());
            startActivity(intent);
        });
        
        shopAdapter.setOnShopClickListener(shop -> {
            Intent intent = new Intent(requireContext(), ShopDetailActivity.class);
            intent.putExtra("SHOP_ID", shop.getId());
            intent.putExtra("SHOP_NAME", shop.getName());
            intent.putExtra("SHOP_ADDRESS", shop.getAddress());
            intent.putExtra("SHOP_DESCRIPTION", shop.getDescription());
            intent.putExtra("SHOP_OPEN_TIME", shop.getOpenTime());
            intent.putExtra("SHOP_CLOSE_TIME", shop.getCloseTime());
            intent.putExtra("SHOP_DISPLAY_STATUS", shop.getDisplayStatus());
            startActivity(intent);
        });

        shopViewModel.getShopData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getContent() != null) {
                List<ShopResponse> approvedShops = new ArrayList<>();

                for (ShopResponse shop : response.getContent()) {
                    if ("APPROVED".equalsIgnoreCase(shop.getStatus()) && shop.isActive()) {
                        approvedShops.add(shop);
                    }
                }

                shopAdapter.submitList(approvedShops);
            } else {
                Toast.makeText(getContext(), "Không tải được danh sách quán", Toast.LENGTH_SHORT).show();
            }
        });

        shopViewModel.loadShops(null);

        foodViewModel.getFoodData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getContent() != null && !response.getContent().isEmpty()) {
                binding.tvHomeEmpty.setVisibility(View.GONE);
                binding.rvMainHomeList.setVisibility(View.VISIBLE);
                foodAdapter.submitList(response.getContent());
            } else {
                foodAdapter.submitList(null);
                binding.rvMainHomeList.setVisibility(View.GONE);
                binding.tvHomeEmpty.setVisibility(View.VISIBLE);
                binding.tvHomeEmpty.setText("Chưa có món ngon nào");
            }
        });
        setupTabListeners();
        selectTab(true);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        foodAdapter.setOnAddToCartClickListener(food -> {
            cartViewModel.addToCart(food.getId());
        });
        cartViewModel.getAddResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Thêm vào giỏ thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupTabListeners() {
        binding.tabRestaurants.setOnClickListener(v -> {
            selectTab(true);
            binding.tvHomeEmpty.setVisibility(View.GONE);
            binding.rvMainHomeList.setVisibility(View.VISIBLE);
            binding.rvMainHomeList.setAdapter(shopAdapter);
            shopViewModel.loadShops(null);
        });

        binding.tabDishes.setOnClickListener(v -> {
            selectTab(false);
            binding.tvHomeEmpty.setVisibility(View.GONE);
            binding.rvMainHomeList.setVisibility(View.VISIBLE);
            binding.rvMainHomeList.setAdapter(foodAdapter);
            foodViewModel.loadExploreFoods();
        });
    }

    private void selectTab(boolean isRestaurants) {
        if (isRestaurants) {
            // Select Restaurants
            binding.tvTabRestaurants.setTextColor(Color.parseColor("#FF7A21"));
            binding.indicatorRestaurants.setVisibility(View.VISIBLE);

            // Unselect Dishes
            binding.tvTabDishes.setTextColor(Color.parseColor("#555555"));
            binding.indicatorDishes.setVisibility(View.INVISIBLE);

        } else {
            // Select Dishes
            binding.tvTabDishes.setTextColor(Color.parseColor("#FF7A21"));
            binding.indicatorDishes.setVisibility(View.VISIBLE);

            // Unselect Restaurants
            binding.tvTabRestaurants.setTextColor(Color.parseColor("#555555"));
            binding.indicatorRestaurants.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
