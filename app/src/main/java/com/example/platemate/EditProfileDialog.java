package com.example.platemate;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.fragment.app.Fragment;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileDialog {
    
    public interface EditProfileDialogListener {
        void onProfileSaved();
        void onCancel();
    }
    
    // Use instance variables instead of static to avoid conflicts
    private Bitmap selectedImageBitmap = null;
    private ImageView currentProfilePreview = null;
    private Dialog currentDialog = null;
    private Activity currentActivity = null;
    private Fragment currentSupportFragment = null;
    private Button btnSaveProfile = null;
    private ProgressBar progressBar = null;
    private EditProfileDialogListener currentListener = null;
    private Customer currentCustomer = null;
    public static final int REQUEST_CODE_PICK_IMAGE = 1001;
    public static final int REQUEST_CODE_CAMERA = 1002;
    
    // Store instance for handling activity result - use WeakReference to avoid memory leaks
    private static EditProfileDialog currentInstance = null;
    private static final Object instanceLock = new Object();
    
    public static void show(Context context, Customer customer, EditProfileDialogListener listener) {
        // Clear any existing instance first
        synchronized (instanceLock) {
            currentInstance = null;
        }
        
        EditProfileDialog dialogInstance = new EditProfileDialog();
        synchronized (instanceLock) {
            currentInstance = dialogInstance;
            android.util.Log.d("EditProfileDialog", "Instance created and stored: " + dialogInstance);
        }
        dialogInstance.showDialog(context, customer, listener);
    }
    
    // Overload to accept Fragment
    public static void show(Fragment fragment, Customer customer, EditProfileDialogListener listener) {
        // Clear any existing instance first
        synchronized (instanceLock) {
            currentInstance = null;
        }
        
        EditProfileDialog dialogInstance = new EditProfileDialog();
        dialogInstance.currentSupportFragment = fragment;
        synchronized (instanceLock) {
            currentInstance = dialogInstance;
            android.util.Log.d("EditProfileDialog", "Instance created with Fragment: " + dialogInstance);
        }
        Context context = fragment.getContext();
        if (context == null) {
            android.util.Log.e("EditProfileDialog", "Fragment context is null!");
            return;
        }
        dialogInstance.showDialog(context, customer, listener);
    }
    
    private void showDialog(Context context, Customer customer, EditProfileDialogListener listener) {
        // Create custom dialog
        currentDialog = new Dialog(context);
        currentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currentDialog.setContentView(R.layout.dialog_edit_profile);
        currentDialog.setCancelable(true);
        
        // Set activity if context is Activity, or if we have a Fragment, get activity from it
        if (currentSupportFragment != null && currentSupportFragment.getActivity() != null) {
            currentActivity = currentSupportFragment.getActivity();
            android.util.Log.d("EditProfileDialog", "Using activity from Fragment: " + currentActivity);
        } else if (context instanceof Activity) {
            currentActivity = (Activity) context;
            android.util.Log.d("EditProfileDialog", "Using activity from context: " + currentActivity);
        }
        
        // Get views
        EditText etFullName = currentDialog.findViewById(R.id.etFullName);
        EditText etDateOfBirth = currentDialog.findViewById(R.id.etDateOfBirth);
        ImageView ivProfilePreview = currentDialog.findViewById(R.id.ivProfilePreview);
        Button btnChangeProfileImage = currentDialog.findViewById(R.id.btnChangeProfileImage);
        Button btnCancel = currentDialog.findViewById(R.id.btnCancel);
        btnSaveProfile = currentDialog.findViewById(R.id.btnSaveProfile);
        progressBar = currentDialog.findViewById(R.id.progressBar);
        
        // Store references for later use
        currentCustomer = customer;
        currentListener = listener;
        
        // Reset selected image
        selectedImageBitmap = null;
        currentProfilePreview = ivProfilePreview;
        
        android.util.Log.d("EditProfileDialog", "Dialog shown, currentInstance set: " + (currentInstance != null));
        
        // Pre-fill fields if editing existing profile
        if (customer != null) {
            if (customer.getFullName() != null && !customer.getFullName().isEmpty()) {
                etFullName.setText(customer.getFullName());
            }
            
            // Populate Date of Birth if available
            String dob = customer.getDateOfBirth();
            if (dob != null && !dob.isEmpty()) {
                etDateOfBirth.setText(dob);
            }
            
            // Load existing profile image
            if (customer.getProfileImageId() != null) {
                loadProfileImage(context, customer.getProfileImageId(), ivProfilePreview);
            } else if (customer.getProfileImageBase64() != null && !customer.getProfileImageBase64().isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(customer.getProfileImageBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        ivProfilePreview.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    // Keep default image
                }
            }
        }
        
        // If customer DOB is not available, try to fetch it from backend
        if (customer != null && (customer.getDateOfBirth() == null || customer.getDateOfBirth().isEmpty())) {
            fetchCustomerDOBForDialog(context, customer.getId(), etDateOfBirth);
        }
        
        // Date picker with validation (no future dates, no today)
        Calendar calendar = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_YEAR, -1); // Yesterday is the maximum date
        
        etDateOfBirth.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);
                    selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                    selectedDate.set(Calendar.MINUTE, 0);
                    selectedDate.set(Calendar.SECOND, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);
                    
                    // Validate: cannot be today or future
                    if (selectedDate.after(today) || selectedDate.equals(today)) {
                        ToastUtils.showError(context, "Date of birth cannot be today or a future date");
                        return;
                    }
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    etDateOfBirth.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            // Set maximum date to yesterday
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });
        
        // Change profile image
        btnChangeProfileImage.setOnClickListener(v -> {
            android.util.Log.d("EditProfileDialog", "Change image button clicked. currentActivity: " + (currentActivity != null ? "exists" : "null"));
            android.util.Log.d("EditProfileDialog", "Current instance: " + (this) + ", static currentInstance: " + (currentInstance != null ? currentInstance : "null"));
            
            // Verify instance is stored before starting activity
            synchronized (instanceLock) {
                if (currentInstance != this) {
                    android.util.Log.w("EditProfileDialog", "Instance mismatch! Setting currentInstance to this instance.");
                    currentInstance = this;
                }
            }
            
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Select Profile Picture")
                .setItems(new String[]{"Camera", "Gallery"}, (d, which) -> {
                    if (currentActivity == null) {
                        android.util.Log.e("EditProfileDialog", "currentActivity is null!");
                        ToastUtils.showError(context, "Activity not available");
                        return;
                    }
                    
                    // Ensure instance is set before starting activity
                    synchronized (instanceLock) {
                        currentInstance = EditProfileDialog.this;
                        android.util.Log.d("EditProfileDialog", "Before starting activity, currentInstance set to: " + currentInstance);
                    }
                    
                    Intent intent = null;
                    int requestCode = 0;
                    
                    if (which == 0) {
                        // Camera
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        requestCode = REQUEST_CODE_CAMERA;
                    } else {
                        // Gallery
                        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        requestCode = REQUEST_CODE_PICK_IMAGE;
                    }
                    
                    // Use Fragment's startActivityForResult if available, otherwise use Activity's
                    if (currentSupportFragment != null) {
                        android.util.Log.d("EditProfileDialog", "Using Fragment.startActivityForResult with request code: " + requestCode);
                        currentSupportFragment.startActivityForResult(intent, requestCode);
                    } else if (currentActivity != null) {
                        android.util.Log.d("EditProfileDialog", "Using Activity.startActivityForResult with request code: " + requestCode);
                        if (which == 0 && intent.resolveActivity(currentActivity.getPackageManager()) != null) {
                            currentActivity.startActivityForResult(intent, requestCode);
                        } else if (which == 1) {
                            currentActivity.startActivityForResult(intent, requestCode);
                        }
                    } else {
                        android.util.Log.e("EditProfileDialog", "Neither Fragment nor Activity available!");
                        ToastUtils.showError(context, "Cannot open image picker");
                    }
                })
                .show();
        });
        
        // Cancel button
        btnCancel.setOnClickListener(v -> {
            synchronized (instanceLock) {
                currentInstance = null; // Clear instance on cancel
            }
            currentDialog.dismiss();
            if (listener != null) {
                listener.onCancel();
            }
        });
        
        // Save button
        btnSaveProfile.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String dateOfBirthStr = etDateOfBirth.getText().toString().trim();
            
            // Validation
            if (fullName.isEmpty()) {
                etFullName.setError("Full name is required");
                etFullName.requestFocus();
                return;
            }
            
            Date dateOfBirth = null;
            if (!dateOfBirthStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    dateOfBirth = sdf.parse(dateOfBirthStr);
                } catch (Exception e) {
                    ToastUtils.showError(context, "Invalid date format. Please use YYYY-MM-DD");
                    return;
                }
            }
            
            // Get the bitmap from this instance
            Bitmap imageToUpload = this.selectedImageBitmap;
            
            // Save to backend - pass the selected image bitmap
            android.util.Log.d("EditProfileDialog", "Save clicked - selectedImageBitmap is " + (imageToUpload != null ? "not null (" + imageToUpload.getWidth() + "x" + imageToUpload.getHeight() + ")" : "null"));
            android.util.Log.d("EditProfileDialog", "Current instance: " + (this) + ", currentInstance static: " + (currentInstance != null ? currentInstance : "null"));
            android.util.Log.d("EditProfileDialog", "Instance selectedImageBitmap: " + (this.selectedImageBitmap != null ? "not null" : "null"));
            
            // If no new image was selected, imageToUpload will be null, which is fine
            // The backend will keep the existing image
            saveProfileToBackend(context, customer, fullName, dateOfBirth, imageToUpload, 
                currentDialog, progressBar, btnSaveProfile, listener);
        });
        
        // Show dialog
        currentDialog.show();
        
        // Make dialog full width
        Window window = currentDialog.getWindow();
        if (window != null) {
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                           android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    
    // Check if dialog is currently open
    public static boolean isDialogOpen() {
        synchronized (instanceLock) {
            return currentInstance != null && currentInstance.currentDialog != null && currentInstance.currentDialog.isShowing();
        }
    }
    
    // Helper method to handle image picker result
    public static void handleImagePickerResult(Activity activity, int requestCode, int resultCode, Intent data) {
        android.util.Log.d("EditProfileDialog", "handleImagePickerResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);
        
        EditProfileDialog instance;
        synchronized (instanceLock) {
            instance = currentInstance;
        }
        
        if (instance == null) {
            android.util.Log.e("EditProfileDialog", "handleImagePickerResult called but currentInstance is null!");
            ToastUtils.showError(activity, "Dialog instance not found. Please try again.");
            return;
        }
        
        android.util.Log.d("EditProfileDialog", "Found instance, processing image...");
        
        if (resultCode != Activity.RESULT_OK) {
            android.util.Log.w("EditProfileDialog", "Activity result not OK: " + resultCode);
            return;
        }
        
        Bitmap bitmap = null;
        
        if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            android.util.Log.d("EditProfileDialog", "Loading image from gallery - URI: " + imageUri);
            try {
                InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                android.util.Log.d("EditProfileDialog", "Bitmap loaded from gallery, size: " + (bitmap != null ? bitmap.getWidth() + "x" + bitmap.getHeight() : "null"));
            } catch (Exception e) {
                android.util.Log.e("EditProfileDialog", "Failed to load image from gallery", e);
                ToastUtils.showError(activity, "Failed to load image: " + e.getMessage());
                return;
            }
        } else if (requestCode == REQUEST_CODE_CAMERA && data != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            android.util.Log.d("EditProfileDialog", "Bitmap loaded from camera, size: " + (bitmap != null ? bitmap.getWidth() + "x" + bitmap.getHeight() : "null"));
        }
        
        if (bitmap != null) {
            instance.selectedImageBitmap = bitmap;
            android.util.Log.d("EditProfileDialog", "Image selected and stored in instance. Preview view: " + (instance.currentProfilePreview != null ? "exists" : "null"));
            // Update preview if dialog is still open
            if (instance.currentProfilePreview != null) {
                instance.currentProfilePreview.setImageBitmap(bitmap);
                android.util.Log.d("EditProfileDialog", "Preview updated successfully");
            } else {
                android.util.Log.w("EditProfileDialog", "Preview ImageView is null, cannot update");
            }
        } else {
            android.util.Log.w("EditProfileDialog", "Bitmap is null after processing");
        }
    }
    
    private void loadProfileImage(Context context, Long imageId, ImageView imageView) {
        if (context == null || imageId == null || imageView == null) return;
        
        ApiInterface apiInterface = RetrofitClient.getInstance(context).getApi();
        Call<okhttp3.ResponseBody> call = apiInterface.getImage(imageId);
        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null && imageView != null) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    } catch (Exception e) {
                        // Keep default image
                    }
                }
            }
            
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                // Keep default image
            }
        });
    }
    
    private void fetchCustomerDOBForDialog(Context context, Long userId, EditText etDateOfBirth) {
        if (context == null || userId == null || etDateOfBirth == null) return;
        
        ApiInterface apiInterface = RetrofitClient.getInstance(context).getApi();
        Call<CustomerUpdateResponse> call = apiInterface.getCustomerByUserId(userId);
        call.enqueue(new Callback<CustomerUpdateResponse>() {
            @Override
            public void onResponse(Call<CustomerUpdateResponse> call, Response<CustomerUpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String dob = response.body().getDateOfBirth();
                    if (dob != null && !dob.isEmpty() && etDateOfBirth != null) {
                        etDateOfBirth.setText(dob);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<CustomerUpdateResponse> call, Throwable t) {
                // Silently fail - DOB is optional
            }
        });
    }
    
    private void fetchCustomerIdAndUpdate(Context context, Long userId, String fullName,
            Date dateOfBirth, Bitmap profileImage, Dialog dialog, ProgressBar progressBar,
            Button btnSaveProfile, EditProfileDialogListener listener) {
        ApiInterface apiInterface = RetrofitClient.getInstance(context).getApi();
        // Use the new endpoint to get customer by userId
        Call<CustomerUpdateResponse> call = apiInterface.getCustomerByUserId(userId);
        call.enqueue(new Callback<CustomerUpdateResponse>() {
            @Override
            public void onResponse(Call<CustomerUpdateResponse> call, 
                    Response<CustomerUpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerUpdateResponse customerResponse = response.body();
                    Long customerId = customerResponse.getId();
                    
                    android.util.Log.d("EditProfileDialog", "Fetched customerId: " + customerId + " for userId: " + userId);
                    
                    if (customerId != null) {
                        // Now update with customerId
                        updateCustomerProfile(context, customerId, fullName, dateOfBirth, 
                            profileImage, dialog, progressBar, btnSaveProfile, listener);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnSaveProfile.setEnabled(true);
                        ToastUtils.showError(context, "Customer profile not found. Customer ID is null.");
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnSaveProfile.setEnabled(true);
                    String errorMsg = "Failed to fetch customer profile. Please ensure your profile is complete.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " Error: " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg += " Status: " + response.code();
                        }
                    }
                    android.util.Log.e("EditProfileDialog", errorMsg);
                    ToastUtils.showError(context, errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<CustomerUpdateResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                ToastUtils.showError(context, "Error: " + t.getMessage());
            }
        });
    }
    
    private void updateCustomerProfile(Context context, Long customerId, String fullName,
            Date dateOfBirth, Bitmap profileImage, Dialog dialog, ProgressBar progressBar,
            Button btnSaveProfile, EditProfileDialogListener listener) {
        ApiInterface apiInterface = RetrofitClient.getInstance(context).getApi();
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(fullName, dateOfBirth);
        
        Call<CustomerUpdateResponse> updateCall = apiInterface.updateCustomer(customerId, updateRequest);
        updateCall.enqueue(new Callback<CustomerUpdateResponse>() {
            @Override
            public void onResponse(Call<CustomerUpdateResponse> call, Response<CustomerUpdateResponse> response) {
                if (response.isSuccessful()) {
                    // If profile image was selected, upload it using customerId (same pattern as provider)
                    if (profileImage != null && customerId != null) {
                        uploadProfileImage(context, customerId, profileImage, dialog, progressBar, 
                            btnSaveProfile, listener);
                    } else {
                        // No image to upload, just close dialog
                        progressBar.setVisibility(View.GONE);
                        btnSaveProfile.setEnabled(true);
                        synchronized (instanceLock) {
                            currentInstance = null; // Clear instance
                        }
                        if (dialog != null) dialog.dismiss();
                        if (listener != null) {
                            listener.onProfileSaved();
                        }
                        ToastUtils.showSuccess(context, "Profile updated successfully!");
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnSaveProfile.setEnabled(true);
                    ToastUtils.showError(context, "Failed to update profile");
                }
            }
            
            @Override
            public void onFailure(Call<CustomerUpdateResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                ToastUtils.showError(context, "Error: " + t.getMessage());
            }
        });
    }
    
    private void saveProfileToBackend(Context context, Customer customer, String fullName, 
            Date dateOfBirth, Bitmap profileImage, Dialog dialog, ProgressBar progressBar,
            Button btnSaveProfile, EditProfileDialogListener listener) {
        
        ApiInterface apiInterface = RetrofitClient.getInstance(context).getApi();
        SessionManager sessionManager = new SessionManager(context);
        Long userId = sessionManager.getUserId();
        
        android.util.Log.d("EditProfileDialog", "saveProfileToBackend - userId from session: " + userId);
        
        if (userId == null) {
            android.util.Log.e("EditProfileDialog", "userId is null! Cannot proceed with profile update.");
            ToastUtils.showError(context, "User not logged in. Please log in again.");
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (btnSaveProfile != null) {
                btnSaveProfile.setEnabled(true);
            }
            return;
        }
        
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (btnSaveProfile != null) {
            btnSaveProfile.setEnabled(false);
        }
        
        // We need customerId, not userId. The customer object's id is actually userId.
        // We need to fetch the actual customerId from backend using userId
        fetchCustomerIdAndUpdate(context, userId, fullName, dateOfBirth, profileImage, 
            dialog, progressBar, btnSaveProfile, listener);
    }
    
    private void uploadProfileImage(Context context, Long customerId, Bitmap bitmap, 
            Dialog dialog, ProgressBar progressBar, Button btnSaveProfile, 
            EditProfileDialogListener listener) {
        try {
            // Validate inputs
            if (bitmap == null) {
                android.util.Log.e("EditProfileDialog", "Bitmap is null, cannot upload");
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
                ToastUtils.showError(context, "No image selected");
                return;
            }
            
            if (customerId == null) {
                android.util.Log.e("EditProfileDialog", "customerId is null, cannot upload");
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
                ToastUtils.showError(context, "Customer ID not found");
                return;
            }
            
            // Convert bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            if (!compressed) {
                android.util.Log.e("EditProfileDialog", "Failed to compress bitmap");
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
                ToastUtils.showError(context, "Failed to process image: compression failed");
                return;
            }
            
            byte[] imageBytes = baos.toByteArray();
            if (imageBytes == null || imageBytes.length == 0) {
                android.util.Log.e("EditProfileDialog", "Compressed image bytes are empty");
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
                ToastUtils.showError(context, "Failed to process image: empty image data");
                return;
            }
            
            android.util.Log.d("EditProfileDialog", "Image compressed successfully. Size: " + imageBytes.length + " bytes");
            
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
            
            ApiInterface apiInterface = RetrofitClient.getInstance(context).getApi();
            // Use customerId as ownerId (same pattern as provider uses providerId)
            String imageType = "CUSTOMER_PROFILE";
            
            // Log for debugging
            android.util.Log.d("EditProfileDialog", "Uploading image with customerId: " + customerId + ", imageType: " + imageType);
            
            Call<Image> call = apiInterface.uploadImage(imageType, customerId, imagePart);
            call.enqueue(new Callback<Image>() {
                @Override
                public void onResponse(Call<Image> call, Response<Image> response) {
                    progressBar.setVisibility(View.GONE);
                    btnSaveProfile.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Image uploadedImage = response.body();
                        android.util.Log.d("EditProfileDialog", "Image uploaded successfully. Image ID: " + uploadedImage.getId());
                        synchronized (instanceLock) {
                            currentInstance = null; // Clear instance on success
                        }
                        if (dialog != null) dialog.dismiss();
                        if (listener != null) {
                            listener.onProfileSaved();
                        }
                        ToastUtils.showSuccess(context, "Profile updated successfully!");
                    } else {
                        String errorMsg = "Profile updated but image upload failed";
                        if (response.errorBody() != null) {
                            try {
                                errorMsg += ": " + response.errorBody().string();
                            } catch (Exception e) {
                                errorMsg += ". Status: " + response.code();
                            }
                        }
                        android.util.Log.e("EditProfileDialog", errorMsg);
                        ToastUtils.showError(context, errorMsg);
                        synchronized (instanceLock) {
                            currentInstance = null; // Clear instance
                        }
                        if (dialog != null) dialog.dismiss();
                        if (listener != null) {
                            listener.onProfileSaved();
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<Image> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnSaveProfile.setEnabled(true);
                    String errorMsg = "Profile updated but image upload failed: " + t.getMessage();
                    android.util.Log.e("EditProfileDialog", errorMsg, t);
                    ToastUtils.showError(context, errorMsg);
                    synchronized (instanceLock) {
                        currentInstance = null; // Clear instance
                    }
                    if (dialog != null) dialog.dismiss();
                    if (listener != null) {
                        listener.onProfileSaved();
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e("EditProfileDialog", "Exception in uploadProfileImage", e);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
            String errorMsg = "Failed to process image";
            if (e.getMessage() != null) {
                errorMsg += ": " + e.getMessage();
            }
            ToastUtils.showError(context, errorMsg);
        }
    }
}

