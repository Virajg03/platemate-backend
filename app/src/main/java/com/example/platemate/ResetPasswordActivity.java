package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

public class ResetPasswordActivity extends AppCompatActivity {
    
    private EditText otpEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button resetPasswordButton;
    private Button resendOtpButton;
    private TextView resendOtpTimer;
    
    private ApiInterface apiInterface;
    private String userUsername;
    private String userEmail;
    private CountDownTimer countDownTimer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        userUsername = getIntent().getStringExtra("username");
        userEmail = getIntent().getStringExtra("email");
        if ((userUsername == null || userUsername.isEmpty()) && 
            (userEmail == null || userEmail.isEmpty())) {
            Toast.makeText(this, "Username or email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        otpEditText = findViewById(R.id.otpEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        resendOtpTimer = findViewById(R.id.resendOtpTimer);
        
        resetPasswordButton.setOnClickListener(v -> resetPassword());
        resendOtpButton.setOnClickListener(v -> resendOtp());
        
        TextView backToLogin = findViewById(R.id.backToLogin);
        if (backToLogin != null) {
            backToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
        
        // Start cooldown timer (60 seconds)
        startResendCooldown();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    
    private void resetPassword() {
        String otp = otpEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        
        if (otp.isEmpty()) {
            otpEditText.setError("OTP is required");
            otpEditText.requestFocus();
            return;
        }
        
        if (otp.length() != 6) {
            otpEditText.setError("OTP must be 6 digits");
            otpEditText.requestFocus();
            return;
        }
        
        if (newPassword.isEmpty()) {
            newPasswordEditText.setError("Password is required");
            newPasswordEditText.requestFocus();
            return;
        }
        
        if (newPassword.length() < 8) {
            newPasswordEditText.setError("Password must be at least 8 characters");
            newPasswordEditText.requestFocus();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }
        
        resetPasswordButton.setEnabled(false);
        resetPasswordButton.setText("Resetting...");
        
        ResetPasswordRequest request = new ResetPasswordRequest(userUsername, userEmail, otp, newPassword, confirmPassword);
        Call<PasswordResetResponse> call = apiInterface.resetPassword(request);
        
        call.enqueue(new Callback<PasswordResetResponse>() {
            @Override
            public void onResponse(Call<PasswordResetResponse> call, Response<PasswordResetResponse> response) {
                resetPasswordButton.setEnabled(true);
                resetPasswordButton.setText("Reset Password");
                
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();
                    Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                    
                    // Navigate back to login after 2 seconds
                    new android.os.Handler().postDelayed(() -> {
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }, 2000);
                } else {
                    String errorMsg = "Failed to reset password";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(ResetPasswordActivity.this, errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<PasswordResetResponse> call, Throwable t) {
                resetPasswordButton.setEnabled(true);
                resetPasswordButton.setText("Reset Password");
                ToastUtils.showError(ResetPasswordActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    private void resendOtp() {
        resendOtpButton.setEnabled(false);
        resendOtpButton.setText("Sending...");
        
        ResendOtpRequest request = new ResendOtpRequest(userUsername, userEmail);
        Call<PasswordResetResponse> call = apiInterface.resendOtp(request);
        
        call.enqueue(new Callback<PasswordResetResponse>() {
            @Override
            public void onResponse(Call<PasswordResetResponse> call, Response<PasswordResetResponse> response) {
                resendOtpButton.setEnabled(true);
                resendOtpButton.setText("Resend OTP");
                
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();
                    Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                    startResendCooldown();
                } else {
                    ToastUtils.showError(ResetPasswordActivity.this, "Failed to resend OTP");
                }
            }
            
            @Override
            public void onFailure(Call<PasswordResetResponse> call, Throwable t) {
                resendOtpButton.setEnabled(true);
                resendOtpButton.setText("Resend OTP");
                ToastUtils.showError(ResetPasswordActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    private void startResendCooldown() {
        resendOtpButton.setEnabled(false);
        if (resendOtpTimer != null) {
            resendOtpTimer.setVisibility(View.VISIBLE);
        }
        
        // 60 seconds cooldown
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                if (resendOtpTimer != null) {
                    resendOtpTimer.setText("Resend OTP in " + secondsRemaining + "s");
                }
            }
            
            @Override
            public void onFinish() {
                resendOtpButton.setEnabled(true);
                if (resendOtpTimer != null) {
                    resendOtpTimer.setVisibility(View.GONE);
                }
            }
        }.start();
    }
}

