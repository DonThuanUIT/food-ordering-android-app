package com.foodorderingapp;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabCart;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        fabCart = findViewById(R.id.fab_cart);

        if (findViewById(R.id.toolbar) != null) {
            setSupportActionBar(findViewById(R.id.toolbar));
        }

        userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole == null) userRole = "STUDENT";

        setupMenuAndNavigation(userRole);

        bottomNav.setItemIconTintList(null);
    }

    private void setupMenuAndNavigation(String role) {
        bottomNav.getMenu().clear();

        if ("VENDOR".equals(role)) {
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
            }

            else if (id == R.id.nav_vendor_orders) {
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