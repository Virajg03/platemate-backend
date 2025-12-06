package com.example.platemate;

import android.content.Context;
import android.content.Intent;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
//    private static final String BASE_URL = "http://10.0.2.2:8080";
    private static final String BASE_URL = "http://10.188.168.202:8080";

//    private static final String BASE_URL = "https://cf3069b9b344.ngrok-free.app";
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private SessionManager sessionManager;
    private static Context appContext;

    private RetrofitClient(Context context) {
        appContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(appContext);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new AuthInterceptor(sessionManager))
            .addInterceptor(new TokenRefreshInterceptor(sessionManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

        retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    public ApiInterface getApi() {
        return retrofit.create(ApiInterface.class);
    }
    
    public static String getBaseUrl() {
        return BASE_URL;
    }

    // Auth Interceptor - adds token to requests
    private static class AuthInterceptor implements Interceptor {
        private SessionManager sessionManager;

        public AuthInterceptor(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            String token = sessionManager.getToken();

            Request.Builder requestBuilder = original.newBuilder();
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }

            return chain.proceed(requestBuilder.build());
        }
    }

    // Token Refresh Interceptor - handles 401 and refreshes token
    private static class TokenRefreshInterceptor implements Interceptor {
        private SessionManager sessionManager;

        public TokenRefreshInterceptor(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            // If token expired (401), try to refresh
            if (response.code() == 401 && sessionManager.isLoggedIn()) {
                synchronized (this) {
                    String refreshToken = sessionManager.getRefreshToken();
                    if (refreshToken != null) {
                        // Create a new Retrofit instance for refresh call (without interceptor to avoid loop)
                        Retrofit refreshRetrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                        ApiInterface refreshApi = refreshRetrofit.create(ApiInterface.class);
                        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken);

                        try {
                            retrofit2.Response<TokenRefreshResponse> refreshResponse = 
                                refreshApi.refreshToken(refreshRequest).execute();

                            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                                TokenRefreshResponse tokenResponse = refreshResponse.body();
                                sessionManager.updateTokens(
                                    tokenResponse.getToken(),
                                    tokenResponse.getRefreshToken()
                                );

                                // Retry original request with new token
                                Request newRequest = request.newBuilder()
                                    .header("Authorization", "Bearer " + tokenResponse.getToken())
                                    .build();
                                return chain.proceed(newRequest);
                            } else {
                                // Refresh failed, logout user
                                sessionManager.logout();
                                if (appContext != null) {
                                    Intent intent = new Intent(appContext, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    appContext.startActivity(intent);
                                }
                            }
                        } catch (Exception e) {
                            // Refresh failed, logout user
                            sessionManager.logout();
                            if (appContext != null) {
                                Intent intent = new Intent(appContext, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                appContext.startActivity(intent);
                            }
                        }
                    }
                }
            }

            return response;
        }
    }
}

