package com.foodorderingapp.ui.home.student;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.foodorderingapp.model.response.FoodExploreResponse;
import com.foodorderingapp.ui.adapter.ShopAdapter;
import com.foodorderingapp.viewmodel.CartViewModel;
import com.foodorderingapp.viewmodel.ShopViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final List<FoodExploreResponse> exploreFoods = new ArrayList<>();
    private Runnable pendingSearchRunnable;
    private boolean showingRestaurants = true;
    private String searchKeyword = "";
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
                if (showingRestaurants) {
                    if (approvedShops.isEmpty()) {
                        showEmpty(searchKeyword.isEmpty()
                                ? "Chưa có quán nào"
                                : "Không tìm thấy quán phù hợp");
                    } else {
                        showList();
                    }
                }
            } else {
                Toast.makeText(getContext(), "Không tải được danh sách quán", Toast.LENGTH_SHORT).show();
            }
        });

        foodViewModel.getFoodData().observe(getViewLifecycleOwner(), response -> {
            exploreFoods.clear();
            if (response != null && response.getContent() != null) {
                exploreFoods.addAll(response.getContent());
            }

            if (!showingRestaurants) {
                filterExploreFoods();
            }
        });
        setupSearchListener();
        setupTabListeners();
        selectTab(true);
        loadShopsForCurrentSearch();

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
            showingRestaurants = true;
            selectTab(true);
            binding.rvMainHomeList.setAdapter(shopAdapter);
            loadShopsForCurrentSearch();
        });

        binding.tabDishes.setOnClickListener(v -> {
            showingRestaurants = false;
            selectTab(false);
            binding.rvMainHomeList.setAdapter(foodAdapter);
            if (exploreFoods.isEmpty()) {
                showEmpty("Đang tải món ngon...");
                foodViewModel.loadExploreFoods();
            } else {
                filterExploreFoods();
            }
        });
    }

    private void setupSearchListener() {
        binding.etHomeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchKeyword = s == null ? "" : s.toString().trim();
                if (pendingSearchRunnable != null) {
                    searchHandler.removeCallbacks(pendingSearchRunnable);
                }
                pendingSearchRunnable = StudentHomeFragment.this::applySearch;
                searchHandler.postDelayed(pendingSearchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void applySearch() {
        if (binding == null) {
            return;
        }

        if (showingRestaurants) {
            loadShopsForCurrentSearch();
        } else {
            filterExploreFoods();
        }
    }

    private void loadShopsForCurrentSearch() {
        String keyword = searchKeyword.isEmpty() ? null : searchKeyword;
        shopViewModel.loadShops(keyword);
    }

    private void filterExploreFoods() {
        List<FoodExploreResponse> filteredFoods = new ArrayList<>();

        for (FoodExploreResponse food : exploreFoods) {
            if (matchesKeyword(food.getFoodName())
                    || matchesKeyword(food.getShopName())
                    || matchesKeyword(food.getCategoryName())
                    || matchesKeyword(food.getDescription())) {
                filteredFoods.add(food);
            }
        }

        foodAdapter.submitList(filteredFoods);
        if (filteredFoods.isEmpty()) {
            showEmpty(searchKeyword.isEmpty()
                    ? "Chưa có món ngon nào"
                    : "Không tìm thấy món phù hợp");
        } else {
            showList();
        }
    }

    private boolean matchesKeyword(String value) {
        if (searchKeyword.isEmpty()) {
            return true;
        }
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(searchKeyword.toLowerCase(Locale.ROOT));
    }

    private void showList() {
        binding.tvHomeEmpty.setVisibility(View.GONE);
        binding.rvMainHomeList.setVisibility(View.VISIBLE);
    }

    private void showEmpty(String message) {
        binding.rvMainHomeList.setVisibility(View.GONE);
        binding.tvHomeEmpty.setVisibility(View.VISIBLE);
        binding.tvHomeEmpty.setText(message);
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
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        binding = null;
    }
}
