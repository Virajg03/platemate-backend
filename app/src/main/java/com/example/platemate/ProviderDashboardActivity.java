package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProviderDashboardActivity extends AppCompatActivity {
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FloatingActionButton fabAddProduct;
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    
    // Profile views
    private TextView tvBusinessName;
    private TextView tvProviderEmail;
    private TextView tvTotalProducts;
    private TextView tvActiveOrders;
    private TextView tvProductCount;
    private CardView cardEditProfile;
    private CardView cardLogout;
    private LinearLayout emptyStateLayout;
    
    // Bottom Navigation
    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;
    private NestedScrollView dashboardScrollView;
    private boolean isDashboardVisible = true;
    
    // Request code for activity result
    private static final int REQUEST_CODE_EDIT_PROFILE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_provider_dashboard);
        
        // Initialize API and Session
        apiInterface = RetrofitClient.getInstance(this).getApi();
        sessionManager = new SessionManager(this);
        
        // Hide action bar for cleaner look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Initialize views
        initializeViews();
        
        // Setup window insets handling for system bars
        setupWindowInsets();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Check if profile was just completed
        boolean profileJustCompleted = getIntent().getBooleanExtra("profileJustCompleted", false);
        if (profileJustCompleted) {
            // Show welcome message after a short delay
            new android.os.Handler().postDelayed(() -> {
                ToastUtils.showSuccess(this, "Welcome! Your profile is now complete. Start adding products!");
            }, 500);
        }
        
        // Load provider details and products
        loadProviderDetails();
        loadProducts();
    }

    private void initializeViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        tvBusinessName = findViewById(R.id.tvBusinessName);
        tvProviderEmail = findViewById(R.id.tvProviderEmail);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvActiveOrders = findViewById(R.id.tvActiveOrders);
        tvProductCount = findViewById(R.id.tvProductCount);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        cardLogout = findViewById(R.id.cardLogout);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        
        // Bottom Navigation views
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        dashboardScrollView = findViewById(R.id.dashboardScrollView);
        
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productsRecyclerView.setAdapter(productAdapter);
        
        // Set initial email from session
        String username = sessionManager.getUsername();
        if (username != null) {
            tvProviderEmail.setText(username);
        }
    }

    private void setupWindowInsets() {
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        
        if (coordinatorLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Handle bottom navigation margin for system navigation bar
                if (bottomNavigationView != null && bottomNavigationView.getParent() instanceof ViewGroup) {
                    ViewGroup parent = (ViewGroup) bottomNavigationView.getParent();
                    ViewGroup.MarginLayoutParams navParams = 
                        (ViewGroup.MarginLayoutParams) parent.getLayoutParams();
                    if (navParams != null) {
                        navParams.bottomMargin = systemBars.bottom;
                        parent.setLayoutParams(navParams);
                    }
                }
                
                // Update fragment container margin dynamically
                updateFragmentContainerMargin(systemBars.bottom);
                
                return insets;
            });
        }
    }

    private void updateFragmentContainerMargin(int systemBarBottom) {
        if (bottomNavigationView != null && fragmentContainer != null) {
            bottomNavigationView.post(() -> {
                try {
                    int bottomNavHeight = bottomNavigationView.getHeight();
                    if (bottomNavHeight == 0) {
                        // If height not measured yet, use default
                        bottomNavHeight = 60;
                    }
                    
                    // Use ViewGroup.MarginLayoutParams instead of FrameLayout.LayoutParams
                    // This works for both FrameLayout and CoordinatorLayout parent containers
                    ViewGroup.MarginLayoutParams params = 
                        (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                    
                    if (params != null) {
                        params.bottomMargin = bottomNavHeight + systemBarBottom;
                        fragmentContainer.setLayoutParams(params);
                    }
                } catch (ClassCastException e) {
                    // Log error but don't crash - layout params might be of unexpected type
                    e.printStackTrace();
                } catch (Exception e) {
                    // Catch any other exceptions to prevent crashes
                    e.printStackTrace();
                }
            });
        }
    }

    private void setupClickListeners() {
        // FAB click listener
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderDashboardActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
        
        // Edit Profile click listener - use startActivityForResult for better data refresh
        cardEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderDashboardActivity.this, ProviderDetailsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
        });
        
        // Logout click listener
        cardLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_dashboard) {
                showDashboard();
                return true;
            } else if (itemId == R.id.nav_orders) {
                showOrdersFragment();
                return true;
            } else if (itemId == R.id.nav_products) {
                showProductsFragment();
                return true;
            }
            return false;
        });
        
        // Set Dashboard as default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }

    private void showDashboard() {
        isDashboardVisible = true;
        dashboardScrollView.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);
        fabAddProduct.setVisibility(View.VISIBLE);
    }

    private void showOrdersFragment() {
        isDashboardVisible = false;
        dashboardScrollView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        fabAddProduct.setVisibility(View.GONE);
        
        // Update fragment container margin when showing fragment
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            updateFragmentContainerMargin(systemBars.bottom);
            return insets;
        });
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        // Always replace to ensure correct fragment is shown when switching tabs
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, new OrdersFragment(), "orders");
        transaction.commit();
    }

    private void showProductsFragment() {
        isDashboardVisible = false;
        dashboardScrollView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        fabAddProduct.setVisibility(View.VISIBLE);
        
        // Update fragment container margin when showing fragment
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            updateFragmentContainerMargin(systemBars.bottom);
            return insets;
        });
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        // Always replace to ensure correct fragment is shown when switching tabs
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, new ProductsFragment(), "products");
        transaction.commit();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                sessionManager.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void loadProviderDetails() {
        Call<Map<String, Object>> call = apiInterface.getProviderDetails();
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> details = response.body();
                    if (details.get("businessName") != null && !details.get("businessName").toString().isEmpty()) {
                        tvBusinessName.setText(details.get("businessName").toString());
                    } else {
                        tvBusinessName.setText("My Tiffin Service");
                    }
                } else {
                    // Use default or username if API fails
                    String username = sessionManager.getUsername();
                    if (username != null) {
                        tvBusinessName.setText(username);
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Use default or username if API fails
                String username = sessionManager.getUsername();
                if (username != null) {
                    tvBusinessName.setText(username);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check onboarding status on resume
        checkOnboardingStatus();
        loadProviderDetails(); // Refresh provider details
        loadProducts(); // Refresh when returning from Add/Edit screen
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Profile was updated, refresh data immediately
            loadProviderDetails();
            loadProducts();
            // Show success message (optional, since ProviderDetailsActivity already shows it)
            // Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkOnboardingStatus() {
        // Only check if user is a provider
        String role = sessionManager.getRole();
        if (!"Provider".equals(role)) {
            return; // Not a provider, skip check
        }
        
        Call<ProfileStatusResponse> call = apiInterface.checkProfileComplete();
        call.enqueue(new Callback<ProfileStatusResponse>() {
            @Override
            public void onResponse(Call<ProfileStatusResponse> call, Response<ProfileStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileStatusResponse status = response.body();
                    boolean isComplete = Boolean.TRUE.equals(status.getIsComplete());
                    boolean isOnboarding = Boolean.TRUE.equals(status.getIsOnboarding());
                    
                    // Update session flag to match backend
                    sessionManager.setProfileComplete(isComplete);
                    
                    if (!isComplete || isOnboarding) {
                        // Profile not complete - force redirect to complete profile
                        Intent intent = new Intent(ProviderDashboardActivity.this, ProviderDetailsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    // If complete, just continue - don't redirect
                }
                // If API fails, don't redirect - just log (user might have network issues)
                // Better UX: assume profile is complete if API fails
            }

            @Override
            public void onFailure(Call<ProfileStatusResponse> call, Throwable t) {
                // On failure, don't block user - just log
                // Only redirect if we're certain profile is incomplete
                // For now, assume profile is complete if API fails (better UX)
                // User can still access dashboard and complete profile later if needed
            }
        });
    }

    private void loadProducts() {
        // Use the new menu-items endpoint which returns base64 images
        Call<List<MenuItemResponse>> call = apiInterface.getProviderMenuItems();
        call.enqueue(new Callback<List<MenuItemResponse>>() {
            @Override
            public void onResponse(Call<List<MenuItemResponse>> call, Response<List<MenuItemResponse>> response) {
                try {
                    if (response.isSuccessful()) {
                        List<MenuItemResponse> menuItems = response.body();
                        if (menuItems != null) {
                            // Convert MenuItemResponse to Product format
                            productList.clear();
                            for (MenuItemResponse menuItem : menuItems) {
                                Product product = convertMenuItemToProduct(menuItem);
                                productList.add(product);
                            }
                            productAdapter.notifyDataSetChanged();
                            updateProductCount();
                            updateEmptyState();
                        } else {
                            // Empty response - no products yet
                            productList.clear();
                            productAdapter.notifyDataSetChanged();
                            updateProductCount();
                            updateEmptyState();
                        }
                    } else {
                        // Handle error response
                        String errorMessage = "Failed to load products";
                        if (response.code() == 403) {
                            errorMessage = "Provider not verified yet. Please wait for admin approval.";
                        } else if (response.code() == 404) {
                            errorMessage = "Menu items endpoint not found";
                        } else if (response.code() == 401) {
                            errorMessage = "Authentication required. Please login again.";
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
                        ToastUtils.showError(ProviderDashboardActivity.this, errorMessage);
                        updateEmptyState();
                    }
                } catch (Exception e) {
                    // Catch any parsing or other exceptions to prevent app crash
                    e.printStackTrace();
                    ToastUtils.showError(ProviderDashboardActivity.this, 
                        "Error loading products: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<MenuItemResponse>> call, Throwable t) {
                // Network error or other failure - don't crash the app
                String errorMessage = "Network error. Please check your connection.";
                if (t.getMessage() != null && !t.getMessage().isEmpty()) {
                    errorMessage = "Error: " + t.getMessage();
                }
                ToastUtils.showError(ProviderDashboardActivity.this, errorMessage);
                updateEmptyState();
            }
        });
    }

    /**
     * Convert MenuItemResponse to Product format for compatibility with existing adapter
     */
    private Product convertMenuItemToProduct(MenuItemResponse menuItem) {
        Product product = new Product();
        
        // Basic fields
        product.setId(menuItem.getId());
        product.setName(menuItem.getItemName() != null ? menuItem.getItemName() : "Unnamed Product");
        product.setDescription(menuItem.getDescription() != null ? menuItem.getDescription() : "");
        product.setPrice(menuItem.getPrice() != null ? menuItem.getPrice() : 0.0);
        product.setIsAvailable(menuItem.getIsAvailable() != null ? menuItem.getIsAvailable() : true);
        product.setQuantity(0); // Menu items don't have quantity in the same way
        
        // Handle images - get first image from base64 list
        if (menuItem.getImageBase64List() != null && !menuItem.getImageBase64List().isEmpty()) {
            product.setImageBase64(menuItem.getImageBase64List().get(0));
            if (menuItem.getImageFileTypeList() != null && !menuItem.getImageFileTypeList().isEmpty()) {
                product.setImageFileType(menuItem.getImageFileTypeList().get(0));
            }
        }
        
        // Set category name if available
        if (menuItem.getCategoryName() != null && !menuItem.getCategoryName().isEmpty()) {
            product.setCategory(menuItem.getCategoryName());
        } else {
            product.setCategory(""); // Will be set to "N/A" in adapter if empty
        }
        
        return product;
    }

    private void updateProductCount() {
        int count = productList.size();
        tvTotalProducts.setText(String.valueOf(count));
        tvProductCount.setText("(" + count + " items)");
    }

    private void updateEmptyState() {
        if (productList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            productsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            productsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.provider_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

