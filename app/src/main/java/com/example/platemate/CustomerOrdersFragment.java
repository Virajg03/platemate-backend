package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrdersFragment extends Fragment {
    
    private RecyclerView rvOrders;
    private LinearLayout emptyOrderLayout;
    private ProgressBar progressBar;
    private Button btnFilterAll, btnFilterPending, btnFilterDelivered, btnFilterCancelled;
    private ImageView backButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private ApiInterface apiInterface;
    private OrderAdapter orderAdapter;
    private List<Order> allOrders = new ArrayList<>();
    private String currentFilter = "ALL";
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_order_history, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        apiInterface = RetrofitClient.getInstance(requireContext()).getApi();
        
        initializeViews(view);
        setupClickListeners();
        setupRecyclerView();
        loadOrders();
    }
    
    private void initializeViews(View view) {
        rvOrders = view.findViewById(R.id.rvOrders);
        emptyOrderLayout = view.findViewById(R.id.emptyOrderLayout);
        backButton = view.findViewById(R.id.backButton);
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterPending = view.findViewById(R.id.btnFilterPending);
        btnFilterDelivered = view.findViewById(R.id.btnFilterDelivered);
        btnFilterCancelled = view.findViewById(R.id.btnFilterCancelled);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // Create progress bar if not in layout
        progressBar = view.findViewById(android.R.id.progress);
        if (progressBar == null) {
            // Add progress bar programmatically if needed
        }
        
        // Setup SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadOrders();
            });
            
            // Configure refresh colors
            swipeRefreshLayout.setColorSchemeResources(
                R.color.login_orange,
                android.R.color.holo_orange_dark,
                android.R.color.holo_orange_light
            );
        }
    }
    
    private void setupClickListeners() {
        // Back button - navigate to home
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigationView);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_home);
                    }
                }
            });
        }
        
        // Filter buttons
        btnFilterAll.setOnClickListener(v -> filterOrders("ALL"));
        btnFilterPending.setOnClickListener(v -> filterOrders("PENDING"));
        btnFilterDelivered.setOnClickListener(v -> filterOrders("DELIVERED"));
        btnFilterCancelled.setOnClickListener(v -> filterOrders("CANCELLED"));
    }
    
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvOrders.setLayoutManager(layoutManager);
        
        // Optimize RecyclerView performance
        rvOrders.setHasFixedSize(true);
        rvOrders.setItemViewCacheSize(20);
        rvOrders.setDrawingCacheEnabled(true);
        rvOrders.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        
        orderAdapter = new OrderAdapter(new ArrayList<>(), order -> {
            // Navigate to order detail activity
            Intent intent = new Intent(requireContext(), OrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            startActivity(intent);
        });
        rvOrders.setAdapter(orderAdapter);
    }
    
    private void loadOrders() {
        // Don't show progress bar if refreshing (swipe refresh has its own indicator)
        if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
            showLoading(true);
        }
        
        Call<List<Order>> call = apiInterface.getCustomerOrders();
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                showLoading(false);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body();
                    filterOrders(currentFilter);
                } else {
                    ToastUtils.showError(requireContext(), "Failed to load orders");
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                showLoading(false);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                ToastUtils.showError(requireContext(), "Error: " + t.getMessage());
                showEmptyState();
            }
        });
    }
    
    private void filterOrders(String filter) {
        currentFilter = filter;
        updateFilterButtons();
        
        List<Order> filteredOrders = new ArrayList<>();
        
        if ("ALL".equals(filter)) {
            filteredOrders = new ArrayList<>(allOrders);
        } else {
            for (Order order : allOrders) {
                if (filter.equalsIgnoreCase(order.getOrderStatus())) {
                    filteredOrders.add(order);
                }
            }
        }
        
        orderAdapter.updateOrders(filteredOrders);
        
        if (filteredOrders.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }
    
    private void updateFilterButtons() {
        // Reset all buttons
        btnFilterAll.setBackgroundResource(R.drawable.neubrutal_button);
        btnFilterPending.setBackgroundResource(R.drawable.neubrutal_button);
        btnFilterDelivered.setBackgroundResource(R.drawable.neubrutal_button);
        btnFilterCancelled.setBackgroundResource(R.drawable.neubrutal_button);
        
        // Highlight selected button
        switch (currentFilter) {
            case "ALL":
                btnFilterAll.setBackgroundResource(R.drawable.neubrutal_button_selected);
                break;
            case "PENDING":
                btnFilterPending.setBackgroundResource(R.drawable.neubrutal_button_selected);
                break;
            case "DELIVERED":
                btnFilterDelivered.setBackgroundResource(R.drawable.neubrutal_button_selected);
                break;
            case "CANCELLED":
                btnFilterCancelled.setBackgroundResource(R.drawable.neubrutal_button_selected);
                break;
        }
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState() {
        emptyOrderLayout.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
    }
    
    private void hideEmptyState() {
        emptyOrderLayout.setVisibility(View.GONE);
        rvOrders.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload orders when fragment resumes (in case order status changed)
        loadOrders();
    }
}
