package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String role = sessionManager.getRole();
        Intent intent;

        switch (role) {
            case "Provider":
                // Check if profile is complete
                if (!sessionManager.isProfileComplete()) {
                    intent = new Intent(this, ProviderDetailsActivity.class);
                } else {
                    intent = new Intent(this, ProviderDashboardActivity.class);
                }
                break;
            case "Delivery Partner":
                // TODO: Create DeliveryPartnerActivity
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