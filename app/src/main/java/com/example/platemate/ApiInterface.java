package com.example.platemate;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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
    Call<ProviderDetails> saveProviderDetails(@Body ProviderDetails providerDetails);

    @GET("/api/provider/details")
    Call<ProviderDetails> getProviderDetails();

    @GET("/api/provider/profile-complete")
    Call<ProfileStatusResponse> checkProfileComplete();

    // Product endpoints
    @GET("/api/products/provider")
    Call<List<Product>> getProviderProducts();

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
