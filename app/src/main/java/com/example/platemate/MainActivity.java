package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SessionManager sessionManager;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        apiInterface = RetrofitClient.getInstance(this).getApi();

        // If not logged in -> go to Login
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in -> redirect to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Log session values for debugging
        String role = sessionManager.getRole();
        String token = sessionManager.getToken();
        Long userId = sessionManager.getUserId();
        Log.d(TAG, "Session values -> role: " + role + " token: " + (token != null ? "[present]" : "null") + " userId: " + userId);

        // Defensive: handle missing role (avoid switch on null)
        if (role == null || role.trim().isEmpty()) {
            // Choose what makes sense in your app:
            // Option A: treat as corrupted/incomplete session -> send to Login to re-authenticate
            Log.w(TAG, "Role is null/empty despite isLoggedIn() == true. Redirecting to LoginActivity.");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;

            // Option B (alternative): set a default role and continue
            // role = "Customer";
        }

        // Now safe to switch on role (role is non-null)
        Intent intent;
        switch (role) {
            case "Provider":
                // Always check onboarding status from API, not local flag
                checkProviderOnboardingStatus();
                return; // Don't navigate yet, wait for API response
            case "Delivery Partner":
                // TODO: create DeliveryPartnerActivity when needed
                intent = new Intent(this, CustomerHomeActivity.class);
                break;

            case "Customer":
            default:
                intent = new Intent(this, CustomerHomeActivity.class);
                break;
        }

        startActivity(intent);
        finish();
    }

    private void checkProviderOnboardingStatus() {
        Call<ProfileStatusResponse> call = apiInterface.checkProfileComplete();
        call.enqueue(new Callback<ProfileStatusResponse>() {
            @Override
            public void onResponse(Call<ProfileStatusResponse> call, Response<ProfileStatusResponse> response) {
                Intent intent;
                if (response.isSuccessful() && response.body() != null) {
                    boolean isComplete = response.body().getIsComplete();
                    if (!isComplete) {
                        // Force show ProviderDetailsActivity if onboarding not complete
                        intent = new Intent(MainActivity.this, ProviderDetailsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    } else {
                        intent = new Intent(MainActivity.this, ProviderDashboardActivity.class);
                    }
                } else {
                    // On error, assume onboarding needed
                    intent = new Intent(MainActivity.this, ProviderDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<ProfileStatusResponse> call, Throwable t) {
                // On failure, force onboarding
                Intent intent = new Intent(MainActivity.this, ProviderDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
