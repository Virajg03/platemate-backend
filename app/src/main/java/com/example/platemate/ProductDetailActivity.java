package com.example.platemate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    
    private ImageView ivProductImage, backButton;
    private TextView tvProductName, tvProductDescription, tvProductPrice, tvIngredients, 
                     tvMealType, tvProviderName, tvProviderBusinessName, tvCategoryName;
    private Button btnAddToCart;
    private ProgressBar progressBar;
    
    private ApiInterface apiInterface;
    private Long menuItemId;
    private MenuItem menuItem;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        
        menuItemId = getIntent().getLongExtra("menuItemId", -1);
        if (menuItemId == -1) {
            ToastUtils.showError(this, "Invalid product");
            finish();
            return;
        }
        
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        initializeViews();
        setupClickListeners();
        loadProductDetails();
    }
    
    private void initializeViews() {
        ivProductImage = findViewById(R.id.ivProductImage);
        backButton = findViewById(R.id.backButton);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvIngredients = findViewById(R.id.tvIngredients);
        tvMealType = findViewById(R.id.tvMealType);
        tvProviderName = findViewById(R.id.tvProviderName);
        tvProviderBusinessName = findViewById(R.id.tvProviderBusinessName);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        btnAddToCart.setOnClickListener(v -> {
            if (menuItem != null) {
                addToCart(menuItem);
            }
        });
    }
    
    private void loadProductDetails() {
        progressBar.setVisibility(View.VISIBLE);
        
        Call<MenuItem> call = apiInterface.getCustomerMenuItemById(menuItemId);
        call.enqueue(new Callback<MenuItem>() {
            @Override
            public void onResponse(Call<MenuItem> call, Response<MenuItem> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    menuItem = response.body();
                    displayProductDetails(menuItem);
                } else {
                    ToastUtils.showError(ProductDetailActivity.this, "Failed to load product details");
                    finish();
                }
            }
            
            @Override
            public void onFailure(Call<MenuItem> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtils.showError(ProductDetailActivity.this, "Error: " + t.getMessage());
                finish();
            }
        });
    }
    
    private void displayProductDetails(MenuItem item) {
        // Product basic info
        tvProductName.setText(item.getItemName() != null ? item.getItemName() : "N/A");
        tvProductDescription.setText(item.getDescription() != null ? item.getDescription() : "No description available");
        tvProductPrice.setText("₹" + String.format("%.2f", item.getPrice() != null ? item.getPrice() : 0.0));
        
        // Ingredients
        if (item.getIngredients() != null && !item.getIngredients().isEmpty()) {
            tvIngredients.setText("Ingredients: " + item.getIngredients());
            tvIngredients.setVisibility(View.VISIBLE);
        } else {
            tvIngredients.setVisibility(View.GONE);
        }
        
        // Meal type
        if (item.getMealType() != null && !item.getMealType().isEmpty()) {
            tvMealType.setText("Type: " + item.getMealType());
            tvMealType.setVisibility(View.VISIBLE);
        } else {
            tvMealType.setVisibility(View.GONE);
        }
        
        // Category
        if (item.getCategoryName() != null && !item.getCategoryName().isEmpty()) {
            tvCategoryName.setText("Category: " + item.getCategoryName());
            tvCategoryName.setVisibility(View.VISIBLE);
        } else {
            tvCategoryName.setVisibility(View.GONE);
        }
        
        // Provider details
        if (item.getProviderName() != null && !item.getProviderName().isEmpty()) {
            tvProviderName.setText("Provider: " + item.getProviderName());
            tvProviderName.setVisibility(View.VISIBLE);
        } else {
            tvProviderName.setVisibility(View.GONE);
        }
        
        if (item.getProviderBusinessName() != null && !item.getProviderBusinessName().isEmpty()) {
            tvProviderBusinessName.setText("Business: " + item.getProviderBusinessName());
            tvProviderBusinessName.setVisibility(View.VISIBLE);
        } else {
            tvProviderBusinessName.setVisibility(View.GONE);
        }
        
        // Load product image
        String base64Image = item.getFirstImageBase64();
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    ivProductImage.setImageBitmap(bitmap);
                } else {
                    ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } catch (Exception e) {
                ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }
    
    private void addToCart(MenuItem menuItem) {
        if (menuItem.getId() == null) {
            ToastUtils.showError(this, "Invalid menu item");
            return;
        }
        
        AddToCartRequest request = new AddToCartRequest(menuItem.getId(), 1, null);
        Call<CartItem> call = apiInterface.addToCart(request);
        call.enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ToastUtils.showSuccess(ProductDetailActivity.this, 
                        "Added " + menuItem.getItemName() + " to cart");
                } else {
                    ToastUtils.showError(ProductDetailActivity.this, "Failed to add to cart");
                }
            }
            
            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                ToastUtils.showError(ProductDetailActivity.this, "Error: " + t.getMessage());
            }
        });
    }
}

