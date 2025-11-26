package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderDetailsActivity extends AppCompatActivity {
    private EditText etBusinessName, etDescription, etCommissionRate, 
                     etDeliveryRadius;
    private AutoCompleteTextView etZone;
    private EditText etStreet, etCity, etState, etZipCode;
    private CheckBox cbProvidesDelivery;
    private Button btnSaveDetails;
    private ApiInterface apiInterface;
    private SessionManager sessionManager;

    private boolean isEditing = false;
    private List<DeliveryZone> zonesList = new ArrayList<>();
    private Long selectedZoneId = null;
    private ArrayAdapter<String> zoneAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_details);

        apiInterface = RetrofitClient.getInstance(this).getApi();
        sessionManager = new SessionManager(this);

        initializeViews();
        setupDeliveryToggle();
        setupBackButton();
        setupZoneDropdown();

        btnSaveDetails.setOnClickListener(v -> saveProviderDetails());
        
        // Load zones first, then check onboarding status and load provider details
        loadDeliveryZones();
    }

    private void initializeViews() {
        etBusinessName = findViewById(R.id.etBusinessName);
        etDescription = findViewById(R.id.etDescription);
        etCommissionRate = findViewById(R.id.etCommissionRate);
        etDeliveryRadius = findViewById(R.id.etDeliveryRadius);
        etZone = findViewById(R.id.etZone);
        cbProvidesDelivery = findViewById(R.id.cbProvidesDelivery);
        btnSaveDetails = findViewById(R.id.btnSaveDetails);

        // Address fields
        etStreet = findViewById(R.id.etStreet);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        etZipCode = findViewById(R.id.etZipCode);
    }

    /**
     * Check onboarding status from API and load provider details
     * This determines if we're in editing mode or onboarding mode
     */
    private void checkOnboardingStatusAndLoadData() {
        Call<ProfileStatusResponse> call = apiInterface.checkProfileComplete();
        call.enqueue(new Callback<ProfileStatusResponse>() {
            @Override
            public void onResponse(Call<ProfileStatusResponse> call, Response<ProfileStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileStatusResponse status = response.body();
                    Boolean isOnboarding = status.getIsOnboarding();
                    
                    // isEditing = false means onboarding (first time), true means editing existing profile
                    isEditing = !Boolean.TRUE.equals(isOnboarding);
                    
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(isEditing ? "Edit Profile" : "Complete Your Profile");
                        getSupportActionBar().setDisplayHomeAsUpEnabled(isEditing);
                    }
                    
                    // Always load provider details (even if onboarding, provider exists with default values)
                    loadProviderDetails();
                } else {
                    // Default to onboarding mode if API fails
                    isEditing = false;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Complete Your Profile");
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                    loadProviderDetails();
                }
            }

            @Override
            public void onFailure(Call<ProfileStatusResponse> call, Throwable t) {
                // Default to onboarding mode on failure
                isEditing = false;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Complete Your Profile");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
                loadProviderDetails();
            }
        });
    }

    private void setupBackButton() {
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (isEditing) {
                    onBackPressed();
                } else {
                    // Block back navigation during onboarding
                    showOnboardingBlockDialog();
                }
            });
        }
    }

    private void showOnboardingBlockDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Complete Profile Required")
            .setMessage("You must complete your profile before accessing the app.")
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    public void onBackPressed() {
        // Block back navigation during onboarding
        if (!isEditing) {
            showOnboardingBlockDialog();
            return;
        }
        super.onBackPressed();
    }
    
    private void loadProviderDetails() {
        Call<Map<String, Object>> call = apiInterface.getProviderDetails();
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> details = response.body();
                    populateFields(details);
                } else {
                    ToastUtils.showError(ProviderDetailsActivity.this, 
                        "Failed to load profile details");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                ToastUtils.showError(ProviderDetailsActivity.this, 
                    "Error loading profile: " + t.getMessage());
            }
        });
    }
    
    private void populateFields(Map<String, Object> details) {
        // Business Name
        if (details.get("businessName") != null) {
            String businessName = details.get("businessName").toString();
            etBusinessName.setText(businessName != null && !businessName.isEmpty() ? businessName : "");
        } else {
            etBusinessName.setText("");
        }
        
        // Description
        if (details.get("description") != null) {
            String description = details.get("description").toString();
            etDescription.setText(description != null && !description.isEmpty() ? description : "");
        } else {
            etDescription.setText("");
        }
        
        // Commission Rate
        if (details.get("commissionRate") != null) {
            Object commissionRate = details.get("commissionRate");
            if (commissionRate instanceof Number) {
                double rate = ((Number) commissionRate).doubleValue();
                etCommissionRate.setText(rate > 0 ? String.valueOf(rate) : "");
            } else {
                etCommissionRate.setText(commissionRate.toString());
            }
        } else {
            etCommissionRate.setText("");
        }
        
        // Zone - set selected zone based on zone ID from provider details
        if (details.get("zone") != null) {
            Object zoneObj = details.get("zone");
            if (zoneObj != null) {
                Long zoneId = null;
                if (zoneObj instanceof Number) {
                    zoneId = ((Number) zoneObj).longValue();
                } else {
                    try {
                        zoneId = Long.parseLong(zoneObj.toString());
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
                
                if (zoneId != null) {
                    selectedZoneId = zoneId;
                    // Find the zone in the list and set the display text
                    for (DeliveryZone zone : zonesList) {
                        if (zone.getId().equals(zoneId)) {
                            etZone.setText(zone.getDisplayText());
                            // Ensure the field remains editable and clickable
                            etZone.setEnabled(true);
                            etZone.setClickable(true);
                            etZone.setFocusable(true);
                            etZone.setFocusableInTouchMode(true);
                            break;
                        }
                    }
                } else {
                    etZone.setText("");
                    selectedZoneId = null;
                }
            } else {
                etZone.setText("");
                selectedZoneId = null;
            }
        } else {
            etZone.setText("");
            selectedZoneId = null;
        }
        
        // Provides Delivery
        if (details.get("providesDelivery") != null) {
            Boolean providesDelivery = (Boolean) details.get("providesDelivery");
            cbProvidesDelivery.setChecked(providesDelivery != null && providesDelivery);
            if (providesDelivery != null && providesDelivery && details.get("deliveryRadius") != null) {
                Object radius = details.get("deliveryRadius");
                if (radius instanceof Number) {
                    etDeliveryRadius.setText(String.valueOf(((Number) radius).doubleValue()));
                } else if (radius != null) {
                    etDeliveryRadius.setText(radius.toString());
                }
            } else {
                etDeliveryRadius.setText("");
            }
        } else {
            cbProvidesDelivery.setChecked(false);
            etDeliveryRadius.setText("");
        }
        
        // Populate address fields - address structure is always returned by backend
        if (details.get("address") != null) {
            Map<String, Object> address = (Map<String, Object>) details.get("address");
            if (address != null) {
                if (address.get("street") != null) {
                    etStreet.setText(address.get("street").toString());
                } else {
                    etStreet.setText("");
                }
                if (address.get("city") != null) {
                    etCity.setText(address.get("city").toString());
                } else {
                    etCity.setText("");
                }
                if (address.get("state") != null) {
                    etState.setText(address.get("state").toString());
                } else {
                    etState.setText("");
                }
                if (address.get("zipCode") != null) {
                    etZipCode.setText(address.get("zipCode").toString());
                } else {
                    etZipCode.setText("");
                }
            }
        } else {
            // Fallback: set empty address fields
            etStreet.setText("");
            etCity.setText("");
            etState.setText("");
            etZipCode.setText("");
        }
    }

    private void setupDeliveryToggle() {
        cbProvidesDelivery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etDeliveryRadius.setEnabled(isChecked);
            if (!isChecked) {
                etDeliveryRadius.setText("");
            }
        });
    }

    private void setupZoneDropdown() {
        // Initialize adapter with empty list, will be populated when zones are loaded
        zoneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        etZone.setAdapter(zoneAdapter);
        etZone.setThreshold(1); // Show dropdown after typing 1 character
        
        // Make sure the field is enabled and clickable
        etZone.setEnabled(true);
        etZone.setClickable(true);
        etZone.setFocusable(true);
        etZone.setFocusableInTouchMode(true);
        
        // Show dropdown when field is clicked (even if it has text)
        etZone.setOnClickListener(v -> {
            if (etZone.getAdapter() != null && etZone.getAdapter().getCount() > 0) {
                etZone.showDropDown();
            }
        });
        
        // Show dropdown when field gets focus
        etZone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && etZone.getAdapter() != null && etZone.getAdapter().getCount() > 0) {
                etZone.showDropDown();
            }
        });
        
        // Handle zone selection - auto-populate address fields
        etZone.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);
            // Find the zone that matches the selected text
            for (DeliveryZone zone : zonesList) {
                if (zone.getDisplayText().equals(selectedText)) {
                    selectedZoneId = zone.getId();
                    // Auto-populate address fields from selected zone
                    autoPopulateAddressFromZone(zone);
                    break;
                }
            }
        });
    }

    /**
     * Auto-populate address fields (city, state, zipCode) from selected zone
     */
    private void autoPopulateAddressFromZone(DeliveryZone zone) {
        if (zone == null) {
            return;
        }
        
        // Auto-populate city
        if (zone.getCity() != null && !zone.getCity().trim().isEmpty()) {
            etCity.setText(zone.getCity().trim());
            
            // Auto-populate state using CityStateHelper (like customer address form)
            String state = CityStateHelper.getStateForCity(zone.getCity().trim());
            if (state != null && !state.isEmpty()) {
                etState.setText(state);
            }
        }
        
        // Auto-populate pincode (extract first pincode from pincodeRanges)
        String pincode = zone.getFirstPincode();
        if (pincode != null && !pincode.isEmpty()) {
            etZipCode.setText(pincode);
        }
    }

    private void loadDeliveryZones() {
        Call<List<DeliveryZone>> call = apiInterface.getDeliveryZones();
        call.enqueue(new Callback<List<DeliveryZone>>() {
            @Override
            public void onResponse(Call<List<DeliveryZone>> call, Response<List<DeliveryZone>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    zonesList = response.body();
                    // Populate adapter with formatted zone names
                    List<String> zoneDisplayNames = new ArrayList<>();
                    for (DeliveryZone zone : zonesList) {
                        zoneDisplayNames.add(zone.getDisplayText());
                    }
                    zoneAdapter.clear();
                    zoneAdapter.addAll(zoneDisplayNames);
                    zoneAdapter.notifyDataSetChanged();
                    
                    // Ensure the dropdown is properly configured
                    if (etZone != null) {
                        etZone.setAdapter(zoneAdapter);
                        etZone.setEnabled(true);
                    }
                    
                    // Now load provider details (which will set the selected zone)
                    checkOnboardingStatusAndLoadData();
                } else {
                    ToastUtils.showError(ProviderDetailsActivity.this, 
                        "Failed to load delivery zones");
                    // Still try to load provider details even if zones fail
                    checkOnboardingStatusAndLoadData();
                }
            }

            @Override
            public void onFailure(Call<List<DeliveryZone>> call, Throwable t) {
                ToastUtils.showError(ProviderDetailsActivity.this, 
                    "Error loading zones: " + t.getMessage());
                // Still try to load provider details even if zones fail
                checkOnboardingStatusAndLoadData();
            }
        });
    }

    private void saveProviderDetails() {
        String businessName = etBusinessName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String commissionRateStr = etCommissionRate.getText().toString().trim();
        String deliveryRadiusStr = etDeliveryRadius.getText().toString().trim();
        boolean providesDelivery = cbProvidesDelivery.isChecked();

        // Address fields
        String street = etStreet.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String zipCode = etZipCode.getText().toString().trim();

        // Validation
        if (businessName.isEmpty() || description.isEmpty() || 
            commissionRateStr.isEmpty() || selectedZoneId == null ||
            street.isEmpty() || city.isEmpty() || state.isEmpty() || 
            zipCode.isEmpty()) {
            ToastUtils.showInfo(this, "Please fill all required fields including zone");
            return;
        }

        if (providesDelivery && deliveryRadiusStr.isEmpty()) {
            ToastUtils.showInfo(this, "Please enter delivery radius");
            return;
        }

        try {
            // Create request map matching backend expectation
            Map<String, Object> request = new HashMap<>();
            request.put("user", sessionManager.getUserId());
            request.put("businessName", businessName);
            request.put("description", description);
            request.put("commissionRate", Double.parseDouble(commissionRateStr));
            request.put("providesDelivery", providesDelivery);
            request.put("zone", selectedZoneId);

            if (providesDelivery) {
                request.put("deliveryRadius", Double.parseDouble(deliveryRadiusStr));
            }

            // Create address map
            Map<String, Object> addressMap = new HashMap<>();
            addressMap.put("street", street);
            addressMap.put("city", city);
            addressMap.put("state", state);
            addressMap.put("zipCode", zipCode);
            request.put("address", addressMap);

            // Use Map-based API call (backend expects Map)
            Call<Map<String, Object>> call = apiInterface.saveProviderDetails(request);
            call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    Boolean isOnboarding = result.get("isOnboarding") != null ? 
                        (Boolean) result.get("isOnboarding") : true;
                    
                    // Verify backend set isOnboarding to false
                    if (Boolean.TRUE.equals(isOnboarding)) {
                        // Backend didn't update properly - show error
                        ToastUtils.showWarning(ProviderDetailsActivity.this, 
                            "Profile saved but onboarding status not updated. Please try again.");
                        return; // Don't navigate if onboarding not complete
                    }
                    
                    // Update local session flag to TRUE (profile is complete)
                    sessionManager.setProfileComplete(true);
                    
                    // Verify the Boolean was saved correctly
                    boolean isProfileComplete = sessionManager.isProfileComplete();
                    if (!isProfileComplete) {
                        // Retry saving the flag
                        sessionManager.setProfileComplete(true);
                    }
                    
                    ToastUtils.showSuccess(ProviderDetailsActivity.this, 
                        isEditing ? "Profile updated successfully!" : "Profile completed successfully! Welcome to PlateMate!");
                    
                    // Always navigate to dashboard after successful save
                    navigateToProviderDashboard();
                } else {
                    // Handle error response
                    String errorMessage = "Failed to save details";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(ProviderDetailsActivity.this, errorMessage);
                }
            }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    ToastUtils.showError(ProviderDetailsActivity.this, 
                        "Error: " + t.getMessage());
                }
            });
        } catch (NumberFormatException e) {
            ToastUtils.showError(this, "Invalid number format");
        }
    }

    private void navigateToProviderDashboard() {
        if (isEditing) {
            // Editing mode: Just finish and return to existing dashboard
            // The dashboard's onActivityResult() or onResume() will refresh the data
            setResult(RESULT_OK); // Signal that data was updated
            finish();
        } else {
            // First time onboarding: Navigate to dashboard with cleared back stack
            Intent intent = new Intent(this, ProviderDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("profileJustCompleted", true);
            startActivity(intent);
            finish();
        }
    }
}

