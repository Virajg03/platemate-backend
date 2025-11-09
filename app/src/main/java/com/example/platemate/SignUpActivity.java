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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class SignUpActivity extends AppCompatActivity {

    String[] userTypes = {"Customer", "Provider", "Delivery Partner"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    private EditText usernameEditText, emailEditText, passwordEditText, 
                     confirmPasswordEditText, phoneEditText;
    private Button signupButton;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize API
        apiInterface = RetrofitClient.getInstance(this).getApi();

        ImageView backButton = findViewById(R.id.backButton);
        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        adapterItems = new ArrayAdapter<>(this, R.layout.list_item, userTypes);
        autoCompleteTextView.setAdapter(adapterItems);

        // Initialize EditTexts
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        phoneEditText = findViewById(R.id.phone);
        signupButton = findViewById(R.id.signupButton);

        backButton.setOnClickListener(v -> onBackPressed());
        signupButton.setOnClickListener(v -> handleSignUp());
        TextView signUpText = findViewById(R.id.textView7);
        String text = getString(R.string.splash_activity_signin_text);
        SpannableString spannableString = new SpannableString(text);
        int startIndex = text.indexOf("Sign In");
        int endIndex = startIndex + "Sign In".length();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Color the Sign In text
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF775C"));
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signUpText.setText(spannableString);
        signUpText.setMovementMethod(LinkMovementMethod.getInstance());
        signUpText.setHighlightColor(Color.TRANSPARENT);
    }

    private void handleSignUp() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String role = autoCompleteTextView.getText().toString().trim();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || 
            confirmPassword.isEmpty() || phone.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!role.equals("Customer") && !role.equals("Provider") && !role.equals("Delivery Partner")) {
            Toast.makeText(this, "Please select a valid role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create signup request
        SignUpInputDetails signUpDetails = new SignUpInputDetails(
            username, email, password, phone, role
        );

        Call<LoginUserDetails> call = apiInterface.signup(signUpDetails);
        call.enqueue(new Callback<LoginUserDetails>() {
            @Override
            public void onResponse(Call<LoginUserDetails> call, Response<LoginUserDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SignUpActivity.this, 
                        "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
                    // Redirect to login
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    intent.putExtra("username", username); // Pre-fill username
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, 
                        "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginUserDetails> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, 
                    "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}