package com.example.platemate;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    // Auth endpoints
    @POST("/api/auth/login")
    Call<LoginUserDetails> login(@Body LoginInputDetails loginInputDetails);

    @POST("/api/auth/signup")
    Call<LoginUserDetails> signup(@Body SignUpInputDetails signUpDetails);

    @POST("/api/auth/refresh")
    Call<TokenRefreshResponse> refreshToken(@Body TokenRefreshRequest refreshRequest);

    // Provider endpoints
    @POST("/api/provider/details")
    Call<java.util.Map<String, Object>> saveProviderDetails(@Body java.util.Map<String, Object> providerDetails);

    @GET("/api/provider/details")
    Call<java.util.Map<String, Object>> getProviderDetails();

    @GET("/api/provider/profile-complete")
    Call<ProfileStatusResponse> checkProfileComplete();

    // Product endpoints (legacy - kept for backward compatibility)
    @GET("/api/provider/products")
    Call<List<Product>> getProviderProducts();

    // Provider Menu Items endpoints
    @GET("/api/providers/menu-items")
    Call<List<MenuItemResponse>> getProviderMenuItems();

    @Multipart
    @POST("/api/providers/menu-items")
    Call<MenuItemResponse> createProviderMenuItem(
        @Part("data") RequestBody data,
        @Part MultipartBody.Part image  // Optional - pass null if no image
    );

    @Multipart
    @PUT("/api/providers/menu-items/{id}")
    Call<MenuItemResponse> updateProviderMenuItem(
        @Path("id") Long id,
        @Part("data") RequestBody data,
        @Part MultipartBody.Part image
    );

    @DELETE("/api/providers/menu-items/{id}")
    Call<Void> deleteProviderMenuItem(@Path("id") Long id);

    @GET("/api/providers/menu-items/{id}")
    Call<MenuItemResponse> getProviderMenuItemById(@Path("id") Long id);

    // Legacy product endpoints (deprecated - use menu-items instead)
    @POST("/api/products")
    Call<Product> createProduct(@Body Product product);

    @PUT("/api/products/{id}")
    Call<Product> updateProduct(@Path("id") Long id, @Body Product product);

    @DELETE("/api/products/{id}")
    Call<Void> deleteProduct(@Path("id") Long id);

    @GET("/api/products/{id}")
    Call<Product> getProductById(@Path("id") Long id);

    // Category endpoints
    @GET("/api/categories")
    Call<List<Category>> getCategories();

    @GET("/api/categories/{id}")
    Call<Category> getCategoryById(@Path("id") Long id);

    // Customer Menu Items endpoints
    @GET("/api/customers/menu-items")
    Call<MenuItemResponse> getCustomerMenuItems(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @GET("/api/customers/menu-items/category/{categoryId}")
    Call<MenuItemResponse> getCustomerMenuItemsByCategory(
            @Path("categoryId") Long categoryId,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @GET("/api/customers/menu-items/{id}")
    Call<MenuItem> getCustomerMenuItemById(@Path("id") Long id);

    @GET("/api/customers/menu-items/search")
    Call<MenuItemResponse> searchCustomerMenuItems(
            @Query("q") String query,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    // Cart endpoints
    @POST("/api/customers/cart")
    Call<CartItem> addToCart(@Body AddToCartRequest request);

    @GET("/api/customers/cart")
    Call<CartSummary> getCart();

    @PUT("/api/customers/cart/{cartItemId}")
    Call<CartItem> updateCartItem(
            @Path("cartItemId") Long cartItemId,
            @Body UpdateCartRequest request
    );

    @DELETE("/api/customers/cart/{cartItemId}")
    Call<Void> removeCartItem(@Path("cartItemId") Long cartItemId);

    @DELETE("/api/customers/cart")
    Call<Void> clearCart();

    // Order endpoints
    @POST("/api/customers/orders")
    Call<Order> createOrder(@Body CreateOrderRequest request);

    @GET("/api/customers/orders")
    Call<List<Order>> getCustomerOrders();

    @GET("/api/customers/orders/{id}")
    Call<Order> getCustomerOrder(@Path("id") Long id);

    @POST("/api/customers/orders/{id}/cancel")
    Call<Order> cancelOrder(@Path("id") Long id);
    
    // Provider Order endpoints
    @GET("/api/providers/orders")
    Call<List<Order>> getProviderOrders();
    
    @GET("/api/providers/orders/{id}")
    Call<Order> getProviderOrder(@Path("id") Long id);
    
    @PUT("/api/providers/orders/{id}/status")
    Call<Order> updateOrderStatus(@Path("id") Long id, @Body java.util.Map<String, String> statusRequest);
    
    // Payment endpoints
    @POST("/api/customers/payments/orders/{orderId}")
    Call<PaymentOrderResponse> createPaymentOrder(@Path("orderId") Long orderId);
    
    @POST("/api/payments/verify/{orderId}")
    Call<Order> verifyPayment(@Path("orderId") Long orderId, @Body VerifyPaymentRequest paymentData);

    // Customer Profile endpoints - using /api/users/{id} instead of /api/customers/profile
    @GET("/api/users/{id}")
    Call<User> getCustomerProfile(@Path("id") Long userId);

    @PUT("/api/users/{id}")
    Call<User> updateCustomerProfile(@Path("id") Long userId, @Body User user);

    // Image upload endpoint (using ImageController)
    @Multipart
    @POST("/images/upload/{imageType}/{ownerId}")
    Call<Image> uploadImage(
        @Path("imageType") String imageType,
        @Path("ownerId") Long ownerId,
        @Part MultipartBody.Part file
    );
    
    // User profile image upload endpoint
    @Multipart
    @POST("/api/users/{id}/profile-image")
    Call<Image> uploadUserProfileImage(
        @Path("id") Long userId,
        @Part MultipartBody.Part file
    );
    
    // Provider profile image upload endpoint
    @Multipart
    @POST("/api/tiffin-providers/{id}/profile-image")
    Call<Image> uploadProviderProfileImage(
        @Path("id") Long providerId,
        @Part MultipartBody.Part file
    );
    
    // Delivery Partner profile image upload endpoint
    @Multipart
    @POST("/api/delivery-partners/{id}/profile-image")
    Call<Image> uploadDeliveryPartnerProfileImage(
        @Path("id") Long deliveryPartnerId,
        @Part MultipartBody.Part file
    );
    
    // Get image by ID
    @GET("/images/view/{id}")
    Call<okhttp3.ResponseBody> getImage(@Path("id") Long id);

    // Customer Address endpoints - using /api/users/{userId}/address
    @POST("/api/users/{userId}/address")
    Call<Address> saveOrUpdateCustomerAddress(@Path("userId") Long userId, @Body AddressRequest addressRequest);
    
    // Customer update endpoint for fullName and DOB
    @PUT("/api/customers/{id}")
    Call<CustomerUpdateResponse> updateCustomer(@Path("id") Long id, @Body CustomerUpdateRequest request);
    
    // Get customer by userId (we'll need to add this endpoint or use list and filter)
    @GET("/api/customers")
    Call<List<CustomerUpdateResponse>> getCustomers();
    
    // Get customer by userId - we'll add this endpoint to backend
    @GET("/api/customers/user/{userId}")
    Call<CustomerUpdateResponse> getCustomerByUserId(@Path("userId") Long userId);
    
    // Delivery Zone endpoints
    @GET("/api/delivery-zones")
    Call<List<DeliveryZone>> getDeliveryZones();
    
    // Provider Delivery Partners endpoints
    @GET("/api/providers/delivery-partners")
    Call<List<DeliveryPartner>> getProviderDeliveryPartners();
    
    @GET("/api/providers/delivery-partners/available")
    Call<List<DeliveryPartner>> getAvailableDeliveryPartners();
    
    @GET("/api/providers/delivery-partners/{id}")
    Call<DeliveryPartner> getProviderDeliveryPartnerById(@Path("id") Long id);
    
    @POST("/api/providers/delivery-partners")
    Call<DeliveryPartner> createProviderDeliveryPartner(@Body DeliveryPartnerCreateRequest request);
    
    @PUT("/api/providers/delivery-partners/{id}")
    Call<DeliveryPartner> updateProviderDeliveryPartner(@Path("id") Long id, @Body DeliveryPartnerUpdateRequest request);
    
    @DELETE("/api/providers/delivery-partners/{id}")
    Call<Void> deleteProviderDeliveryPartner(@Path("id") Long id);
    
    // Rating & Review endpoints
    public static class RateMenuItemRequest {
        public Long orderId;
        public Long menuItemId;
        public Integer rating;
        public String review;
    }
    
    public static class RatingSummary {
        public long count;
        public double average;
    }
    
    @POST("/api/customers/ratings/menu-item")
    Call<RatingReview> rateMenuItem(@Body RateMenuItemRequest request);
    
    @GET("/api/ratings/menu-item/{menuItemId}")
    Call<List<RatingReview>> getMenuItemReviews(@Path("menuItemId") Long menuItemId);
    
    @GET("/api/ratings/menu-item/{menuItemId}/summary")
    Call<RatingSummary> getMenuItemRatingSummary(@Path("menuItemId") Long menuItemId);
    // Delivery Partner Order endpoints
    @GET("/api/delivery-partners/orders")
    Call<List<Order>> getDeliveryPartnerOrders();
    
    @GET("/api/delivery-partners/available-orders")
    Call<List<Order>> getAvailableOrdersForDelivery();
    
    @GET("/api/delivery-partners/orders/{id}")
    Call<Order> getDeliveryPartnerOrder(@Path("id") Long id);
    
    // Delivery Partner Profile endpoints
    @GET("/api/delivery-partners")
    Call<List<DeliveryPartner>> getDeliveryPartners(); // For delivery partners, returns their own profile(s)
    
    @GET("/api/delivery-partners/{id}")
    Call<DeliveryPartner> getDeliveryPartnerById(@Path("id") Long id);
    
    @GET("/api/delivery-partners/{id}/profile-image-id")
    Call<Long> getDeliveryPartnerProfileImageId(@Path("id") Long id);
    
    @PUT("/api/delivery-partners/{id}")
    Call<DeliveryPartner> updateDeliveryPartner(@Path("id") Long id, @Body DeliveryPartnerUpdateRequest request);
    
    @POST("/api/delivery-partners/orders/{id}/accept")
    Call<Order> acceptOrder(@Path("id") Long id);
    
    @POST("/api/delivery-partners/orders/{id}/pickup")
    Call<Order> pickupOrder(@Path("id") Long id);
    
    @POST("/api/delivery-partners/orders/{id}/deliver")
    Call<Order> deliverOrder(@Path("id") Long id, @Body java.util.Map<String, String> otpRequest);
    
    @POST("/api/delivery-partners/orders/{id}/verify-otp")
    Call<java.util.Map<String, Object>> verifyDeliveryOTP(@Path("id") Long id, @Body java.util.Map<String, String> otpRequest);
    
    // Provider assign delivery partner
    @POST("/api/providers/orders/{orderId}/assign-delivery/{deliveryPartnerId}")
    Call<Order> assignDeliveryPartner(@Path("orderId") Long orderId, @Path("deliveryPartnerId") Long deliveryPartnerId);
    
    // Payout endpoints
    @GET("/api/providers/payouts/pending")
    Call<java.util.Map<String, Double>> getPendingAmount();
}
