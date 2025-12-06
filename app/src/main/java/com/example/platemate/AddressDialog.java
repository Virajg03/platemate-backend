package com.example.platemate;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressDialog {
    
    public interface AddressDialogListener {
        void onAddressSaved(String street, String city, String state, String zipCode);
        void onCancel();
    }
    
    public static void show(Context context, AddressDialogListener listener) {
        show(context, null, listener);
    }
    
    public static void show(Context context, Address existingAddress, AddressDialogListener listener) {
        // Create custom dialog
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_address_form);
        dialog.setCancelable(true);
        
        // Get views
        EditText etStreet = dialog.findViewById(R.id.etStreet);
        EditText etStreet2 = dialog.findViewById(R.id.etStreet2);
        Spinner spinnerCity = dialog.findViewById(R.id.spinnerCity);
        EditText etState = dialog.findViewById(R.id.etState);
        EditText etZipCode = dialog.findViewById(R.id.etZipCode);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSaveAddress = dialog.findViewById(R.id.btnSaveAddress);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        
        // Setup city spinner
        List<String> cities = CityStateHelper.getAllCities();
        Collections.sort(cities);
        cities.add(0, "Select City"); // Add placeholder
        
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(context, 
            android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);
        
        // Auto-populate state when city is selected
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip placeholder
                    String selectedCity = cities.get(position);
                    String state = CityStateHelper.getStateForCity(selectedCity);
                    if (state != null) {
                        etState.setText(state);
                    }
                } else {
                    etState.setText("");
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etState.setText("");
            }
        });
        
        // Pre-fill if editing existing address
        if (existingAddress != null) {
            // Try both field names for backward compatibility
            String street1 = existingAddress.getStreet1() != null ? existingAddress.getStreet1() : existingAddress.getStreet();
            if (street1 != null && !street1.isEmpty()) {
                etStreet.setText(street1);
            }
            
            // Pre-fill street2 if available
            if (existingAddress.getStreet2() != null && !existingAddress.getStreet2().isEmpty()) {
                etStreet2.setText(existingAddress.getStreet2());
            }
            
            if (existingAddress.getCity() != null) {
                String city = existingAddress.getCity();
                int cityPosition = cities.indexOf(city);
                if (cityPosition > 0) {
                    spinnerCity.setSelection(cityPosition);
                }
            }
            if (existingAddress.getState() != null) {
                etState.setText(existingAddress.getState());
            }
            // Try both field names for zip code
            String zipCode = existingAddress.getPincode() != null ? existingAddress.getPincode() : existingAddress.getZipCode();
            if (zipCode != null && !zipCode.isEmpty()) {
                etZipCode.setText(zipCode);
            }
        }
        
        // Cancel button
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onCancel();
            }
        });
        
        // Save button
        btnSaveAddress.setOnClickListener(v -> {
            String street1 = etStreet.getText().toString().trim();
            String street2 = etStreet2.getText().toString().trim(); // Optional field
            String city = spinnerCity.getSelectedItemPosition() > 0 ? 
                spinnerCity.getSelectedItem().toString() : "";
            String state = etState.getText().toString().trim();
            String zipCode = etZipCode.getText().toString().trim();
            
            // Validation
            if (street1.isEmpty()) {
                etStreet.setError("Street address 1 is required");
                etStreet.requestFocus();
                return;
            }
            
            if (city.isEmpty() || spinnerCity.getSelectedItemPosition() == 0) {
                ToastUtils.showError(context, "Please select a city");
                return;
            }
            
            if (state.isEmpty()) {
                ToastUtils.showError(context, "State is required. Please select a city.");
                return;
            }
            
            if (zipCode.isEmpty()) {
                etZipCode.setError("Zip code is required");
                etZipCode.requestFocus();
                return;
            }
            
            // Validate zip code format
            if (!zipCode.matches("\\d{5,10}")) {
                etZipCode.setError("Please enter a valid zip code (5-10 digits)");
                etZipCode.requestFocus();
                return;
            }
            
            // Save to backend (street2 can be empty)
            saveAddressToBackend(context, street1, street2, city, state, zipCode, existingAddress != null, 
                dialog, progressBar, btnSaveAddress, listener);
        });
        
        // Show dialog
        dialog.show();
        
        // Make dialog full width
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                           android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    
    private static void saveAddressToBackend(Context context, String street1, String street2, 
            String city, String state, String zipCode, boolean isUpdate, Dialog dialog, ProgressBar progressBar,
            Button btnSaveAddress, AddressDialogListener listener) {
        
        ApiInterface apiInterface = RetrofitClient.getInstance(context).getApi();
        SessionManager sessionManager = new SessionManager(context);
        Long userId = sessionManager.getUserId();
        
        if (userId == null) {
            ToastUtils.showError(context, "User not logged in");
            progressBar.setVisibility(View.GONE);
            btnSaveAddress.setEnabled(true);
            return;
        }
        
        // Create AddressRequest matching backend format
        // Backend AddressType enum accepts: OTHER, OFFICE, HOME, BUSINESS
        // Using "HOME" for customer delivery addresses
        AddressRequest request = new AddressRequest(
            street1,         // street1 (required)
            street2 != null ? street2 : "",  // street2 (optional)
            city,
            state,
            zipCode,         // pincode
            "HOME"           // address_type (valid enum values: OTHER, OFFICE, HOME, BUSINESS)
        );
        
        progressBar.setVisibility(View.VISIBLE);
        btnSaveAddress.setEnabled(false);
        
        // Backend uses POST for both create and update (upsert)
        Call<Address> call = apiInterface.saveOrUpdateCustomerAddress(userId, request);
        
        call.enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                progressBar.setVisibility(View.GONE);
                btnSaveAddress.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    Address savedAddress = response.body();
                    android.util.Log.d("AddressDialog", "Address saved successfully: " +
                        "street1=" + savedAddress.getStreet1() + ", " +
                        "street2=" + savedAddress.getStreet2() + ", " +
                        "city=" + savedAddress.getCity() + ", " +
                        "state=" + savedAddress.getState() + ", " +
                        "pincode=" + savedAddress.getPincode());
                    
                    // Also save to SessionManager for quick access
                    // Combine street1 and street2 for display in SessionManager
                    String fullStreet = street1;
                    if (street2 != null && !street2.isEmpty()) {
                        fullStreet = street1 + ", " + street2;
                    }
                    SessionManager sessionManager = new SessionManager(context);
                    sessionManager.saveDeliveryAddress(fullStreet, city, state, zipCode);
                    
                    dialog.dismiss();
                    if (listener != null) {
                        // Pass combined street address for backward compatibility
                        listener.onAddressSaved(fullStreet, city, state, zipCode);
                    }
                    ToastUtils.showSuccess(context, "Address saved successfully!");
                } else {
                    android.util.Log.e("AddressDialog", "Failed to save address: " + response.code());
                    ToastUtils.showError(context, "Failed to save address");
                }
            }
            
            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSaveAddress.setEnabled(true);
                ToastUtils.showError(context, "Error: " + t.getMessage());
            }
        });
    }
}

