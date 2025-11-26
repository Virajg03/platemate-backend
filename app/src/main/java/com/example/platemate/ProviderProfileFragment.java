package com.example.platemate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import com.example.platemate.Image;
import com.example.platemate.User;
import com.example.platemate.Address;
import com.example.platemate.ToastUtils;

public class ProviderProfileFragment extends Fragment {
    
    private ImageView ivProfilePicture, btnEditProfilePicture, backButton, btnEditAddress;
    private LinearLayout btnEditProfile;
    private TextView tvBusinessName, tvUsername, tvEmail, tvDescription, 
                     tvCommissionRate, tvZone, tvVerificationStatus,
                     tvStreetAddress, tvCityStateZip;
    private LinearLayout logoutButton, addressDisplayLayout, noAddressLayout;
    private ProgressBar progressBar;
    
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    private Map<String, Object> providerDetails;
    private Address providerAddress;
    private User currentUser;
    private Long currentUserId;
    private Long currentProviderId; // Store providerId for image upload
    
    private static final int REQUEST_CODE_PICK_IMAGE = 2001;
    private static final int REQUEST_CODE_CAMERA = 2002;
    private static final int REQUEST_CODE_EDIT_PROFILE = 100;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provider_profile, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        sessionManager = new SessionManager(getContext());
        apiInterface = RetrofitClient.getInstance(getContext()).getApi();
        
