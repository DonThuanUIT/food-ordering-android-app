package com.foodorderingapp.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.model.response.ShopDetailResponse;
import com.foodorderingapp.model.response.ShopResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopRepository {
    public void getShops(int page, int size, String keyword, MutableLiveData<PageResponse<ShopResponse>> liveData) {
        ApiClient.getApiService().getShops(page, size, keyword).enqueue(new Callback<PageResponse<ShopResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ShopResponse>> call, Response<PageResponse<ShopResponse>> response) {
                if (response.isSuccessful()) {
                    liveData.postValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<PageResponse<ShopResponse>> call, Throwable t) {
                liveData.postValue(null);
            }
        });
    }

    public void getShopDetail(String shopId, MutableLiveData<ShopDetailResponse> liveData) {
        ApiClient.getApiService().getShopDetail(shopId).enqueue(new Callback<ShopDetailResponse>() {
            @Override
            public void onResponse(Call<ShopDetailResponse> call, Response<ShopDetailResponse> response) {
                liveData.postValue(response.isSuccessful() ? response.body() : null);
            }

            @Override
            public void onFailure(Call<ShopDetailResponse> call, Throwable t) {
                liveData.postValue(null);
            }
        });
    }
}
