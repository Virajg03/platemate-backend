package com.example.platemate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
    private TextView tvUserName, tvUsername, tvFullName, tvEmail, tvStreetAddress, tvCityStateZip;
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
    }
    
    private void loadCustomerProfile() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Get user ID from stored currentUserId or session
        Long userId = currentUserId != null ? currentUserId : sessionManager.getUserId();
        if (userId == null) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            loadFromSessionManager();
            return;
        }
        
        Call<User> call = apiInterface.getCustomerProfile(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Store user ID for later use (e.g., image upload)
                    currentUserId = user.getId();
                    // Also save to session manager if not already saved
                    if (currentUserId != null && sessionManager.getUserId() == null) {
                        // Note: We can't directly set userId in SessionManager, 
                        // but we'll use currentUserId variable
                    }
                    // Convert User to Customer format for display
                    customer = convertUserToCustomer(user);
                    // Also get address from user
                    if (user.getAddress() != null) {
                        customerAddress = user.getAddress();
                        displayAddress(customerAddress);
                    }
                    displayCustomerProfile(customer);
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
        // Note: profileImageId would need to be fetched separately if needed
        return c;
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
        
        // Full name (using username if no full name)
        if (tvFullName != null) {
            tvFullName.setText(username != null ? username : "N/A");
        }
        
        // Email
        if (tvEmail != null) {
            String email = customer.getEmail();
            tvEmail.setText(email != null && !email.isEmpty() ? email : "N/A");
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
        
        // Get user ID - prioritize currentUserId, then customer object, then session
        Long customerId = null;
        if (currentUserId != null) {
            customerId = currentUserId;
        } else if (customer != null && customer.getId() != null) {
            customerId = customer.getId();
        } else {
            customerId = sessionManager.getUserId();
        }
        
        // If still null, we can't proceed
        if (customerId == null) {
            ToastUtils.showError(getContext(), "Unable to identify user. Please ensure you are logged in and try again.");
            return;
        }
        
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
            
            // Use ImageController endpoint: /images/upload/{imageType}/{ownerId}
            // ImageType: CUSTOMER_PROFILE (assuming this is the enum value)
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
}
