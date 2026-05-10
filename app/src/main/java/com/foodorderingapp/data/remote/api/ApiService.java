package com.foodorderingapp.data.remote.api;

import com.foodorderingapp.model.request.LoginRequest;
import com.foodorderingapp.model.request.StudentRegisterRequest;
import com.foodorderingapp.model.request.VendorRegisterRequest;
import com.foodorderingapp.model.request.VerifyOtpRequest;
import com.foodorderingapp.model.response.AuthResponse;
import com.foodorderingapp.model.response.RegisterResponse;
import com.foodorderingapp.model.response.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/register/student")
    Call<RegisterResponse> registerStudent(@Body StudentRegisterRequest request);

    @POST("auth/register/vendor")
    Call<RegisterResponse> registerVendor(@Body VendorRegisterRequest request);

    @POST("auth/verify-otp")
    Call<AuthResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
}