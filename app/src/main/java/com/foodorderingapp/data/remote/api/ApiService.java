package com.foodorderingapp.data.remote.api;

import com.foodorderingapp.model.request.CartItemRequest;
import com.foodorderingapp.model.request.CheckoutRequest;
import com.foodorderingapp.model.request.LoginRequest;
import com.foodorderingapp.model.request.ReviewRequest;
import com.foodorderingapp.model.request.StudentRegisterRequest;
import com.foodorderingapp.model.request.UpdateCartQuantityRequest;
import com.foodorderingapp.model.request.VendorRegisterRequest;
import com.foodorderingapp.model.request.VerifyOtpRequest;
import com.foodorderingapp.model.response.AuthResponse;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.model.response.RegisterResponse;
import com.foodorderingapp.model.response.ShopDetailResponse;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.model.response.FoodExploreResponse;
import com.foodorderingapp.model.response.CartResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
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

    @GET("shops")
    Call<PageResponse<ShopResponse>> getShops(
            @Query("page") int page,
            @Query("size") int size,
            @Query("keyword") String keyword
    );

    @GET("shops/{shopId}/detail-menu")
    Call<ShopDetailResponse> getShopDetail(@Path("shopId") String shopId);

    @GET("foods/explore")
    Call<PageResponse<FoodExploreResponse>> getExploreFoods(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("cart")
    Call<CartResponse> getCart();

    @POST("cart/items")
    Call<Void> addToCart(@Body CartItemRequest request);

    @PATCH("cart/items/{cartItemId}")
    Call<Void> updateCartItemQuantity(
            @Path("cartItemId") String cartItemId,
            @Body UpdateCartQuantityRequest request
    );

    @DELETE("cart/items/{cartItemId}")
    Call<Void> deleteCartItem(@Path("cartItemId") String cartItemId);

    @DELETE("cart/shops/{shopId}")
    Call<Void> clearShopCart(@Path("shopId") String shopId);

    @POST("orders/checkout")
    Call<List<OrderResponse>> checkout(@Body CheckoutRequest request);


    @GET("orders/active")
    Call<List<OrderResponse>> getActiveOrders();

    @GET("orders/history")
    Call<List<OrderResponse>> getOrderHistory();

    @POST("orders/{orderId}/reviews")
    Call<Void> createReview(
            @Path("orderId") String orderId,
            @Body ReviewRequest request
    );

}
