package com.foodorderingapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.foodorderingapp.ui.home.admin.AdminApprovalsFragment;
import com.foodorderingapp.ui.home.admin.AdminOverviewFragment;
import com.foodorderingapp.ui.home.admin.AdminUsersFragment;
import com.foodorderingapp.ui.home.student.StudentHomeFragment;
import com.foodorderingapp.ui.home.student.StudentHistoryFragment;
import com.foodorderingapp.ui.home.vendor.VendorOrdersFragment;
import com.foodorderingapp.ui.home.vendor.VendorMessagesFragment;
import com.foodorderingapp.ui.home.student.StudentOrdersFragment;
import com.foodorderingapp.ui.home.student.StudentProfileFragment;
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
            
            // Adjust ViewPager2 margin dynamically to avoid navigation overlap on all devices
            android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
            int baseHeight = (int) (60 * getResources().getDisplayMetrics().density); // 60dp base height
            params.bottomMargin = baseHeight + systemBars.bottom;
            viewPager.setLayoutParams(params);
            
            return insets;
        });

        userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole == null) userRole = "STUDENT";

        setupMenuAndNavigation(userRole);

        if ("VENDOR".equalsIgnoreCase(userRole)) {
            if (findViewById(R.id.appBarLayout) != null) {
                findViewById(R.id.appBarLayout).setBackgroundColor(Color.parseColor("#1B110F"));
            }
            if (findViewById(R.id.toolbar) != null) {
                findViewById(R.id.toolbar).setBackgroundColor(Color.parseColor("#1B110F"));
            }
            if (bottomNav != null) {
                bottomNav.setBackgroundColor(Color.parseColor("#1B110F"));
            }
            if (tvAppTitle != null) {
                tvAppTitle.setTextColor(Color.parseColor("#FFFFFF"));
            }
            ImageView ivMenu = findViewById(R.id.ivMenu);
            if (ivMenu != null) {
                ivMenu.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            }
            ImageView ivProfile = findViewById(R.id.ivProfile);
            if (ivProfile != null) {
                ivProfile.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                ivProfile.setBackground(null);
            }
            if (getWindow() != null) {
                getWindow().setStatusBarColor(Color.parseColor("#1B110F"));
                getWindow().getDecorView().setSystemUiVisibility(0); // clear light status bar so status text is white
            }
        }

        setupHeaderActions();
        requestNotificationPermissionIfNeeded();
        handleStartTab(getIntent());
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(this, R.color.nav_item_color));
        bottomNav.setItemTextColor(ContextCompat.getColorStateList(this, R.color.nav_item_color));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleStartTab(intent);
    }

    private void setupMenuAndNavigation(String role) {
        bottomNav.getMenu().clear();

        if (isAdminRole(role)) {
            bottomNav.inflateMenu(R.menu.menu_admin);
        } else if (isVendorRole(role)) {
            bottomNav.inflateMenu(R.menu.menu_vendor);
        } else {
            bottomNav.inflateMenu(R.menu.menu_student);
        }

        if (isAdminRole(role)) {
            updateHeader("UniEats Admin");
        } else if (isVendorRole(role)) {
            updateHeader("Đơn hàng");
        } else {
            updateHeader("UniEats");
        }

        viewPager.setAdapter(new MainPagerAdapter(this, role));
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(3);

        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_overview) {
                viewPager.setCurrentItem(0, false);
                updateHeader("UniEats Admin");
                return true;
            } else if (id == R.id.nav_admin_approvals) {
                viewPager.setCurrentItem(1, false);
                updateHeader("UniEats Admin");
                return true;
            } else if (id == R.id.nav_admin_users) {
                viewPager.setCurrentItem(2, false);
                updateHeader("UniEats Admin");
                return true;
            } else if (id == R.id.nav_home || id == R.id.nav_vendor_orders) {
                viewPager.setCurrentItem(0, false);
                updateHeader("VENDOR".equalsIgnoreCase(userRole) ? "Đơn hàng" : "UniEats");
                return true;
            } else if (id == R.id.nav_vendor_messages) {
                viewPager.setCurrentItem(1, false);
                updateHeader("Tin nhắn");
                return true;
            } else if (id == R.id.nav_orders || id == R.id.nav_vendor_stats) {
                viewPager.setCurrentItem("VENDOR".equalsIgnoreCase(userRole) ? 2 : 1, false);
                updateHeader("VENDOR".equalsIgnoreCase(userRole) ? "Thống kê" : "Trạng thái đơn hàng");
                return true;
            } else if (id == R.id.nav_cart || id == R.id.nav_vendor_menu) {
                viewPager.setCurrentItem("VENDOR".equalsIgnoreCase(userRole) ? 3 : 2, false);
                updateHeader("VENDOR".equalsIgnoreCase(userRole) ? "Thực đơn" : "Giỏ hàng");
                return true;
            } else if (id == R.id.nav_history || id == R.id.nav_vendor_settings) {
                viewPager.setCurrentItem("VENDOR".equalsIgnoreCase(userRole) ? 4 : 3, false);
                updateHeader("VENDOR".equalsIgnoreCase(userRole) ? "Cài đặt" : "Lịch sử");
                return true;
            } else if (id == R.id.nav_profile) {
                viewPager.setCurrentItem(4, false);
                updateHeader("Cá nhân");
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
            if (isAdminRole(role)) {
                switch (position) {
                    case 0: return new AdminOverviewFragment();
                    case 1: return new AdminApprovalsFragment();
                    case 2: return new AdminUsersFragment();
                }
            } else if (isVendorRole(role)) {
                switch (position) {
                    case 0: return new VendorOrdersFragment();
                    case 1: return new VendorMessagesFragment();
                    case 2: return new VendorStatsFragment();
                    case 3: return new VendorMenuFragment();
                    case 4: return new VendorSettingsFragment();
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
            if (isAdminRole(role)) {
                return 3;
            }
            return 5;
        }
    }

    private void setupHeaderActions() {
        findViewById(R.id.ivProfile).setOnClickListener(v -> openProfileShortcut());
        findViewById(R.id.ivMenu).setOnClickListener(v -> showQuickMenu());
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                1001
        );
    }

    private void openProfileShortcut() {
        if (isAdminRole(userRole)) {
            bottomNav.setSelectedItemId(R.id.nav_admin_users);
        } else if (isVendorRole(userRole)) {
            bottomNav.setSelectedItemId(R.id.nav_vendor_settings);
        } else {
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
    }

    private void showQuickMenu() {
        androidx.appcompat.widget.PopupMenu popupMenu =
                new androidx.appcompat.widget.PopupMenu(this, findViewById(R.id.ivMenu));
        Menu menu = popupMenu.getMenu();

        if (isAdminRole(userRole)) {
            menu.add(Menu.NONE, R.id.nav_admin_overview, Menu.NONE, "Overview");
            menu.add(Menu.NONE, R.id.nav_admin_approvals, Menu.NONE, "Approvals");
            menu.add(Menu.NONE, R.id.nav_admin_users, Menu.NONE, "Users");
        } else if (isVendorRole(userRole)) {
            menu.add(Menu.NONE, R.id.nav_vendor_orders, Menu.NONE, "Đơn hàng");
            menu.add(Menu.NONE, R.id.nav_vendor_messages, Menu.NONE, "Tin nhắn");
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
        if (isAdminRole(userRole) && "APPROVALS".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(R.id.nav_admin_approvals);
        } else if (isAdminRole(userRole) && "USERS".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(R.id.nav_admin_users);
        } else if (isVendorRole(userRole) && "MESSAGES".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(R.id.nav_vendor_messages);
        } else if ("ORDERS".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(isVendorRole(userRole) ? R.id.nav_vendor_orders : R.id.nav_orders);
        } else if ("HISTORY".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(R.id.nav_history);
        } else if ("CART".equalsIgnoreCase(openTab)) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
        }
    }

    private static boolean isVendorRole(String role) {
        return "VENDOR".equalsIgnoreCase(role);
    }

    private static boolean isAdminRole(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
