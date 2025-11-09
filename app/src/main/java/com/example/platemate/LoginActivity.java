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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    }

    protected void loginToBackend() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
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
                    sessionManager.saveLoginSession(token, refreshToken, role, username, userId);

                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // Check if provider needs to fill details
                    if ("Provider".equals(role)) {
                        checkProviderProfileComplete();
                    } else {
                        navigateToMainActivity();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginUserDetails> call, Throwable t) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Network Error")
                        .setMessage(t.toString())  // shows full exception
                        .setPositiveButton("OK", null)
                        .show();
//                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if (!isComplete) {
                        // Redirect to provider details form
                        Intent intent = new Intent(LoginActivity.this, ProviderDetailsActivity.class);
                        startActivity(intent);
                    } else {
                        navigateToMainActivity();
                    }
                } else {
                    // If API fails, assume profile incomplete for safety
                    Intent intent = new Intent(LoginActivity.this, ProviderDetailsActivity.class);
                    startActivity(intent);
                }
                finish();
            }

            @Override
            public void onFailure(Call<ProfileStatusResponse> call, Throwable t) {
                // On failure, redirect to provider details form
                Intent intent = new Intent(LoginActivity.this, ProviderDetailsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
