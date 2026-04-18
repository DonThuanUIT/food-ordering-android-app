package com.foodorderingapp.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.RegisterRequest;
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
    private final MutableLiveData<String> fullNameError = new MutableLiveData<>();
    private final MutableLiveData<String> phoneError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordError = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsSuccess() {
        return isSuccess;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<String> getFullNameError() {
        return fullNameError;
    }

    public LiveData<String> getPhoneError() {
        return phoneError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<String> getConfirmPasswordError() {
        return confirmPasswordError;
    }

    public void register(String fullName, String phone, String password, String confirmPassword) {
        clearErrors();
        isSuccess.setValue(false);
        boolean isValid = true;

        if (fullName == null || fullName.trim().isEmpty()) {
            fullNameError.setValue("Họ và tên không được để trống");
            isValid = false;
        }
        if (phone == null || phone.trim().isEmpty()) {
            phoneError.setValue("Số điện thoại không được để trống");
            isValid = false;
        } else if (phone.trim().length() < 10) {
                phoneError.setValue("Số điện thoại không hợp lệ");
                isValid = false;
        }

        if (password == null || password.trim().isEmpty()) {
            passwordError.setValue("Mật khẩu không được để trống");
            isValid = false;
        }  else if (password.trim().length() < 6) {
            passwordError.setValue("Mật khẩu phải từ 6 ký tự");
            isValid = false;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            confirmPasswordError.setValue("Xác nhận mật khẩu không được để trống");
            isValid = false;
        }

        if(password != null && confirmPassword != null
            && !password.trim().isEmpty()
            && !confirmPassword.trim().isEmpty()
            && !password.trim().equals(confirmPassword.trim())) {
            confirmPasswordError.setValue("Mật khẩu không khớp");
            isValid = false;
            }

        if (!isValid) {
            return;
        }

        isLoading.setValue(true);

        RegisterRequest request = new RegisterRequest(phone.trim(), password.trim(), fullName.trim());

        ApiClient.getAuthApiService().register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    isSuccess.setValue(true);
                    message.setValue(response.body().getMessage() != null ? response.body().getMessage() : "Đăng ký thành công");
                } else {
                    String errorMessage = "Đăng ký thất bại";
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
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                message.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void clearErrors() {
        fullNameError.setValue(null);
        phoneError.setValue(null);
        passwordError.setValue(null);
        confirmPasswordError.setValue(null);
    }
}
