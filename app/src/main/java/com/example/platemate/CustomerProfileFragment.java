package com.example.platemate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
    private SwipeRefreshLayout swipeRefreshLayout;
    
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
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // Setup SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                refreshProfile();
            });
            
            // Configure refresh colors
            swipeRefreshLayout.setColorSchemeResources(
                R.color.login_orange,
                android.R.color.holo_orange_dark,
                android.R.color.holo_orange_light
            );
        }
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
    
    private void refreshProfile() {
        // Reload profile which will also load address from backend
        loadCustomerProfile();
    }
    
    private void loadCustomerProfile() {
        // Don't show progress bar if refreshing (swipe refresh has its own indicator)
        if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
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
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
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
                    
                    // Update UI on main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                    // Also get address from user - add detailed logging
                    if (user.getAddress() != null) {
                        Address addr = user.getAddress();
                        Log.d("CustomerProfileFragment", "Address received from backend: " +
                            "street1=" + addr.getStreet1() + ", " +
                            "city=" + addr.getCity() + ", " +
                            "state=" + addr.getState() + ", " +
                            "pincode=" + addr.getPincode());
                        
                        customerAddress = addr;
                        
                        // Ensure we have valid address data before displaying
                        String street1 = addr.getStreet1() != null ? addr.getStreet1() : addr.getStreet();
                        String street2 = addr.getStreet2();
                        String city = addr.getCity();
                        String state = addr.getState();
                        String zipCode = addr.getPincode() != null ? addr.getPincode() : addr.getZipCode();
                        
                        // Combine street1 and street2 for SessionManager
                        String fullStreet = street1;
                        if (street2 != null && !street2.isEmpty()) {
                            fullStreet = street1 + ", " + street2;
                        }
                        
                        if (street1 != null && !street1.isEmpty() && 
                            city != null && !city.isEmpty() && 
                            state != null && !state.isEmpty() && 
                            zipCode != null && !zipCode.isEmpty()) {
                            
                            displayAddress(customerAddress);
                            
                            // Sync address to SessionManager for use in checkout
                            sessionManager.saveDeliveryAddress(fullStreet, city, state, zipCode);
                            Log.d("CustomerProfileFragment", "Address synced to SessionManager: " + fullStreet);
                        } else {
                            Log.w("CustomerProfileFragment", "Address data incomplete, showing no address");
                            showNoAddress();
                        }
                    } else {
                        Log.d("CustomerProfileFragment", "No address in user profile response");
                        // No address in user profile, try loading from session or show no address
                        loadCustomerAddress();
                    }
                            displayCustomerProfile(customer);
                        });
                    }
                    
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
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
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
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                SessionManager sm = new SessionManager(getContext());
                if (sm.hasDeliveryAddress()) {
                    Address sessionAddress = new Address();
                    sessionAddress.setStreet(sm.getDeliveryStreet());
                    sessionAddress.setCity(sm.getDeliveryCity());
                    sessionAddress.setState(sm.getDeliveryState());
                    sessionAddress.setZipCode(sm.getDeliveryZipCode());
                    customerAddress = sessionAddress; // Update cached address
                    displayAddress(sessionAddress);
                } else if (customerAddress != null) {
                    displayAddress(customerAddress);
                } else {
                    showNoAddress();
                }
            });
        } else {
            // Fallback if activity is null
            SessionManager sm = new SessionManager(getContext());
            if (sm.hasDeliveryAddress()) {
                Address sessionAddress = new Address();
                sessionAddress.setStreet(sm.getDeliveryStreet());
                sessionAddress.setCity(sm.getDeliveryCity());
                sessionAddress.setState(sm.getDeliveryState());
                sessionAddress.setZipCode(sm.getDeliveryZipCode());
                customerAddress = sessionAddress;
                displayAddress(sessionAddress);
            } else if (customerAddress != null) {
                displayAddress(customerAddress);
            } else {
                showNoAddress();
            }
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
                        
                        // Decode with downsampling
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
                        
                        // Calculate sample size
                        int reqSize = 800;
                        int inSampleSize = 1;
                        int maxDim = Math.max(options.outWidth, options.outHeight);
                        if (maxDim > reqSize) {
                            inSampleSize = (int) Math.ceil((double) maxDim / reqSize);
                            inSampleSize = (int) Math.pow(2, Math.ceil(Math.log(inSampleSize) / Math.log(2)));
                        }
                        if (maxDim > 2000) inSampleSize = Math.max(inSampleSize, 4);
                        if (maxDim > 4000) inSampleSize = Math.max(inSampleSize, 8);
                        
                        // Decode with sample size
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = inSampleSize;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
                        if (bitmap != null) {
                            // Scale down further if needed
                            bitmap = scaleDownBitmap(bitmap, 800);
                            if (bitmap != null) {
                                ivProfilePicture.setImageBitmap(bitmap);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("CustomerProfileFragment", "Error loading base64 image", e);
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
        // Ensure UI updates happen on main thread
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (address == null) {
                    Log.d("CustomerProfileFragment", "displayAddress called with null address");
                    showNoAddress();
                    return;
                }
                
                // Try both field names for backward compatibility
                String street1 = address.getStreet1() != null ? address.getStreet1() : address.getStreet();
                String street2 = address.getStreet2();
                String city = address.getCity();
                String state = address.getState();
                String zipCode = address.getPincode() != null ? address.getPincode() : address.getZipCode();
                
                // Combine street1 and street2 for display
                String fullStreet = street1;
                if (street2 != null && !street2.isEmpty()) {
                    fullStreet = street1 + ", " + street2;
                }
                
                Log.d("CustomerProfileFragment", "Displaying address: street1=" + street1 + 
                    ", street2=" + street2 + ", city=" + city + ", state=" + state + ", zipCode=" + zipCode);
                
                if (street1 != null && !street1.isEmpty() && 
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
                        tvStreetAddress.setText(fullStreet);
                    }
                    if (tvCityStateZip != null) {
                        tvCityStateZip.setText(city + ", " + state + " - " + zipCode);
                    }
                    
                    Log.d("CustomerProfileFragment", "Address UI updated successfully");
                    
                    // Force view refresh
                    if (getView() != null) {
                        getView().invalidate();
                        getView().requestLayout();
                    }
                } else {
                    Log.w("CustomerProfileFragment", "Address data incomplete for display");
                    showNoAddress();
                }
            });
        } else {
            // Fallback if activity is null
            if (address == null) {
                showNoAddress();
                return;
            }
            
            String street1 = address.getStreet1() != null ? address.getStreet1() : address.getStreet();
            String street2 = address.getStreet2();
            String city = address.getCity();
            String state = address.getState();
            String zipCode = address.getPincode() != null ? address.getPincode() : address.getZipCode();
            
            // Combine street1 and street2 for display
            String fullStreet = street1;
            if (street2 != null && !street2.isEmpty()) {
                fullStreet = street1 + ", " + street2;
            }
            
            if (street1 != null && !street1.isEmpty() && 
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
                    tvStreetAddress.setText(fullStreet);
                }
                if (tvCityStateZip != null) {
                    tvCityStateZip.setText(city + ", " + state + " - " + zipCode);
                }
            } else {
                showNoAddress();
            }
        }
    }
    
    private void showNoAddress() {
        // Ensure UI updates happen on main thread
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (addressDisplayLayout != null) {
                    addressDisplayLayout.setVisibility(View.GONE);
                }
                if (noAddressLayout != null) {
                    noAddressLayout.setVisibility(View.VISIBLE);
                }
            });
        } else {
            // Fallback if activity is null
            if (addressDisplayLayout != null) {
                addressDisplayLayout.setVisibility(View.GONE);
            }
            if (noAddressLayout != null) {
                noAddressLayout.setVisibility(View.VISIBLE);
            }
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
                // Simple approach: decode with downsampling to prevent memory issues
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                
                InputStream inputStream1 = getActivity().getContentResolver().openInputStream(imageUri);
                BitmapFactory.decodeStream(inputStream1, null, options);
                inputStream1.close();
                
                // Calculate sample size
                int reqWidth = 800;
                int reqHeight = 800;
                int inSampleSize = 1;
                if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                    int halfHeight = options.outHeight / 2;
                    int halfWidth = options.outWidth / 2;
                    while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                        inSampleSize *= 2;
                    }
                }
                
                // Ensure minimum downsampling for very large images
                if (options.outWidth > 2000 || options.outHeight > 2000) {
                    inSampleSize = Math.max(inSampleSize, 4);
                }
                if (options.outWidth > 4000 || options.outHeight > 4000) {
                    inSampleSize = Math.max(inSampleSize, 8);
                }
                
                // Decode with downsampling
                options.inJustDecodeBounds = false;
                options.inSampleSize = inSampleSize;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                
                InputStream inputStream2 = getActivity().getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream2, null, options);
                inputStream2.close();
                
                // Final safety check - scale down if still too large
                if (bitmap != null) {
                    int maxDim = Math.max(bitmap.getWidth(), bitmap.getHeight());
                    if (maxDim > 1000) {
                        float scale = 1000f / maxDim;
                        int newW = (int) (bitmap.getWidth() * scale);
                        int newH = (int) (bitmap.getHeight() * scale);
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true);
                        if (scaled != bitmap && !bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        bitmap = scaled;
                    }
                }
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
    
    /**
     * Get image orientation from EXIF data
     */
    private int getImageOrientation(Activity activity, Uri imageUri) {
        if (imageUri == null || activity == null) {
            return ExifInterface.ORIENTATION_NORMAL;
        }
        
        try {
            // Try using FileDescriptor first (for API 24+)
            android.os.ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                parcelFileDescriptor = activity.getContentResolver().openFileDescriptor(imageUri, "r");
                if (parcelFileDescriptor != null) {
                    java.io.FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    ExifInterface exif = new ExifInterface(fileDescriptor);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    parcelFileDescriptor.close();
                    return orientation;
                }
            } catch (Exception e) {
                android.util.Log.w("CustomerProfileFragment", "Failed to read EXIF from FileDescriptor, trying InputStream", e);
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (Exception ignored) {}
                }
            }
            
            // Fallback: Try using InputStream
            try (InputStream inputStream = activity.getContentResolver().openInputStream(imageUri)) {
                if (inputStream != null) {
                    ExifInterface exif = new ExifInterface(inputStream);
                    return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("CustomerProfileFragment", "Error reading EXIF orientation", e);
        }
        return ExifInterface.ORIENTATION_NORMAL;
    }
    
    /**
     * Calculate inSampleSize for bitmap decoding to avoid memory issues
     */
    private int calculateInSampleSize(Activity activity, Uri imageUri, int reqWidth, int reqHeight) {
        if (activity == null || imageUri == null) {
            return 4; // Return safe default
        }
        
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            
            InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            int height = options.outHeight;
            int width = options.outWidth;
            int inSampleSize = 1;
            
            // Calculate the maximum dimension
            int maxDimension = Math.max(height, width);
            int maxRequired = Math.max(reqWidth, reqHeight);
            
            // Calculate inSampleSize to ensure image is not too large
            if (maxDimension > maxRequired) {
                // Calculate the ratio and round up
                inSampleSize = (int) Math.ceil((double) maxDimension / maxRequired);
                
                // Round up to nearest power of 2 for better performance
                inSampleSize = (int) Math.pow(2, Math.ceil(Math.log(inSampleSize) / Math.log(2)));
            }
            
            // Ensure minimum sample size to prevent huge bitmaps
            // For very large images, use at least 4x downsampling
            if (maxDimension > 2000) {
                inSampleSize = Math.max(inSampleSize, 4);
            }
            if (maxDimension > 4000) {
                inSampleSize = Math.max(inSampleSize, 8);
            }
            
            android.util.Log.d("CustomerProfileFragment", "Image dimensions: " + width + "x" + height + ", inSampleSize: " + inSampleSize);
            
            return inSampleSize;
        } catch (Exception e) {
            android.util.Log.e("CustomerProfileFragment", "Error calculating inSampleSize", e);
            return 4; // Return safe default instead of 1
        }
    }
    
    /**
     * Scale down bitmap to prevent memory issues
     */
    private Bitmap scaleDownBitmap(Bitmap bitmap, int maxSize) {
        if (bitmap == null) return null;
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int maxDim = Math.max(width, height);
        
        // Always scale if larger than maxSize, or if bitmap is extremely large (>2000px)
        if (maxDim <= maxSize && maxDim <= 2000) {
            return bitmap; // Already small enough
        }
        
        // Use the smaller of maxSize or 2000 to prevent huge bitmaps
        int targetSize = Math.min(maxSize, 2000);
        if (maxDim > targetSize) {
            float scale = (float) targetSize / maxDim;
            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);
            
            try {
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                if (scaled != bitmap && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                return scaled;
            } catch (Exception e) {
                android.util.Log.e("CustomerProfileFragment", "Error scaling bitmap", e);
                return null; // Return null on error to prevent crash
            }
        }
        
        return bitmap;
    }
    
    /**
     * Rotate bitmap based on EXIF orientation
     */
    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        if (bitmap == null) {
            return null;
        }
        
        if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED) {
            return bitmap; // No rotation needed
        }
        
        Matrix matrix = new Matrix();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1, width / 2f, height / 2f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1, width / 2f, height / 2f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(270);
                matrix.postScale(-1, 1);
                break;
            default:
                return bitmap; // No rotation needed
        }
        
        try {
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            if (rotatedBitmap != bitmap && !bitmap.isRecycled()) {
                bitmap.recycle(); // Recycle original if we created a new one
            }
            return rotatedBitmap;
        } catch (Exception e) {
            android.util.Log.e("CustomerProfileFragment", "Error creating rotated bitmap", e);
            return bitmap; // Return original on error
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
                // Immediately update UI with the saved address (optimistic update)
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Create address object from saved data
                        Address savedAddress = new Address();
                        savedAddress.setStreet(street);
                        savedAddress.setCity(city);
                        savedAddress.setState(state);
                        savedAddress.setZipCode(zipCode);
                        
                        // Update cached address
                        customerAddress = savedAddress;
                        
                        // Immediately display the address in UI
                        displayAddress(savedAddress);
                    });
                }
                
                // Also reload profile from backend to ensure consistency
                // Use a small delay to allow backend to fully process the save
                if (getView() != null) {
                    getView().postDelayed(() -> {
                        loadCustomerProfile();
                    }, 500); // 500ms delay to ensure backend has processed
                }
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
