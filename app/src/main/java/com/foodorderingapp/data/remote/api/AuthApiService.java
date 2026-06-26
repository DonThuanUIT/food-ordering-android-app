package com.foodorderingapp.data.remote.api;

import com.foodorderingapp.model.request.ResendOtpRequest;
import com.foodorderingapp.model.request.BaseRegisterRequest;
import com.foodorderingapp.model.request.StudentRegisterRequest;
import com.foodorderingapp.model.request.VendorRegisterRequest;
import com.foodorderingapp.model.request.VerifyOtpRequest;
import com.foodorderingapp.model.response.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("auth/register/student")
    Call<AuthResponse> registerStudent(@Body StudentRegisterRequest request);

    @POST("auth/register/vendor")
    Call<AuthResponse> registerVendor(@Body VendorRegisterRequest request);

    @POST("auth/register/shipper")
    Call<AuthResponse> registerShipper(@Body BaseRegisterRequest request);

    @POST("auth/verify-otp")
    Call<AuthResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auth/resend-otp")
    Call<AuthResponse> resendOtp(@Body ResendOtpRequest request);

}
