package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class CustomerHomeActivity extends AppCompatActivity {
    
    private TextView welcomeText, userNameText;
    private ImageView logoutBtn, cartBtn, searchIcon;
    // private ImageView filterBtn; // Commented out - filter button is not in layout
    private EditText searchEditText;
    private RecyclerView bestFoodRecyclerView, categoryRecyclerView;
    private ProgressBar progressBarBestFood, progressBarCategory;
    private TextView todayBestFoodTitle, viewAllText, chooseCategoryTitle;
    
    private BottomNavigationView bottomNavigationView;
    private ScrollView homeScrollView;
    private FrameLayout fragmentContainer;
    
    private SessionManager sessionManager;
    private ApiInterface apiInterface;
    
    private BestFoodAdapter bestFoodAdapter;
    private CategoryAdapter categoryAdapter;
    
    private boolean isHomeVisible = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_home);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        sessionManager = new SessionManager(this);
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        initializeViews();
        setupClickListeners();
        setupBottomNavigation();
        loadUserData();
        setupRecyclerViews();
        loadBestFoods();
        loadCategories();
    }
    
    private void initializeViews() {
        welcomeText = findViewById(R.id.textView14);
        userNameText = findViewById(R.id.textView13);
        logoutBtn = findViewById(R.id.logoutbtn);
        cartBtn = findViewById(R.id.cartbtn);
        searchIcon = findViewById(R.id.searchfilter);
        searchEditText = findViewById(R.id.editTextText);
        bestFoodRecyclerView = findViewById(R.id.bestfoodview);
        categoryRecyclerView = findViewById(R.id.catogoryview);
        progressBarBestFood = findViewById(R.id.progressbarbestfood);
        progressBarCategory = findViewById(R.id.progressBarcatogory);
        todayBestFoodTitle = findViewById(R.id.textView10);
        viewAllText = findViewById(R.id.textView11);
        chooseCategoryTitle = findViewById(R.id.textView12);
        
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        homeScrollView = findViewById(R.id.homeScrollView);
        fragmentContainer = findViewById(R.id.fragmentContainer);
    }
    
    private void setupClickListeners() {
        logoutBtn.setOnClickListener(v -> showLogoutDialog());
        cartBtn.setOnClickListener(v -> {
            // Navigate to cart activity
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });
        // filterBtn is commented out in layout, so skip setting listener
        // if (filterBtn != null) {
        //     filterBtn.setOnClickListener(v -> {
        //         // TODO: Show filter dialog
        //         Toast.makeText(this, "Filter feature coming soon!", Toast.LENGTH_SHORT).show();
        //     });
        // }
        viewAllText.setOnClickListener(v -> {
            // Navigate to all products activity
            Intent intent = new Intent(this, AllProductsActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadUserData() {
        String username = sessionManager.getUsername();
        if (username != null && !username.isEmpty()) {
            userNameText.setText(username);
        } else {
            userNameText.setText("Guest");
        }
    }
    
    private void setupRecyclerViews() {
        // Setup best food RecyclerView
        bestFoodRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        bestFoodAdapter = new BestFoodAdapter(new ArrayList<>());
        bestFoodAdapter.setOnItemClickListener(new BestFoodAdapter.OnItemClickListener() {
            @Override
            public void onAddToCartClick(MenuItem menuItem) {
                addToCart(menuItem);
            }

            @Override
            public void onItemClick(MenuItem menuItem) {
                // Navigate to product detail activity
                if (menuItem.getId() != null) {
                    Intent intent = new Intent(CustomerHomeActivity.this, ProductDetailActivity.class);
                    intent.putExtra("menuItemId", menuItem.getId());
                    startActivity(intent);
                } else {
                    ToastUtils.showError(CustomerHomeActivity.this, "Invalid product");
                }
            }
        });
        bestFoodRecyclerView.setAdapter(bestFoodAdapter);
        
        // Setup category RecyclerView with Grid Layout (2 columns)
        androidx.recyclerview.widget.GridLayoutManager gridLayoutManager = 
            new androidx.recyclerview.widget.GridLayoutManager(this, 2);
        categoryRecyclerView.setLayoutManager(gridLayoutManager);
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        categoryAdapter.setOnItemClickListener(category -> {
            // Filter products by category
            if (category.getId() != null) {
                // TODO: Navigate to filtered menu items or filter current list
                ToastUtils.showInfo(CustomerHomeActivity.this, 
                    "Selected category: " + category.getCategoryName());
                // You can add navigation to a filtered product list here
            } else {
                ToastUtils.showInfo(CustomerHomeActivity.this, 
                    "Selected category: " + category.getCategoryName());
            }
        });
        categoryRecyclerView.setAdapter(categoryAdapter);
    }
    
    private void loadBestFoods() {
        progressBarBestFood.setVisibility(View.VISIBLE);
        bestFoodRecyclerView.setVisibility(View.GONE);
        
        // Load menu items from backend
        Call<MenuItemResponse> call = apiInterface.getCustomerMenuItems(0, 20, "id,desc");
        call.enqueue(new Callback<MenuItemResponse>() {
            @Override
            public void onResponse(Call<MenuItemResponse> call, Response<MenuItemResponse> response) {
                progressBarBestFood.setVisibility(View.GONE);
                bestFoodRecyclerView.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful() && response.body() != null) {
                    MenuItemResponse menuItemResponse = response.body();
                    if (menuItemResponse.getContent() != null && !menuItemResponse.getContent().isEmpty()) {
                        bestFoodAdapter.updateList(menuItemResponse.getContent());
                    } else {
                        showEmptyMenuItems();
                    }
                } else {
                    ToastUtils.showError(CustomerHomeActivity.this, 
                        "Failed to load menu items");
                    showEmptyMenuItems();
                }
            }
            
            @Override
            public void onFailure(Call<MenuItemResponse> call, Throwable t) {
                progressBarBestFood.setVisibility(View.GONE);
                bestFoodRecyclerView.setVisibility(View.VISIBLE);
                ToastUtils.showError(CustomerHomeActivity.this, 
                    "Error: " + t.getMessage());
                showEmptyMenuItems();
            }
        });
    }
    
    private void showEmptyMenuItems() {
        // Show empty state if no menu items
        bestFoodAdapter.updateList(new ArrayList<>());
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
                    ToastUtils.showSuccess(CustomerHomeActivity.this, 
                        "Added " + menuItem.getItemName() + " to cart");
                } else {
                    ToastUtils.showError(CustomerHomeActivity.this, 
                        "Failed to add to cart");
                }
            }
            
            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                ToastUtils.showError(CustomerHomeActivity.this, 
                    "Error: " + t.getMessage());
            }
        });
    }
    
    private void loadCategories() {
        progressBarCategory.setVisibility(View.VISIBLE);
        categoryRecyclerView.setVisibility(View.GONE);
        
        Call<List<Category>> call = apiInterface.getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressBarCategory.setVisibility(View.GONE);
                categoryRecyclerView.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    if (categories != null && !categories.isEmpty()) {
                        categoryAdapter.updateList(categories);
                    } else {
                        // Show empty state or default categories
                        showEmptyCategories();
                    }
                } else {
                    ToastUtils.showError(CustomerHomeActivity.this, 
                        "Failed to load categories");
                    showEmptyCategories();
                }
            }
            
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressBarCategory.setVisibility(View.GONE);
                categoryRecyclerView.setVisibility(View.VISIBLE);
                ToastUtils.showError(CustomerHomeActivity.this, 
                    "Error: " + t.getMessage());
                showEmptyCategories();
            }
        });
    }
    
    private void showEmptyCategories() {
        // Show default/empty categories if API fails
        List<Category> defaultCategories = new ArrayList<>();
        defaultCategories.add(new Category("All", android.R.drawable.ic_menu_gallery));
        categoryAdapter.updateList(defaultCategories);
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> handleLogout())
            .setNegativeButton("No", null)
            .show();
    }
    
    private void handleLogout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add menu items if needed
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                showHome();
                return true;
            } else if (itemId == R.id.nav_orders) {
                showOrdersFragment();
                return true;
            } else if (itemId == R.id.nav_profile) {
                showProfileFragment();
                return true;
            }
            return false;
        });
        
        // Set Home as default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
    
    private void showHome() {
        isHomeVisible = true;
        homeScrollView.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);
    }
    
    private void showOrdersFragment() {
        isHomeVisible = false;
        homeScrollView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // Update fragment container margin when showing fragment
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            updateFragmentContainerMargin(systemBars.bottom);
            return insets;
        });
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        // Always replace to ensure correct fragment is shown when switching tabs
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, new CustomerOrdersFragment(), "orders");
        transaction.commit();
    }
    
    private void showProfileFragment() {
        isHomeVisible = false;
        homeScrollView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // Update fragment container margin when showing fragment
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            updateFragmentContainerMargin(systemBars.bottom);
            return insets;
        });
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        // Always replace to ensure correct fragment is shown when switching tabs
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, new CustomerProfileFragment(), "profile");
        transaction.commit();
    }
    
    private void updateFragmentContainerMargin(int systemBarBottom) {
        if (bottomNavigationView != null && fragmentContainer != null) {
            bottomNavigationView.post(() -> {
                int bottomNavHeight = bottomNavigationView.getHeight();
                if (bottomNavHeight == 0) {
                    // If height not measured yet, use default
                    bottomNavHeight = 60;
                }
                
                CoordinatorLayout.LayoutParams params = 
                    (CoordinatorLayout.LayoutParams) fragmentContainer.getLayoutParams();
                params.bottomMargin = bottomNavHeight + systemBarBottom;
                fragmentContainer.setLayoutParams(params);
            });
        }
    }
}
