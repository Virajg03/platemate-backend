package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

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
                if (!sessionManager.isProfileComplete()) {
                    intent = new Intent(this, ProviderDetailsActivity.class);
                } else {
                    intent = new Intent(this, ProviderDashboardActivity.class);
                }
                break;

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
}
