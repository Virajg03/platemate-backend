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
import android.widget.Toast;
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
                int bottomNavHeight = bottomNavigationView.getHeight();
                if (bottomNavHeight == 0) {
                    // If height not measured yet, use default
                    bottomNavHeight = 60;
                }
                
                FrameLayout.LayoutParams params = 
                    (FrameLayout.LayoutParams) fragmentContainer.getLayoutParams();
                params.bottomMargin = bottomNavHeight + systemBarBottom;
                fragmentContainer.setLayoutParams(params);
            });
        }
    }

    private void setupClickListeners() {
        // FAB click listener
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderDashboardActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
        
        // Edit Profile click listener
        cardEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderDashboardActivity.this, ProviderDetailsActivity.class);
            startActivity(intent);
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
        Call<ProviderDetails> call = apiInterface.getProviderDetails();
        call.enqueue(new Callback<ProviderDetails>() {
            @Override
            public void onResponse(Call<ProviderDetails> call, Response<ProviderDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProviderDetails details = response.body();
                    if (details.getBusinessName() != null && !details.getBusinessName().isEmpty()) {
                        tvBusinessName.setText(details.getBusinessName());
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
            public void onFailure(Call<ProviderDetails> call, Throwable t) {
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
        loadProviderDetails(); // Refresh provider details
        loadProducts(); // Refresh when returning from Add/Edit screen
    }

    private void loadProducts() {
        Call<List<Product>> call = apiInterface.getProviderProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    productAdapter.notifyDataSetChanged();
                    updateProductCount();
                    updateEmptyState();
                } else {
                    Toast.makeText(ProviderDashboardActivity.this, 
                        "Failed to load products", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(ProviderDashboardActivity.this, 
                    "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
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

