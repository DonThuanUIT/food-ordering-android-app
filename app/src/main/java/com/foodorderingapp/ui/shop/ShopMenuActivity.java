package com.foodorderingapp.ui.shop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.model.response.CartItemResponse;
import com.foodorderingapp.model.response.ShopCartResponse;
import com.foodorderingapp.model.response.ShopDetailResponse;
import com.foodorderingapp.ui.adapter.ShopMenuFoodAdapter;
import com.foodorderingapp.ui.adapter.ShopMenuTabAdapter;
import com.foodorderingapp.viewmodel.CartViewModel;
import com.foodorderingapp.viewmodel.ShopViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopMenuActivity extends AppCompatActivity {

    private TextView tvMenuShopName;
    private TextView tvMenuEmpty;
    private TextView tvCartCount;
    private TextView tvCartTotal;
    private EditText etMenuSearch;
    private LinearLayout layoutCartSummary;
    private RecyclerView rvMenuTabs;
    private RecyclerView rvShopMenu;

    private ShopMenuTabAdapter tabAdapter;
    private ShopMenuFoodAdapter foodAdapter;
    private ShopViewModel shopViewModel;
    private CartViewModel cartViewModel;

    private final List<ShopDetailResponse.CategoryMenu> categories = new ArrayList<>();
    private String shopId;
    private int selectedCategoryPosition = 0;
    private String menuSearchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#081126"));
        getWindow().setNavigationBarColor(Color.WHITE);
        setContentView(R.layout.activity_shop_menu);

        shopId = getIntent().getStringExtra("SHOP_ID");
        if (shopId == null || shopId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy quán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupRecyclerViews();
        setupViewModels();
        bindClickListeners();
        observeData();

        String shopName = getIntent().getStringExtra("SHOP_NAME");
        tvMenuShopName.setText("Menu - " + nullToDefault(shopName, "Quán"));
        updateCartSummary(0, 0);
        showLoading();
        shopViewModel.loadShopDetail(shopId);
        cartViewModel.loadCart();
    }

    private void bindViews() {
        tvMenuShopName = findViewById(R.id.tvMenuShopName);
        tvMenuEmpty = findViewById(R.id.tvMenuEmpty);
        tvCartCount = findViewById(R.id.tvMenuCartCount);
        tvCartTotal = findViewById(R.id.tvMenuCartTotal);
        etMenuSearch = findViewById(R.id.etMenuSearch);
        layoutCartSummary = findViewById(R.id.layoutCartSummary);
        rvMenuTabs = findViewById(R.id.rvMenuTabs);
        rvShopMenu = findViewById(R.id.rvShopMenu);
    }

    private void setupRecyclerViews() {
        tabAdapter = new ShopMenuTabAdapter();
        foodAdapter = new ShopMenuFoodAdapter();

        rvMenuTabs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvMenuTabs.setAdapter(tabAdapter);

        rvShopMenu.setLayoutManager(new LinearLayoutManager(this));
        rvShopMenu.setAdapter(foodAdapter);
    }

    private void setupViewModels() {
        shopViewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
    }

    private void bindClickListeners() {
        ImageButton btnMenuBack = findViewById(R.id.btnMenuBack);
        ImageButton btnMenuSearch = findViewById(R.id.btnMenuSearch);

        btnMenuBack.setOnClickListener(v -> finish());
        btnMenuSearch.setOnClickListener(v -> toggleMenuSearch());

        etMenuSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                menuSearchKeyword = s == null ? "" : s.toString().trim();
                showCurrentMenuFoods();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tabAdapter.setOnTabClickListener(position -> {
            if (position < 0 || position >= categories.size()) {
                return;
            }
            selectedCategoryPosition = position;
            tabAdapter.setSelectedPosition(position);
            showCurrentMenuFoods();
        });

        foodAdapter.setOnFoodClickListener(food -> cartViewModel.addToCart(food.getId()));

        layoutCartSummary.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USER_ROLE", "STUDENT");
            intent.putExtra("OPEN_TAB", "CART");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void observeData() {
        shopViewModel.getShopDetail().observe(this, detail -> {
            if (detail == null) {
                showEmpty("Không tải được menu quán");
                return;
            }

            tvMenuShopName.setText("Menu - " + nullToDefault(detail.getName(), "Quán"));

            categories.clear();
            if (detail.getMenu() != null) {
                categories.addAll(detail.getMenu());
            }

            if (categories.isEmpty()) {
                showEmpty("Quán chưa có menu");
                return;
            }

            tvMenuEmpty.setVisibility(View.GONE);
            rvMenuTabs.setVisibility(View.VISIBLE);
            rvShopMenu.setVisibility(View.VISIBLE);
            tabAdapter.submitList(categories);
            selectedCategoryPosition = 0;
            tabAdapter.setSelectedPosition(0);
            showCurrentMenuFoods();
        });

        cartViewModel.getAddResult().observe(this, success -> {
            if (success == null) {
                return;
            }

            if (success) {
                Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                cartViewModel.loadCart();
            } else {
                Toast.makeText(this, "Không thêm được món", Toast.LENGTH_SHORT).show();
            }
        });

        cartViewModel.getCartData().observe(this, cart -> {
            if (cart == null || cart.getShops() == null) {
                return;
            }

            int quantity = 0;
            for (ShopCartResponse shop : cart.getShops()) {
                if (shop.getItems() == null) {
                    continue;
                }
                for (CartItemResponse item : shop.getItems()) {
                    quantity += item.getQuantity();
                }
            }

            updateCartSummary(quantity, cart.getTotalAmount());
        });
    }

    private void toggleMenuSearch() {
        if (etMenuSearch.getVisibility() == View.VISIBLE) {
            etMenuSearch.setText("");
            etMenuSearch.setVisibility(View.GONE);
            tvMenuShopName.setVisibility(View.VISIBLE);
            hideKeyboard();
            showCurrentMenuFoods();
            return;
        }

        tvMenuShopName.setVisibility(View.GONE);
        etMenuSearch.setVisibility(View.VISIBLE);
        etMenuSearch.requestFocus();
        showKeyboard();
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(etMenuSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(etMenuSearch.getWindowToken(), 0);
        }
    }

    private void showCurrentMenuFoods() {
        if (categories.isEmpty()) {
            showEmpty("Quán chưa có menu");
            return;
        }

        if (!menuSearchKeyword.isEmpty()) {
            showMenuSearchResults();
            return;
        }

        if (selectedCategoryPosition < 0 || selectedCategoryPosition >= categories.size()) {
            selectedCategoryPosition = 0;
        }
        showFoods(categories.get(selectedCategoryPosition));
    }

    private void showMenuSearchResults() {
        List<ShopDetailResponse.FoodItem> filteredFoods = new ArrayList<>();

        for (ShopDetailResponse.CategoryMenu category : categories) {
            if (category.getFoods() == null) {
                continue;
            }

            for (ShopDetailResponse.FoodItem food : category.getFoods()) {
                if (matchesMenuKeyword(food.getName())
                        || matchesMenuKeyword(food.getDescription())
                        || matchesMenuKeyword(food.getCategoryName())
                        || matchesMenuKeyword(category.getName())) {
                    filteredFoods.add(food);
                }
            }
        }

        foodAdapter.submitList(filteredFoods);
        if (filteredFoods.isEmpty()) {
            tvMenuEmpty.setVisibility(View.VISIBLE);
            rvShopMenu.setVisibility(View.GONE);
            tvMenuEmpty.setText("Không tìm thấy món phù hợp");
        } else {
            tvMenuEmpty.setVisibility(View.GONE);
            rvShopMenu.setVisibility(View.VISIBLE);
        }
    }

    private boolean matchesMenuKeyword(String value) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(menuSearchKeyword.toLowerCase(Locale.ROOT));
    }

    private void showFoods(ShopDetailResponse.CategoryMenu category) {
        if (category.getFoods() == null || category.getFoods().isEmpty()) {
            tvMenuEmpty.setVisibility(View.VISIBLE);
            rvShopMenu.setVisibility(View.GONE);
            tvMenuEmpty.setText("Danh mục này chưa có món");
            foodAdapter.submitList(null);
            return;
        }

        tvMenuEmpty.setVisibility(View.GONE);
        rvShopMenu.setVisibility(View.VISIBLE);
        foodAdapter.submitList(category.getFoods());
    }

    private void updateCartSummary(int quantity, double totalAmount) {
        tvCartCount.setText(quantity + " món");
        tvCartTotal.setText(formatPrice(totalAmount));
    }

    private void showLoading() {
        rvMenuTabs.setVisibility(View.GONE);
        rvShopMenu.setVisibility(View.GONE);
        tvMenuEmpty.setVisibility(View.VISIBLE);
        tvMenuEmpty.setText("Đang tải menu...");
    }

    private void showEmpty(String message) {
        rvMenuTabs.setVisibility(View.GONE);
        rvShopMenu.setVisibility(View.GONE);
        tvMenuEmpty.setVisibility(View.VISIBLE);
        tvMenuEmpty.setText(message);
        tabAdapter.submitList(null);
        foodAdapter.submitList(null);
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }
}
