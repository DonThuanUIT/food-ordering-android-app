package com.foodorderingapp.data.remote.api;

import com.foodorderingapp.model.request.RegisterRequest;
import com.foodorderingapp.model.response.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/register/student")
    Call<AuthResponse> registerStudent(@Body RegisterRequest request);
}
