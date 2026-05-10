package com.foodorderingapp.ui.home.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.foodorderingapp.R;
import com.foodorderingapp.ui.adapter.DummyAdapter;

public class StudentHomeFragment extends Fragment {

    private RecyclerView rvCategories;
    private RecyclerView rvFeaturedRestaurants;
    private RecyclerView rvStudentDishes;

    public StudentHomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCategories = view.findViewById(R.id.rvCategories);
        rvFeaturedRestaurants = view.findViewById(R.id.rvFeaturedRestaurants);
        rvStudentDishes = view.findViewById(R.id.rvStudentDishes);

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeaturedRestaurants.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvStudentDishes.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tắt Nested Scrolling để cuộn mượt hơn trong NestedScrollView
        rvCategories.setNestedScrollingEnabled(false);
        rvFeaturedRestaurants.setNestedScrollingEnabled(false);
        rvStudentDishes.setNestedScrollingEnabled(false);

        DummyAdapter adapter = new DummyAdapter();
        rvCategories.setAdapter(adapter);
        rvFeaturedRestaurants.setAdapter(adapter);
        rvStudentDishes.setAdapter(adapter);
    }
}