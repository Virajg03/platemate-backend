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

    // Menu Items endpoints (new standard)
    @GET("/api/providers/menu-items")
    Call<List<MenuItemResponse>> getProviderMenuItems();

    @Multipart
    @POST("/api/providers/menu-items")
    Call<MenuItemResponse> createMenuItem(
        @Part("data") RequestBody data,
        @Part MultipartBody.Part image  // Optional - pass null if no image
    );

    @Multipart
    @PUT("/api/providers/menu-items/{id}")
    Call<MenuItemResponse> updateMenuItem(
        @Path("id") Long id,
        @Part("data") RequestBody data,
        @Part MultipartBody.Part image
    );

    @DELETE("/api/providers/menu-items/{id}")
    Call<Void> deleteMenuItem(@Path("id") Long id);

    @GET("/api/providers/menu-items/{id}")
    Call<MenuItemResponse> getMenuItemById(@Path("id") Long id);

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
}
