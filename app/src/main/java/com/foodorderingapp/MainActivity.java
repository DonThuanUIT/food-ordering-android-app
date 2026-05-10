package com.foodorderingapp;

import android.os.Bundle;
import android.util.Log;
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
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        if (findViewById(R.id.toolbar) != null) {
            setSupportActionBar(findViewById(R.id.toolbar));
        }

        // Xử lý Window Insets để tránh bị BottomNav đè lên navigation bar của hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Chỉ cần padding bottom cho bottomNav, phần top sẽ do AppBarLayout xử lý qua fitsSystemWindows
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Lấy role từ Intent
        userRole = getIntent().getStringExtra("USER_ROLE");
        Log.d("MAIN_DEBUG", "User Role in MainActivity: " + userRole);

        // Nếu role null, mặc định là STUDENT để tránh lỗi trắng màn hình
        if (userRole == null) userRole = "STUDENT";

        setupMenuAndNavigation(userRole);
        bottomNav.setItemIconTintList(null);
    }

    private void setupMenuAndNavigation(String role) {
        bottomNav.getMenu().clear();

        // Sử dụng equalsIgnoreCase để khớp với chữ hoa từ Backend (STUDENT/VENDOR)
        if ("VENDOR".equalsIgnoreCase(role)) {
            bottomNav.inflateMenu(R.menu.menu_vendor);
            loadFragment(new VendorOrdersFragment());
        } else {
            bottomNav.inflateMenu(R.menu.menu_student);
            loadFragment(new StudentHomeFragment());
        }

        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new StudentHomeFragment();
            } else if (id == R.id.nav_orders) {
                selectedFragment = new StudentOrdersFragment();
            } else if (id == R.id.nav_history) {
                selectedFragment = new StudentHistoryFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new StudentProfileFragment();
            } else if (id == R.id.nav_vendor_orders) {
                selectedFragment = new VendorOrdersFragment();
            } else if (id == R.id.nav_vendor_stats) {
                selectedFragment = new VendorStatsFragment();
            } else if (id == R.id.nav_vendor_menu) {
                selectedFragment = new VendorMenuFragment();
            } else if (id == R.id.nav_vendor_settings) {
                selectedFragment = new VendorSettingsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
