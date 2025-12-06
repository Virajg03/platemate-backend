package com.example.platemate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.platemate.User;

public class ProviderDashboardActivity extends AppCompatActivity {
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FloatingActionButton fabAddProduct;
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    private boolean isProviderApproved = false; // Track approval status
    
    // Profile views
    private ImageView ivProfilePicture;
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
    private SwipeRefreshLayout swipeRefreshLayout;
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
        
        // Load provider details, profile image, and products
        loadProviderDetails();
        loadUserProfileImage();
        loadProducts();
    }

    private void initializeViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        // Initially hide FAB until we check approval status
        fabAddProduct.setVisibility(View.GONE);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
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
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        // Setup SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.login_orange, null)
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                refreshDashboard();
            });
        }
        
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productsRecyclerView.setAdapter(productAdapter);
        
        // Setup scroll listener for dashboard scroll view
        setupScrollListener();
        
        // Set initial email from session
        String username = sessionManager.getUsername();
        if (username != null) {
            tvProviderEmail.setText(username);
        }
    }
    
    private int lastScrollY = 0;
    private boolean isScrollingDown = false;
    
    private void setupScrollListener() {
        // Scroll listener for dashboard scroll view
        dashboardScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    // Scrolling down
                    if (!isScrollingDown) {
                        isScrollingDown = true;
                        hideFab();
                    }
                } else if (scrollY < oldScrollY) {
                    // Scrolling up
                    if (isScrollingDown) {
                        isScrollingDown = false;
                        showFab();
                    }
                }
                lastScrollY = scrollY;
            }
        });
    }
    
    public void showFab() {
        if (fabAddProduct != null && isProviderApproved && 
            (isDashboardVisible || getSupportFragmentManager().findFragmentByTag("products") != null)) {
            // Only show if on dashboard or products tab
            if (fabAddProduct.getVisibility() != View.VISIBLE) {
                fabAddProduct.show();
            }
        }
    }
    
    public void hideFab() {
        if (fabAddProduct != null && fabAddProduct.getVisibility() == View.VISIBLE) {
            fabAddProduct.hide();
        }
    }

    private void setupWindowInsets() {
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        
        if (coordinatorLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Handle status bar padding for dashboard scroll view only
                if (dashboardScrollView != null) {
                    dashboardScrollView.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        0
                    );
                }
                
                // Don't add padding to fragment container - fragments handle their own spacing
                // This prevents double padding and odd spacing
                
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
            } else if (itemId == R.id.nav_profile) {
                showProfileFragment();
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
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
        updateFabVisibility();
        // Show FAB when returning to dashboard if scrolling up
        if (!isScrollingDown) {
            showFab();
        }
    }

    private void showOrdersFragment() {
        isDashboardVisible = false;
        dashboardScrollView.setVisibility(View.GONE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setVisibility(View.GONE);
        }
        fragmentContainer.setVisibility(View.VISIBLE);
        fabAddProduct.setVisibility(View.GONE);
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment ordersFragment = fragmentManager.findFragmentByTag("orders");
        Fragment productsFragment = fragmentManager.findFragmentByTag("products");
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Hide products fragment if it exists
        if (productsFragment != null) {
            transaction.hide(productsFragment);
        }
        
        // Show or create orders fragment
        if (ordersFragment != null) {
            transaction.show(ordersFragment);
        } else {
            ordersFragment = new OrdersFragment();
            transaction.add(R.id.fragmentContainer, ordersFragment, "orders");
        }
        
        transaction.commit();
    }

    private void showProductsFragment() {
        isDashboardVisible = false;
        dashboardScrollView.setVisibility(View.GONE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setVisibility(View.GONE);
        }
        fragmentContainer.setVisibility(View.VISIBLE);
        updateFabVisibility();
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment ordersFragment = fragmentManager.findFragmentByTag("orders");
        Fragment productsFragment = fragmentManager.findFragmentByTag("products");
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Hide orders fragment if it exists
        if (ordersFragment != null) {
            transaction.hide(ordersFragment);
        }
        
        // Show or create products fragment
        if (productsFragment != null) {
            transaction.show(productsFragment);
        } else {
            productsFragment = new ProductsFragment();
            transaction.add(R.id.fragmentContainer, productsFragment, "products");
        }
        
        transaction.commit();
    }

    private void showProfileFragment() {
        isDashboardVisible = false;
        dashboardScrollView.setVisibility(View.GONE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setVisibility(View.GONE);
        }
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
        transaction.replace(R.id.fragmentContainer, new ProviderProfileFragment(), "profile");
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
                onRefreshComplete();
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> details = response.body();
                    if (details.get("businessName") != null && !details.get("businessName").toString().isEmpty()) {
                        tvBusinessName.setText(details.get("businessName").toString());
                    } else {
                        tvBusinessName.setText("My Tiffin Service");
                    }
                    
                    // Check if provider is approved/verified
                    boolean previousApprovalStatus = sessionManager.getProviderApproved();
                    isProviderApproved = false;
                    if (details.get("isApproved") != null) {
                        Object approvedObj = details.get("isApproved");
                        if (approvedObj instanceof Boolean) {
                            isProviderApproved = (Boolean) approvedObj;
                        } else if (approvedObj instanceof String) {
                            isProviderApproved = Boolean.parseBoolean((String) approvedObj);
                        }
                    } else if (details.get("isVerified") != null) {
                        Object verifiedObj = details.get("isVerified");
                        if (verifiedObj instanceof Boolean) {
                            isProviderApproved = (Boolean) verifiedObj;
                        } else if (verifiedObj instanceof String) {
                            isProviderApproved = Boolean.parseBoolean((String) verifiedObj);
                        }
                    }
                    
                    // Check if approval status changed and show notification
                    if (sessionManager.hasApprovalStatusChanged(isProviderApproved)) {
                        // Status changed - check if it's an approval (false -> true)
                        if (isProviderApproved && !previousApprovalStatus) {
                            showApprovalNotification();
                        }
                    }
                    
                    // Update FAB visibility based on approval status
                    updateFabVisibility();
                } else {
                    // Use default or username if API fails
                    String username = sessionManager.getUsername();
                    if (username != null) {
                        tvBusinessName.setText(username);
                    }
                    // On API failure, hide FAB to be safe
                    fabAddProduct.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                onRefreshComplete();
                // Use default or username if API fails
                String username = sessionManager.getUsername();
                if (username != null) {
                    tvBusinessName.setText(username);
                }
                // On failure, hide FAB to be safe
                fabAddProduct.setVisibility(View.GONE);
            }
        });
    }
    
    /**
     * Load and display user profile image in the dashboard header
     */
    private void loadUserProfileImage() {
        Long userId = sessionManager.getUserId();
        if (userId == null || ivProfilePicture == null) {
            onRefreshComplete(); // Complete refresh even if we can't load
            return;
        }
        
        // Load user profile to get profileImageId
        Call<User> userCall = apiInterface.getCustomerProfile(userId);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Long profileImageId = user.getProfileImageId();
                    
                    if (profileImageId != null) {
                        loadProfileImageFromId(profileImageId);
                    } else {
                        // Set default icon if no profile image
                        ivProfilePicture.setImageResource(R.drawable.ic_profile);
                        ivProfilePicture.setColorFilter(getResources().getColor(R.color.login_orange, null));
                    }
                } else {
                    // Set default icon on error
                    if (ivProfilePicture != null) {
                        ivProfilePicture.setImageResource(R.drawable.ic_profile);
                        ivProfilePicture.setColorFilter(getResources().getColor(R.color.login_orange, null));
                    }
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("ProviderDashboard", "Failed to load user profile", t);
                // Set default icon on failure
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageResource(R.drawable.ic_profile);
                    ivProfilePicture.setColorFilter(getResources().getColor(R.color.login_orange, null));
                }
            }
        });
    }
    
    /**
     * Load profile image from image ID and display it
     */
    private void loadProfileImageFromId(Long imageId) {
        if (imageId == null || ivProfilePicture == null) {
            return;
        }
        
        Call<okhttp3.ResponseBody> call = apiInterface.getImage(imageId);
        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                onRefreshComplete();
                if (response.isSuccessful() && response.body() != null && ivProfilePicture != null) {
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
                            if (bitmap != null) {
                                ivProfilePicture.setImageBitmap(bitmap);
                                ivProfilePicture.clearColorFilter(); // Remove tint when showing actual image
                                Log.d("ProviderDashboard", "Profile image loaded successfully");
                            } else {
                                // Fallback to default icon
                                ivProfilePicture.setImageResource(R.drawable.ic_profile);
                                ivProfilePicture.setColorFilter(getResources().getColor(R.color.login_orange, null));
                            }
                        } else {
                            // Fallback to default icon
                            ivProfilePicture.setImageResource(R.drawable.ic_profile);
                            ivProfilePicture.setColorFilter(getResources().getColor(R.color.login_orange, null));
                        }
                    } catch (Exception e) {
                        Log.e("ProviderDashboard", "Error processing image bytes", e);
                        // Fallback to default icon
                        if (ivProfilePicture != null) {
                            ivProfilePicture.setImageResource(R.drawable.ic_profile);
                            ivProfilePicture.setColorFilter(getResources().getColor(R.color.login_orange, null));
                        }
                    }
                } else {
                    // Fallback to default icon
                    if (ivProfilePicture != null) {
                        ivProfilePicture.setImageResource(R.drawable.ic_profile);
                        ivProfilePicture.setColorFilter(getResources().getColor(R.color.login_orange, null));
                    }
                }
            }
            
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                onRefreshComplete();
                Log.e("ProviderDashboard", "Failed to load profile image", t);
                // Fallback to default icon
                if (ivProfilePicture != null) {
                    ivProfilePicture.setImageResource(R.drawable.ic_profile);
                    ivProfilePicture.setColorFilter(getResources().getColor(R.color.login_orange, null));
                }
            }
        });
    }
    
    /**
     * Scale down bitmap to prevent memory issues
     */
    private Bitmap scaleDownBitmap(Bitmap bitmap, int maxSize) {
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
                Log.d("ProviderDashboard", "Scaled bitmap from " + width + "x" + height + " to " + newWidth + "x" + newHeight);
                return scaled;
            } catch (Exception e) {
                Log.e("ProviderDashboard", "Error scaling bitmap", e);
                return null; // Return null on error to prevent crash
            }
        }
        
        return bitmap;
    }
    
    /**
     * Fix image orientation from byte array (handles EXIF data) - REMOVED to prevent memory issues
     */
    private Bitmap fixImageOrientationFromBytes(byte[] imageBytes, Bitmap bitmap) {
        // Return bitmap as-is to prevent memory issues
        return bitmap;
    }

    /**
     * Show notification when provider gets approved
     */
    private void showApprovalNotification() {
        new AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage("Your provider account has been approved by the admin. You can now add products and start receiving orders!")
            .setPositiveButton("Got it!", (dialog, which) -> {
                dialog.dismiss();
                // Update FAB visibility to show the add button
                updateFabVisibility();
            })
            .setCancelable(false)
            .show();
    }
    
    private int refreshCounter = 0;
    private static final int TOTAL_REFRESH_CALLS = 3; // provider details, profile image, products
    
    /**
     * Refresh dashboard data
     */
    private void refreshDashboard() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        refreshCounter = 0;
        
        // Refresh all data
        loadProviderDetails();
        loadUserProfileImage();
        loadProducts();
    }
    
    /**
     * Called when a refresh operation completes
     */
    private void onRefreshComplete() {
        refreshCounter++;
        if (refreshCounter >= TOTAL_REFRESH_CALLS) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            refreshCounter = 0;
        }
    }
    
    /**
     * Update FAB visibility based on provider approval status and current tab
     */
    private void updateFabVisibility() {
        // Check if we're on dashboard or products tab
        boolean isOnDashboard = isDashboardVisible && dashboardScrollView.getVisibility() == View.VISIBLE;
        boolean isOnProducts = !isDashboardVisible && 
                               fragmentContainer.getVisibility() == View.VISIBLE &&
                               getSupportFragmentManager().findFragmentByTag("products") != null;
        
        // Only show FAB if provider is approved and on dashboard/products tab
        if (isProviderApproved && (isOnDashboard || isOnProducts)) {
            fabAddProduct.setVisibility(View.VISIBLE);
        } else {
            fabAddProduct.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check onboarding status on resume
        checkOnboardingStatus();
        loadProviderDetails(); // Refresh provider details
        loadUserProfileImage(); // Refresh profile image (in case it was updated)
        loadProducts(); // Refresh when returning from Add/Edit screen
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Profile was updated, refresh data immediately
            loadProviderDetails();
            loadUserProfileImage(); // Refresh profile image in case it was updated
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
                onRefreshComplete();
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
                onRefreshComplete();
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

