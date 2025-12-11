package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeliveryPartnerFormActivity extends AppCompatActivity {
    // Intent extras keys
    public static final String EXTRA_DELIVERY_PARTNER = "delivery_partner";
    public static final String EXTRA_IS_EDIT = "is_edit";
    
    private DeliveryPartner partner;
    private boolean isEditMode;
    
    // User credential fields (for create mode only)
    private EditText etUsername, etEmail, etPassword;
    private android.widget.TextView tvUsernameLabel, tvEmailLabel, tvPasswordLabel;
    
    // Delivery partner fields
    private EditText etFullName, etServiceArea;
    private Spinner spVehicleType;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar; // Optional - can be null if not in layout
    private String[] vehicleTypes = {"BIKE", "SCOOTER", "BICYCLE", "CAR"};
    
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_partner_form);
        
        // Setup action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Delivery Partner");
        }
        
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        // Get intent data
        Intent intent = getIntent();
        if (intent != null) {
            partner = (DeliveryPartner) intent.getSerializableExtra(EXTRA_DELIVERY_PARTNER);
            isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT, false);
        }
        
        initializeViews();
        setupSpinner();
        setupMode();
        
        btnSave.setOnClickListener(v -> saveDeliveryPartner());
        btnCancel.setOnClickListener(v -> finish());
    }
    
    private void setupMode() {
        if (isEditMode && partner != null) {
            // Edit mode - hide user credential fields (can't change username/email/password after creation)
            etUsername.setVisibility(View.GONE);
            etEmail.setVisibility(View.GONE);
            etPassword.setVisibility(View.GONE);
            tvUsernameLabel.setVisibility(View.GONE);
            tvEmailLabel.setVisibility(View.GONE);
            tvPasswordLabel.setVisibility(View.GONE);
            
            // Populate existing fields
            etFullName.setText(partner.getFullName() != null ? partner.getFullName() : "");
            etServiceArea.setText(partner.getServiceArea() != null ? partner.getServiceArea() : "");
            
            // Set vehicle type
            if (partner.getVehicleType() != null) {
                int position = Arrays.asList(vehicleTypes).indexOf(partner.getVehicleType());
                if (position >= 0) {
                    spVehicleType.setSelection(position);
                }
            }
        } else {
            // Create mode - show user credential fields
            etUsername.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.VISIBLE);
            etPassword.setVisibility(View.VISIBLE);
            tvUsernameLabel.setVisibility(View.VISIBLE);
            tvEmailLabel.setVisibility(View.VISIBLE);
            tvPasswordLabel.setVisibility(View.VISIBLE);
        }
    }

    private void initializeViews() {
        // User credential fields
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvUsernameLabel = findViewById(R.id.tvUsernameLabel);
        tvEmailLabel = findViewById(R.id.tvEmailLabel);
        tvPasswordLabel = findViewById(R.id.tvPasswordLabel);
        
        // Delivery partner fields
        etFullName = findViewById(R.id.etFullName);
        etServiceArea = findViewById(R.id.etServiceArea);
        spVehicleType = findViewById(R.id.spVehicleType);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        // Progress bar is optional - findViewById returns null if not found
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        List<String> vehicleTypeList = new ArrayList<>(Arrays.asList(vehicleTypes));
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            vehicleTypeList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVehicleType.setAdapter(adapter);
    }

    private void saveDeliveryPartner() {
        String fullName = etFullName.getText().toString().trim();
        String serviceArea = etServiceArea.getText().toString().trim();
        String vehicleType = spVehicleType.getSelectedItem().toString();

        // Validate user credentials (only for create mode)
        String username = "";
        String email = "";
        String password = "";
        
        if (!isEditMode) {
            // CREATE MODE: Validate user credentials
            username = etUsername.getText().toString().trim();
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            
            if (username.isEmpty()) {
                Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (email.isEmpty()) {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Basic email validation
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (password.isEmpty()) {
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validate delivery partner fields
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (serviceArea.isEmpty()) {
            Toast.makeText(this, "Service area is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && partner != null) {
            // EDIT MODE: Update existing partner (user credentials unchanged)
            updateDeliveryPartner(fullName, vehicleType, serviceArea);
        } else {
            // CREATE MODE: Create new partner with user credentials
            createDeliveryPartner(username, email, password, fullName, vehicleType, serviceArea);
        }
    }
    
    private void createDeliveryPartner(String username, String email, String password, 
                                      String fullName, String vehicleType, String serviceArea) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        
        DeliveryPartnerCreateRequest request = new DeliveryPartnerCreateRequest(
            username,
            email,
            password,
            fullName,
            vehicleType,
            serviceArea
        );

        Call<DeliveryPartner> call = apiInterface.createProviderDeliveryPartner(request);
        call.enqueue(new Callback<DeliveryPartner>() {
            @Override
            public void onResponse(Call<DeliveryPartner> call, Response<DeliveryPartner> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(DeliveryPartnerFormActivity.this, "Delivery partner created successfully");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = "Failed to create delivery partner";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(DeliveryPartnerFormActivity.this, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<DeliveryPartner> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                ToastUtils.showError(DeliveryPartnerFormActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    private void updateDeliveryPartner(String fullName, String vehicleType, String serviceArea) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        
        DeliveryPartnerUpdateRequest request = new DeliveryPartnerUpdateRequest();
        request.setFullName(fullName);
        request.setVehicleType(vehicleType);
        request.setServiceArea(serviceArea);
        request.setIsAvailable(partner.getIsAvailable()); // Preserve availability

        // Use correct endpoint based on user role
        SessionManager sessionManager = new SessionManager(this);
        String role = normalizeRole(sessionManager.getRole());
        Call<DeliveryPartner> call;
        
        if ("Delivery".equals(role)) {
            // Delivery partner editing their own profile - use delivery partner endpoint
            call = apiInterface.updateDeliveryPartner(partner.getId(), request);
        } else {
            // Provider/Admin editing delivery partner - use provider endpoint
            call = apiInterface.updateProviderDeliveryPartner(partner.getId(), request);
        }
        call.enqueue(new Callback<DeliveryPartner>() {
            @Override
            public void onResponse(Call<DeliveryPartner> call, Response<DeliveryPartner> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(DeliveryPartnerFormActivity.this, "Delivery partner updated successfully");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = "Failed to update delivery partner";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(DeliveryPartnerFormActivity.this, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<DeliveryPartner> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                ToastUtils.showError(DeliveryPartnerFormActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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

