package com.example.platemate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
        
        // Setup phone input formatting
        setupPhoneInput();
        
        // Setup error clearing on text change
        setupErrorClearing();
        
        // Setup password visibility toggles
        setupPasswordToggles();
        
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
    
    private void setupPhoneInput() {
        // Set max length for phone number (e.g., 10 digits)
        phoneEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(10) });
        
        // Add TextWatcher to format phone number (only digits) and clear errors
        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                // Clear error
                phoneEditText.setError(null);
                
                // Remove all non-digit characters
                String digits = s.toString().replaceAll("[^0-9]", "");
                if (!s.toString().equals(digits)) {
                    phoneEditText.setText(digits);
                    phoneEditText.setSelection(digits.length());
                }
            }
        });
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }
    
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
    
    private String getPasswordValidationMessage(String password) {
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters long";
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }
        
        StringBuilder message = new StringBuilder("Password must contain: ");
        boolean needsComma = false;
        
        if (!hasUpperCase) {
            message.append("uppercase letter");
            needsComma = true;
        }
        if (!hasLowerCase) {
            if (needsComma) message.append(", ");
            message.append("lowercase letter");
            needsComma = true;
        }
        if (!hasDigit) {
            if (needsComma) message.append(", ");
            message.append("number");
            needsComma = true;
        }
        if (!hasSpecialChar) {
            if (needsComma) message.append(", ");
            message.append("special character");
        }
        
        return message.toString();
    }
    
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Phone should be 10 digits
        return phone.matches("^[0-9]{10}$");
    }
    
    private void setupErrorClearing() {
        // Clear email error when user starts typing
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                emailEditText.setError(null);
            }
        });
        
        // Clear password error when user starts typing
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                passwordEditText.setError(null);
            }
        });
        
        // Clear confirm password error when user starts typing
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                confirmPasswordEditText.setError(null);
            }
        });
    }

    private void handleSignUp() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String selectedRole = autoCompleteTextView.getText().toString().trim();

        // Validation - Check empty fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || 
            confirmPassword.isEmpty() || phone.isEmpty() || selectedRole.isEmpty()) {
            ToastUtils.showInfo(this, "Please fill all fields");
            return;
        }
        
        // Email validation
        if (!isValidEmail(email)) {
            emailEditText.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            ToastUtils.showError(this, "Please enter a valid email address");
            return;
        }
        
        // Phone validation
        if (!isValidPhone(phone)) {
            phoneEditText.setError("Please enter a valid 10-digit phone number");
            phoneEditText.requestFocus();
            ToastUtils.showError(this, "Please enter a valid 10-digit phone number");
            return;
        }
        
        // Password validation
        if (!isValidPassword(password)) {
            passwordEditText.setError(getPasswordValidationMessage(password));
            passwordEditText.requestFocus();
            ToastUtils.showWarning(this, getPasswordValidationMessage(password));
            return;
        }
        
        // Password confirmation validation
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            ToastUtils.showError(this, "Passwords do not match");
            return;
        }

        // Map user-friendly dropdown selection to backend enum format
        String backendRole = mapRoleToBackendFormat(selectedRole);
        if (backendRole == null) {
            ToastUtils.showError(this, "Please select a valid profile type");
            return;
        }

        // Create signup request with properly formatted role
        SignUpInputDetails signUpDetails = new SignUpInputDetails(
            username, email, password, phone, backendRole
        );

        Call<LoginUserDetails> call = apiInterface.signup(signUpDetails);
        call.enqueue(new Callback<LoginUserDetails>() {
            @Override
            public void onResponse(Call<LoginUserDetails> call, Response<LoginUserDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ToastUtils.showSuccess(SignUpActivity.this, 
                        "Registration successful! Please login.");
                    // Redirect to login
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    intent.putExtra("username", username); // Pre-fill username
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Registration failed";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(SignUpActivity.this, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<LoginUserDetails> call, Throwable t) {
                ToastUtils.showError(SignUpActivity.this, 
                    "Error: " + t.getMessage());
            }
        });
    }

    private void setupPasswordToggles() {
        ImageButton passwordToggle = findViewById(R.id.passwordToggle);
        ImageButton confirmPasswordToggle = findViewById(R.id.confirmPasswordToggle);
        
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
        
        if (confirmPasswordToggle != null && confirmPasswordEditText != null) {
            confirmPasswordToggle.setOnClickListener(v -> {
                if (confirmPasswordEditText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    // Show password
                    confirmPasswordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confirmPasswordToggle.setImageResource(R.drawable.ic_visibility);
                } else {
                    // Hide password
                    confirmPasswordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confirmPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
                }
                // Move cursor to end
                confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
            });
        }
    }
    
    /**
     * Maps user-friendly dropdown values to backend Role enum format
     * 
     * @param selectedRole The role selected from dropdown (e.g., "Customer", "Provider", "Delivery Partner")
     * @return Backend enum format (e.g., "ROLE_CUSTOMER", "ROLE_PROVIDER", "DELIVERY_PARTNER") or null if invalid
     */
    private String mapRoleToBackendFormat(String selectedRole) {
        if (selectedRole == null || selectedRole.trim().isEmpty()) {
            return null;
        }
        
        switch (selectedRole.trim()) {
            case "Customer":
                return "ROLE_CUSTOMER";
            case "Provider":
                return "ROLE_PROVIDER";
            case "Delivery Partner":
                return "DELIVERY_PARTNER";
            default:
                return null;
        }
    }
}