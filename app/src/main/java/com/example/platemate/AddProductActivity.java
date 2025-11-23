package com.example.platemate;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {
    private EditText etProductName, etDescription, etPrice, etIngredients;
    private AutoCompleteTextView etCategory;
    private Spinner spMealType;
    private Button btnSaveProduct;
    private ApiInterface apiInterface;
    
    private List<Category> categoriesList = new ArrayList<>();
    private Map<String, Long> categoryNameToIdMap = new HashMap<>();
    private String[] mealTypes = {"VEG", "NON_VEG", "JAIN"};

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
        loadCategories();
        
        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void initializeViews() {
        etProductName = findViewById(R.id.etProductName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etIngredients = findViewById(R.id.etIngredients);
        etCategory = findViewById(R.id.etCategory);
        spMealType = findViewById(R.id.spMealType);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        
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
                                Toast.makeText(AddProductActivity.this, 
                                    "No valid categories found. Please contact admin.", 
                                    Toast.LENGTH_LONG).show();
                            });
                        }
                    } else {
                        // Empty response
                        runOnUiThread(() -> {
                            Toast.makeText(AddProductActivity.this, 
                                "No categories found. Please contact admin to add categories.", 
                                Toast.LENGTH_LONG).show();
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
                        Toast.makeText(AddProductActivity.this, finalErrorMessage, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AddProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get categoryId from category name
        Long categoryId = categoryNameToIdMap.get(categoryName);
        if (categoryId == null) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
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

            // Handle image upload - for now, we'll skip image since it requires file handling
            // You can add image picker later if needed
            MultipartBody.Part imagePart = null;
            // TODO: Add image picker functionality if needed
            // File imageFile = new File(imagePath);
            // if (imageFile.exists()) {
            //     RequestBody imageRequestBody = RequestBody.create(
            //         MediaType.parse("image/*"), 
            //         imageFile
            //     );
            //     imagePart = MultipartBody.Part.createFormData(
            //         "image", 
            //         imageFile.getName(), 
            //         imageRequestBody
            //     );
            // }

            // Make API call
            Call<MenuItemResponse> call = apiInterface.createMenuItem(dataPart, imagePart);
            call.enqueue(new Callback<MenuItemResponse>() {
                @Override
                public void onResponse(Call<MenuItemResponse> call, Response<MenuItemResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddProductActivity.this, 
                            "Product added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMessage = "Failed to add product";
                        if (response.code() == 403) {
                            errorMessage = "Provider not verified yet. Please wait for admin approval.";
                        } else if (response.code() == 400) {
                            errorMessage = "Invalid data. Please check all fields.";
                        } else if (response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(AddProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MenuItemResponse> call, Throwable t) {
                    Toast.makeText(AddProductActivity.this, 
                        "Error: " + (t.getMessage() != null ? t.getMessage() : "Network error"), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

