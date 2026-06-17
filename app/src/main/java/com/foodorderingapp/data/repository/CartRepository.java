package com.foodorderingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.CartItemRequest;
import com.foodorderingapp.model.request.UpdateCartQuantityRequest;
import com.foodorderingapp.model.response.CartResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    public void addToCart(String foodId, int quantity, String note, MutableLiveData<Boolean> result) {
        CartItemRequest request = new CartItemRequest(foodId, quantity, note);

        ApiClient.getApiService().addToCart(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(false);
            }
        });
    }

    public void getCart(MutableLiveData<CartResponse> cartData) {
        ApiClient.getApiService().getCart().enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful()) {
                    cartData.postValue(response.body());
                } else {
                    cartData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                cartData.postValue(null);
            }
        });

    }

    public void updateCartItemQuantity(String cartItemId, int quantity, MutableLiveData<Boolean> result) {
        UpdateCartQuantityRequest request = new UpdateCartQuantityRequest(quantity);

        ApiClient.getApiService().updateCartItemQuantity(cartItemId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(false);
            }
        });
    }

    public void deleteCartItem(String cartItemId, MutableLiveData<Boolean> result) {
        ApiClient.getApiService().deleteCartItem(cartItemId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(false);
            }
        });
    }

    public void clearShopCart(String shopId, MutableLiveData<Boolean> result) {
        ApiClient.getApiService().clearShopCart(shopId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(false);
            }
        });
    }
}
