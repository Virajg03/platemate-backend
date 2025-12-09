package com.example.platemate;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OTPVerificationDialog {
    
    public interface OTPVerificationListener {
        void onOTPVerified(String otp);
        void onCancel();
    }
    
    public static void show(Context context, OTPVerificationListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_otp_verification);
        dialog.setCancelable(true);
        
        EditText etOTP = dialog.findViewById(R.id.etOTP);
        Button btnVerify = dialog.findViewById(R.id.btnVerify);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        TextView tvError = dialog.findViewById(R.id.tvError);
        
        // Auto-submit when 6 digits are entered
        etOTP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String otp = s.toString();
                if (otp.length() == 6) {
                    // Auto-submit when 6 digits are entered
                    String otpValue = etOTP.getText().toString().trim();
                    if (otpValue.length() == 6) {
                        btnVerify.performClick();
                    }
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        btnVerify.setOnClickListener(v -> {
            String otp = etOTP.getText().toString().trim();
            
            if (otp.isEmpty()) {
                tvError.setText("Please enter OTP");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            
            if (otp.length() != 6) {
                tvError.setText("OTP must be 6 digits");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            
            // Hide error, show progress
            tvError.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            btnVerify.setEnabled(false);
            
            // Call listener
            if (listener != null) {
                listener.onOTPVerified(otp);
            }
            
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dialog.dismiss();
        });
        
        dialog.show();
    }
}






