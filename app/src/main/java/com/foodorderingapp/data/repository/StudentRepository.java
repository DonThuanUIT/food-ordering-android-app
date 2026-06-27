package com.foodorderingapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.data.remote.api.ApiService;
import com.foodorderingapp.model.request.UpdateProfileRequest;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.SpendingSummaryResponse;
import com.foodorderingapp.model.response.StudentReviewResponse;
import com.foodorderingapp.model.response.UserProfileResponse;

import java.util.List;

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
                    postMessage(message, "Không tải được thông tin cá nhân");
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                profile.postValue(null);
                postMessage(message, "Lỗi kết nối khi tải hồ sơ");
            }
        });
    }

    public void getSpendingSummary(MutableLiveData<SpendingSummaryResponse> summary,
                                   MutableLiveData<String> message) {
        getSpendingSummary(null, null, summary, message);
    }

    public void getSpendingSummary(String from,
                                   String to,
                                   MutableLiveData<SpendingSummaryResponse> summary,
                                   MutableLiveData<String> message) {
        apiService.getSpendingSummary(from, to).enqueue(new Callback<SpendingSummaryResponse>() {
            @Override
            public void onResponse(Call<SpendingSummaryResponse> call,
                                   Response<SpendingSummaryResponse> response) {
                if (response.isSuccessful()) {
                    summary.postValue(response.body());
                } else {
                    summary.postValue(null);
                    postMessage(message, "Không tải được tổng chi tiêu");
                }
            }

            @Override
            public void onFailure(Call<SpendingSummaryResponse> call, Throwable t) {
                summary.postValue(null);
                postMessage(message, "Lỗi kết nối khi tải chi tiêu");
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
                    postMessage(message, "Không tải được danh sách tòa nhà");
                }
            }

            @Override
            public void onFailure(Call<List<BuildingResponse>> call, Throwable t) {
                buildings.postValue(null);
                postMessage(message, "Lỗi kết nối khi tải tòa nhà");
            }
        });
    }

    public void updateMyProfile(UpdateProfileRequest request,
                                 MutableLiveData<UserProfileResponse> profile,
                                 MutableLiveData<Boolean> updateResult,
                                 MutableLiveData<String> message) {
        apiService.updateMyProfile(request).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call,
                                   Response<UserProfileResponse> response) {
                boolean success = response.isSuccessful() && response.body() != null;
                if (success) {
                    profile.postValue(response.body());
                    updateResult.postValue(true);
                } else {
                    updateResult.postValue(false);
                    postMessage(message, "Không cập nhật được hồ sơ");
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                updateResult.postValue(false);
                postMessage(message, "Lỗi kết nối khi cập nhật hồ sơ");
            }
        });
    }

    public void getMyReviews(MutableLiveData<List<StudentReviewResponse>> reviews,
                             MutableLiveData<String> message) {
        apiService.getMyReviews().enqueue(new Callback<List<StudentReviewResponse>>() {
            @Override
            public void onResponse(Call<List<StudentReviewResponse>> call,
                                   Response<List<StudentReviewResponse>> response) {
                if (response.isSuccessful()) {
                    reviews.postValue(response.body());
                } else {
                    reviews.postValue(null);
                    postMessage(message, "Không tải được đánh giá của tôi");
                }
            }

            @Override
            public void onFailure(Call<List<StudentReviewResponse>> call, Throwable t) {
                reviews.postValue(null);
                postMessage(message, "Lỗi kết nối khi tải đánh giá");
            }
        });
    }

    private void postMessage(MutableLiveData<String> message, String value) {
        if (message != null) {
            message.postValue(value);
        }
    }
}
