package com.foodorderingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.data.remote.api.ApiService;
import com.foodorderingapp.model.request.CheckoutRequest;
import com.foodorderingapp.model.request.ReviewRequest;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.DropOffPointResponse;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.model.response.VoucherResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.foodorderingapp.model.request.ReviewSubmitRequest;
import java.util.ArrayList;

public class OrderRepository {
    private final ApiService apiService = ApiClient.getApiService();

    public void checkout(String shopId, List<String> cartItemIds, String paymentMethod,
                         String buildingId, String dropOffPointId,
                         String voucherCode, MutableLiveData<Boolean> result,
                         MutableLiveData<String> message) {
        CheckoutRequest request = new CheckoutRequest(
                shopId,
                cartItemIds,
                paymentMethod,
                buildingId,
                dropOffPointId,
                voucherCode,
                null
        );

        apiService.checkout(request).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                boolean success = response.isSuccessful();
                result.postValue(success);
                if (!success) {
                    postMessage(message, "Khong the dat hang. Kiem tra thong tin nhan hang hoac ma giam gia.");
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                result.postValue(false);
                postMessage(message, "Loi ket noi khi dat hang");
            }
        });
    }

    public void getBuildings(MutableLiveData<List<BuildingResponse>> buildings,
                             MutableLiveData<String> message) {
        apiService.getBuildings().enqueue(new Callback<List<BuildingResponse>>() {
            @Override
            public void onResponse(Call<List<BuildingResponse>> call,
                                   Response<List<BuildingResponse>> response) {
                if (response.isSuccessful()) {
                    buildings.postValue(response.body());
                } else {
                    buildings.postValue(null);
                    postMessage(message, "Khong tai duoc danh sach toa nha");
                }
            }

            @Override
            public void onFailure(Call<List<BuildingResponse>> call, Throwable t) {
                buildings.postValue(null);
                postMessage(message, "Loi ket noi khi tai toa nha");
            }
        });
    }

    public void getDropOffPoints(String buildingId,
                                 MutableLiveData<List<DropOffPointResponse>> dropOffPoints,
                                 MutableLiveData<String> message) {
        apiService.getDropOffPoints(buildingId).enqueue(new Callback<List<DropOffPointResponse>>() {
            @Override
            public void onResponse(Call<List<DropOffPointResponse>> call,
                                   Response<List<DropOffPointResponse>> response) {
                if (response.isSuccessful()) {
                    dropOffPoints.postValue(response.body());
                } else {
                    dropOffPoints.postValue(null);
                    postMessage(message, "Khong tai duoc diem nhan hang");
                }
            }

            @Override
            public void onFailure(Call<List<DropOffPointResponse>> call, Throwable t) {
                dropOffPoints.postValue(null);
                postMessage(message, "Loi ket noi khi tai diem nhan hang");
            }
        });
    }

    public void getActiveVouchers(String shopId,
                                  MutableLiveData<List<VoucherResponse>> vouchers,
                                  MutableLiveData<String> message) {
        apiService.getActiveVouchers(shopId).enqueue(new Callback<List<VoucherResponse>>() {
            @Override
            public void onResponse(Call<List<VoucherResponse>> call,
                                   Response<List<VoucherResponse>> response) {
                if (response.isSuccessful()) {
                    vouchers.postValue(response.body());
                } else {
                    vouchers.postValue(null);
                    postMessage(message, "Khong tai duoc voucher cua quan");
                }
            }

            @Override
            public void onFailure(Call<List<VoucherResponse>> call, Throwable t) {
                vouchers.postValue(null);
                postMessage(message, "Loi ket noi khi tai voucher");
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
        ReviewSubmitRequest request = new ReviewSubmitRequest();
        request.setOrderRating(rating);
        request.setOrderComment(comment);
        request.setShopRating(5);
        request.setShopComment("");
        request.setFoodReviews(new ArrayList<>());

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
