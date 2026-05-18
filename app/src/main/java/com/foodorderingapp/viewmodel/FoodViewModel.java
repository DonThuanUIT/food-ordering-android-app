package com.foodorderingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.repository.FoodRepository;
import com.foodorderingapp.model.response.FoodExploreResponse;
import com.foodorderingapp.model.response.PageResponse;

public class FoodViewModel extends ViewModel {
    private final FoodRepository repository = new FoodRepository();
    private final MutableLiveData<PageResponse<FoodExploreResponse>> foodData = new MutableLiveData<>();

    public LiveData<PageResponse<FoodExploreResponse>> getFoodData() {
        return foodData;
    }

    public void loadExploreFoods() {
        repository.getExploreFoods(0, 20, foodData);
    }
}