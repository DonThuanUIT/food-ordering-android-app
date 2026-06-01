package com.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.foodorderingapp.ui.home.student.StudentHomeFragment;
import com.foodorderingapp.ui.home.vendor.VendorOrdersFragment;
import com.foodorderingapp.ui.home.student.StudentOrdersFragment;
import com.foodorderingapp.ui.home.student.StudentProfileFragment;
import com.foodorderingapp.ui.home.student.StudentHistoryFragment;
import com.foodorderingapp.ui.home.vendor.VendorStatsFragment;
import com.foodorderingapp.ui.home.vendor.VendorMenuFragment;
import com.foodorderingapp.ui.home.vendor.VendorSettingsFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private TextView tvAppTitle;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
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
            updateHeader("Đơn Hàng");
            loadFragment(new VendorOrdersFragment());
        } else {
            bottomNav.inflateMenu(R.menu.menu_student);
            updateHeader("UniEats");
            loadFragment(new StudentHomeFragment());
        }

        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "UniEats";
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new StudentHomeFragment();
                title = "UniEats";
            } else if (id == R.id.nav_orders) {
                selectedFragment = new StudentOrdersFragment();
                title = "Đơn Hàng";
            } else if (id == R.id.nav_history) {
                selectedFragment = new StudentHistoryFragment();
                title = "Lịch Sử";
            } else if (id == R.id.nav_profile) {
                selectedFragment = new StudentProfileFragment();
                title = "Cá Nhân";
            } else if (id == R.id.nav_vendor_orders) {
                selectedFragment = new VendorOrdersFragment();
                title = "Đơn Hàng";
            } else if (id == R.id.nav_vendor_stats) {
                selectedFragment = new VendorStatsFragment();
                title = "Thống Kê";
            } else if (id == R.id.nav_vendor_menu) {
                selectedFragment = new VendorMenuFragment();
                title = "Thực Đơn";
            } else if (id == R.id.nav_vendor_settings) {
                selectedFragment = new VendorSettingsFragment();
                title = "Cài Đặt";
            }

            if (selectedFragment != null) {
                updateHeader(title);
                loadFragment(selectedFragment);
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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void handleStartTab(Intent intent) {
        if (intent == null || bottomNav == null) {
            return;
        }

        String openTab = intent.getStringExtra("OPEN_TAB");
        if ("ORDERS".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(R.id.nav_orders);
        }
    }
}
