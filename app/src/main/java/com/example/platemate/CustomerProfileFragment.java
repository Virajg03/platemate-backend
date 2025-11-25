package com.example.platemate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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

public class CustomerProfileFragment extends Fragment {
    
    private ImageView ivProfilePicture, btnEditProfilePicture, backButton, btnEditAddress;
    private LinearLayout btnEditProfile;
    private TextView tvUserName, tvUsername, tvFullName, tvEmail, tvDateOfBirth, tvStreetAddress, tvCityStateZip;
    private LinearLayout logoutButton, addressDisplayLayout, noAddressLayout;
    private ProgressBar progressBar;
    
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    private Customer customer;
    private Address customerAddress;
    private Long currentUserId; // Store user ID for image upload
    
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_CAMERA = 1002;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_customer_profile, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        sessionManager = new SessionManager(getContext());
        apiInterface = RetrofitClient.getInstance(getContext()).getApi();
        
        initializeViews(view);
        setupClickListeners(view);
        loadCustomerProfile();
        loadCustomerAddress();
    }
    
    private void initializeViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnEditProfilePicture = view.findViewById(R.id.btnEditProfilePicture);
        backButton = view.findViewById(R.id.backButton);
        btnEditAddress = view.findViewById(R.id.btnEditAddress);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvDateOfBirth = view.findViewById(R.id.tvDateOfBirth);
        tvStreetAddress = view.findViewById(R.id.tvStreetAddress);
        tvCityStateZip = view.findViewById(R.id.tvCityStateZip);
        logoutButton = view.findViewById(R.id.btnLogout);
        addressDisplayLayout = view.findViewById(R.id.addressDisplayLayout);
        noAddressLayout = view.findViewById(R.id.noAddressLayout);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners(View view) {
        // Back button
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigationView);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_home);
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
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }
    }
    
    private void loadCustomerProfile() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Get user ID from stored currentUserId or session
        Long userId = currentUserId != null ? currentUserId : sessionManager.getUserId();
        Log.d("User Id On Customer Profile", "currentUserId: " + currentUserId + ", sessionUserId: " + sessionManager.getUserId() + ", using: " + userId);
        if (userId == null) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            loadFromSessionManager();
            return;
        }
        Log.d("Before Calling getCustomerProfile", "Dummy Message");
        Call<User> call = apiInterface.getCustomerProfile(userId);
        Log.d("After Calling getCustomerProfile", "Dummy Message");
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.d("In onResponse getCustomerProfile", String.valueOf(response));

                
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Store user ID for later use (e.g., image upload)
                    currentUserId = user.getId();
                    Log.d("CustomerProfileFragment", "Profile loaded - userId from response: " + currentUserId);
                    
                    // Save userId to session manager if not already saved or if different
                    if (currentUserId != null) {
                        Long savedUserId = sessionManager.getUserId();
                        Log.d("CustomerProfileFragment", "Saved userId in session: " + savedUserId);
                        if (savedUserId == null || !savedUserId.equals(currentUserId)) {
                            // Update session with userId - we need to preserve other session data
                            String token = sessionManager.getToken();
                            String refreshToken = sessionManager.getRefreshToken();
                            String role = sessionManager.getRole();
                            String username = sessionManager.getUsername();
                            sessionManager.saveLoginSession(token, refreshToken, role, username, currentUserId);
                            Log.d("CustomerProfileFragment", "Updated session with userId: " + currentUserId);
                        }
                    } else {
                        Log.e("CustomerProfileFragment", "WARNING: User response has null ID!");
                    }
                    // Convert User to Customer format for display
                    customer = convertUserToCustomer(user);
                    // Also get address from user
                    if (user.getAddress() != null) {
                        customerAddress = user.getAddress();
                        displayAddress(customerAddress);
                    }
                    displayCustomerProfile(customer);
                    
                    // Fetch DOB separately from Customer endpoint
                    loadCustomerDOB(currentUserId);
                    
                    // Check for missing fields and show notification
                    checkAndNotifyMissingFields(user, customer);
                } else {
                    // Load from session manager as fallback
                    loadFromSessionManager();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                // Load from session manager as fallback
                loadFromSessionManager();
            }
        });
    }
    
    private Customer convertUserToCustomer(User user) {
        Customer c = new Customer();
        c.setId(user.getId());
        c.setUsername(user.getUsername());
        c.setEmail(user.getEmail());
        c.setPhone(user.getPhoneNumber());
        c.setFullName(user.getFullName()); // Set fullName from User model
        c.setProfileImageId(user.getProfileImageId()); // Set profileImageId from User model
        // Note: DOB will be fetched separately from Customer endpoint
        return c;
    }
    
    private void loadCustomerDOB(Long userId) {
        // Fetch customer details to get DOB
        Call<CustomerUpdateResponse> call = apiInterface.getCustomerByUserId(userId);
        call.enqueue(new Callback<CustomerUpdateResponse>() {
            @Override
            public void onResponse(Call<CustomerUpdateResponse> call, Response<CustomerUpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerUpdateResponse customerResponse = response.body();
                    String dob = customerResponse.getDateOfBirth();
                    if (dob != null && !dob.isEmpty() && customer != null) {
                        customer.setDateOfBirth(dob);
                        displayDOB(dob);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<CustomerUpdateResponse> call, Throwable t) {
                // Silently fail - DOB is optional
            }
        });
    }
    
    private void displayDOB(String dob) {
        if (tvDateOfBirth != null && dob != null && !dob.isEmpty()) {
            // Format the date for display (assuming backend returns yyyy-MM-dd)
            try {
                // If it's already in a readable format, use it as is
                // Otherwise, format it
                tvDateOfBirth.setText(dob);
            } catch (Exception e) {
                tvDateOfBirth.setText(dob);
            }
        } else if (tvDateOfBirth != null) {
            tvDateOfBirth.setText("N/A");
        }
    }
    
    private void loadCustomerAddress() {
        // Address is loaded as part of user profile, so this method is now redundant
        // But keeping it for backward compatibility and fallback to session manager
        SessionManager sm = new SessionManager(getContext());
        if (sm.hasDeliveryAddress()) {
            Address sessionAddress = new Address();
            sessionAddress.setStreet(sm.getDeliveryStreet());
            sessionAddress.setCity(sm.getDeliveryCity());
            sessionAddress.setState(sm.getDeliveryState());
            sessionAddress.setZipCode(sm.getDeliveryZipCode());
            displayAddress(sessionAddress);
        } else if (customerAddress != null) {
            displayAddress(customerAddress);
        } else {
            showNoAddress();
        }
    }
    
    private void loadFromSessionManager() {
        String username = sessionManager.getUsername();
        if (username != null && !username.isEmpty()) {
            if (tvUserName != null) {
                tvUserName.setText(username);
            }
            if (tvUsername != null) {
                tvUsername.setText(username);
            }
        }
    }
    
    private void displayCustomerProfile(Customer customer) {
        if (customer == null) return;
        
        // Username
        String username = customer.getUsername();
        if (username != null && !username.isEmpty()) {
            if (tvUserName != null) {
                tvUserName.setText(username);
            }
            if (tvUsername != null) {
                tvUsername.setText(username);
            }
        }
        
        // Full name (from customer model, fallback to username if not available)
        if (tvFullName != null) {
            String fullName = customer.getFullName();
            if (fullName == null || fullName.isEmpty()) {
                // Use username as fallback
                tvFullName.setText(username != null ? username : "N/A");
            } else {
                tvFullName.setText(fullName);
            }
        }
        
        // Email
        if (tvEmail != null) {
            String email = customer.getEmail();
            tvEmail.setText(email != null && !email.isEmpty() ? email : "N/A");
        }
        
        // Date of Birth
        if (tvDateOfBirth != null) {
            String dob = customer.getDateOfBirth();
            if (dob != null && !dob.isEmpty()) {
                displayDOB(dob);
            } else {
                tvDateOfBirth.setText("N/A");
            }
        }
        
        // Profile image - prioritize imageId, then imageUrl, then base64
        if (ivProfilePicture != null) {
            Long imageId = customer.getProfileImageId();
            
            if (imageId != null) {
                // Load image using ImageController endpoint
                loadProfileImageFromId(imageId);
            } else {
                String imageUrl = customer.getProfileImageUrl();
                String imageBase64 = customer.getProfileImageBase64();
                
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    try {
                        byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        if (bitmap != null) {
                            ivProfilePicture.setImageBitmap(bitmap);
                        }
                    } catch (Exception e) {
                        // Keep default image
                    }
                } else if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Load from URL using Glide
                    if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                        String baseUrl = "https://trypanosomal-annalise-stenographic.ngrok-free.dev";
                        imageUrl = baseUrl + imageUrl;
                    }
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.baseline_person_24)
                        .error(R.drawable.baseline_person_24)
                        .into(ivProfilePicture);
                }
            }
        }
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
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, REQUEST_CODE_PICK_IMAGE);
                }
            })
            .show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        android.util.Log.d("CustomerProfileFragment", "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
        
        // Handle image picker for EditProfileDialog ONLY if the dialog is open
        if ((requestCode == EditProfileDialog.REQUEST_CODE_PICK_IMAGE || 
             requestCode == EditProfileDialog.REQUEST_CODE_CAMERA) &&
            EditProfileDialog.isDialogOpen()) {
            android.util.Log.d("CustomerProfileFragment", "Handling EditProfileDialog image picker result");
            EditProfileDialog.handleImagePickerResult(getActivity(), requestCode, resultCode, data);
            return;
        }
        
        // Handle direct profile image upload (from edit icon beside profile image)
        if (resultCode != Activity.RESULT_OK) return;
        
        Bitmap bitmap = null;
        
        if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                android.util.Log.e("CustomerProfileFragment", "Failed to load image from gallery", e);
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
        
        // We need customerId, not userId. Fetch it from backend if we don't have it
        // First try to get it from customer object if available
        Long customerId = null;
        if (customer != null && customer.getId() != null) {
            // Check if this is actually customerId or userId
            // We need to fetch customerId from backend using userId
            Long userId = currentUserId != null ? currentUserId : sessionManager.getUserId();
            if (userId == null) {
                ToastUtils.showError(getContext(), "Unable to identify user. Please ensure you are logged in and try again.");
                return;
            }
            // Fetch customerId from backend
            fetchCustomerIdAndUploadImage(userId, bitmap);
            return;
        } else {
            Long userId = currentUserId != null ? currentUserId : sessionManager.getUserId();
            if (userId == null) {
                ToastUtils.showError(getContext(), "Unable to identify user. Please ensure you are logged in and try again.");
                return;
            }
            fetchCustomerIdAndUploadImage(userId, bitmap);
            return;
        }
    }
    
    private void fetchCustomerIdAndUploadImage(Long userId, Bitmap bitmap) {
        // Fetch customerId from backend using userId
        Call<CustomerUpdateResponse> call = apiInterface.getCustomerByUserId(userId);
        call.enqueue(new Callback<CustomerUpdateResponse>() {
            @Override
            public void onResponse(Call<CustomerUpdateResponse> call, Response<CustomerUpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long customerId = response.body().getId();
                    if (customerId != null) {
                        uploadProfileImageWithCustomerId(bitmap, customerId);
                    } else {
                        ToastUtils.showError(getContext(), "Customer profile not found");
                    }
                } else {
                    ToastUtils.showError(getContext(), "Failed to fetch customer profile");
                }
            }
            
            @Override
            public void onFailure(Call<CustomerUpdateResponse> call, Throwable t) {
                ToastUtils.showError(getContext(), "Error: " + t.getMessage());
            }
        });
    }
    
    private void uploadProfileImageWithCustomerId(Bitmap bitmap, Long customerId) {
        try {
            // Convert bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Create request body
            RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/jpeg"),
                imageBytes
            );
            
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "file",
                "profile_image.jpg",
                requestFile
            );
            
            // Show progress
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            // Use customerId as ownerId (same pattern as provider uses providerId)
            String imageType = "CUSTOMER_PROFILE";
            Call<Image> call = apiInterface.uploadImage(imageType, customerId, imagePart);
            call.enqueue(new Callback<Image>() {
                @Override
                public void onResponse(Call<Image> call, Response<Image> response) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Image uploadedImage = response.body();
                        // Update customer profile with new image ID
                        if (customer != null) {
                            customer.setProfileImageId(uploadedImage.getId());
                        }
                        // Reload profile to get updated data
                        loadCustomerProfile();
                        ToastUtils.showSuccess(getContext(), "Profile picture updated successfully!");
                    } else {
                        ToastUtils.showError(getContext(), "Failed to upload profile picture");
                    }
                }
                
                @Override
                public void onFailure(Call<Image> call, Throwable t) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    ToastUtils.showError(getContext(), "Error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            ToastUtils.showError(getContext(), "Failed to process image");
        }
    }
    
    private void loadProfileImageFromId(Long imageId) {
        if (getContext() == null || imageId == null || ivProfilePicture == null) return;
        
        // Load image using API call to handle authentication
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
                        } else {
                            ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                        }
                    } catch (Exception e) {
                        if (ivProfilePicture != null) {
                            ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                        }
                    }
                } else {
                    if (ivProfilePicture != null) {
                        ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageResource(R.drawable.baseline_person_24);
                }
            }
        });
    }
    
    private void showAddressEditDialog() {
        AddressDialog.show(getContext(), customerAddress, new AddressDialog.AddressDialogListener() {
            @Override
            public void onAddressSaved(String street, String city, String state, String zipCode) {
                // Reload address
                loadCustomerAddress();
            }
            
            @Override
            public void onCancel() {
                // Do nothing
            }
        });
    }
    
    private void showEditProfileDialog() {
        // Ensure we have userId before showing dialog
        Long userId = currentUserId != null ? currentUserId : sessionManager.getUserId();
        if (userId == null) {
            ToastUtils.showError(getContext(), "Unable to identify user. Please log in again.");
            return;
        }
        
        Log.d("CustomerProfileFragment", "Showing EditProfileDialog with userId: " + userId + ", customer: " + (customer != null ? customer.getId() : "null"));
        
        // Ensure customer has DOB before showing dialog - fetch if needed
        if (customer != null && (customer.getDateOfBirth() == null || customer.getDateOfBirth().isEmpty())) {
            // Fetch customer details to get DOB
            Call<CustomerUpdateResponse> call = apiInterface.getCustomerByUserId(userId);
            call.enqueue(new Callback<CustomerUpdateResponse>() {
                @Override
                public void onResponse(Call<CustomerUpdateResponse> call, Response<CustomerUpdateResponse> response) {
                    if (response.isSuccessful() && response.body() != null && customer != null) {
                        String dob = response.body().getDateOfBirth();
                        if (dob != null && !dob.isEmpty()) {
                            customer.setDateOfBirth(dob);
                        }
                    }
                    // Show dialog regardless of DOB fetch result
                    showEditProfileDialogInternal();
                }
                
                @Override
                public void onFailure(Call<CustomerUpdateResponse> call, Throwable t) {
                    // Show dialog even if DOB fetch fails
                    showEditProfileDialogInternal();
                }
            });
        } else {
            showEditProfileDialogInternal();
        }
    }
    
    private void showEditProfileDialogInternal() {
        // Pass Fragment instead of Context so we can use Fragment's startActivityForResult
        EditProfileDialog.show(this, customer, new EditProfileDialog.EditProfileDialogListener() {
            @Override
            public void onProfileSaved() {
                // Reload profile
                loadCustomerProfile();
            }
            
            @Override
            public void onCancel() {
                // Do nothing
            }
        });
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
            SessionManager sessionManager = new SessionManager(getActivity());
            sessionManager.logout();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
    
    private void checkAndNotifyMissingFields(User user, Customer customer) {
        if (getContext() == null || getActivity() == null) return;
        
        java.util.List<String> missingFields = new java.util.ArrayList<>();
        
        // Check fullName
        String fullName = customer != null ? customer.getFullName() : null;
        if (fullName == null || fullName.trim().isEmpty()) {
            missingFields.add("Full Name");
        }
        
        // Check email
        String email = user != null ? user.getEmail() : null;
        if (email == null || email.trim().isEmpty()) {
            missingFields.add("Email");
        }
        
        // Show notification if fields are missing
        if (!missingFields.isEmpty()) {
            String message = "Please complete your profile. Missing: " + 
                String.join(", ", missingFields) + ". Click Edit Profile to update.";
            
            // Use Snackbar for better UX
            com.google.android.material.snackbar.Snackbar snackbar = 
                com.google.android.material.snackbar.Snackbar.make(
                    getView(),
                    message,
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                );
            
            snackbar.setAction("EDIT PROFILE", v -> {
                snackbar.dismiss();
                showEditProfileDialog();
            });
            
            snackbar.setActionTextColor(getResources().getColor(R.color.login_orange, null));
            snackbar.show();
        }
    }
}
