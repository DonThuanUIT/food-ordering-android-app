package com.foodorderingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.data.remote.api.ApiService;
import com.foodorderingapp.model.response.SpendingSummaryResponse;
import com.foodorderingapp.model.response.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentRepository {
    private final ApiService apiService = ApiClient.getApiService();

    public void getMyProfile(MutableLiveData<UserProfileResponse> profile,
                             MutableLiveData<String> message) {
        apiService.getMyProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call,
                                   Response<UserProfileResponse> response) {
                if (response.isSuccessful()) {
                    profile.postValue(response.body());
                } else {
                    profile.postValue(null);
                    postMessage(message, "Khong tai duoc thong tin ca nhan");
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                profile.postValue(null);
                postMessage(message, "Loi ket noi khi tai profile");
            }
        });
    }

    public void getSpendingSummary(MutableLiveData<SpendingSummaryResponse> summary,
                                   MutableLiveData<String> message) {
        apiService.getSpendingSummary().enqueue(new Callback<SpendingSummaryResponse>() {
            @Override
            public void onResponse(Call<SpendingSummaryResponse> call,
                                   Response<SpendingSummaryResponse> response) {
                if (response.isSuccessful()) {
                    summary.postValue(response.body());
                } else {
                    summary.postValue(null);
                    postMessage(message, "Khong tai duoc tong chi tieu");
                }
            }

            @Override
            public void onFailure(Call<SpendingSummaryResponse> call, Throwable t) {
                summary.postValue(null);
                postMessage(message, "Loi ket noi khi tai chi tieu");
            }
        });
    }

    private void postMessage(MutableLiveData<String> message, String value) {
        if (message != null) {
            message.postValue(value);
        }
    }
}
