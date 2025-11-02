package com.example.platemate;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {

    @POST("/api/auth/login")
    Call<LoginUserDetails> login(@Body LoginInputDetails loginInputDetails);




}
