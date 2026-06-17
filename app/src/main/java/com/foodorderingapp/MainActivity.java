package com.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.foodorderingapp.ui.home.student.StudentHomeFragment;
import com.foodorderingapp.ui.home.student.StudentHistoryFragment;
import com.foodorderingapp.ui.home.vendor.VendorOrdersFragment;
import com.foodorderingapp.ui.home.student.StudentOrdersFragment;
import com.foodorderingapp.ui.home.student.StudentProfileFragment;
import com.foodorderingapp.ui.home.student.StudentHistoryFragment;
import com.foodorderingapp.ui.home.student.StudentCartFragment;
import com.foodorderingapp.ui.home.vendor.VendorStatsFragment;
import com.foodorderingapp.ui.home.vendor.VendorMenuFragment;
import com.foodorderingapp.ui.home.vendor.VendorSettingsFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private ViewPager2 viewPager;
    private TextView tvAppTitle;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.view_pager);
        tvAppTitle = findViewById(R.id.tvAppTitle);

        if (findViewById(R.id.toolbar) != null) {
            setSupportActionBar(findViewById(R.id.toolbar));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole == null) userRole = "STUDENT";

        setupMenuAndNavigation(userRole);
        setupHeaderActions();
        handleStartTab(getIntent());
        bottomNav.setItemIconTintList(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleStartTab(intent);
    }

    private void setupMenuAndNavigation(String role) {
        bottomNav.getMenu().clear();

        if ("VENDOR".equalsIgnoreCase(role)) {
            bottomNav.inflateMenu(R.menu.menu_vendor);
        } else {
            bottomNav.inflateMenu(R.menu.menu_student);
        }

        if ("VENDOR".equalsIgnoreCase(role)) {
            updateHeader("Đơn Hàng");
        } else {
            updateHeader("UniEats");
        }

        viewPager.setAdapter(new MainPagerAdapter(this, role));
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(3);

        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home || id == R.id.nav_vendor_orders) {
                viewPager.setCurrentItem(0, false);
                updateHeader("VENDOR".equalsIgnoreCase(userRole) ? "Đơn Hàng" : "UniEats");
                return true;
            } else if (id == R.id.nav_orders || id == R.id.nav_vendor_stats) {
                viewPager.setCurrentItem(1, false);
                updateHeader("VENDOR".equalsIgnoreCase(userRole) ? "Thống Kê" : "Trạng Thái Đơn Hàng");
                return true;
            } else if (id == R.id.nav_cart || id == R.id.nav_vendor_menu) {
                viewPager.setCurrentItem(2, false);
                updateHeader("VENDOR".equalsIgnoreCase(userRole) ? "Thực Đơn" : "Giỏ Hàng");
                return true;
            } else if (id == R.id.nav_history || id == R.id.nav_vendor_settings) {
                viewPager.setCurrentItem(3, false);
                updateHeader("VENDOR".equalsIgnoreCase(userRole) ? "Cài Đặt" : "Lịch Sử");
                return true;
            } else if (id == R.id.nav_profile) {
                viewPager.setCurrentItem(4, false);
                updateHeader("Cá Nhân");
                return true;
            }
            return false;
        });
    }

    private void updateHeader(String title) {
        if (tvAppTitle != null) {
            tvAppTitle.setText(title);
        }
    }

    private static class MainPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
        private final String role;

        public MainPagerAdapter(@NonNull androidx.fragment.app.FragmentActivity fragmentActivity, String role) {
            super(fragmentActivity);
            this.role = role;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if ("VENDOR".equalsIgnoreCase(role)) {
                switch (position) {
                    case 0: return new VendorOrdersFragment();
                    case 1: return new VendorStatsFragment();
                    case 2: return new VendorMenuFragment();
                    case 3: return new VendorSettingsFragment();
                }
            } else {
                switch (position) {
                    case 0: return new StudentHomeFragment();
                    case 1: return new StudentOrdersFragment();
                    case 2: return new StudentCartFragment();
                    case 3: return new StudentHistoryFragment();
                    case 4: return new StudentProfileFragment();
                }
            }
            return new Fragment();
        }

        @Override
        public int getItemCount() {
            return "VENDOR".equalsIgnoreCase(role) ? 4 : 5;
        }
    }

    private void setupHeaderActions() {
        findViewById(R.id.ivProfile).setOnClickListener(v -> openProfileShortcut());
        findViewById(R.id.ivMenu).setOnClickListener(v -> showQuickMenu());
    }

    private void openProfileShortcut() {
        if ("VENDOR".equalsIgnoreCase(userRole)) {
            bottomNav.setSelectedItemId(R.id.nav_vendor_settings);
        } else {
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
    }

    private void showQuickMenu() {
        androidx.appcompat.widget.PopupMenu popupMenu =
                new androidx.appcompat.widget.PopupMenu(this, findViewById(R.id.ivMenu));
        Menu menu = popupMenu.getMenu();

        if ("VENDOR".equalsIgnoreCase(userRole)) {
            menu.add(Menu.NONE, R.id.nav_vendor_orders, Menu.NONE, "Đơn hàng");
            menu.add(Menu.NONE, R.id.nav_vendor_stats, Menu.NONE, "Thống kê");
            menu.add(Menu.NONE, R.id.nav_vendor_menu, Menu.NONE, "Thực đơn");
            menu.add(Menu.NONE, R.id.nav_vendor_settings, Menu.NONE, "Cài đặt");
        } else {
            menu.add(Menu.NONE, R.id.nav_home, Menu.NONE, "Trang chủ");
            menu.add(Menu.NONE, R.id.nav_orders, Menu.NONE, "Đơn đang xử lý");
            menu.add(Menu.NONE, R.id.nav_history, Menu.NONE, "Lịch sử đơn");
            menu.add(Menu.NONE, R.id.nav_cart, Menu.NONE, "Giỏ hàng");
            menu.add(Menu.NONE, R.id.nav_profile, Menu.NONE, "Cá nhân");
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            bottomNav.setSelectedItemId(item.getItemId());
            return true;
        });
        popupMenu.show();
    }

    private void handleStartTab(Intent intent) {
        if (intent == null || bottomNav == null) {
            return;
        }

        String openTab = intent.getStringExtra("OPEN_TAB");
        if ("ORDERS".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(R.id.nav_orders);
        } else if ("HISTORY".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(R.id.nav_history);
        } else if ("CART".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
        }
    }
}
