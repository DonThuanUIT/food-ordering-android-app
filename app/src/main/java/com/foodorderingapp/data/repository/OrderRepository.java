package com.foodorderingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.data.remote.api.ApiService;
import com.foodorderingapp.model.request.CheckoutRequest;
import com.foodorderingapp.model.request.ReviewRequest;
import com.foodorderingapp.model.response.OrderResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {
    private final ApiService apiService = ApiClient.getApiService();

    public void checkout(String building, String dropOff, MutableLiveData<Boolean> result) {
        CheckoutRequest request = new CheckoutRequest(building, dropOff);

        apiService.checkout(request).enqueue(new Callback<List<OrderResponse>>() {
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

    public void getActiveOrders(MutableLiveData<List<OrderResponse>> activeOrders) {
        getActiveOrders(activeOrders, null);
    }

    public void getActiveOrders(MutableLiveData<List<OrderResponse>> activeOrders,
                                MutableLiveData<String> message) {
        apiService.getActiveOrders().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                if (response.isSuccessful()) {
                    activeOrders.postValue(response.body());
                } else {
                    activeOrders.postValue(null);
                    postMessage(message, "Không tải được đơn đang xử lý");
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                activeOrders.postValue(null);
                postMessage(message, "Lỗi kết nối khi tải đơn đang xử lý");
            }
        });
    }

    public void getOrderHistory(MutableLiveData<List<OrderResponse>> orderHistory,
                                MutableLiveData<String> message) {
        apiService.getOrderHistory().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                if (response.isSuccessful()) {
                    orderHistory.postValue(response.body());
                } else {
                    orderHistory.postValue(null);
                    postMessage(message, "Không tải được lịch sử đơn");
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                orderHistory.postValue(null);
                postMessage(message, "Lỗi kết nối khi tải lịch sử đơn");
            }
        });
    }

    public void createReview(String orderId, int rating, String comment,
                             MutableLiveData<Boolean> reviewResult,
                             MutableLiveData<String> message) {
        ReviewRequest request = new ReviewRequest(rating, comment);

        apiService.createReview(orderId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                boolean success = response.isSuccessful();
                reviewResult.postValue(success);
                postMessage(message, success ? "Đã gửi đánh giá" : "Không thể gửi đánh giá cho đơn này");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                reviewResult.postValue(false);
                postMessage(message, "Lỗi kết nối khi gửi đánh giá");
            }
        });
    }

    private void postMessage(MutableLiveData<String> message, String value) {
        if (message != null) {
            message.postValue(value);
        }
    }
}
