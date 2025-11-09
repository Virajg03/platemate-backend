package com.example.platemate;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {
    private EditText etProductName, etDescription, etPrice, etQuantity, etImageUrl;
    private AutoCompleteTextView etCategory;
    private Button btnSaveProduct;
    private ApiInterface apiInterface;
    
    private String[] categories = {"Breakfast", "Lunch", "Dinner", "Snacks", "Beverages"};

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
        setupCategoryDropdown();
        
        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void initializeViews() {
        etProductName = findViewById(R.id.etProductName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etImageUrl = findViewById(R.id.etImageUrl);
        etCategory = findViewById(R.id.etCategory);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, R.layout.list_item, categories);
        etCategory.setAdapter(adapter);
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || 
            category.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Double price = Double.parseDouble(priceStr);
            Integer quantity = Integer.parseInt(quantityStr);

            Product product = new Product(name, description, price, category);
            product.setQuantity(quantity);
            if (!imageUrl.isEmpty()) {
                product.setImageUrl(imageUrl);
            }

            Call<Product> call = apiInterface.createProduct(product);
            call.enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddProductActivity.this, 
                            "Product added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddProductActivity.this, 
                            "Failed to add product", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    Toast.makeText(AddProductActivity.this, 
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or quantity", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

