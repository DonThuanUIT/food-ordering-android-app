package com.foodorderingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.AdminOverviewResponse;
import com.foodorderingapp.model.response.AdminUserResponse;
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.model.response.ShopResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    public void getOverview(MutableLiveData<AdminOverviewResponse> liveData) {
        ApiClient.getApiService().getAdminOverview().enqueue(new Callback<AdminOverviewResponse>() {
            @Override
            public void onResponse(Call<AdminOverviewResponse> call, Response<AdminOverviewResponse> response) {
                liveData.postValue(response.isSuccessful() ? response.body() : null);
            }

            @Override
            public void onFailure(Call<AdminOverviewResponse> call, Throwable t) {
                liveData.postValue(null);
            }
        });
    }

    public void getPendingShops(MutableLiveData<PageResponse<ShopResponse>> liveData) {
        getShopsByStatus("PENDING", liveData);
    }

    public void getShopsByStatus(String status, MutableLiveData<PageResponse<ShopResponse>> liveData) {
        getShopsByStatus(status, 0, 50, liveData);
    }

    public void getShopsByStatus(String status,
                                 int page,
                                 int size,
                                 MutableLiveData<PageResponse<ShopResponse>> liveData) {
        ApiClient.getApiService().getAdminShops(status, page, size).enqueue(new Callback<PageResponse<ShopResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ShopResponse>> call, Response<PageResponse<ShopResponse>> response) {
                liveData.postValue(response.isSuccessful() ? response.body() : null);
            }

            @Override
            public void onFailure(Call<PageResponse<ShopResponse>> call, Throwable t) {
                liveData.postValue(null);
            }
        });
    }

    public void updateShopStatus(String shopId, String status, MutableLiveData<Boolean> liveData) {
        Map<String, String> body = new HashMap<>();
        body.put("status", status);

        ApiClient.getApiService().updateAdminShopStatus(shopId, body).enqueue(new Callback<ShopResponse>() {
            @Override
            public void onResponse(Call<ShopResponse> call, Response<ShopResponse> response) {
                liveData.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<ShopResponse> call, Throwable t) {
                liveData.postValue(false);
            }
        });
    }

    public void getUsers(String search, MutableLiveData<PageResponse<AdminUserResponse>> liveData) {
        getUsers(search, null, liveData);
    }

    public void getUsers(String search, String role, MutableLiveData<PageResponse<AdminUserResponse>> liveData) {
        getUsers(search, role, 0, 50, liveData);
    }

    public void getUsers(String search,
                         String role,
                         int page,
                         int size,
                         MutableLiveData<PageResponse<AdminUserResponse>> liveData) {
        ApiClient.getApiService()
                .getAdminUsers(search, role, page, size, "createdAt", "desc")
                .enqueue(new Callback<PageResponse<AdminUserResponse>>() {
                    @Override
                    public void onResponse(Call<PageResponse<AdminUserResponse>> call, Response<PageResponse<AdminUserResponse>> response) {
                        liveData.postValue(response.isSuccessful() ? response.body() : null);
                    }

                    @Override
                    public void onFailure(Call<PageResponse<AdminUserResponse>> call, Throwable t) {
                        liveData.postValue(null);
                    }
                });
    }

    public void toggleUserLock(String userId, MutableLiveData<Boolean> liveData) {
        ApiClient.getApiService().toggleAdminUserLock(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                liveData.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                liveData.postValue(false);
            }
        });
    }
}
