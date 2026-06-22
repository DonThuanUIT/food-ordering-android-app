package com.foodorderingapp.ui.home.admin;

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

import java.util.List;

public class AdminUsersFragment extends Fragment {

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private AdminViewModel viewModel;
    private AdminUserAdapter adapter;
    private EditText edtSearch;
    private TextView tvEmpty;
    private Runnable searchRunnable;
    private boolean pendingActiveState;

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

        RecyclerView recyclerView = view.findViewById(R.id.rvAdminUsers);
        adapter = new AdminUserAdapter();
        adapter.setOnUserActiveChangeListener((user, isActive) -> {
            if (user.getId() == null) {
                ToastUtils.error(requireContext(), "Không tìm thấy user");
                return;
            }
            pendingActiveState = isActive;
            viewModel.toggleUserLock(user.getId());
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

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
                ToastUtils.success(requireContext(), pendingActiveState ? "Đã mở khóa user" : "Đã khóa user");
                loadUsers();
            } else {
                ToastUtils.error(requireContext(), "Không thể cập nhật trạng thái user");
                loadUsers();
            }
            viewModel.clearUserLockResult();
        });

        loadUsers();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            loadUsers();
        }
    }

    @Override
    public void onDestroyView() {
        searchHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    private void bindUsers(PageResponse<AdminUserResponse> page) {
        List<AdminUserResponse> users = page == null ? null : page.getContent();
        int count = users == null ? 0 : users.size();
        tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        adapter.submitList(users);
    }

    private void scheduleSearch() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        searchRunnable = this::loadUsers;
        searchHandler.postDelayed(searchRunnable, 350);
    }

    private void loadUsers() {
        String query = edtSearch == null ? "" : edtSearch.getText().toString().trim();
        viewModel.loadUsers(query.isEmpty() ? null : query);
    }
}
