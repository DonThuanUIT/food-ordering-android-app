package com.foodorderingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.repository.FoodRepository;
import com.foodorderingapp.model.response.FoodExploreResponse;
import com.foodorderingapp.model.response.PageResponse;

import java.util.ArrayList;
import java.util.List;

public class FoodViewModel extends ViewModel {
    private static final int PAGE_SIZE = 20;

    private final FoodRepository repository = new FoodRepository();
    private final MutableLiveData<List<FoodExploreResponse>> foodData = new MutableLiveData<>();
    private final List<FoodExploreResponse> loadedFoods = new ArrayList<>();
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    public LiveData<List<FoodExploreResponse>> getFoodData() {
        return foodData;
    }

    public void loadExploreFoods() {
        currentPage = 0;
        isLastPage = false;
        loadedFoods.clear();
        loadExploreFoodsPage(currentPage);
    }

    public void loadMoreExploreFoods() {
        if (canLoadMore()) {
            loadExploreFoodsPage(currentPage);
        }
    }

    public boolean canLoadMore() {
        return !isLoading && !isLastPage;
    }

    private void loadExploreFoodsPage(int page) {
        isLoading = true;
        repository.getExploreFoods(page, PAGE_SIZE, new FoodRepository.FoodPageCallback() {
            @Override
            public void onSuccess(PageResponse<FoodExploreResponse> response) {
                isLoading = false;
                if (response == null) {
                    if (page == 0) {
                        foodData.postValue(null);
                    }
                    return;
                }

                if (page == 0) {
                    loadedFoods.clear();
                }
                if (response.getContent() != null) {
                    loadedFoods.addAll(response.getContent());
                }
                isLastPage = response.isLast();
                currentPage = page + 1;
                foodData.postValue(new ArrayList<>(loadedFoods));
            }

            @Override
            public void onError() {
                isLoading = false;
                if (page == 0) {
                    foodData.postValue(null);
                }
            }
        });
    }
}
