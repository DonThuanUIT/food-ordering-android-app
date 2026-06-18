package com.foodorderingapp.data.remote.api;

import com.foodorderingapp.model.request.CategoryRequest;
import com.foodorderingapp.model.request.FoodRequest;
import com.foodorderingapp.model.request.CartItemRequest;
import com.foodorderingapp.model.request.CheckoutRequest;
import com.foodorderingapp.model.request.LoginRequest;
import com.foodorderingapp.model.request.ReviewRequest;
import com.foodorderingapp.model.request.StudentRegisterRequest;
import com.foodorderingapp.model.request.UpdateCartQuantityRequest;
import com.foodorderingapp.model.request.VendorRegisterRequest;
import com.foodorderingapp.model.request.VerifyOtpRequest;
import com.foodorderingapp.model.request.ShopUpdateRequest;
import com.foodorderingapp.model.request.VoucherCreateRequest;
import com.foodorderingapp.model.response.AuthResponse;
import com.foodorderingapp.model.response.CategoryResponse;
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.model.response.RegisterResponse;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.model.response.ShopDetailResponse;
import com.foodorderingapp.model.response.FoodExploreResponse;
import com.foodorderingapp.model.response.CartResponse;
import com.foodorderingapp.model.response.VoucherResponse;

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

    @PUT("vendor/shops/{shopId}/profile")
    Call<ShopResponse> updateShopProfile(
            @Path("shopId") UUID shopId,
            @Body ShopUpdateRequest request
    );

    @PATCH("vendor/shops/{shopId}/status")
    Call<ShopResponse> toggleShopStatus(
            @Path("shopId") UUID shopId,
            @Body Map<String, Boolean> body
    );

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

    @PUT("vendor/shops/{shopId}/categories/{categoryId}")
    Call<CategoryResponse> updateCategory(
            @Path("shopId") UUID shopId,
            @Path("categoryId") UUID categoryId,
            @Body CategoryRequest request
    );

    // --- Vendor Voucher Management ---
    @GET("vendor/shops/{shopId}/vouchers")
    Call<List<VoucherResponse>> getShopVouchers(
            @Path("shopId") UUID shopId
    );

    @POST("vendor/shops/{shopId}/vouchers")
    Call<VoucherResponse> createVoucher(
            @Path("shopId") UUID shopId,
            @Body VoucherCreateRequest request
    );

    @PUT("vendor/shops/{shopId}/vouchers/{voucherId}")
    Call<VoucherResponse> updateVoucher(
            @Path("shopId") UUID shopId,
            @Path("voucherId") UUID voucherId,
            @Body VoucherCreateRequest request
    );

    @DELETE("vendor/shops/{shopId}/vouchers/{voucherId}")
    Call<Void> deleteVoucher(
            @Path("shopId") UUID shopId,
            @Path("voucherId") UUID voucherId
    );

    @PATCH("vendor/shops/{shopId}/vouchers/{voucherId}/status")
    Call<VoucherResponse> toggleVoucherStatus(
            @Path("shopId") UUID shopId,
            @Path("voucherId") UUID voucherId,
            @Body Map<String, Boolean> body
    );

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
