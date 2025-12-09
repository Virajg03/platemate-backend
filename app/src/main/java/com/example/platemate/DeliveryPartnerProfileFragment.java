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
import com.bumptech.glide.Glide;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class DeliveryPartnerProfileFragment extends Fragment {
    
    private ImageView ivProfilePicture, btnEditProfilePicture, backButton;
    private android.widget.RelativeLayout btnEditProfile;
    private TextView tvFullName, tvUsername, tvEmail, tvVehicleType, 
                     tvCommissionRate, tvServiceArea, tvAvailability;
    private LinearLayout logoutButton;
    private ProgressBar progressBar;
    
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    private DeliveryPartner deliveryPartner;
    private User currentUser;
    private Long currentUserId;
    private Long currentDeliveryPartnerId;
    
    private static final int REQUEST_CODE_PICK_IMAGE = 3001;
    private static final int REQUEST_CODE_CAMERA = 3002;
    private static final int REQUEST_CODE_EDIT_PROFILE = 3003;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delivery_partner_profile, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        sessionManager = new SessionManager(getContext());
        apiInterface = RetrofitClient.getInstance(getContext()).getApi();
        
        initializeViews(view);
        setupClickListeners(view);
        loadDeliveryPartnerProfile();
    }
    
    private void initializeViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnEditProfilePicture = view.findViewById(R.id.btnEditProfilePicture);
        backButton = view.findViewById(R.id.backButton);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvVehicleType = view.findViewById(R.id.tvVehicleType);
        tvCommissionRate = view.findViewById(R.id.tvCommissionRate);
        tvServiceArea = view.findViewById(R.id.tvServiceArea);
        tvAvailability = view.findViewById(R.id.tvAvailability);
        logoutButton = view.findViewById(R.id.btnLogout);
        progressBar = view.findViewById(R.id.progressBar);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
    }
    
    private void setupClickListeners(View view) {
        // Back button
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
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
        
        // Edit profile
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }
    }
    
    private void loadDeliveryPartnerProfile() {
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
        Log.d("DeliveryPartnerProfile", "Loading user data for userId: " + userId);
        Call<User> userCall = apiInterface.getCustomerProfile(userId); // Reuse same endpoint
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    Log.d("DeliveryPartnerProfile", "User data loaded successfully. Username: " + currentUser.getUsername());
                    displayUserInfo(currentUser);
                    
                    // Step 2: Load Delivery Partner details
                    loadDeliveryPartnerDetails();
                } else {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e("DeliveryPartnerProfile", "Failed to load user data. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("DeliveryPartnerProfile", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("DeliveryPartnerProfile", "Could not read error body", e);
                        }
                    }
                    loadFromSessionManager();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e("DeliveryPartnerProfile", "Network error loading user data", t);
                loadFromSessionManager();
            }
        });
    }
    
    private void loadDeliveryPartnerDetails() {
        // Use correct endpoint: GET /api/delivery-partners
        // For delivery partners, this returns their own profile(s)
        Log.d("DeliveryPartnerProfile", "Loading delivery partner details from /api/delivery-partners");
        Call<List<DeliveryPartner>> call = apiInterface.getDeliveryPartners();
        call.enqueue(new Callback<List<DeliveryPartner>>() {
            @Override
            public void onResponse(Call<List<DeliveryPartner>> call, Response<List<DeliveryPartner>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        // Get the first delivery partner profile (user can have multiple for different providers)
                        deliveryPartner = response.body().get(0);
                        currentDeliveryPartnerId = deliveryPartner.getId(); // Store for image upload
                        Log.d("DeliveryPartnerProfile", "Loaded delivery partner: " + deliveryPartner.getFullName() + ", ID: " + deliveryPartner.getId());
                        displayDeliveryPartnerProfile(deliveryPartner);
                    } else {
                        Log.w("DeliveryPartnerProfile", "Response body is null or empty. Response code: " + response.code());
                        // Try fallback: get delivery partner from orders
                        tryGetDeliveryPartnerFromOrders();
                    }
                } else {
                    // Handle error response
                    String errorMsg = "Failed to load delivery partner profile";
                    if (response.code() == 401) {
                        errorMsg = "Authentication failed. Please login again.";
                    } else if (response.code() == 403) {
                        errorMsg = "Access denied. You may not have permission to view this profile.";
                    } else if (response.code() == 404) {
                        errorMsg = "Profile not found. Please contact support.";
                    } else if (response.errorBody() != null) {
                        try {
                            errorMsg = "Error: " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg = "Failed to load profile (Code: " + response.code() + ")";
                        }
                    }
                    Log.e("DeliveryPartnerProfile", "Failed to load profile - HTTP " + response.code() + ": " + errorMsg);
                    
                    // If 403, try fallback method
                    if (response.code() == 403) {
                        Log.d("DeliveryPartnerProfile", "Got 403, trying fallback method");
                        tryGetDeliveryPartnerFromOrders();
                    } else {
                        ToastUtils.showError(getContext(), errorMsg);
                        loadFromSessionManager();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<List<DeliveryPartner>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e("DeliveryPartnerProfile", "Network error loading delivery partner profile", t);
                // Try fallback method
                tryGetDeliveryPartnerFromOrders();
            }
        });
    }
    
    /**
     * Fallback method: Try to get delivery partner info from orders
     * This is used when the direct profile endpoint fails
     */
    private void tryGetDeliveryPartnerFromOrders() {
        Log.d("DeliveryPartnerProfile", "Trying fallback: get delivery partner from orders");
        Call<List<Order>> call = apiInterface.getDeliveryPartnerOrders();
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Try to get delivery partner ID from first order
                    Order firstOrder = response.body().get(0);
                    if (firstOrder.getDeliveryPartnerId() != null) {
                        // Get delivery partner by ID
                        loadDeliveryPartnerById(firstOrder.getDeliveryPartnerId());
                    } else {
                        // No delivery partner ID in orders
                        ToastUtils.showError(getContext(), "Delivery partner profile not found");
                        loadFromSessionManager();
                    }
                } else {
                    ToastUtils.showError(getContext(), "Unable to load delivery partner profile");
                    loadFromSessionManager();
                }
            }
            
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                ToastUtils.showError(getContext(), "Error loading profile: " + t.getMessage());
                loadFromSessionManager();
                Log.e("DeliveryPartnerProfile", "Fallback method also failed", t);
            }
        });
    }
    
    /**
     * Load delivery partner by ID (used as fallback)
     */
    private void loadDeliveryPartnerById(Long deliveryPartnerId) {
        Log.d("DeliveryPartnerProfile", "Loading delivery partner by ID: " + deliveryPartnerId);
        Call<DeliveryPartner> call = apiInterface.getDeliveryPartnerById(deliveryPartnerId);
        call.enqueue(new Callback<DeliveryPartner>() {
            @Override
            public void onResponse(Call<DeliveryPartner> call, Response<DeliveryPartner> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    deliveryPartner = response.body();
                    currentDeliveryPartnerId = deliveryPartner.getId(); // Store for image upload
                    Log.d("DeliveryPartnerProfile", "Successfully loaded delivery partner by ID");
                    displayDeliveryPartnerProfile(deliveryPartner);
                } else {
                    ToastUtils.showError(getContext(), "Failed to load delivery partner profile");
                    loadFromSessionManager();
                }
            }
            
            @Override
            public void onFailure(Call<DeliveryPartner> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                ToastUtils.showError(getContext(), "Error: " + t.getMessage());
                loadFromSessionManager();
                Log.e("DeliveryPartnerProfile", "Failed to load delivery partner by ID", t);
            }
        });
    }
    
    private void displayUserInfo(User user) {
        if (user == null) {
            Log.w("DeliveryPartnerProfile", "displayUserInfo called with null user");
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
        
        // Profile image - same pattern as provider
        Long profileImageId = user.getProfileImageId();
        if (profileImageId != null) {
            Log.d("DeliveryPartnerProfile", "Loading profile image for user. ImageId: " + profileImageId);
            loadProfileImageFromId(profileImageId);
        } else {
            Log.d("DeliveryPartnerProfile", "No profile image ID found for user. Using default placeholder.");
            if (ivProfilePicture != null) {
                ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
            }
        }
    }
    
    private void displayDeliveryPartnerProfile(DeliveryPartner partner) {
        if (partner == null) {
            Log.w("DeliveryPartnerProfile", "displayDeliveryPartnerProfile called with null partner");
            return;
        }
        
        Log.d("DeliveryPartnerProfile", "Displaying profile for: " + partner.getFullName());
        
        // Full Name
        if (tvFullName != null) {
            String fullName = partner.getFullName();
            tvFullName.setText(fullName != null && !fullName.isEmpty() ? fullName : "N/A");
        }
        
        // Vehicle Type
        if (tvVehicleType != null) {
            String vehicleType = partner.getVehicleType();
            tvVehicleType.setText(vehicleType != null && !vehicleType.isEmpty() ? vehicleType : "N/A");
        }
        
        // Commission Rate
        if (tvCommissionRate != null) {
            if (partner.getCommissionRate() != null) {
                tvCommissionRate.setText(String.format("%.2f%%", partner.getCommissionRate()));
            } else {
                tvCommissionRate.setText("N/A");
            }
        }
        
        // Service Area
        if (tvServiceArea != null) {
            String serviceArea = partner.getServiceArea();
            tvServiceArea.setText(serviceArea != null && !serviceArea.isEmpty() ? serviceArea : "N/A");
        }
        
        // Availability
        if (tvAvailability != null) {
            boolean isAvailable = partner.getIsAvailable() != null && partner.getIsAvailable();
            tvAvailability.setText(isAvailable ? "Available" : "Not Available");
            tvAvailability.setTextColor(isAvailable ? 
                getResources().getColor(android.R.color.holo_green_dark) : 
                getResources().getColor(android.R.color.holo_red_dark));
        }
    }
    
    /**
     * Load profile image from image ID - same pattern as ProviderProfileFragment
     */
    private void loadProfileImageFromId(Long imageId) {
        if (getContext() == null || imageId == null || ivProfilePicture == null) {
            Log.e("DeliveryPartnerProfile", "Cannot load image: context=" + (getContext() != null) + 
                  ", imageId=" + imageId + ", imageView=" + (ivProfilePicture != null));
            return;
        }
        
        // Show placeholder while loading
        ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
        
        Log.d("DeliveryPartnerProfile", "Loading profile image with ID: " + imageId);
        Call<okhttp3.ResponseBody> call = apiInterface.getImage(imageId);
        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null && ivProfilePicture != null) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        if (bitmap != null) {
                            ivProfilePicture.setImageBitmap(bitmap);
                            Log.d("DeliveryPartnerProfile", "Image loaded successfully: " + imageId);
                        } else {
                            Log.e("DeliveryPartnerProfile", "Failed to decode bitmap for imageId: " + imageId);
                            ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                        }
                    } catch (Exception e) {
                        Log.e("DeliveryPartnerProfile", "Error processing image bytes for imageId: " + imageId, e);
                        if (ivProfilePicture != null) {
                            ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                        }
                    }
                } else {
                    Log.e("DeliveryPartnerProfile", "Image API call failed: code=" + (response != null ? response.code() : "null") + 
                          ", message=" + (response != null ? response.message() : "null") + 
                          ", imageId=" + imageId);
                    if (ivProfilePicture != null) {
                        ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Log.e("DeliveryPartnerProfile", "Failed to load image for imageId: " + imageId, t);
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                }
            }
        });
    }
    
    private void showImagePickerDialog() {
        if (getActivity() == null) return;
        
        String[] options = {"Camera", "Gallery", "Cancel"};
        new AlertDialog.Builder(getActivity())
            .setTitle("Select Profile Picture")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Camera
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
                    }
                } else if (which == 1) {
                    // Gallery
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, REQUEST_CODE_PICK_IMAGE);
                }
            })
            .show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_EDIT_PROFILE) {
            // Profile was updated, reload it
            Log.d("DeliveryPartnerProfile", "Profile updated, reloading...");
            loadDeliveryPartnerProfile();
            return;
        }
        
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        
        Bitmap bitmap = null;
        
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                } catch (Exception e) {
                    Log.e("DeliveryPartnerProfile", "Error loading image from gallery", e);
                    ToastUtils.showError(getContext(), "Failed to load image");
                    return;
                }
            }
        } else if (requestCode == REQUEST_CODE_CAMERA) {
            bitmap = (Bitmap) data.getExtras().get("data");
        }
        
        if (bitmap != null) {
            if (currentDeliveryPartnerId != null) {
                uploadProfileImage(bitmap);
            } else {
                ToastUtils.showError(getContext(), "Delivery partner profile not loaded. Please wait...");
                Log.e("DeliveryPartnerProfile", "Cannot upload image: delivery partner ID is null");
            }
        }
    }
    
    private void uploadProfileImage(Bitmap bitmap) {
        if (currentDeliveryPartnerId == null) {
            ToastUtils.showError(getContext(), "Delivery partner profile not loaded");
            Log.e("DeliveryPartnerProfile", "Cannot upload image: delivery partner ID is null");
            return;
        }
        
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        try {
            // Convert bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Create request body
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "profile.jpg", requestFile);
            
            // Upload image using delivery partner specific endpoint
            Log.d("DeliveryPartnerProfile", "Uploading image with deliveryPartnerId: " + currentDeliveryPartnerId);
            Call<Image> call = apiInterface.uploadDeliveryPartnerProfileImage(currentDeliveryPartnerId, imagePart);
            call.enqueue(new Callback<Image>() {
                @Override
                public void onResponse(Call<Image> call, Response<Image> response) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Image uploadedImage = response.body();
                        Log.d("DeliveryPartnerProfile", "Image uploaded successfully. ImageId: " + uploadedImage.getId());
                        ToastUtils.showSuccess(getContext(), "Profile picture updated successfully");
                        // Reload profile to show new image (backend will set profileImageId in user object)
                        loadDeliveryPartnerProfile();
                    } else {
                        ToastUtils.showError(getContext(), "Failed to upload profile picture");
                    }
                }
                
                @Override
                public void onFailure(Call<Image> call, Throwable t) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    ToastUtils.showError(getContext(), "Error uploading image: " + t.getMessage());
                    Log.e("DeliveryPartnerProfile", "Error uploading image", t);
                }
            });
        } catch (Exception e) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            ToastUtils.showError(getContext(), "Error processing image");
            Log.e("DeliveryPartnerProfile", "Error processing image", e);
        }
    }
    
    private void showEditProfileDialog() {
        if (deliveryPartner == null || deliveryPartner.getId() == null) {
            ToastUtils.showError(getContext(), "Profile data not loaded");
            return;
        }
        
        // Navigate to DeliveryPartnerFormActivity in edit mode
        if (getActivity() == null) return;
        
        Intent intent = new Intent(getActivity(), DeliveryPartnerFormActivity.class);
        intent.putExtra(DeliveryPartnerFormActivity.EXTRA_DELIVERY_PARTNER, deliveryPartner);
        intent.putExtra(DeliveryPartnerFormActivity.EXTRA_IS_EDIT, true);
        startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
    }
    
    private void loadFromSessionManager() {
        String username = sessionManager.getUsername();
        if (username != null && !username.isEmpty()) {
            if (tvUsername != null) {
                tvUsername.setText(username);
            }
            if (tvFullName != null) {
                tvFullName.setText(username);
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
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload profile when fragment resumes (e.g., returning from edit screen)
        if (currentUserId != null) {
            loadDeliveryPartnerProfile();
        }
    }
}

