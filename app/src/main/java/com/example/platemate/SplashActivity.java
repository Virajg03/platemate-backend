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
        binding.startBtn.setOnClickListener(v ->
            startActivity(new Intent(SplashActivity.this, SignUpActivity.class)));
        setupSignInClickableText();
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
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
}
