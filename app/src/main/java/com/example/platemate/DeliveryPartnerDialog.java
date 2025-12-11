package com.example.platemate;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeliveryPartnerDialog extends Dialog {
    private DeliveryPartner partner;
    private Long userId; // Keep for edit mode compatibility
    private OnSaveListener listener;
    
    // User credential fields (for create mode only)
    private EditText etUsername, etEmail, etPassword;
    private android.widget.TextView tvUsernameLabel, tvEmailLabel, tvPasswordLabel;
    
    // Delivery partner fields
    private EditText etFullName, etServiceArea;
    private Spinner spVehicleType;
    private Button btnSave, btnCancel;
    private String[] vehicleTypes = {"BIKE", "SCOOTER", "BICYCLE", "CAR"};

    public interface OnSaveListener {
        void onSave(DeliveryPartner partner, boolean isEdit);
    }

    public DeliveryPartnerDialog(@NonNull Context context, DeliveryPartner partner, Long userId, OnSaveListener listener) {
        super(context);
        this.partner = partner;
        this.userId = userId; // Store userId
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_delivery_partner);

        initializeViews();
        setupSpinner();
        
        if (partner != null) {
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

        btnSave.setOnClickListener(v -> saveDeliveryPartner());
        btnCancel.setOnClickListener(v -> dismiss());
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
    }

    private void setupSpinner() {
        List<String> vehicleTypeList = new ArrayList<>(Arrays.asList(vehicleTypes));
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            getContext(),
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
        
        if (partner == null) {
            // CREATE MODE: Validate user credentials
            username = etUsername.getText().toString().trim();
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            
            if (username.isEmpty()) {
                Toast.makeText(getContext(), "Username is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Email is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Basic email validation
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (password.isEmpty()) {
                Toast.makeText(getContext(), "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (password.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validate delivery partner fields
        if (fullName.isEmpty()) {
            Toast.makeText(getContext(), "Full name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (serviceArea.isEmpty()) {
            Toast.makeText(getContext(), "Service area is required", Toast.LENGTH_SHORT).show();
            return;
        }

        DeliveryPartner deliveryPartner;
        if (partner != null) {
            // EDIT MODE: Update existing partner (user credentials unchanged)
            deliveryPartner = partner;
            deliveryPartner.setFullName(fullName);
            deliveryPartner.setVehicleType(vehicleType);
            deliveryPartner.setServiceArea(serviceArea);
        } else {
            // CREATE MODE: Create new partner with user credentials
            deliveryPartner = new DeliveryPartner();
            
            // Set user credentials (will be used to create User account on backend)
            deliveryPartner.setUsername(username);
            deliveryPartner.setEmail(email);
            deliveryPartner.setPassword(password);
            
            deliveryPartner.setFullName(fullName);
            deliveryPartner.setVehicleType(vehicleType);
            deliveryPartner.setServiceArea(serviceArea);
            deliveryPartner.setIsAvailable(false); // Default to unavailable
        }

        listener.onSave(deliveryPartner, partner != null);
        dismiss();
    }
}

