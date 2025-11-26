package com.example.platemate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    
    private ImageView backButton;
    private ProgressBar progressBar;
    private LinearLayout contentLayout, emptyLayout;
    
    // Order Info
    private TextView tvOrderId, tvOrderDate, tvOrderStatus;
    
    // Provider Info
    private TextView tvProviderName;
    
    // Delivery Info
    private TextView tvDeliveryAddress, tvDeliveryPartner, tvEstimatedDelivery, tvDeliveryTime;
    
    // Items
    private RecyclerView rvOrderItems;
    private OrderItemsAdapter itemsAdapter;
    
    // Payment Info
    private TextView tvPaymentMethod, tvPaymentStatus, tvPaymentType, tvTransactionId, tvPaymentTime;
    
    // Order Summary
    private TextView tvSubtotal, tvDeliveryFee, tvPlatformCommission, tvTotalAmount;
    
    // Actions
    private Button btnCancelOrder;
    
    private ApiInterface apiInterface;
    private Long orderId;
    private Order currentOrder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        
        orderId = getIntent().getLongExtra("orderId", -1);
        if (orderId == -1) {
            ToastUtils.showError(this, "Invalid order ID");
            finish();
            return;
        }
        
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        loadOrderDetails();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);
        contentLayout = findViewById(R.id.contentLayout);
        emptyLayout = findViewById(R.id.emptyLayout);
        
        // Order Info
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        
        // Provider Info
        tvProviderName = findViewById(R.id.tvProviderName);
        
        // Delivery Info
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvDeliveryPartner = findViewById(R.id.tvDeliveryPartner);
        tvEstimatedDelivery = findViewById(R.id.tvEstimatedDelivery);
        tvDeliveryTime = findViewById(R.id.tvDeliveryTime);
        
        // Items
        rvOrderItems = findViewById(R.id.rvOrderItems);
        
        // Payment Info
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentType = findViewById(R.id.tvPaymentType);
        tvTransactionId = findViewById(R.id.tvTransactionId);
        tvPaymentTime = findViewById(R.id.tvPaymentTime);
        
        // Order Summary
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvPlatformCommission = findViewById(R.id.tvPlatformCommission);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        
        // Actions
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        if (btnCancelOrder != null) {
            btnCancelOrder.setOnClickListener(v -> {
                if (currentOrder != null) {
                    cancelOrder();
                }
            });
        }
    }
    
    private void setupRecyclerView() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        itemsAdapter = new OrderItemsAdapter(new ArrayList<>());
        rvOrderItems.setAdapter(itemsAdapter);
    }
    
    private void loadOrderDetails() {
        showLoading(true);
        
        Call<Order> call = apiInterface.getCustomerOrder(orderId);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentOrder = response.body();
                    displayOrderDetails(currentOrder);
                    loadPaymentDetails(); // Load payment details separately
                } else {
                    ToastUtils.showError(OrderDetailActivity.this, "Failed to load order details");
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                showLoading(false);
                ToastUtils.showError(OrderDetailActivity.this, "Error: " + t.getMessage());
                showEmptyState();
            }
        });
    }
    
    private void loadPaymentDetails() {
        // TODO: Add API endpoint to get payment details for an order
        // For now, we'll show payment info based on order data
        // In the future, you can add: @GET("/api/customers/orders/{id}/payment")
    }
    
    private void displayOrderDetails(Order order) {
        contentLayout.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
        
        // Order Info
        if (order.getId() != null) {
            tvOrderId.setText("Order #" + order.getId());
        }
        
        if (order.getOrderTime() != null) {
            tvOrderDate.setText(formatDate(order.getOrderTime()));
        }
        
        if (order.getOrderStatus() != null) {
            tvOrderStatus.setText(order.getOrderStatus());
            tvOrderStatus.setTextColor(getStatusColor(order.getOrderStatus()));
        }
        
        // Provider Info
        if (order.getProviderName() != null) {
            tvProviderName.setText(order.getProviderName());
        }
        
        // Delivery Info
        if (order.getDeliveryAddress() != null) {
            tvDeliveryAddress.setText(order.getDeliveryAddress());
        }
        
        if (order.getDeliveryPartnerName() != null) {
            tvDeliveryPartner.setText(order.getDeliveryPartnerName());
            findViewById(R.id.deliveryPartnerLayout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.deliveryPartnerLayout).setVisibility(View.GONE);
        }
        
        if (order.getEstimatedDeliveryTime() != null) {
            tvEstimatedDelivery.setText(formatDate(order.getEstimatedDeliveryTime()));
            findViewById(R.id.estimatedDeliveryLayout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.estimatedDeliveryLayout).setVisibility(View.GONE);
        }
        
        if (order.getDeliveryTime() != null) {
            tvDeliveryTime.setText(formatDate(order.getDeliveryTime()));
            findViewById(R.id.deliveryTimeLayout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.deliveryTimeLayout).setVisibility(View.GONE);
        }
        
        // Order Items
        if (order.getCartItems() != null && !order.getCartItems().isEmpty()) {
            itemsAdapter.updateItems(order.getCartItems());
        }
        
        // Order Summary
        if (order.getSubtotal() != null) {
            tvSubtotal.setText("₹" + String.format("%.2f", order.getSubtotal()));
        }
        
        if (order.getDeliveryFee() != null) {
            tvDeliveryFee.setText("₹" + String.format("%.2f", order.getDeliveryFee()));
        }
        
        if (order.getPlatformCommission() != null) {
            tvPlatformCommission.setText("₹" + String.format("%.2f", order.getPlatformCommission()));
        }
        
        if (order.getTotalAmount() != null) {
            tvTotalAmount.setText("₹" + String.format("%.2f", order.getTotalAmount()));
        }
        
        // Cancel Button - only show for PENDING or CONFIRMED orders
        if (btnCancelOrder != null) {
            String status = order.getOrderStatus();
            if (status != null && (status.equals("PENDING") || status.equals("CONFIRMED"))) {
                btnCancelOrder.setVisibility(View.VISIBLE);
            } else {
                btnCancelOrder.setVisibility(View.GONE);
            }
        }
    }
    
    private void cancelOrder() {
        if (orderId == null) return;
        
        Call<Order> call = apiInterface.cancelOrder(orderId);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ToastUtils.showSuccess(OrderDetailActivity.this, "Order cancelled successfully");
                    currentOrder = response.body();
                    displayOrderDetails(currentOrder); // Refresh display
                } else {
                    String errorMsg = "Failed to cancel order";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(OrderDetailActivity.this, errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                ToastUtils.showError(OrderDetailActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    private String formatDate(String dateString) {
        try {
            // Parse ISO 8601 format from backend
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            // If parsing fails, return original string
        }
        return dateString;
    }
    
    private int getStatusColor(String status) {
        switch (status.toUpperCase()) {
            case "PENDING":
                return getColor(R.color.orange);
            case "CONFIRMED":
            case "PREPARING":
                return getColor(R.color.blue);
            case "READY":
            case "OUT_FOR_DELIVERY":
                return getColor(R.color.purple);
            case "DELIVERED":
                return getColor(R.color.green);
            case "CANCELLED":
                return getColor(R.color.red);
            default:
                return getColor(R.color.black);
        }
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState() {
        contentLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }
}

