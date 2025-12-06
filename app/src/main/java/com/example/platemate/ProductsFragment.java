package com.example.platemate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsFragment extends Fragment {
    
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ApiInterface apiInterface;
    private LinearLayout emptyProductsLayout;
    private TextView tvProductsCount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isDataLoaded = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_provider_product, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        emptyProductsLayout = view.findViewById(R.id.emptyProductsLayout);
        tvProductsCount = view.findViewById(R.id.tvProductsCount);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeColors(
            getResources().getColor(R.color.login_orange, null)
        );
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadProducts(true);
        });
        
        // Initialize API
        apiInterface = RetrofitClient.getInstance(getContext()).getApi();
        
        // Setup RecyclerView
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, null);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productsRecyclerView.setAdapter(productAdapter);
        
        // Setup scroll listener for FAB visibility
        setupScrollListener();
        
        // Load products immediately when fragment is created
        if (!isDataLoaded) {
            loadProducts(false);
            isDataLoaded = true;
        }
    }
    
    private int lastScrollY = 0;
    private boolean isScrollingDown = false;
    
    private void setupScrollListener() {
        productsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (dy > 0) {
                    // Scrolling down
                    if (!isScrollingDown) {
                        isScrollingDown = true;
                        hideFab();
                    }
                } else if (dy < 0) {
                    // Scrolling up
                    if (isScrollingDown) {
                        isScrollingDown = false;
                        showFab();
                    }
                }
            }
        });
    }
    
    private void showFab() {
        if (getActivity() instanceof ProviderDashboardActivity) {
            ((ProviderDashboardActivity) getActivity()).showFab();
        }
    }
    
    private void hideFab() {
        if (getActivity() instanceof ProviderDashboardActivity) {
            ((ProviderDashboardActivity) getActivity()).hideFab();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Only refresh if data was already loaded (to avoid double loading on first creation)
        if (isDataLoaded && productList != null && productList.isEmpty()) {
            loadProducts(false);
        }
    }
    
    private void loadProducts(boolean isRefresh) {
        if (!isRefresh) {
            // Show loading indicator only on initial load
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }
        
        // Use the same endpoint as ProviderDashboardActivity
        Call<List<MenuItemResponse>> call = apiInterface.getProviderMenuItems();
        call.enqueue(new Callback<List<MenuItemResponse>>() {
            @Override
            public void onResponse(Call<List<MenuItemResponse>> call, Response<List<MenuItemResponse>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                try {
                    if (response.isSuccessful()) {
                        List<MenuItemResponse> menuItems = response.body();
                        if (menuItems != null && !menuItems.isEmpty()) {
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
                        } else if (response.code() == 401) {
                            errorMessage = "Authentication required. Please login again.";
                        }
                        if (isRefresh) {
                            ToastUtils.showError(getContext(), errorMessage);
                        }
                        updateEmptyState();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isRefresh) {
                        ToastUtils.showError(getContext(), 
                            "Error loading products: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                    }
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<MenuItemResponse>> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                // Network error or other failure
                String errorMessage = "Network error. Please check your connection.";
                if (t.getMessage() != null && !t.getMessage().isEmpty()) {
                    errorMessage = "Error: " + t.getMessage();
                }
                if (isRefresh) {
                    ToastUtils.showError(getContext(), errorMessage);
                }
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
        if (tvProductsCount != null) {
            int count = productList.size();
            tvProductsCount.setText("(" + count + " items)");
        }
    }
    
    private void updateEmptyState() {
        if (productList.isEmpty()) {
            if (emptyProductsLayout != null) {
                emptyProductsLayout.setVisibility(View.VISIBLE);
            }
            if (productsRecyclerView != null) {
                productsRecyclerView.setVisibility(View.GONE);
            }
        } else {
            if (emptyProductsLayout != null) {
                emptyProductsLayout.setVisibility(View.GONE);
            }
            if (productsRecyclerView != null) {
                productsRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}

