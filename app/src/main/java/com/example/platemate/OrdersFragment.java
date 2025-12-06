package com.example.platemate;

import android.content.Intent;
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

public class OrdersFragment extends Fragment {
    
    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private ApiInterface apiInterface;
    private LinearLayout emptyOrdersLayout;
    private TextView tvOrdersCount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isDataLoaded = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_provider_orders, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView);
        emptyOrdersLayout = view.findViewById(R.id.emptyOrdersLayout);
        tvOrdersCount = view.findViewById(R.id.tvOrdersCount);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeColors(
            getResources().getColor(R.color.login_orange, null)
        );
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadOrders(true);
        });
        
        // Initialize API
        apiInterface = RetrofitClient.getInstance(getContext()).getApi();
        
        // Setup RecyclerView
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, order -> {
            // Handle order click - navigate to order details
            Intent intent = new Intent(getContext(), OrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            startActivity(intent);
        });
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersRecyclerView.setAdapter(orderAdapter);
        
        // Setup scroll listener for FAB visibility (even though FAB is hidden on orders tab)
        setupScrollListener();
        
        // Load orders immediately when fragment is created
        if (!isDataLoaded) {
            loadOrders(false);
            isDataLoaded = true;
        }
    }
    
    private int lastScrollY = 0;
    private boolean isScrollingDown = false;
    
    private void setupScrollListener() {
        ordersRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        // FAB is hidden on orders tab, but we still handle it for consistency
        if (getActivity() instanceof ProviderDashboardActivity) {
            // Don't show FAB on orders tab
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
        if (isDataLoaded && orderList != null && orderList.isEmpty()) {
            loadOrders(false);
        }
    }
    
    private void loadOrders(boolean isRefresh) {
        if (!isRefresh) {
            // Show loading indicator only on initial load
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }
        
        // Use the provider orders endpoint
        Call<List<Order>> call = apiInterface.getProviderOrders();
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                try {
                    if (response.isSuccessful()) {
                        List<Order> orders = response.body();
                        if (orders != null && !orders.isEmpty()) {
                            orderList.clear();
                            orderList.addAll(orders);
                            orderAdapter.updateOrders(orderList);
                            updateOrderCount();
                            updateEmptyState();
                        } else {
                            // Empty response - no orders yet
                            orderList.clear();
                            orderAdapter.updateOrders(orderList);
                            updateOrderCount();
                            updateEmptyState();
                        }
                    } else {
                        // Handle error response
                        String errorMessage = "Failed to load orders";
                        if (response.code() == 403) {
                            errorMessage = "Provider not verified yet. Please wait for admin approval.";
                        } else if (response.code() == 401) {
                            errorMessage = "Authentication required. Please login again.";
                        } else if (response.code() == 404) {
                            // Endpoint might not exist yet, show empty state
                            orderList.clear();
                            orderAdapter.updateOrders(orderList);
                            updateOrderCount();
                            updateEmptyState();
                            if (isRefresh) {
                                ToastUtils.showError(getContext(), "Orders endpoint not available");
                            }
                            return;
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
                            "Error loading orders: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                    }
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
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
    
    private void updateOrderCount() {
        if (tvOrdersCount != null) {
            int count = orderList.size();
            tvOrdersCount.setText("(" + count + " orders)");
        }
    }
    
    private void updateEmptyState() {
        if (orderList.isEmpty()) {
            if (emptyOrdersLayout != null) {
                emptyOrdersLayout.setVisibility(View.VISIBLE);
            }
            if (ordersRecyclerView != null) {
                ordersRecyclerView.setVisibility(View.GONE);
            }
        } else {
            if (emptyOrdersLayout != null) {
                emptyOrdersLayout.setVisibility(View.GONE);
            }
            if (ordersRecyclerView != null) {
                ordersRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}

