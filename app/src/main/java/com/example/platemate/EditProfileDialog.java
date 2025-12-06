package com.example.platemate;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
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
                        ivProfilePreview.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    android.util.Log.e("EditProfileDialog", "Error loading base64 image", e);
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
                // Simple approach: decode with downsampling to prevent memory issues
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                
                InputStream inputStream1 = activity.getContentResolver().openInputStream(imageUri);
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
                
                InputStream inputStream2 = activity.getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream2, null, options);
                inputStream2.close();
                
                // Final safety check - ALWAYS scale down to prevent memory issues
                if (bitmap != null) {
                    bitmap = scaleDownBitmap(bitmap, 800); // Max 800px
                }
            } catch (Exception e) {
                android.util.Log.e("EditProfileDialog", "Failed to load image from gallery", e);
                ToastUtils.showError(activity, "Failed to load image: " + e.getMessage());
                return;
            }
        } else if (requestCode == REQUEST_CODE_CAMERA && data != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            android.util.Log.d("EditProfileDialog", "Bitmap loaded from camera, size: " + (bitmap != null ? bitmap.getWidth() + "x" + bitmap.getHeight() : "null"));
            // Camera images from thumbnail are usually already rotated correctly
        }
        
        if (bitmap != null) {
            // ALWAYS scale down before storing or displaying to prevent crashes
            Bitmap scaledBitmap = scaleDownBitmap(bitmap, 800);
            if (scaledBitmap == null) {
                android.util.Log.e("EditProfileDialog", "Failed to scale bitmap, using original");
                scaledBitmap = bitmap;
            } else if (scaledBitmap != bitmap && !bitmap.isRecycled()) {
                bitmap.recycle(); // Recycle original if we created a scaled version
            }
            
            instance.selectedImageBitmap = scaledBitmap;
            android.util.Log.d("EditProfileDialog", "Image selected and stored in instance. Preview view: " + (instance.currentProfilePreview != null ? "exists" : "null"));
            // Update preview if dialog is still open
            if (instance.currentProfilePreview != null) {
                instance.currentProfilePreview.setImageBitmap(scaledBitmap);
                android.util.Log.d("EditProfileDialog", "Preview updated successfully");
            } else {
                android.util.Log.w("EditProfileDialog", "Preview ImageView is null, cannot update");
            }
        } else {
            android.util.Log.w("EditProfileDialog", "Bitmap is null after processing");
        }
    }
    
    /**
     * Get image orientation from EXIF data
     */
    private int getImageOrientation(Activity activity, Uri imageUri) {
        if (imageUri == null) {
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
                android.util.Log.w("EditProfileDialog", "Failed to read EXIF from FileDescriptor, trying InputStream", e);
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
            android.util.Log.e("EditProfileDialog", "Error reading EXIF orientation", e);
        }
        return ExifInterface.ORIENTATION_NORMAL;
    }
    
    /**
     * Calculate inSampleSize for bitmap decoding to avoid memory issues
     */
    private int calculateInSampleSize(Activity activity, Uri imageUri, int reqWidth, int reqHeight) {
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
            
            android.util.Log.d("EditProfileDialog", "Image dimensions: " + width + "x" + height + ", inSampleSize: " + inSampleSize);
            
            return inSampleSize;
        } catch (Exception e) {
            android.util.Log.e("EditProfileDialog", "Error calculating inSampleSize", e);
            return 4; // Return safe default instead of 1
        }
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
            android.util.Log.e("EditProfileDialog", "Error creating rotated bitmap", e);
            return bitmap; // Return original on error
        }
    }
    
    /**
     * Scale down bitmap to prevent memory issues - ALWAYS scales to ensure safe size
     */
    private static Bitmap scaleDownBitmap(Bitmap bitmap, int maxSize) {
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
                android.util.Log.d("EditProfileDialog", "Scaled bitmap from " + width + "x" + height + " to " + newWidth + "x" + newHeight);
                return scaled;
            } catch (Exception e) {
                android.util.Log.e("EditProfileDialog", "Error scaling bitmap", e);
                // If scaling fails, try to create a smaller bitmap
                try {
                    return Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true);
                } catch (Exception e2) {
                    android.util.Log.e("EditProfileDialog", "Failed to create scaled bitmap", e2);
                    return null; // Return null on error to prevent crash
                }
            }
        }
        
        return bitmap;
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
                        
                        // Decode with downsampling
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                        
                        // Calculate sample size
                        int reqSize = 800;
                        int inSampleSize = 1;
                        int maxDim = Math.max(options.outWidth, options.outHeight);
                        if (maxDim > reqSize) {
                            inSampleSize = (int) Math.ceil((double) maxDim / reqSize);
                            // Round to power of 2
                            inSampleSize = (int) Math.pow(2, Math.ceil(Math.log(inSampleSize) / Math.log(2)));
                        }
                        if (maxDim > 2000) inSampleSize = Math.max(inSampleSize, 4);
                        if (maxDim > 4000) inSampleSize = Math.max(inSampleSize, 8);
                        
                        // Decode with sample size
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = inSampleSize;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                        if (bitmap != null) {
                            // Scale down further if needed
                            bitmap = scaleDownBitmap(bitmap, 800);
                            imageView.setImageBitmap(bitmap);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("EditProfileDialog", "Error loading profile image", e);
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
            
            // Scale down bitmap before uploading to prevent memory issues
            Bitmap uploadBitmap = scaleDownBitmap(bitmap, 1024); // Max 1024px for upload
            
            // Convert bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean compressed = uploadBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
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

