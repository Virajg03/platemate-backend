package com.example.platemate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.platemate.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private AppCompatButton startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        Log.d("SplashActivity", "Session Manager:================");
        // Check if user is logged in
        if (sessionManager.isLoggedIn()) {
            // User is logged in, navigate directly to appropriate activity (no delay)
            navigateToHomeActivity(sessionManager);
        } else {
            // User is not logged in, show splash screen with buttons
            setupSignInClickableText();
            binding.startBtn.setOnClickListener(v ->
                startActivity(new Intent(SplashActivity.this, SignUpActivity.class)));
        }
    }

    private void navigateToHomeActivity(SessionManager sessionManager) {
        String role = normalizeRole(sessionManager.getRole());
        Intent intent;
        
        // Navigate based on user role
        if (role == null || role.trim().isEmpty()) {
            // No role set, go to login
            intent = new Intent(this, LoginActivity.class);
        } else {
            switch (role) {
                case "Provider":
                    if (!sessionManager.isProfileComplete()) {
                        intent = new Intent(this, ProviderDetailsActivity.class);
                    } else {
                        intent = new Intent(this, ProviderDashboardActivity.class);
                    }
                    break;
                    
                case "Delivery":
                    intent = new Intent(this, DeliveryPartnerDashboardActivity.class);
                    break;
                    
                case "Customer":
                default:
                    // For customers, go directly to customer home
                    intent = new Intent(this, CustomerHomeActivity.class);
                    break;
            }
        }
        
        startActivity(intent);
        finish();
    }

    private void setupSignInClickableText() {
        String text = getString(R.string.splash_activity_signin_text);

        SpannableString spannableString = new SpannableString(text);
        int startIndex = text.indexOf("Sign In");
        int endIndex = startIndex + "Sign In".length();

        ClickableSpan loginSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
        };

        spannableString.setSpan(loginSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Optional: Make it orange or your theme color
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#dc6f31")),
                startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.textView5.setText(spannableString);
        binding.textView5.setMovementMethod(LinkMovementMethod.getInstance());
        binding.textView5.setHighlightColor(Color.TRANSPARENT);
    }
    
    /**
     * Normalizes role names to handle backend variations
     * "Delivery Partner" -> "Delivery"
     */
    private String normalizeRole(String role) {
        if (role == null) return null;
        String normalized = role.trim();
        if ("Delivery Partner".equals(normalized)) {
            return "Delivery";
        }
        return normalized;
    }
}
