package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    
    private EditText usernameOrEmailEditText;
    private Button requestOtpButton;
    private ApiInterface apiInterface;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        usernameOrEmailEditText = findViewById(R.id.usernameOrEmailEditText);
        requestOtpButton = findViewById(R.id.requestOtpButton);
        
        TextView backToLogin = findViewById(R.id.backToLogin);
        if (backToLogin != null) {
            backToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
        
        requestOtpButton.setOnClickListener(v -> requestOtp());
    }
    
    private void requestOtp() {
        String input = usernameOrEmailEditText.getText().toString().trim();
        
        if (input.isEmpty()) {
            usernameOrEmailEditText.setError("Username or email is required");
            usernameOrEmailEditText.requestFocus();
            return;
        }
        
        // Determine if input is email or username
        final String username;
        final String email;
        
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            // It's an email
            username = null;
            email = input;
        } else {
            // It's a username
            username = input;
            email = null;
        }
        
        requestOtpButton.setEnabled(false);
        requestOtpButton.setText("Sending...");
        
        ForgotPasswordRequest request = new ForgotPasswordRequest(username, email);
        Call<PasswordResetResponse> call = apiInterface.forgotPassword(request);
        
        call.enqueue(new Callback<PasswordResetResponse>() {
            @Override
            public void onResponse(Call<PasswordResetResponse> call, Response<PasswordResetResponse> response) {
                requestOtpButton.setEnabled(true);
                requestOtpButton.setText("Send OTP");
                
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                    
                    // Navigate to ResetPasswordActivity
                    Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Failed to send OTP. Please try again.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(ForgotPasswordActivity.this, errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<PasswordResetResponse> call, Throwable t) {
                requestOtpButton.setEnabled(true);
                requestOtpButton.setText("Send OTP");
                ToastUtils.showError(ForgotPasswordActivity.this, "Error: " + t.getMessage());
            }
        });
    }
}

