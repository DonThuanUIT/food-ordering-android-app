package com.foodorderingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.FoodExploreResponse;
import com.foodorderingapp.model.response.PageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodRepository {
    public void getExploreFoods(
            int page,
            int size,
            MutableLiveData<PageResponse<FoodExploreResponse>> liveData
    ) {
        ApiClient.getApiService().getExploreFoods(page, size)
                .enqueue(new Callback<PageResponse<FoodExploreResponse>>() {
                    @Override
                    public void onResponse(
                            Call<PageResponse<FoodExploreResponse>> call,
                            Response<PageResponse<FoodExploreResponse>> response
                    ) {
                        if (response.isSuccessful()) {
                            liveData.postValue(response.body());
                        } else {
                            liveData.postValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<PageResponse<FoodExploreResponse>> call, Throwable t) {
                        liveData.postValue(null);
                    }
                });
    }
}