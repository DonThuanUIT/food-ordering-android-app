package com.foodorderingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.CheckoutRequest;
import com.foodorderingapp.model.response.OrderResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {
    public void checkout(String building, String dropOff, MutableLiveData<Boolean> result) {
        CheckoutRequest request = new CheckoutRequest(building, dropOff);

        ApiClient.getApiService().checkout(request).enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                result.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                result.postValue(false);
            }
        });
    }

    public void getHistory(MutableLiveData<List<OrderResponse>> data) {
        ApiClient.getApiService().getOrderHistory().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                data.postValue(response.isSuccessful() ? response.body() : null);
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                data.postValue(null);
            }
        });
    }

    public void getActiveOrders(MutableLiveData<List<OrderResponse>> data) {
        ApiClient.getApiService().getActiveOrders().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                data.postValue(response.isSuccessful() ? response.body() : null);
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                data.postValue(null);
            }
        });
    }
}
