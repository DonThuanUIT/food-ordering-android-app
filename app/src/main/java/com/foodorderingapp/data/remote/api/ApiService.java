package com.foodorderingapp.data.remote.api;

import com.foodorderingapp.model.request.CategoryRequest;
import com.foodorderingapp.model.request.FoodRequest;
import com.foodorderingapp.model.request.LoginRequest;
import com.foodorderingapp.model.request.StudentRegisterRequest;
import com.foodorderingapp.model.request.VendorRegisterRequest;
import com.foodorderingapp.model.request.VerifyOtpRequest;
import com.foodorderingapp.model.response.AuthResponse;
import com.foodorderingapp.model.response.CategoryResponse;
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.model.response.RegisterResponse;
import com.foodorderingapp.model.response.ShopResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/register/student")
    Call<RegisterResponse> registerStudent(@Body StudentRegisterRequest request);

    @POST("auth/register/vendor")
    Call<RegisterResponse> registerVendor(@Body VendorRegisterRequest request);

    @POST("auth/verify-otp")
    Call<AuthResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    // --- Image Upload ---
    @Multipart
    @POST("upload/image")
    Call<Map<String, String>> uploadImage(@Part MultipartBody.Part file);

    // --- Vendor Shop Management ---
    @GET("vendor/shops")
    Call<List<ShopResponse>> getVendorShops();

    // --- Vendor Food Management ---
    @GET("vendor/shops/{shopId}/foods")
    Call<List<FoodResponse>> getAllFoods(
            @Path("shopId") UUID shopId,
            @Query("categoryId") UUID categoryId
    );

    @POST("vendor/shops/{shopId}/foods")
    Call<FoodResponse> createFood(
            @Path("shopId") UUID shopId,
            @Body FoodRequest request
    );

    @PATCH("vendor/shops/{shopId}/foods/{foodId}/toggle")
    Call<FoodResponse> toggleFoodAvailability(
            @Path("shopId") UUID shopId,
            @Path("foodId") UUID foodId
    );

    @PUT("vendor/shops/{shopId}/foods/{foodId}")
    Call<FoodResponse> updateFood(
            @Path("shopId") UUID shopId,
            @Path("foodId") UUID foodId,
            @Body FoodRequest request
    );

    @DELETE("vendor/shops/{shopId}/foods/{foodId}")
    Call<Void> deleteFood(
            @Path("shopId") UUID shopId,
            @Path("foodId") UUID foodId
    );

    // --- Categories ---
    @GET("vendor/shops/{shopId}/categories")
    Call<List<CategoryResponse>> getAllCategories(
            @Path("shopId") UUID shopId
    );

    @POST("vendor/shops/{shopId}/categories")
    Call<CategoryResponse> createCategory(
            @Path("shopId") UUID shopId,
            @Body CategoryRequest request
    );
}
