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
                result.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                result.postValue(false);
            }
        });
    }

    public void getActiveOrders(MutableLiveData<List<OrderResponse>> activeOrders) {
        ApiClient.getApiService().getActiveOrders().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                if (response.isSuccessful()) {
                    activeOrders.postValue(response.body());
                } else {
                    activeOrders.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                activeOrders.postValue(null);
            }
        });
    }
}
