package com.foodorderingapp.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.ResendOtpRequest;
import com.foodorderingapp.model.request.VerifyOtpRequest;
import com.foodorderingapp.model.response.ApiError;
import com.foodorderingapp.model.response.AuthResponse;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isVerified = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Biến kiểm soát trạng thái hết hạn của OTP
    private boolean isOtpExpired = false;

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsVerified() { return isVerified; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void setOtpExpired(boolean expired) {
        this.isOtpExpired = expired;
    }

    public void verifyOtp(String email, String otpCode) {
        if (isOtpExpired) {
            errorMessage.setValue("Mã OTP đã hết hạn. Vui lòng nhấn gửi lại mã mới.");
            return;
        }

        if (otpCode == null || otpCode.length() < 6) {
            errorMessage.setValue("Vui lòng nhập đủ 6 số");
            return;
        }

        isLoading.setValue(true);
        VerifyOtpRequest request = new VerifyOtpRequest(email, otpCode);

        ApiClient.getAuthApiService().verifyOtp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    isVerified.setValue(true);
                } else {
                    handleError(response);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void resendOtp(String email) {
        isLoading.setValue(true);
        isOtpExpired = false; // Reset trạng thái hết hạn khi gửi mã mới

        ApiClient.getAuthApiService().resendOtp(new ResendOtpRequest(email)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);
                if (!response.isSuccessful()) {
                    errorMessage.setValue("Không thể gửi lại mã");
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối");
            }
        });
    }

    private void handleError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                ApiError error = new Gson().fromJson(response.errorBody().string(), ApiError.class);
                errorMessage.setValue(error.getMessage());
            }
        } catch (Exception e) {
            errorMessage.setValue("Xác thực thất bại");
        }
    }
}
