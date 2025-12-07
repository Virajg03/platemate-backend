package com.example.platemate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {
    
    private ImageView ivProductImage, backButton;
    private TextView tvProductName, tvProductDescription, tvProductPrice, tvIngredients, 
                     tvMealType, tvProviderName, tvProviderBusinessName, tvCategoryName;
    private Button btnAddToCart, btnRateItem;
    private ProgressBar progressBar;
    private RatingBar ratingBar;
    private TextView tvRatingAverage, tvRatingCount, tvNoReviews;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    
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
        
        // Rating views
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingAverage = findViewById(R.id.tvRatingAverage);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        btnRateItem = findViewById(R.id.btnRateItem);
        rvReviews = findViewById(R.id.rvReviews);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        
        // Setup RecyclerView
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        btnAddToCart.setOnClickListener(v -> {
            if (menuItem != null) {
                addToCart(menuItem);
            }
        });
        
        btnRateItem.setOnClickListener(v -> showRatingDialog());
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
        
        // Display ratings
        if (item.getAverageRating() != null && item.getAverageRating() > 0) {
            ratingBar.setRating(item.getAverageRating().floatValue());
            tvRatingAverage.setText(String.format("%.1f", item.getAverageRating()));
            long count = item.getRatingCount() != null ? item.getRatingCount() : 0;
            tvRatingCount.setText(String.format("(%d %s)", count, count == 1 ? "review" : "reviews"));
        } else {
            ratingBar.setRating(0);
            tvRatingAverage.setText("0.0");
            tvRatingCount.setText("(No reviews yet)");
        }
        
        // Show/hide rate button
        if (item.getHasUserRated() != null && !item.getHasUserRated()) {
            btnRateItem.setVisibility(View.VISIBLE);
        } else {
            btnRateItem.setVisibility(View.GONE);
        }
        
        // Load reviews
        loadReviews(item.getId());
    }
    
    private void loadReviews(Long menuItemId) {
        Call<List<RatingReview>> call = apiInterface.getMenuItemReviews(menuItemId);
        call.enqueue(new Callback<List<RatingReview>>() {
            @Override
            public void onResponse(Call<List<RatingReview>> call, Response<List<RatingReview>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RatingReview> reviews = response.body();
                    if (reviews.isEmpty()) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        rvReviews.setVisibility(View.GONE);
                    } else {
                        tvNoReviews.setVisibility(View.GONE);
                        rvReviews.setVisibility(View.VISIBLE);
                        reviewAdapter.updateReviews(reviews);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<List<RatingReview>> call, Throwable t) {
                // Silently fail - reviews are not critical
            }
        });
    }
    
    private void showRatingDialog() {
        if (menuItem == null) {
            ToastUtils.showError(this, "Unable to rate item");
            return;
        }
        
        // Fetch user's recent delivered orders to get orderId
        fetchRecentOrderForRating();
    }
    
    private void fetchRecentOrderForRating() {
        Call<List<Order>> call = apiInterface.getCustomerOrders();
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    // Find a delivered order containing this menu item
                    for (Order order : orders) {
                        if ("DELIVERED".equals(order.getOrderStatus())) {
                            // For now, we'll use the first delivered order
                            // In a more sophisticated implementation, you'd check if order contains this menu item
                            showRatingDialogWithOrder(order.getId());
                            return;
                        }
                    }
                    ToastUtils.showInfo(ProductDetailActivity.this, 
                        "Please order and receive this item before rating");
                } else {
                    ToastUtils.showError(ProductDetailActivity.this, "Unable to load orders");
                }
            }
            
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                ToastUtils.showError(ProductDetailActivity.this, "Error loading orders");
            }
        });
    }
    
    private void showRatingDialogWithOrder(Long orderId) {
        RatingDialog dialog = new RatingDialog(this, 
            "Rate " + (menuItem != null ? menuItem.getItemName() : "this item"),
            (rating, review) -> submitRating(orderId, rating, review));
        dialog.show();
    }
    
    private void submitRating(Long orderId, int rating, String review) {
        if (orderId == null || menuItem == null) {
            ToastUtils.showError(this, "Unable to submit rating");
            return;
        }
        
        ApiInterface.RateMenuItemRequest request = new ApiInterface.RateMenuItemRequest();
        request.orderId = orderId;
        request.menuItemId = menuItem.getId();
        request.rating = rating;
        request.review = review;
        
        Call<RatingReview> call = apiInterface.rateMenuItem(request);
        call.enqueue(new Callback<RatingReview>() {
            @Override
            public void onResponse(Call<RatingReview> call, Response<RatingReview> response) {
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(ProductDetailActivity.this, "Thank you for your rating!");
                    btnRateItem.setVisibility(View.GONE);
                    // Reload product details to refresh ratings
                    loadProductDetails();
                } else {
                    String errorMsg = "Failed to submit rating";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(ProductDetailActivity.this, errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<RatingReview> call, Throwable t) {
                ToastUtils.showError(ProductDetailActivity.this, "Error: " + t.getMessage());
            }
        });
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

