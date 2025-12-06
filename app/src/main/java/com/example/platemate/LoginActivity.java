package com.example.platemate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView signUpText = findViewById(R.id.textView7);
        String text = getString(R.string.login_acitivity_signup_text);

        SpannableString spannableString = new SpannableString(text);

        int startIndex = text.indexOf("Sign Up");
        int endIndex = startIndex + "Sign Up".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Color the Sign Up text
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF775C"));
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        signUpText.setText(spannableString);
        signUpText.setMovementMethod(LinkMovementMethod.getInstance());
        signUpText.setHighlightColor(Color.TRANSPARENT);

        // Initialize API
        apiInterface = RetrofitClient.getInstance(this).getApi();

        // Pre-fill username if coming from signup
        String prefillUsername = getIntent().getStringExtra("username");
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        if (prefillUsername != null) {
            usernameEditText.setText(prefillUsername);
        }

        loginButton.setOnClickListener(v -> loginToBackend());
        
        // Setup password visibility toggle
        setupPasswordToggle();
    }
    
    private void setupPasswordToggle() {
        ImageButton passwordToggle = findViewById(R.id.passwordToggle);
        if (passwordToggle != null && passwordEditText != null) {
            passwordToggle.setOnClickListener(v -> {
                if (passwordEditText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    // Show password
                    passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordToggle.setImageResource(R.drawable.ic_visibility);
                } else {
                    // Hide password
                    passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordToggle.setImageResource(R.drawable.ic_visibility_off);
                }
                // Move cursor to end
                passwordEditText.setSelection(passwordEditText.getText().length());
            });
        }
    }

    protected void loginToBackend() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            ToastUtils.showInfo(this, "Please enter username and password");
            return;
        }

        LoginInputDetails loginDetails = new LoginInputDetails(username, password);
        Call<LoginUserDetails> call = apiInterface.login(loginDetails);
        call.enqueue(new Callback<LoginUserDetails>() {
            @Override
            public void onResponse(Call<LoginUserDetails> call, Response<LoginUserDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginUserDetails userDetails = response.body();
                    String token = userDetails.getToken();
                    String refreshToken = userDetails.getRefreshToken();
                    String role = userDetails.getRole();
                    String username = userDetails.getUsername();
                    Long userId = userDetails.getUserId();

                    SessionManager sessionManager = new SessionManager(LoginActivity.this);
                    
                    // If userId is not in response, fetch it from backend
                    if (userId == null) {
                        fetchUserIdAndSaveSession(token, refreshToken, role, username, sessionManager);
                    } else {
                        sessionManager.saveLoginSession(token, refreshToken, role, username, userId);
                        proceedAfterLogin(role);
                    }
                } else {
                    ToastUtils.showError(LoginActivity.this, "Invalid credentials!");
                }
            }
            @Override
            public void onFailure(Call<LoginUserDetails> call, Throwable t) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Network Error")
                        .setMessage(t.toString())  // shows full exception
                        .setPositiveButton("OK", null)
                        .show();
                ToastUtils.showError(LoginActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void checkProviderProfileComplete() {
        Call<ProfileStatusResponse> call = apiInterface.checkProfileComplete();
        call.enqueue(new Callback<ProfileStatusResponse>() {
            @Override
            public void onResponse(Call<ProfileStatusResponse> call, Response<ProfileStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isComplete = response.body().getIsComplete();
                    // If onboarding is not complete, force show ProviderDetailsActivity
                    if (!isComplete) {
                        Intent intent = new Intent(LoginActivity.this, ProviderDetailsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        navigateToMainActivity();
                    }
                } else {
                    // If API fails, assume profile incomplete for safety - force onboarding
                    Intent intent = new Intent(LoginActivity.this, ProviderDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();
            }

            @Override
            public void onFailure(Call<ProfileStatusResponse> call, Throwable t) {
                // On failure, redirect to provider details form - force onboarding
                Intent intent = new Intent(LoginActivity.this, ProviderDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void fetchUserIdAndSaveSession(String token, String refreshToken, String role, 
            String username, SessionManager sessionManager) {
        // Save session without userId first (will be updated once we fetch it)
        sessionManager.saveLoginSession(token, refreshToken, role, username, null);
        
        // Note: We can't easily fetch userId without knowing it first
        // The profile fragment will handle fetching userId when the profile is loaded
        // This is a fallback for cases where backend hasn't been updated yet
        // Once backend is updated, userId will be in login response
        
        proceedAfterLogin(role);
    }
    
    private void proceedAfterLogin(String role) {
        ToastUtils.showSuccess(LoginActivity.this, "Login Successful!");

        // Check if provider needs to fill details
        if ("Provider".equals(role)) {
            checkProviderProfileComplete();
        } else {
            navigateToMainActivity();
        }
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
