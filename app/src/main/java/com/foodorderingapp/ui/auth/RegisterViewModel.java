package com.foodorderingapp.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.StudentRegisterRequest;
import com.foodorderingapp.model.request.VendorRegisterRequest;
import com.foodorderingapp.model.response.ApiError;
import com.foodorderingapp.model.response.AuthResponse;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getMessage() { return message; }

    public void register(String fullName, String phone, String email, String password,
                         boolean isStudent, String buildingId, String shopName) {

        isLoading.setValue(true);
        isSuccess.setValue(null); // Reset để Observer luôn nhận được sự kiện mới

        Call<AuthResponse> call;
        if (isStudent) {
            StudentRegisterRequest request = new StudentRegisterRequest(
                    phone.trim(), password.trim(), fullName.trim(), email.trim(), buildingId
            );
            call = ApiClient.getAuthApiService().registerStudent(request);
        } else {
            VendorRegisterRequest request = new VendorRegisterRequest(
                    phone.trim(), password.trim(), fullName.trim(), email.trim(),
                    shopName.trim(), "", "", ""
            );
            call = ApiClient.getAuthApiService().registerVendor(request);
        }

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);
                // Nếu Server gửi mail thành công và trả về code 200/201
                if (response.isSuccessful()) {
                    isSuccess.setValue(true);
                    message.setValue("Mã OTP đã được gửi vào Email của bạn.");
                } else {
                    isSuccess.setValue(false);
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                isSuccess.setValue(false);
                message.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void handleErrorResponse(Response<AuthResponse> response) {
        String errorMessage = "Đăng ký thất bại (Lỗi " + response.code() + ")";
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ApiError apiError = new Gson().fromJson(errorJson, ApiError.class);
                if (apiError != null && apiError.getMessage() != null) {
                    errorMessage = apiError.getMessage();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        message.setValue(errorMessage);
    }
}