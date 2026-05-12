package com.foodorderingapp.ui.home.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.foodorderingapp.databinding.FragmentStudentHomeBinding;
import com.foodorderingapp.ui.adapter.DummyAdapter;

public class StudentHomeFragment extends Fragment {

    private FragmentStudentHomeBinding binding;

    public StudentHomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Sử dụng View Binding để kết nối với layout fragment_student_home.xml
        binding = FragmentStudentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (binding.rvMainHomeList != null) {
            binding.rvMainHomeList.setLayoutManager(new LinearLayoutManager(getContext()));

            binding.rvMainHomeList.setNestedScrollingEnabled(false);

            DummyAdapter adapter = new DummyAdapter();
            binding.rvMainHomeList.setAdapter(adapter);
        }

        setupTabListeners();
    }

    private void setupTabListeners() {
        binding.tvTabRestaurants.setOnClickListener(v -> {
            // Xử lý đổi dữ liệu sang danh sách Quán ăn tại đây
        });

        binding.tabDishes.setOnClickListener(v -> {
            // Xử lý đổi dữ liệu sang danh sách Món ngon tại đây
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy binding để tránh rò rỉ bộ nhớ
        binding = null;
    }
}
