package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class AllProductsActivity extends AppCompatActivity {
    
    private RecyclerView rvProducts;
    private ProgressBar progressBar, progressBarLoadMore;
    private ImageView backButton;
    private TextView emptyStateText, tvTitle;
    
    private ApiInterface apiInterface;
    private BestFoodAdapter adapter;
    private List<MenuItem> productList;
    
    private int currentPage = 0;
    private int pageSize = 20;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private GridLayoutManager layoutManager;
    private Long categoryId = null; // Category filter
    private String categoryName = null; // Category name for display
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);
        
        apiInterface = RetrofitClient.getInstance(this).getApi();
        productList = new ArrayList<>();
        
        // Get category filter from intent
        if (getIntent() != null) {
            categoryId = getIntent().hasExtra("categoryId") ? 
                getIntent().getLongExtra("categoryId", -1) : null;
            if (categoryId != null && categoryId == -1) {
                categoryId = null;
            }
            categoryName = getIntent().getStringExtra("categoryName");
        }
        
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadProducts();
    }
    
    private void initializeViews() {
        rvProducts = findViewById(R.id.rvProducts);
        progressBar = findViewById(R.id.progressBar);
        progressBarLoadMore = findViewById(R.id.progressBarLoadMore);
        backButton = findViewById(R.id.backButton);
        emptyStateText = findViewById(R.id.emptyStateText);
        tvTitle = findViewById(R.id.tvTitle);
        
        // Update title based on category filter
        if (categoryName != null && !categoryName.isEmpty()) {
            tvTitle.setText(categoryName.toUpperCase());
        } else {
            tvTitle.setText("ALL PRODUCTS");
        }
    }
    
    private void setupRecyclerView() {
        // Use GridLayoutManager for 2 columns
        layoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(layoutManager);
        
        adapter = new BestFoodAdapter(productList);
        adapter.setOnItemClickListener(new BestFoodAdapter.OnItemClickListener() {
            @Override
            public void onAddToCartClick(MenuItem menuItem) {
                addToCart(menuItem);
            }

            @Override
            public void onItemClick(MenuItem menuItem) {
                // Navigate to product detail page
                if (menuItem.getId() != null) {
                    Intent intent = new Intent(AllProductsActivity.this, ProductDetailActivity.class);
                    intent.putExtra("menuItemId", menuItem.getId());
                    startActivity(intent);
                } else {
                    ToastUtils.showError(AllProductsActivity.this, "Invalid product");
                }
            }
        });
        rvProducts.setAdapter(adapter);
        
        // Add scroll listener for infinite scroll
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                
                if (!isLoading && hasMorePages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= pageSize) {
                        loadMoreProducts();
                    }
                }
            }
        });
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }
    
    private void loadProducts() {
        if (isLoading) return;
        
        isLoading = true;
        if (currentPage == 0) {
            progressBar.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            progressBarLoadMore.setVisibility(View.VISIBLE);
        }
        
        Call<MenuItemResponse> call;
        // Use category-filtered endpoint if categoryId is provided
        if (categoryId != null) {
            call = apiInterface.getCustomerMenuItemsByCategory(categoryId, currentPage, pageSize, "id,desc");
        } else {
            call = apiInterface.getCustomerMenuItems(currentPage, pageSize, "id,desc");
        }
        
        call.enqueue(new Callback<MenuItemResponse>() {
            @Override
            public void onResponse(Call<MenuItemResponse> call, Response<MenuItemResponse> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                progressBarLoadMore.setVisibility(View.GONE);
                rvProducts.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful() && response.body() != null) {
                    MenuItemResponse menuItemResponse = response.body();
                    List<MenuItem> newItems = menuItemResponse.getContent();
                    
                    if (newItems != null && !newItems.isEmpty()) {
                        if (currentPage == 0) {
                            productList.clear();
                        }
                        productList.addAll(newItems);
                        adapter.notifyDataSetChanged();
                        
                        // Check if there are more pages
                        int totalPages = menuItemResponse.getTotalPages() != null ? 
                            menuItemResponse.getTotalPages() : 0;
                        hasMorePages = currentPage < totalPages - 1;
                        currentPage++;
                        
                        showEmptyState(false);
                    } else {
                        hasMorePages = false;
                        if (productList.isEmpty()) {
                            showEmptyState(true);
                        }
                    }
                } else {
                    hasMorePages = false;
                    if (productList.isEmpty()) {
                        showEmptyState(true);
                        ToastUtils.showError(AllProductsActivity.this, "Failed to load products");
                    }
                }
            }
            
            @Override
            public void onFailure(Call<MenuItemResponse> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                progressBarLoadMore.setVisibility(View.GONE);
                hasMorePages = false;
                
                if (productList.isEmpty()) {
                    showEmptyState(true);
                    ToastUtils.showError(AllProductsActivity.this, "Error: " + t.getMessage());
                } else {
                    ToastUtils.showError(AllProductsActivity.this, "Failed to load more products");
                }
            }
        });
    }
    
    private void loadMoreProducts() {
        loadProducts();
    }
    
    private void showEmptyState(boolean show) {
        if (show) {
            rvProducts.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            rvProducts.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
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
                    ToastUtils.showSuccess(AllProductsActivity.this, 
                        "Added " + menuItem.getItemName() + " to cart");
                } else {
                    ToastUtils.showError(AllProductsActivity.this, "Failed to add to cart");
                }
            }
            
            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                ToastUtils.showError(AllProductsActivity.this, "Error: " + t.getMessage());
            }
        });
    }
}

