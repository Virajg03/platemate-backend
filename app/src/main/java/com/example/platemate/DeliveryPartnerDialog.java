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
    private Long userId; // Store userId from constructor
    private OnSaveListener listener;
    private EditText etFullName, etCommissionRate, etServiceArea; // Removed etUserId
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
            // Edit mode - populate fields (no userId field to populate)
            etFullName.setText(partner.getFullName() != null ? partner.getFullName() : "");
            etCommissionRate.setText(partner.getCommissionRate() != null ? partner.getCommissionRate().toString() : "");
            etServiceArea.setText(partner.getServiceArea() != null ? partner.getServiceArea() : "");
            
            // Set vehicle type
            if (partner.getVehicleType() != null) {
                int position = Arrays.asList(vehicleTypes).indexOf(partner.getVehicleType());
                if (position >= 0) {
                    spVehicleType.setSelection(position);
                }
            }
        }

        btnSave.setOnClickListener(v -> saveDeliveryPartner());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void initializeViews() {
        // Removed etUserId - no longer needed
        etFullName = findViewById(R.id.etFullName);
        etCommissionRate = findViewById(R.id.etCommissionRate);
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
        // Validate fields (removed userId validation - it's automatically set)
        String fullName = etFullName.getText().toString().trim();
        String commissionRateStr = etCommissionRate.getText().toString().trim();
        String serviceArea = etServiceArea.getText().toString().trim();
        String vehicleType = spVehicleType.getSelectedItem().toString();

        if (fullName.isEmpty()) {
            Toast.makeText(getContext(), "Full name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (commissionRateStr.isEmpty()) {
            Toast.makeText(getContext(), "Commission rate is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (serviceArea.isEmpty()) {
            Toast.makeText(getContext(), "Service area is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate userId is available for new delivery partners
        if (partner == null && userId == null) {
            Toast.makeText(getContext(), "User ID not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Use stored userId instead of reading from EditText
            Double commissionRate = Double.parseDouble(commissionRateStr);

            DeliveryPartner deliveryPartner;
            if (partner != null) {
                // Edit mode - use existing partner
                deliveryPartner = partner;
                deliveryPartner.setFullName(fullName);
                deliveryPartner.setVehicleType(vehicleType);
                deliveryPartner.setCommissionRate(commissionRate);
                deliveryPartner.setServiceArea(serviceArea);
            } else {
                // Add mode - create new partner with stored userId
                deliveryPartner = new DeliveryPartner();
                deliveryPartner.setUserId(userId); // Use stored userId from constructor
                deliveryPartner.setFullName(fullName);
                deliveryPartner.setVehicleType(vehicleType);
                deliveryPartner.setCommissionRate(commissionRate);
                deliveryPartner.setServiceArea(serviceArea);
                deliveryPartner.setIsAvailable(false); // Default to unavailable
            }

            listener.onSave(deliveryPartner, partner != null);
            dismiss();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }
}

