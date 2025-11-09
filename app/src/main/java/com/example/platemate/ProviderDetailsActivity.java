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
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProviderDetailsActivity extends AppCompatActivity {
    private EditText etBusinessName, etDescription, etCommissionRate, 
                     etDeliveryRadius, etZone;
    private EditText etStreet, etCity, etState, etZipCode, etCountry;
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

        // Check if profile is already complete (editing mode)
        isEditing = sessionManager.isProfileComplete();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditing ? "Edit Profile" : "Complete Your Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(isEditing);
        }

        initializeViews();
        setupDeliveryToggle();
        setupBackButton();

        btnSaveDetails.setOnClickListener(v -> saveProviderDetails());
        
        // Load existing profile if editing
        if (isEditing) {
            loadProviderDetails();
        }
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
        etCountry = findViewById(R.id.etCountry);
    }

    private void setupBackButton() {
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (isEditing) {
                    onBackPressed();
                } else {
                    // Prevent going back during initial setup
                    onBackPressed();
                }
            });
        }
    }
    
    private void loadProviderDetails() {
        Call<ProviderDetails> call = apiInterface.getProviderDetails();
        call.enqueue(new Callback<ProviderDetails>() {
            @Override
            public void onResponse(Call<ProviderDetails> call, Response<ProviderDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProviderDetails details = response.body();
                    populateFields(details);
                } else {
                    Toast.makeText(ProviderDetailsActivity.this, 
                        "Failed to load profile details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProviderDetails> call, Throwable t) {
                Toast.makeText(ProviderDetailsActivity.this, 
                    "Error loading profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void populateFields(ProviderDetails details) {
        if (details.getBusinessName() != null) {
            etBusinessName.setText(details.getBusinessName());
        }
        if (details.getDescription() != null) {
            etDescription.setText(details.getDescription());
        }
        if (details.getCommissionRate() != null) {
            etCommissionRate.setText(String.valueOf(details.getCommissionRate()));
        }
        if (details.getZone() != null) {
            etZone.setText(String.valueOf(details.getZone()));
        }
        if (details.getProvidesDelivery() != null) {
            cbProvidesDelivery.setChecked(details.getProvidesDelivery());
            if (details.getProvidesDelivery() && details.getDeliveryRadius() != null) {
                etDeliveryRadius.setText(String.valueOf(details.getDeliveryRadius()));
            }
        }
        
        // Populate address fields
        if (details.getAddress() != null) {
            Address address = details.getAddress();
            if (address.getStreet() != null) {
                etStreet.setText(address.getStreet());
            }
            if (address.getCity() != null) {
                etCity.setText(address.getCity());
            }
            if (address.getState() != null) {
                etState.setText(address.getState());
            }
            if (address.getZipCode() != null) {
                etZipCode.setText(address.getZipCode());
            }
            if (address.getCountry() != null) {
                etCountry.setText(address.getCountry());
            }
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
        String country = etCountry.getText().toString().trim();

        // Validation
        if (businessName.isEmpty() || description.isEmpty() || 
            commissionRateStr.isEmpty() || zoneStr.isEmpty() ||
            street.isEmpty() || city.isEmpty() || state.isEmpty() || 
            zipCode.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (providesDelivery && deliveryRadiusStr.isEmpty()) {
            Toast.makeText(this, "Please enter delivery radius", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ProviderDetails providerDetails = new ProviderDetails();
            providerDetails.setUser(sessionManager.getUserId());
            providerDetails.setBusinessName(businessName);
            providerDetails.setDescription(description);
            providerDetails.setCommissionRate(Double.parseDouble(commissionRateStr));
            providerDetails.setProvidesDelivery(providesDelivery);
            providerDetails.setZone(Long.parseLong(zoneStr));

            if (providesDelivery) {
                providerDetails.setDeliveryRadius(Double.parseDouble(deliveryRadiusStr));
            }

            // Create address
            Address address = new Address();
            address.setStreet(street);
            address.setCity(city);
            address.setState(state);
            address.setZipCode(zipCode);
            address.setCountry(country);
            providerDetails.setAddress(address);

            Call<ProviderDetails> call = apiInterface.saveProviderDetails(providerDetails);
            call.enqueue(new Callback<ProviderDetails>() {
                @Override
                public void onResponse(Call<ProviderDetails> call, Response<ProviderDetails> response) {
                    if (response.isSuccessful()) {
                        sessionManager.setProfileComplete(true);
                        Toast.makeText(ProviderDetailsActivity.this, 
                            isEditing ? "Profile updated successfully!" : "Profile completed successfully!", 
                            Toast.LENGTH_SHORT).show();
                        navigateToProviderDashboard();
                    } else {
                        Toast.makeText(ProviderDetailsActivity.this, 
                            "Failed to save details", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ProviderDetails> call, Throwable t) {
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
            // Just finish and go back to dashboard
            finish();
        } else {
            // First time setup - navigate to dashboard
            Intent intent = new Intent(this, ProviderDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}