        initializeViews(view);
        setupClickListeners(view);
        loadProviderProfile();
    }
    
    private void initializeViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnEditProfilePicture = view.findViewById(R.id.btnEditProfilePicture);
        backButton = view.findViewById(R.id.backButton);
        btnEditAddress = view.findViewById(R.id.btnEditAddress);
        tvBusinessName = view.findViewById(R.id.tvBusinessName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvCommissionRate = view.findViewById(R.id.tvCommissionRate);
        tvZone = view.findViewById(R.id.tvZone);
        tvVerificationStatus = view.findViewById(R.id.tvVerificationStatus);
        tvStreetAddress = view.findViewById(R.id.tvStreetAddress);
        tvCityStateZip = view.findViewById(R.id.tvCityStateZip);
        logoutButton = view.findViewById(R.id.btnLogout);
        addressDisplayLayout = view.findViewById(R.id.addressDisplayLayout);
        noAddressLayout = view.findViewById(R.id.noAddressLayout);
        progressBar = view.findViewById(R.id.progressBar);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
    }
    
    private void setupClickListeners(View view) {
        // Delivery Partners button
        View btnDeliveryPartners = view.findViewById(R.id.btnDeliveryPartners);
        if (btnDeliveryPartners != null) {
            btnDeliveryPartners.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DeliveryPartnersActivity.class);
                startActivity(intent);
            });
        }
        // Back button - return to dashboard
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // Switch to dashboard tab
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigationView);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_dashboard);
                    }
                }
            });
        }
        
        // Logout button
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> showLogoutDialog());
        }
        
        // Edit profile picture
        if (btnEditProfilePicture != null) {
            btnEditProfilePicture.setOnClickListener(v -> showImagePickerDialog());
        }
        
        // Edit address
        if (btnEditAddress != null) {
            btnEditAddress.setOnClickListener(v -> showAddressEditDialog());
        }
        
        // Edit profile
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }
    }
    
    private void loadProviderProfile() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Get user ID from session
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            loadFromSessionManager();
            return;
        }
        
        currentUserId = userId;
        
        // Step 1: Load User data (username, email, profile image)
        Call<User> userCall = apiInterface.getCustomerProfile(userId); // Reuse same endpoint
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    displayUserInfo(currentUser);
                    
                    // Step 2: Load Provider details
                    loadProviderDetails();
                } else {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    loadFromSessionManager();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                loadFromSessionManager();
                Log.e("ProviderProfileFragment", "Failed to load user data", t);
            }
        });
    }
    
    private void loadProviderDetails() {
        // Get provider details using existing endpoint
        Call<Map<String, Object>> call = apiInterface.getProviderDetails();
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    providerDetails = response.body();
                    
                    // Store providerId for image upload
                    Object providerIdObj = providerDetails.get("id");
                    if (providerIdObj != null) {
                        currentProviderId = parseProviderId(providerIdObj);
                        if (currentProviderId != null) {
                            Log.d("ProviderProfile", "Stored providerId: " + currentProviderId);
                        } else {
                            Log.e("ProviderProfile", "Failed to parse providerId: " + providerIdObj);
                            // Log all keys to help debug
                            Log.e("ProviderProfile", "Available keys in response: " + providerDetails.keySet());
                        }
                    } else {
                        Log.w("ProviderProfile", "Provider ID not found in response. Available keys: " + providerDetails.keySet());
                    }
                    
                    displayProviderProfile(providerDetails);
                    
                    // Load address
                    if (providerDetails.get("address") != null) {
                        Map<String, Object> addressMap = (Map<String, Object>) providerDetails.get("address");
                        providerAddress = convertMapToAddress(addressMap);
                        displayAddress(providerAddress);
                    } else {
                        showNoAddress();
                    }
                } else {
                    ToastUtils.showError(getContext(), "Failed to load provider profile");
                    loadFromSessionManager();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                ToastUtils.showError(getContext(), "Error: " + t.getMessage());
                loadFromSessionManager();
                Log.e("ProviderProfileFragment", "Failed to load provider details", t);
            }
        });
    }
    
    private void displayUserInfo(User user) {
        if (user == null) {
            Log.w("ProviderProfile", "displayUserInfo called with null user");
            return;
        }
        
        // Username
        if (tvUsername != null) {
            String username = user.getUsername();
            tvUsername.setText(username != null && !username.isEmpty() ? username : "N/A");
        }
        
        // Email
        if (tvEmail != null) {
            String email = user.getEmail();
            tvEmail.setText(email != null && !email.isEmpty() ? email : "N/A");
        }
        
        // Profile image
        Long profileImageId = user.getProfileImageId();
        if (profileImageId != null) {
            Log.d("ProviderProfile", "Loading profile image for user. ImageId: " + profileImageId);
            loadProfileImageFromId(profileImageId);
        } else {
            Log.d("ProviderProfile", "No profile image ID found for user. Using default placeholder.");
            if (ivProfilePicture != null) {
                ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
            }
        }
    }
    
    private void displayProviderProfile(Map<String, Object> details) {
        if (details == null) return;
        
        // Business Name
        if (tvBusinessName != null) {
            String businessName = details.get("businessName") != null ? 
                details.get("businessName").toString() : "N/A";
            tvBusinessName.setText(businessName);
        }
        
        // Description
        if (tvDescription != null) {
            String description = details.get("description") != null ? 
                details.get("description").toString() : "No description";
            if (description.isEmpty()) {
                description = "No description";
            }
            tvDescription.setText(description);
        }
        
        // Commission Rate
        if (tvCommissionRate != null) {
            Object commissionRate = details.get("commissionRate");
            if (commissionRate != null) {
                try {
                    double rate = Double.parseDouble(commissionRate.toString());
                    tvCommissionRate.setText(rate + "%");
                } catch (NumberFormatException e) {
                    tvCommissionRate.setText(commissionRate.toString() + "%");
                }
            } else {
                tvCommissionRate.setText("N/A");
            }
        }
        
        // Zone
        if (tvZone != null) {
            Object zone = details.get("zone");
            if (zone != null) {
                // Zone is returned as ID, we might want to fetch zone name
                // For now, just show the ID
                tvZone.setText("Zone ID: " + zone.toString());
            } else {
                tvZone.setText("N/A");
            }
        }
        
        // Verification Status
        if (tvVerificationStatus != null) {
            Object isVerified = details.get("isVerified");
            boolean verified = isVerified != null && Boolean.parseBoolean(isVerified.toString());
            if (verified) {
                tvVerificationStatus.setText("✓ Verified");
                tvVerificationStatus.setBackgroundResource(R.drawable.category_badge);
                // Use green color for verified status
                tvVerificationStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else {
                tvVerificationStatus.setText("Pending Verification");
                tvVerificationStatus.setBackgroundResource(R.drawable.category_badge);
                tvVerificationStatus.setTextColor(getResources().getColor(R.color.login_orange, null));
            }
        }
    }
    
    private Address convertMapToAddress(Map<String, Object> addressMap) {
        Address address = new Address();
        if (addressMap != null) {
            address.setStreet(addressMap.get("street") != null ? 
                addressMap.get("street").toString() : "");
            address.setCity(addressMap.get("city") != null ? 
                addressMap.get("city").toString() : "");
            address.setState(addressMap.get("state") != null ? 
                addressMap.get("state").toString() : "");
            address.setZipCode(addressMap.get("zipCode") != null ? 
                addressMap.get("zipCode").toString() : "");
        }
        return address;
    }
    
    private void displayAddress(Address address) {
        if (address == null) {
            showNoAddress();
            return;
        }
        
        String street = address.getStreet();
        String city = address.getCity();
        String state = address.getState();
        String zipCode = address.getZipCode();
        
        if (street != null && !street.isEmpty() && 
            city != null && !city.isEmpty() && 
            state != null && !state.isEmpty() && 
            zipCode != null && !zipCode.isEmpty()) {
            
            if (addressDisplayLayout != null) {
                addressDisplayLayout.setVisibility(View.VISIBLE);
            }
            if (noAddressLayout != null) {
                noAddressLayout.setVisibility(View.GONE);
            }
            
            if (tvStreetAddress != null) {
                tvStreetAddress.setText(street);
            }
            if (tvCityStateZip != null) {
                tvCityStateZip.setText(city + ", " + state + " - " + zipCode);
            }
        } else {
            showNoAddress();
        }
    }
    
    private void showNoAddress() {
        if (addressDisplayLayout != null) {
            addressDisplayLayout.setVisibility(View.GONE);
        }
        if (noAddressLayout != null) {
            noAddressLayout.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Helper method to parse provider ID from various number formats.
     * Handles both integer (4) and decimal (4.0) formats that might come from JSON.
     */
    private Long parseProviderId(Object providerIdObj) {
        if (providerIdObj == null) {
            return null;
        }
        
        try {
            // If it's already a Long or Integer
            if (providerIdObj instanceof Long) {
                return (Long) providerIdObj;
            }
            if (providerIdObj instanceof Integer) {
                return ((Integer) providerIdObj).longValue();
            }
            
            // If it's a Double or Float, convert to long
            if (providerIdObj instanceof Double) {
                return ((Double) providerIdObj).longValue();
            }
            if (providerIdObj instanceof Float) {
                return ((Float) providerIdObj).longValue();
            }
            
            // Try parsing as string - handle both "4" and "4.0" formats
            String idStr = providerIdObj.toString().trim();
            
            // If it contains a decimal point, parse as double first
            if (idStr.contains(".")) {
                double doubleValue = Double.parseDouble(idStr);
                return (long) doubleValue;
            } else {
                // Parse directly as long
                return Long.parseLong(idStr);
            }
        } catch (NumberFormatException e) {
            Log.e("ProviderProfile", "Failed to parse providerId: " + providerIdObj + " (type: " + 
                  (providerIdObj != null ? providerIdObj.getClass().getSimpleName() : "null") + ")", e);
            return null;
        }
    }
    
    private void loadProfileImageFromId(Long imageId) {
        if (getContext() == null || imageId == null || ivProfilePicture == null) {
            Log.e("ProviderProfile", "Cannot load image: context=" + (getContext() != null) + 
                  ", imageId=" + imageId + ", imageView=" + (ivProfilePicture != null));
            return;
        }
        
        // Show placeholder while loading
        ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
        
        Log.d("ProviderProfile", "Loading profile image with ID: " + imageId);
        Call<ResponseBody> call = apiInterface.getImage(imageId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null && ivProfilePicture != null) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        if (bitmap != null) {
                            ivProfilePicture.setImageBitmap(bitmap);
                            Log.d("ProviderProfile", "Image loaded successfully: " + imageId);
                        } else {
                            Log.e("ProviderProfile", "Failed to decode bitmap for imageId: " + imageId);
                            ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                        }
                    } catch (Exception e) {
                        Log.e("ProviderProfile", "Error processing image bytes for imageId: " + imageId, e);
                        if (ivProfilePicture != null) {
                            ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                        }
                    }
                } else {
                    Log.e("ProviderProfile", "Image API call failed: code=" + (response != null ? response.code() : "null") + 
                          ", message=" + (response != null ? response.message() : "null") + 
                          ", imageId=" + imageId);
                    if (ivProfilePicture != null) {
                        ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ProviderProfile", "Image API call failed for imageId: " + imageId, t);
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                }
            }
        });
    }
    
    private void showImagePickerDialog() {
        if (getActivity() == null) return;
        
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(getActivity())
            .setTitle("Select Profile Picture")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Camera
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
                    }
                } else {
                    // Gallery
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, 
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, REQUEST_CODE_PICK_IMAGE);
                }
            })
            .show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_EDIT_PROFILE) {
            // Profile was edited, reload data
            loadProviderProfile();
            return;
        }
        
        if (resultCode != Activity.RESULT_OK) return;
        
        Bitmap bitmap = null;
        
        if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                ToastUtils.showError(getContext(), "Failed to load image");
                return;
            }
        } else if (requestCode == REQUEST_CODE_CAMERA && data != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
        }
        
        if (bitmap != null) {
            uploadProfileImage(bitmap);
        }
    }
    
    private void uploadProfileImage(Bitmap bitmap) {
        if (getContext() == null) return;
        
        // Check if we have providerId, if not fetch it first
        if (currentProviderId == null) {
            Log.w("ProviderProfile", "ProviderId not available, fetching provider details first");
            fetchProviderIdAndUpload(bitmap);
            return;
        }
        
        uploadImageWithProviderId(bitmap, currentProviderId);
    }
    
    private void fetchProviderIdAndUpload(Bitmap bitmap) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        Call<Map<String, Object>> call = apiInterface.getProviderDetails();
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> details = response.body();
                    Object providerIdObj = details.get("id");
                    if (providerIdObj != null) {
                        Long providerId = parseProviderId(providerIdObj);
                        if (providerId != null) {
                            currentProviderId = providerId;
                            Log.d("ProviderProfile", "Fetched providerId for upload: " + providerId);
                            uploadImageWithProviderId(bitmap, providerId);
                        } else {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            Log.e("ProviderProfile", "Failed to parse providerId: " + providerIdObj);
                            Log.e("ProviderProfile", "Available keys: " + details.keySet());
                            ToastUtils.showError(getContext(), "Failed to get provider ID");
                        }
                    } else {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Log.e("ProviderProfile", "Provider ID not found in response. Available keys: " + details.keySet());
                        ToastUtils.showError(getContext(), "Provider ID not found in response");
                    }
                } else {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e("ProviderProfile", "Failed to fetch provider details. Code: " + 
                          (response != null ? response.code() : "null") + 
                          ", Message: " + (response != null ? response.message() : "null"));
                    ToastUtils.showError(getContext(), "Failed to fetch provider details");
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e("ProviderProfile", "Failed to fetch provider details", t);
                ToastUtils.showError(getContext(), "Error: " + t.getMessage());
            }
        });
    }
    
    private void uploadImageWithProviderId(Bitmap bitmap, Long providerId) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            
            RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/jpeg"),
                imageBytes
            );
            
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "file",
                "profile_image.jpg",
                requestFile
            );
            
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            // Upload using provider profile image endpoint with providerId
            Log.d("ProviderProfile", "Uploading image with providerId: " + providerId);
            Call<Image> call = apiInterface.uploadProviderProfileImage(providerId, imagePart);
            call.enqueue(new Callback<Image>() {
                @Override
                public void onResponse(Call<Image> call, Response<Image> response) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Image uploadedImage = response.body();
                        Long newImageId = uploadedImage.getId();
                        
                        Log.d("ProviderProfile", "Image uploaded successfully. New imageId: " + newImageId);
                        
                        // Update currentUser with new imageId immediately
                        if (currentUser != null && newImageId != null) {
                            currentUser.setProfileImageId(newImageId);
                            // Display immediately without waiting for reload
                            loadProfileImageFromId(newImageId);
                        }
                        
                        // Also reload profile to get all updated data
                        loadProviderProfile();
                        ToastUtils.showSuccess(getContext(), "Profile picture updated successfully!");
                    } else {
                        Log.e("ProviderProfile", "Image upload failed: code=" + (response != null ? response.code() : "null") + 
                              ", message=" + (response != null ? response.message() : "null"));
                        ToastUtils.showError(getContext(), "Failed to upload profile picture");
                    }
                }
                
                @Override
                public void onFailure(Call<Image> call, Throwable t) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e("ProviderProfile", "Image upload failed", t);
                    ToastUtils.showError(getContext(), "Error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Log.e("ProviderProfile", "Failed to process image", e);
            ToastUtils.showError(getContext(), "Failed to process image");
        }
    }
    
    private void showAddressEditDialog() {
        if (getContext() == null) return;
        
        AddressDialog.show(getContext(), providerAddress, new AddressDialog.AddressDialogListener() {
            @Override
            public void onAddressSaved(String street, String city, String state, String zipCode) {
                // Reload address
                loadProviderDetails();
            }
            
            @Override
            public void onCancel() {
                // Do nothing
            }
        });
    }
    
    private void showEditProfileDialog() {
        // Navigate to ProviderDetailsActivity in edit mode
        if (getActivity() == null) return;
        
        Intent intent = new Intent(getActivity(), ProviderDetailsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
    }
    
    private void loadFromSessionManager() {
        String username = sessionManager.getUsername();
        if (username != null && !username.isEmpty()) {
            if (tvUsername != null) {
                tvUsername.setText(username);
            }
            if (tvBusinessName != null) {
                tvBusinessName.setText(username);
            }
        }
    }
    
    private void showLogoutDialog() {
        if (getActivity() != null) {
            new AlertDialog.Builder(getActivity())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> handleLogout())
                .setNegativeButton("No", null)
                .show();
        }
    }
    
    private void handleLogout() {
        if (getActivity() != null) {
            sessionManager.logout();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}

