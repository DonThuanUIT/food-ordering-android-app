package com.foodorderingapp.data.remote.api;

import com.foodorderingapp.model.request.CategoryRequest;
import com.foodorderingapp.model.request.FoodRequest;
import com.foodorderingapp.model.request.AIRecommendationRequest;
import com.foodorderingapp.model.request.CartItemRequest;
import com.foodorderingapp.model.request.CheckoutRequest;
import com.foodorderingapp.model.request.LoginRequest;
import com.foodorderingapp.model.request.ReviewRequest;
import com.foodorderingapp.model.request.ReviewSubmitRequest;
import com.foodorderingapp.model.request.SendChatMessageRequest;
import com.foodorderingapp.model.request.StudentRegisterRequest;
import com.foodorderingapp.model.request.UpdateCartQuantityRequest;
import com.foodorderingapp.model.request.UpdateProfileRequest;
import com.foodorderingapp.model.request.VendorRegisterRequest;
import com.foodorderingapp.model.request.VerifyOtpRequest;
import com.foodorderingapp.model.request.ShopUpdateRequest;
import com.foodorderingapp.model.request.UpdateStatusRequest;
import com.foodorderingapp.model.request.VoucherCreateRequest;
import com.foodorderingapp.model.response.AdminOverviewResponse;
import com.foodorderingapp.model.response.AdminUserResponse;
import com.foodorderingapp.model.response.AuthResponse;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.CategoryResponse;
import com.foodorderingapp.model.response.ChatMessageResponse;
import com.foodorderingapp.model.response.ChatRoomResponse;
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.model.response.DropOffPointResponse;
import com.foodorderingapp.model.response.RegisterResponse;
import com.foodorderingapp.model.response.SpendingSummaryResponse;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.model.response.StudentReviewResponse;
import com.foodorderingapp.model.response.ReviewResponse;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.model.response.ShopDetailResponse;
import com.foodorderingapp.model.response.FoodExploreResponse;
import com.foodorderingapp.model.response.CartResponse;
import com.foodorderingapp.model.response.VoucherResponse;
import com.foodorderingapp.model.response.VendorDashboardResponse;
import com.foodorderingapp.model.response.UploadImageResponse;
import com.foodorderingapp.model.response.UserProfileResponse;
import com.foodorderingapp.model.response.AIRecommendationResponse;

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
    Call<UploadImageResponse> uploadImage(@Part MultipartBody.Part file);

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

    @POST("vendor/shops/{shopId}/close/otp-request")
    Call<Void> requestCloseShopOtp(
            @Path("shopId") UUID shopId
    );

    @POST("vendor/shops/{shopId}/close")
    Call<Void> confirmCloseShop(
            @Path("shopId") UUID shopId,
            @Body com.foodorderingapp.model.request.ShopCloseRequest request
    );

    // --- Vendor Food Management ---
    @GET("vendor/shops/{shopId}/foods")
    Call<PageResponse<FoodResponse>> getAllFoods(
            @Path("shopId") UUID shopId,
            @Query("categoryId") UUID categoryId,
            @Query("page") Integer page,
            @Query("size") Integer size
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

    @DELETE("vendor/shops/{shopId}/categories/{categoryId}")
    Call<Void> deleteCategory(
            @Path("shopId") UUID shopId,
            @Path("categoryId") UUID categoryId
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

    // --- Vendor Order Management ---
    @GET("vendor/shops/{shopId}/orders")
    Call<List<OrderResponse>> getShopOrders(
            @Path("shopId") UUID shopId,
            @Query("status") String status
    );

    @PATCH("vendor/shops/{shopId}/orders/{orderId}/status")
    Call<OrderResponse> updateOrderStatus(
            @Path("shopId") UUID shopId,
            @Path("orderId") UUID orderId,
            @Body UpdateStatusRequest request
    );

    @GET("shops")
    Call<PageResponse<ShopResponse>> getShops(
            @Query("page") int page,
            @Query("size") int size,
            @Query("keyword") String keyword
    );

    @GET("shops/{shopId}/detail-menu")
    Call<ShopDetailResponse> getShopDetail(@Path("shopId") String shopId);

    @GET("shops/{shopId}/vouchers")
    Call<List<VoucherResponse>> getActiveVouchers(@Path("shopId") String shopId);

    @GET("foods/explore")
    Call<PageResponse<FoodExploreResponse>> getExploreFoods(
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("ai/recommend")
    Call<List<AIRecommendationResponse>> getAIRecommendations(
            @Body AIRecommendationRequest request,
            @Query("shopId") UUID shopId
    );

    @GET("buildings")
    Call<List<BuildingResponse>> getBuildings();

    @GET("buildings/{buildingId}/drop-off-points")
    Call<List<DropOffPointResponse>> getDropOffPoints(@Path("buildingId") String buildingId);

    @GET("users/me")
    Call<UserProfileResponse> getMyProfile();

    @PATCH("users/me")
    Call<UserProfileResponse> updateMyProfile(@Body UpdateProfileRequest request);

    @GET("users/me/spending-summary")
    Call<SpendingSummaryResponse> getSpendingSummary(
            @Query("from") String from,
            @Query("to") String to
    );

    @GET("users/me/reviews")
    Call<List<StudentReviewResponse>> getMyReviews();

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
    Call<OrderResponse> checkout(@Body CheckoutRequest request);

    @GET("orders/active")
    Call<List<OrderResponse>> getActiveOrders();

    @GET("orders/history")
    Call<List<OrderResponse>> getOrderHistory();

    @GET("orders/available-for-delivery")
    Call<List<OrderResponse>> getAvailableOrdersForDelivery();

    @POST("orders/{orderId}/claim")
    Call<OrderResponse> claimOrder(@Path("orderId") String orderId);

    @POST("orders/{orderId}/location")
    Call<OrderResponse> updateShipperLocation(
            @Path("orderId") String orderId,
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude
    );

    @PUT("orders/{orderId}/status")
    Call<OrderResponse> updateDeliveryOrderStatus(
            @Path("orderId") String orderId,
            @Body UpdateStatusRequest request
    );

    @GET("orders/shipper/active")
    Call<List<OrderResponse>> getShipperActiveOrders();

    @GET("orders/shipper/history")
    Call<List<OrderResponse>> getShipperOrderHistory();

    @POST("orders/{orderId}/reviews")
    Call<Void> createReview(
            @Path("orderId") String orderId,
            @Body ReviewSubmitRequest request
    );

    @GET("orders/shop/{shopId}/reviews")
    Call<List<ReviewResponse>> getShopReviews(@Path("shopId") String shopId);

    @GET("orders/shop/{shopId}/delivery-reviews")
    Call<List<ReviewResponse>> getDeliveryReviews(@Path("shopId") String shopId);

    @GET("orders/shop/{shopId}/rating")
    Call<Double> getShopRating(@Path("shopId") String shopId);

    @GET("orders/food/{foodId}/reviews")
    Call<List<ReviewResponse>> getFoodReviews(@Path("foodId") String foodId);

    @GET("orders/food/{foodId}/rating")
    Call<Double> getFoodRating(@Path("foodId") String foodId);

    @GET("orders/{shopId}/dashboard")
    Call<VendorDashboardResponse> getDashboardStats(
            @Path("shopId") UUID shopId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    // --- Chat ---
    @GET("chat/rooms")
    Call<List<ChatRoomResponse>> getChatRooms();

    @GET("chat/shops/{shopId}/room")
    Call<ChatRoomResponse> getChatRoomByShop(@Path("shopId") String shopId);

    @GET("chat/orders/{orderId}/room")
    Call<ChatRoomResponse> getChatRoomByOrder(@Path("orderId") String orderId);

    @GET("chat/{roomId}/history")
    Call<List<ChatMessageResponse>> getChatHistory(@Path("roomId") String roomId);

    @POST("chat/send")
    Call<ChatMessageResponse> sendChatMessage(@Body SendChatMessageRequest request);

    @PUT("chat/{roomId}/read")
    Call<Void> markChatRoomRead(@Path("roomId") String roomId);

    @GET("chat/unread-count")
    Call<Long> getChatUnreadCount();

    @GET("chat/rooms/{roomId}/unread-count")
    Call<Long> getChatRoomUnreadCount(@Path("roomId") String roomId);

    // --- Admin ---
    @GET("admin/overview")
    Call<AdminOverviewResponse> getAdminOverview();

    @GET("admin/shops")
    Call<PageResponse<ShopResponse>> getAdminShops(
            @Query("status") String status,
            @Query("page") int page,
            @Query("size") int size
    );

    @PATCH("admin/shops/{shopId}/status")
    Call<ShopResponse> updateAdminShopStatus(
            @Path("shopId") String shopId,
            @Body Map<String, String> body
    );

    @GET("admin/users")
    Call<PageResponse<AdminUserResponse>> getAdminUsers(
            @Query("search") String search,
            @Query("role") String role,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("direction") String direction
    );

    @PATCH("admin/users/{userId}/lock")
    Call<Void> toggleAdminUserLock(@Path("userId") String userId);

    /**
     * Gửi FCM Token lên Backend mỗi khi người dùng Mở app (hoặc Vừa Đăng Nhập xong)
     * Body của Map ví dụ: {"fcmToken": "asd...", "deviceInfo": "Android 14"}
     */
    @POST("notifications/device-token")
    Call<Void> registerDeviceToken(@Body Map<String, String> body);

    /**
     * Gỡ bỏ Token ra khỏi Database của Backend trước khi bấm nút ĐĂNG XUẤT.
     */
    @DELETE("notifications/device-token")
    Call<Void> removeDeviceToken(@Query("fcmToken") String fcmToken);
}
