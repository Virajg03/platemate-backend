package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class ProviderDetailsActivity extends AppCompatActivity {
    private EditText etBusinessName, etDescription, etCommissionRate, 
                     etDeliveryRadius, etZone;
    private EditText etStreet, etCity, etState, etZipCode;
    private CheckBox cbProvidesDelivery;
    private Button btnSaveDetails;
    private ApiInterface apiInterface;
    private SessionManager sessionManager;

    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_details);

        apiInterface = RetrofitClient.getInstance(this).getApi();
        sessionManager = new SessionManager(this);

        initializeViews();
        setupDeliveryToggle();
        setupBackButton();

        btnSaveDetails.setOnClickListener(v -> saveProviderDetails());
        
        // Check onboarding status from API and load provider details
        checkOnboardingStatusAndLoadData();
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
                    Toast.makeText(ProviderDetailsActivity.this, 
                        "Failed to load profile details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ProviderDetailsActivity.this, 
                    "Error loading profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        
        // Zone
        if (details.get("zone") != null) {
            Object zone = details.get("zone");
            etZone.setText(zone != null ? String.valueOf(zone) : "");
        } else {
            etZone.setText("");
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

    private void saveProviderDetails() {
        String businessName = etBusinessName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String commissionRateStr = etCommissionRate.getText().toString().trim();
        String deliveryRadiusStr = etDeliveryRadius.getText().toString().trim();
        String zoneStr = etZone.getText().toString().trim();
        boolean providesDelivery = cbProvidesDelivery.isChecked();

        // Address fields
        String street = etStreet.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String zipCode = etZipCode.getText().toString().trim();

        // Validation
        if (businessName.isEmpty() || description.isEmpty() || 
            commissionRateStr.isEmpty() || zoneStr.isEmpty() ||
            street.isEmpty() || city.isEmpty() || state.isEmpty() || 
            zipCode.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (providesDelivery && deliveryRadiusStr.isEmpty()) {
            Toast.makeText(this, "Please enter delivery radius", Toast.LENGTH_SHORT).show();
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
            request.put("zone", Long.parseLong(zoneStr));

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
                        Toast.makeText(ProviderDetailsActivity.this, 
                            "Profile saved but onboarding status not updated. Please try again.", 
                            Toast.LENGTH_LONG).show();
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
                    
                    Toast.makeText(ProviderDetailsActivity.this, 
                        isEditing ? "Profile updated successfully!" : "Profile completed successfully! Welcome to PlateMate!", 
                        Toast.LENGTH_SHORT).show();
                    
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
                    Toast.makeText(ProviderDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(ProviderDetailsActivity.this, 
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
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

