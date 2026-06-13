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
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private ViewPager2 viewPager;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.view_pager);

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
        } else {
            bottomNav.inflateMenu(R.menu.menu_student);
        }

        viewPager.setAdapter(new MainPagerAdapter(this, role));
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(3);

        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home || id == R.id.nav_vendor_orders) {
                viewPager.setCurrentItem(0, false);
                return true;
            } else if (id == R.id.nav_orders || id == R.id.nav_vendor_stats) {
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (id == R.id.nav_history || id == R.id.nav_vendor_menu) {
                viewPager.setCurrentItem(2, false);
                return true;
            } else if (id == R.id.nav_profile || id == R.id.nav_vendor_settings) {
                viewPager.setCurrentItem(3, false);
                return true;
            }
            return false;
        });
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
            if ("VENDOR".equals(role)) {
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
                    case 2: return new StudentHistoryFragment();
                    case 3: return new StudentProfileFragment();
                }
            }
            return new Fragment();
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}