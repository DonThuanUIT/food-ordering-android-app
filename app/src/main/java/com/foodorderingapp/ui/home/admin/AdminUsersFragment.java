package com.foodorderingapp.ui.home.admin;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.AdminUserResponse;
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.ui.adapter.AdminUserAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.viewmodel.AdminViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminUsersFragment extends Fragment {
    private static final int PAGE_SIZE = 20;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private AdminViewModel viewModel;
    private AdminUserAdapter adapter;
    private EditText edtSearch;
    private TextView tvEmpty;
    private Runnable searchRunnable;
    private boolean pendingActiveState;
    private String selectedRole = "VENDOR";
    private MaterialButton btnRoleStudent;
    private MaterialButton btnRoleVendor;
    private MaterialButton btnLoadMore;
    private int currentPage;
    private int loadedUserCount;
    private boolean isLastPage = true;
    private boolean isLoading;

    public AdminUsersFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        edtSearch = view.findViewById(R.id.edtAdminUserSearch);
        tvEmpty = view.findViewById(R.id.tvAdminUsersEmpty);
        btnRoleStudent = view.findViewById(R.id.btnAdminRoleStudent);
        btnRoleVendor = view.findViewById(R.id.btnAdminRoleVendor);
        btnLoadMore = view.findViewById(R.id.btnAdminUsersLoadMore);

        RecyclerView recyclerView = view.findViewById(R.id.rvAdminUsers);
        adapter = new AdminUserAdapter();
        adapter.setOnUserActiveChangeListener((user, isActive) -> {
            if (user.getId() == null) {
                ToastUtils.error(requireContext(), "Không tìm thấy người dùng");
                return;
            }
            pendingActiveState = isActive;
            viewModel.updateUserLock(user.getId(), !isActive);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        setupRoleFilters();
        btnLoadMore.setOnClickListener(v -> loadNextPage());
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        viewModel.getUsers().observe(getViewLifecycleOwner(), this::bindUsers);
        viewModel.getUserLockResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                return;
            }
            if (result) {
                ToastUtils.success(requireContext(), pendingActiveState ? "Đã mở khóa người dùng" : "Đã khóa người dùng");
            } else {
                ToastUtils.error(requireContext(), "Không thể cập nhật trạng thái người dùng");
            }
            loadUsers(true);
            viewModel.clearUserLockResult();
        });

        loadUsers(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            loadUsers(true);
        }
    }

    @Override
    public void onDestroyView() {
        searchHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    private void setupRoleFilters() {
        btnRoleVendor.setOnClickListener(v -> selectRole("VENDOR"));
        btnRoleStudent.setOnClickListener(v -> selectRole("STUDENT"));
        updateRoleButtonStates();
    }

    private void selectRole(String role) {
        if (role != null && role.equals(selectedRole)) {
            return;
        }
        selectedRole = role;
        updateRoleButtonStates();
        loadUsers(true);
    }

    private void updateRoleButtonStates() {
        setFilterButtonState(btnRoleVendor, "VENDOR".equals(selectedRole));
        setFilterButtonState(btnRoleStudent, "STUDENT".equals(selectedRole));
    }

    private void setFilterButtonState(MaterialButton button, boolean selected) {
        int backgroundColor = requireContext().getColor(selected ? R.color.brand_orange : R.color.white);
        int strokeColor = requireContext().getColor(selected ? R.color.brand_orange : R.color.profile_divider);
        int textColor = requireContext().getColor(selected ? R.color.white : R.color.text_primary);
        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setStrokeWidth(dp(selected ? 0 : 1));
        button.setTextColor(textColor);
    }

    private void bindUsers(PageResponse<AdminUserResponse> page) {
        isLoading = false;
        if (page == null) {
            if (currentPage > 0) {
                currentPage--;
            } else {
                loadedUserCount = 0;
                adapter.submitList(null);
            }
            ToastUtils.error(requireContext(), "Không tải được danh sách người dùng");
            updateLoadMoreButton();
            return;
        }

        List<AdminUserResponse> users = page == null ? null : page.getContent();
        int count = users == null ? 0 : users.size();
        if (currentPage == 0) {
            loadedUserCount = count;
            adapter.submitList(users);
        } else {
            loadedUserCount += count;
            adapter.appendList(users);
        }

        isLastPage = page.isLast() || loadedUserCount >= page.getTotalElements();
        tvEmpty.setVisibility(loadedUserCount == 0 ? View.VISIBLE : View.GONE);
        tvEmpty.setText("STUDENT".equals(selectedRole)
                ? "Không có sinh viên phù hợp"
                : "Không có chủ quán phù hợp");
        updateLoadMoreButton();
    }

    private void scheduleSearch() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        searchRunnable = () -> loadUsers(true);
        searchHandler.postDelayed(searchRunnable, 350);
    }

    private void loadUsers(boolean reset) {
        if (viewModel == null) {
            return;
        }
        if (reset) {
            currentPage = 0;
            loadedUserCount = 0;
            isLastPage = false;
            adapter.submitList(null);
        } else if (isLoading || isLastPage) {
            return;
        }
        isLoading = true;
        updateLoadMoreButton();
        String query = edtSearch == null ? "" : edtSearch.getText().toString().trim();
        viewModel.loadUsers(query.isEmpty() ? null : query, selectedRole, currentPage, PAGE_SIZE);
    }

    private void loadNextPage() {
        currentPage++;
        loadUsers(false);
    }

    private void updateLoadMoreButton() {
        if (btnLoadMore == null) {
            return;
        }
        boolean hasMore = loadedUserCount > 0 && !isLastPage;
        btnLoadMore.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        btnLoadMore.setEnabled(!isLoading);
        btnLoadMore.setText(isLoading ? "Đang tải..." : "Tải thêm");
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
