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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.foodorderingapp.R;
import com.foodorderingapp.databinding.FragmentStudentHomeBinding;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;

import com.foodorderingapp.model.response.FoodExploreResponse;
import com.foodorderingapp.ui.chat.AiRecommendationActivity;
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
import com.foodorderingapp.utils.ToastUtils;

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
    private boolean showingFavorites = false;
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
            if (!isVisibleStudentShop(shop)) {
                ToastUtils.error(getContext(), "Quán ăn hiện không khả dụng");
                loadShopsForCurrentSearch();
                return;
            }
            Intent intent = new Intent(requireContext(), ShopDetailActivity.class);
            intent.putExtra("SHOP_ID", shop.getId());
            intent.putExtra("SHOP_NAME", shop.getName());
            intent.putExtra("SHOP_ADDRESS", shop.getAddress());
            intent.putExtra("SHOP_DESCRIPTION", shop.getDescription());
            intent.putExtra("SHOP_OPEN_TIME", shop.getOpenTime());
            intent.putExtra("SHOP_CLOSE_TIME", shop.getCloseTime());
            intent.putExtra("SHOP_DISPLAY_STATUS", shop.getDisplayStatus());
            if (shop.getCurrentlyOpen() != null) {
                intent.putExtra("SHOP_CURRENTLY_OPEN", shop.getCurrentlyOpen());
            }
            startActivity(intent);
        });

        binding.btnHomeMap.setOnClickListener(v -> {
            // Open Food Map
            Intent intent = new Intent(requireContext(), com.foodorderingapp.ui.map.FoodMapActivity.class);
            startActivity(intent);
        });

        shopViewModel.getShopData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getContent() != null) {
                List<ShopResponse> approvedShops = new ArrayList<>();

                for (ShopResponse shop : response.getContent()) {
                    if (isVisibleStudentShop(shop)) {
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
                ToastUtils.error(getContext(), "Không tải được danh sách quán");
            }
        });

        foodViewModel.getFoodData().observe(getViewLifecycleOwner(), foods -> {
            exploreFoods.clear();
            if (foods != null) {
                exploreFoods.addAll(foods);
            }

            if (!showingRestaurants && !showingFavorites) {
                filterExploreFoods();
            }
        });
        setupSearchListener();
        setupTabListeners();
        setupExplorePagination();
        selectTab(0);
        loadShopsForCurrentSearch();

        binding.btnHomeAiAssistant.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AiRecommendationActivity.class);
            startActivity(intent);
        });

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        foodAdapter.setOnAddToCartClickListener(food -> {
            cartViewModel.addToCart(food.getId());
        });
        cartViewModel.getAddResult().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                ToastUtils.success(getContext(), "Đã thêm vào giỏ hàng");
            } else if (success != null) {
                ToastUtils.error(getContext(), "Thêm vào giỏ thất bại");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null || shopViewModel == null || foodViewModel == null) {
            return;
        }
        loadShopsForCurrentSearch();
        if (!showingRestaurants) {
            foodViewModel.loadExploreFoods();
        }
    }

    private boolean isVisibleStudentShop(ShopResponse shop) {
        if (shop == null) {
            return false;
        }
        String status = shop.getStatus();
        return "APPROVED".equalsIgnoreCase(status)
                && shop.isActive()
                && !"BANNED".equalsIgnoreCase(status)
                && !"REJECTED".equalsIgnoreCase(status)
                && !"CLOSED".equalsIgnoreCase(status)
                && !"PENDING".equalsIgnoreCase(status);
    }

    private void setupTabListeners() {
        binding.tabRestaurants.setOnClickListener(v -> {
            showingRestaurants = true;
            showingFavorites = false;
            selectTab(0);
            binding.rvMainHomeList.setAdapter(shopAdapter);
            loadShopsForCurrentSearch();
        });

        binding.tabDishes.setOnClickListener(v -> {
            showingRestaurants = false;
            showingFavorites = false;
            selectTab(1);
            binding.rvMainHomeList.setAdapter(foodAdapter);
            if (exploreFoods.isEmpty()) {
                showEmpty("Đang tải món ngon...");
                foodViewModel.loadExploreFoods();
            } else {
                filterExploreFoods();
            }
        });

        binding.tabFavorites.setOnClickListener(v -> {
            showingRestaurants = false;
            showingFavorites = true;
            selectTab(2);
            binding.rvMainHomeList.setAdapter(shopAdapter);
            loadFavoriteShops();
        });
    }

    private void loadFavoriteShops() {
        showEmpty("Đang tải danh sách yêu thích...");
        com.foodorderingapp.data.remote.api.ApiClient.getApiService().getFavoriteShops().enqueue(new retrofit2.Callback<List<ShopResponse>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<List<ShopResponse>> call, @NonNull retrofit2.Response<List<ShopResponse>> response) {
                if (binding == null || !showingFavorites) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<ShopResponse> favorites = response.body();
                    List<ShopResponse> filtered = new ArrayList<>();
                    for (ShopResponse shop : favorites) {
                        if (searchKeyword.isEmpty() || (shop.getName() != null && shop.getName().toLowerCase(Locale.ROOT).contains(searchKeyword.toLowerCase(Locale.ROOT)))) {
                            filtered.add(shop);
                        }
                    }
                    shopAdapter.submitList(filtered);
                    if (filtered.isEmpty()) {
                        showEmpty(searchKeyword.isEmpty() ? "Danh sách yêu thích trống" : "Không tìm thấy quán yêu thích phù hợp");
                    } else {
                        showList();
                    }
                } else {
                    showEmpty("Lỗi tải danh sách yêu thích");
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<List<ShopResponse>> call, @NonNull Throwable t) {
                if (binding == null || !showingFavorites) {
                    return;
                }
                showEmpty("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void setupExplorePagination() {
        NestedScrollView scrollView = (NestedScrollView) binding.getRoot();
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (showingRestaurants || showingFavorites || foodViewModel == null || !foodViewModel.canLoadMore()) {
                return;
            }

            View content = v.getChildAt(0);
            if (content == null) {
                return;
            }

            int distanceToBottom = content.getMeasuredHeight() - v.getMeasuredHeight() - scrollY;
            if (distanceToBottom <= 240) {
                foodViewModel.loadMoreExploreFoods();
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
        } else if (showingFavorites) {
            loadFavoriteShops();
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

    private void selectTab(int tabIndex) {
        if (getContext() == null) {
            return;
        }
        
        binding.tvTabRestaurants.setTextColor(tabIndex == 0 ? Color.parseColor("#FF7A21") : androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vendor_dark_text_secondary));
        binding.indicatorRestaurants.setVisibility(tabIndex == 0 ? View.VISIBLE : View.INVISIBLE);

        binding.tvTabDishes.setTextColor(tabIndex == 1 ? Color.parseColor("#FF7A21") : androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vendor_dark_text_secondary));
        binding.indicatorDishes.setVisibility(tabIndex == 1 ? View.VISIBLE : View.INVISIBLE);

        binding.tvTabFavorites.setTextColor(tabIndex == 2 ? Color.parseColor("#FF7A21") : androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vendor_dark_text_secondary));
        binding.indicatorFavorites.setVisibility(tabIndex == 2 ? View.VISIBLE : View.INVISIBLE);
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
