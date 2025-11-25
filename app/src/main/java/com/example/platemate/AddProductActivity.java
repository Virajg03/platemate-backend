package com.example.platemate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {
    private EditText etProductName, etDescription, etPrice, etIngredients;
    private AutoCompleteTextView etCategory;
    private Spinner spMealType;
    private Button btnSaveProduct, btnSelectImage;
    private ImageView ivProductImage;
    private ApiInterface apiInterface;
    
    private List<Category> categoriesList = new ArrayList<>();
    private Map<String, Long> categoryNameToIdMap = new HashMap<>();
    private String[] mealTypes = {"VEG", "NON_VEG", "JAIN"};
    private Uri selectedImageUri;
    private File selectedImageFile;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Product");
        }
        
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        initializeViews();
        setupMealTypeSpinner();
        setupImagePicker();
        loadCategories();
        
        btnSaveProduct.setOnClickListener(v -> saveProduct());
        btnSelectImage.setOnClickListener(v -> requestImagePermissionAndPick());
    }

    private void initializeViews() {
        etProductName = findViewById(R.id.etProductName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etIngredients = findViewById(R.id.etIngredients);
        etCategory = findViewById(R.id.etCategory);
        spMealType = findViewById(R.id.spMealType);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivProductImage = findViewById(R.id.ivProductImage);
        
        // Configure AutoCompleteTextView for dropdown
        etCategory.setFocusable(false);
        etCategory.setClickable(true);
        
        // Add click listener to show dropdown
        etCategory.setOnClickListener(v -> {
            if (etCategory.getAdapter() != null && etCategory.getAdapter().getCount() > 0) {
                etCategory.showDropDown();
            } else {
                // Reload categories if adapter is empty
                loadCategories();
            }
        });
        
        // Handle focus to show dropdown
        etCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && etCategory.getAdapter() != null && etCategory.getAdapter().getCount() > 0) {
                etCategory.showDropDown();
            }
        });
    }

    private void setupMealTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, mealTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMealType.setAdapter(adapter);
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageUri = uri;
                        selectedImageFile = getImageFileFromUri(uri);
                        if (selectedImageFile != null) {
                            ivProductImage.setImageURI(uri);
                            ivProductImage.setVisibility(android.view.View.VISIBLE);
                        }
                    }
                }
            }
        );
        
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void requestImagePermissionAndPick() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            permissionLauncher.launch(permission);
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private File getImageFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            
            // Create a temporary file
            File tempFile = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadCategories() {
        Call<List<Category>> call = apiInterface.getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    if (categories != null && !categories.isEmpty()) {
                        categoriesList = categories;
                        categoryNameToIdMap.clear();
                        
                        List<String> categoryNames = new ArrayList<>();
                        for (Category category : categories) {
                            // Filter out null/empty category names
                            String categoryName = category.getCategoryName();
                            Long categoryId = category.getId();
                            
                            if (categoryName != null && !categoryName.trim().isEmpty() && categoryId != null && categoryId > 0) {
                                String trimmedName = categoryName.trim();
                                categoryNames.add(trimmedName);
                                categoryNameToIdMap.put(trimmedName, categoryId);
                            }
                        }
                        
                        if (!categoryNames.isEmpty()) {
                            // Update adapter on UI thread
                            runOnUiThread(() -> {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    AddProductActivity.this, 
                                    android.R.layout.simple_dropdown_item_1line, 
                                    categoryNames);
                                etCategory.setAdapter(adapter);
                                etCategory.setEnabled(true);
                            });
                        } else {
                            // No valid categories found
                            runOnUiThread(() -> {
                                ToastUtils.showWarning(AddProductActivity.this, 
                                    "No valid categories found. Please contact admin.");
                            });
                        }
                    } else {
                        // Empty response
                        runOnUiThread(() -> {
                            ToastUtils.showWarning(AddProductActivity.this, 
                                "No categories found. Please contact admin to add categories.");
                        });
                    }
                } else {
                    // API error
                    String errorMessage = "Failed to load categories";
                    if (response != null) {
                        if (response.code() == 401) {
                            errorMessage = "Authentication required. Please login again.";
                        } else if (response.code() == 403) {
                            errorMessage = "Access denied. Please check your permissions.";
                        } else if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                if (errorBody != null && !errorBody.isEmpty()) {
                                    errorMessage = errorBody;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    final String finalErrorMessage = errorMessage;
                    runOnUiThread(() -> {
                        ToastUtils.showError(AddProductActivity.this, finalErrorMessage);
                    });
                }
            }
            
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                runOnUiThread(() -> {
                    String errorMessage = "Network error. Please check your connection.";
                    if (t.getMessage() != null && !t.getMessage().isEmpty()) {
                        errorMessage = "Error: " + t.getMessage();
                    }
                    ToastUtils.showError(AddProductActivity.this, errorMessage);
                });
            }
        });
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String ingredients = etIngredients.getText().toString().trim();
        String categoryName = etCategory.getText().toString().trim();
        String mealType = (String) spMealType.getSelectedItem();

        // Validate required fields
        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || 
            categoryName.isEmpty() || ingredients.isEmpty() || mealType == null) {
            ToastUtils.showInfo(this, "Please fill all required fields");
            return;
        }

        // Get categoryId from category name
        Long categoryId = categoryNameToIdMap.get(categoryName);
        if (categoryId == null) {
            ToastUtils.showInfo(this, "Please select a valid category");
            return;
        }

        try {
            Double price = Double.parseDouble(priceStr);

            // Create request object
            MenuItemCreateRequest request = new MenuItemCreateRequest();
            request.setCategoryId(categoryId);
            request.setItemName(name);
            request.setDescription(description);
            request.setPrice(price);
            request.setIngredients(ingredients);
            request.setMealType(mealType);
            request.setIsAvailable(true);

            // Convert to JSON
            Gson gson = new Gson();
            String jsonData = gson.toJson(request);
            RequestBody dataPart = RequestBody.create(
                MediaType.parse("application/json"), 
                jsonData
            );

            // Handle image upload
            MultipartBody.Part imagePart = null;
            if (selectedImageFile != null && selectedImageFile.exists()) {
                RequestBody imageRequestBody = RequestBody.create(
                    MediaType.parse("image/*"), 
                    selectedImageFile
                );
                imagePart = MultipartBody.Part.createFormData(
                    "image", 
                    selectedImageFile.getName(), 
                    imageRequestBody
                );
            }

            // Make API call
            Call<MenuItemResponse> call = apiInterface.createProviderMenuItem(dataPart, imagePart);
            call.enqueue(new Callback<MenuItemResponse>() {
                @Override
                public void onResponse(Call<MenuItemResponse> call, Response<MenuItemResponse> response) {
                    if (response.isSuccessful()) {
                        ToastUtils.showSuccess(AddProductActivity.this, 
                            "Product added successfully!");
                        finish();
                    } else {
                        String errorMessage = "Failed to add product";
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Gson gson = new Gson();
                                ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);
                                
                                if (errorResponse != null) {
                                    if (errorResponse.getFieldErrors() != null && !errorResponse.getFieldErrors().isEmpty()) {
                                        // Build error message from field errors
                                        StringBuilder sb = new StringBuilder();
                                        for (Map.Entry<String, String> entry : errorResponse.getFieldErrors().entrySet()) {
                                            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                                        }
                                        errorMessage = sb.toString().trim();
                                    } else if (errorResponse.getMessage() != null) {
                                        errorMessage = errorResponse.getMessage();
                                    } else if (errorResponse.getError() != null) {
                                        errorMessage = errorResponse.getError();
                                    }
                                } else {
                                    errorMessage = errorBody;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                // Fallback to status code based messages
                                if (response.code() == 403) {
                                    errorMessage = "Provider not verified yet. Please wait for admin approval.";
                                } else if (response.code() == 400) {
                                    errorMessage = "Invalid data. Please check all fields.";
                                } else if (response.code() == 404) {
                                    errorMessage = "Resource not found.";
                                } else if (response.code() == 500) {
                                    errorMessage = "Server error. Please try again later.";
                                }
                            }
                        } else {
                            // Fallback to status code based messages
                            if (response.code() == 403) {
                                errorMessage = "Provider not verified yet. Please wait for admin approval.";
                            } else if (response.code() == 400) {
                                errorMessage = "Invalid data. Please check all fields.";
                            } else if (response.code() == 404) {
                                errorMessage = "Resource not found.";
                            } else if (response.code() == 500) {
                                errorMessage = "Server error. Please try again later.";
                            }
                        }
                        ToastUtils.showError(AddProductActivity.this, errorMessage);
                    }
                }

                @Override
                public void onFailure(Call<MenuItemResponse> call, Throwable t) {
                    ToastUtils.showError(AddProductActivity.this, 
                        "Error: " + (t.getMessage() != null ? t.getMessage() : "Network error"));
                }
            });
        } catch (NumberFormatException e) {
            ToastUtils.showError(this, "Invalid price format");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

