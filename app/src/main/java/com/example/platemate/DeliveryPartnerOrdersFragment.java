package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class DeliveryPartnerOrdersFragment extends Fragment {
    
    private RecyclerView rvOrders;
    private LinearLayout emptyOrderLayout;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvFragmentTitle;
    
    private ApiInterface apiInterface;
    private OrderAdapter orderAdapter;
    private List<Order> allOrders = new ArrayList<>();
    
    private String orderType; // "ASSIGNED", "AVAILABLE", "COMPLETED"
    
    public static DeliveryPartnerOrdersFragment newInstance(String orderType) {
        DeliveryPartnerOrdersFragment fragment = new DeliveryPartnerOrdersFragment();
        Bundle args = new Bundle();
        args.putString("orderType", orderType);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delivery_partner_orders, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            orderType = getArguments().getString("orderType", "ASSIGNED");
        }
        
        apiInterface = RetrofitClient.getInstance(requireContext()).getApi();
        
        initializeViews(view);
        setupRecyclerView();
        loadOrders();
    }
    
    private void initializeViews(View view) {
        rvOrders = view.findViewById(R.id.rvOrders);
        emptyOrderLayout = view.findViewById(R.id.emptyOrderLayout);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        tvFragmentTitle = view.findViewById(R.id.tvFragmentTitle);
        
        // Set title based on order type
        if (tvFragmentTitle != null) {
            switch (orderType) {
                case "ASSIGNED":
                    tvFragmentTitle.setText("My Orders");
                    break;
                case "AVAILABLE":
                    tvFragmentTitle.setText("Available Orders");
                    break;
                case "COMPLETED":
                    tvFragmentTitle.setText("Completed Orders");
                    break;
            }
        }
        
        // Setup SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadOrders();
            });
            
            swipeRefreshLayout.setColorSchemeResources(
                R.color.login_orange,
                android.R.color.holo_orange_dark,
                android.R.color.holo_orange_light
            );
        }
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
            Intent intent = new Intent(requireContext(), DeliveryPartnerOrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            intent.putExtra("orderType", orderType);
            startActivity(intent);
        });
        rvOrders.setAdapter(orderAdapter);
    }
    
    public void loadOrders() {
        // Don't show progress bar if refreshing
        if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
            showLoading(true);
        }
        
        Call<List<Order>> call;
        
        switch (orderType) {
            case "ASSIGNED":
                call = apiInterface.getDeliveryPartnerOrders();
                break;
            case "AVAILABLE":
                call = apiInterface.getAvailableOrdersForDelivery();
                break;
            case "COMPLETED":
                call = apiInterface.getDeliveryPartnerOrders(); // Filter by DELIVERED status
                break;
            default:
                call = apiInterface.getDeliveryPartnerOrders();
                break;
        }
        
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                showLoading(false);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body();
                    Log.d("DeliveryPartnerOrders", "Received " + allOrders.size() + " orders for type: " + orderType);
                    
                    // Filter orders based on type
                    List<Order> filteredOrders = filterOrdersByType(allOrders);
                    Log.d("DeliveryPartnerOrders", "Filtered to " + filteredOrders.size() + " orders");
                    orderAdapter.updateOrders(filteredOrders);
                    
                    if (filteredOrders.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                    }
                } else {
                    String errorMsg = "Failed to load orders";
                    if (response.code() == 401) {
                        errorMsg = "Authentication failed. Please login again.";
                        Log.e("DeliveryPartnerOrders", "Authentication error - 401 Unauthorized");
                    } else if (response.code() == 403) {
                        errorMsg = "Access denied. You may not have permission to view orders.";
                        Log.e("DeliveryPartnerOrders", "Access denied - 403 Forbidden");
                    } else if (response.code() == 404) {
                        errorMsg = "Orders endpoint not found. Please contact support.";
                        Log.e("DeliveryPartnerOrders", "Endpoint not found - 404");
                    } else {
                        Log.e("DeliveryPartnerOrders", "Failed to load orders - HTTP " + response.code());
                    }
                    ToastUtils.showError(requireContext(), errorMsg);
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                showLoading(false);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Log.e("DeliveryPartnerOrders", "Network error loading orders for type: " + orderType, t);
                String errorMsg = "Network error: " + (t.getMessage() != null ? t.getMessage() : "Unable to connect to server");
                ToastUtils.showError(requireContext(), errorMsg);
                showEmptyState();
            }
        });
    }
    
    private List<Order> filterOrdersByType(List<Order> orders) {
        List<Order> filtered = new ArrayList<>();
        
        for (Order order : orders) {
            String status = order.getOrderStatus();
            
            switch (orderType) {
                case "ASSIGNED":
                    // Show orders assigned to this delivery partner (READY, OUT_FOR_DELIVERY)
                    if (order.getDeliveryPartnerId() != null && 
                        (status != null && (status.equals("READY") || status.equals("OUT_FOR_DELIVERY")))) {
                        filtered.add(order);
                    }
                    break;
                case "AVAILABLE":
                    // Show orders without delivery partner assigned (READY status)
                    if (order.getDeliveryPartnerId() == null && 
                        status != null && status.equals("READY")) {
                        filtered.add(order);
                    }
                    break;
                case "COMPLETED":
                    // Show completed/delivered orders
                    if (status != null && status.equals("DELIVERED")) {
                        filtered.add(order);
                    }
                    break;
            }
        }
        
        return filtered;
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState() {
        if (emptyOrderLayout != null) {
            emptyOrderLayout.setVisibility(View.VISIBLE);
        }
        rvOrders.setVisibility(View.GONE);
    }
    
    private void hideEmptyState() {
        if (emptyOrderLayout != null) {
            emptyOrderLayout.setVisibility(View.GONE);
        }
        rvOrders.setVisibility(View.VISIBLE);
    }
    
    // Removed automatic refresh on resume - data loads once when fragment is created
    // Users can refresh manually using swipe-to-refresh
}

