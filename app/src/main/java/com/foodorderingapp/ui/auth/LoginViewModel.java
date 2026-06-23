package com.foodorderingapp.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.LoginRequest;
import com.foodorderingapp.model.response.ApiError;
import com.foodorderingapp.model.response.AuthResponse;
import com.foodorderingapp.utils.TokenManager;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<AuthResponse> loginResponse = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<AuthResponse> getLoginResponse() { return loginResponse; }

    public void login(String phone, String password) {
        if (phone.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        isLoading.setValue(true);
        LoginRequest request = new LoginRequest(phone, password);

        ApiClient.getApiService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Lưu Token
                    AuthResponse authResponse = response.body();
                    TokenManager.getInstance().saveTokens(
                            authResponse.getAccessToken(),
                            authResponse.getRefreshToken()
                    );
                    TokenManager.getInstance().saveUserSession(
                            authResponse.getPhone(),
                            authResponse.getRole(),
                            authResponse.getFullName()
                    );
                    sendFcmTokenToServer();
                    loginResponse.setValue(authResponse);
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

    private void handleError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                ApiError error = new Gson().fromJson(response.errorBody().string(), ApiError.class);
                errorMessage.setValue(error.getMessage());
            } else {
                errorMessage.setValue("Đăng nhập thất bại");
            }
        } catch (IOException e) {
            errorMessage.setValue("Lỗi hệ thống");
        }
    }

    private void sendFcmTokenToServer() {
        // Chủ động gọi thẳng Firebase để đòi Token hiện tại
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        android.util.Log.w("FCM_SERVICE", " Lấy FCM token từ Firebase thất bại", task.getException());
                        return;
                    }

                    String fcmToken = task.getResult();

                    // Lưu dự phòng vào Local
                    TokenManager.getInstance().saveFcmToken(fcmToken);

                    // 2. Đóng gói thông tin thiết bị
                    String deviceInfo = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
                    java.util.Map<String, String> body = new java.util.HashMap<>();
                    body.put("fcmToken", fcmToken);
                    body.put("deviceInfo", deviceInfo);

                    // 3. Bắn lên Server
                    ApiClient.getApiService().registerDeviceToken(body).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                android.util.Log.d("FCM_SERVICE", " Đã nộp FCM Token lên Backend thành công!");
                            } else {
                                android.util.Log.e("FCM_SERVICE", " Nộp FCM Token thất bại. Code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            android.util.Log.e("FCM_SERVICE", " Rớt mạng khi nộp FCM Token: " + t.getMessage());
                        }
                    });
                });
    }
}
