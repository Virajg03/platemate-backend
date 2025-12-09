package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeliveryPartnerOrderDetailActivity extends AppCompatActivity {
    
    private ImageView backButton;
    private ProgressBar progressBar;
    private LinearLayout contentLayout, emptyLayout;
    
    // Order Info
    private TextView tvOrderId, tvOrderDate, tvOrderStatus;
    private TextView tvProviderName, tvDeliveryAddress;
    
    // Items
    private RecyclerView rvOrderItems;
    private OrderItemsAdapter itemsAdapter;
    
    // Order Summary
    private TextView tvSubtotal, tvDeliveryFee, tvTotalAmount;
    
    // Actions
    private Button btnAcceptOrder, btnPickupOrder, btnDeliverOrder;
    private LinearLayout actionButtonsLayout;
    
    private ApiInterface apiInterface;
    private Long orderId;
    private Order currentOrder;
    private String orderType; // "ASSIGNED", "AVAILABLE"
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_partner_order_detail);
        
        orderId = getIntent().getLongExtra("orderId", -1);
        orderType = getIntent().getStringExtra("orderType");
        
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
        
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvProviderName = findViewById(R.id.tvProviderName);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        
        rvOrderItems = findViewById(R.id.rvOrderItems);
        
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        
        btnAcceptOrder = findViewById(R.id.btnAcceptOrder);
        btnPickupOrder = findViewById(R.id.btnPickupOrder);
        btnDeliverOrder = findViewById(R.id.btnDeliverOrder);
        actionButtonsLayout = findViewById(R.id.actionButtonsLayout);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        if (btnAcceptOrder != null) {
            btnAcceptOrder.setOnClickListener(v -> acceptOrder());
        }
        
        if (btnPickupOrder != null) {
            btnPickupOrder.setOnClickListener(v -> pickupOrder());
        }
        
        if (btnDeliverOrder != null) {
            btnDeliverOrder.setOnClickListener(v -> showOTPDialog());
        }
    }
    
    private void setupRecyclerView() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        itemsAdapter = new OrderItemsAdapter(new ArrayList<>());
        rvOrderItems.setAdapter(itemsAdapter);
    }
    
    private void loadOrderDetails() {
        showLoading(true);
        
        Call<Order> call;
        if ("AVAILABLE".equals(orderType)) {
            // For available orders, use customer endpoint (any authenticated user can view)
            call = apiInterface.getCustomerOrder(orderId);
        } else {
            // For assigned orders, use delivery partner endpoint
            call = apiInterface.getDeliveryPartnerOrder(orderId);
        }
        
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentOrder = response.body();
                    displayOrderDetails();
                    updateActionButtons();
                } else {
                    ToastUtils.showError(DeliveryPartnerOrderDetailActivity.this, "Failed to load order details");
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                showLoading(false);
                ToastUtils.showError(DeliveryPartnerOrderDetailActivity.this, "Error: " + t.getMessage());
                showEmptyState();
            }
        });
    }
    
    private void displayOrderDetails() {
        if (currentOrder == null) return;
        
        // Order ID
        if (currentOrder.getId() != null) {
            tvOrderId.setText("Order #" + currentOrder.getId());
        }
        
        // Order Date
        if (currentOrder.getOrderTime() != null) {
            tvOrderDate.setText(formatDate(currentOrder.getOrderTime()));
        }
        
        // Order Status
        if (currentOrder.getOrderStatus() != null) {
            tvOrderStatus.setText(currentOrder.getOrderStatus());
        }
        
        // Provider Name
        if (currentOrder.getProviderName() != null) {
            tvProviderName.setText(currentOrder.getProviderName());
        }
        
        // Delivery Address
        if (currentOrder.getDeliveryAddress() != null) {
            tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        }
        
        // Order Items
        if (currentOrder.getCartItems() != null) {
            itemsAdapter.updateItems(currentOrder.getCartItems());
        }
        
        // Order Summary
        if (currentOrder.getSubtotal() != null) {
            tvSubtotal.setText("₹" + String.format("%.2f", currentOrder.getSubtotal()));
        }
        
        if (currentOrder.getDeliveryFee() != null) {
            tvDeliveryFee.setText("₹" + String.format("%.2f", currentOrder.getDeliveryFee()));
        }
        
        if (currentOrder.getTotalAmount() != null) {
            tvTotalAmount.setText("₹" + String.format("%.2f", currentOrder.getTotalAmount()));
        }
        
        hideEmptyState();
    }
    
    private void updateActionButtons() {
        if (currentOrder == null || actionButtonsLayout == null) return;
        
        String status = currentOrder.getOrderStatus();
        
        // Hide all buttons first
        if (btnAcceptOrder != null) btnAcceptOrder.setVisibility(View.GONE);
        if (btnPickupOrder != null) btnPickupOrder.setVisibility(View.GONE);
        if (btnDeliverOrder != null) btnDeliverOrder.setVisibility(View.GONE);
        
        // Show appropriate button based on status and order type
        if ("AVAILABLE".equals(orderType) && status != null && status.equals("READY") && 
            currentOrder.getDeliveryPartnerId() == null) {
            // Show accept button for available orders
            if (btnAcceptOrder != null) {
                btnAcceptOrder.setVisibility(View.VISIBLE);
            }
        } else if (status != null && status.equals("READY") && currentOrder.getDeliveryPartnerId() != null) {
            // Show pickup button for ready orders assigned to this partner
            if (btnPickupOrder != null) {
                btnPickupOrder.setVisibility(View.VISIBLE);
            }
        } else if (status != null && status.equals("OUT_FOR_DELIVERY")) {
            // Show deliver button for orders out for delivery
            if (btnDeliverOrder != null) {
                btnDeliverOrder.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void acceptOrder() {
        if (orderId == null) return;
        
        new AlertDialog.Builder(this)
            .setTitle("Accept Order")
            .setMessage("Do you want to accept this order for delivery?")
            .setPositiveButton("Accept", (dialog, which) -> {
                showLoading(true);
                
                Call<Order> call = apiInterface.acceptOrder(orderId);
                call.enqueue(new Callback<Order>() {
                    @Override
                    public void onResponse(Call<Order> call, Response<Order> response) {
                        showLoading(false);
                        if (response.isSuccessful()) {
                            ToastUtils.showSuccess(DeliveryPartnerOrderDetailActivity.this, "Order accepted successfully");
                            // Reload order details
                            loadOrderDetails();
                            // Refresh parent activity
                            setResult(RESULT_OK);
                        } else {
                            ToastUtils.showError(DeliveryPartnerOrderDetailActivity.this, "Failed to accept order");
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Order> call, Throwable t) {
                        showLoading(false);
                        ToastUtils.showError(DeliveryPartnerOrderDetailActivity.this, "Error: " + t.getMessage());
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void pickupOrder() {
        if (orderId == null) return;
        
        new AlertDialog.Builder(this)
            .setTitle("Pick Up Order")
            .setMessage("Have you picked up this order from the provider?")
            .setPositiveButton("Yes, Picked Up", (dialog, which) -> {
                showLoading(true);
                
                Call<Order> call = apiInterface.pickupOrder(orderId);
                call.enqueue(new Callback<Order>() {
                    @Override
                    public void onResponse(Call<Order> call, Response<Order> response) {
                        showLoading(false);
                        if (response.isSuccessful()) {
                            ToastUtils.showSuccess(DeliveryPartnerOrderDetailActivity.this, "Order picked up successfully");
                            loadOrderDetails();
                            setResult(RESULT_OK);
                        } else {
                            ToastUtils.showError(DeliveryPartnerOrderDetailActivity.this, "Failed to update order");
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Order> call, Throwable t) {
                        showLoading(false);
                        ToastUtils.showError(DeliveryPartnerOrderDetailActivity.this, "Error: " + t.getMessage());
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showOTPDialog() {
        OTPVerificationDialog.show(this, new OTPVerificationDialog.OTPVerificationListener() {
            @Override
            public void onOTPVerified(String otp) {
                deliverOrder(otp);
            }
            
            @Override
            public void onCancel() {
                // User cancelled OTP entry
            }
        });
    }
    
    private void deliverOrder(String otp) {
        if (orderId == null) return;
        
        showLoading(true);
        
        Map<String, String> otpRequest = new HashMap<>();
        otpRequest.put("otp", otp);
        
        Call<Order> call = apiInterface.deliverOrder(orderId, otpRequest);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(DeliveryPartnerOrderDetailActivity.this, "Order delivered successfully!");
                    loadOrderDetails();
                    setResult(RESULT_OK);
                    // Finish activity after successful delivery
                    finish();
                } else {
                    ToastUtils.showError(DeliveryPartnerOrderDetailActivity.this, "Failed to deliver order. Please check OTP.");
                }
            }
            
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                showLoading(false);
                ToastUtils.showError(DeliveryPartnerOrderDetailActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    private String formatDate(String dateString) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateString);
            return date != null ? outputFormat.format(date) : dateString;
        } catch (Exception e) {
            return dateString;
        }
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (contentLayout != null) {
            contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
    private void showEmptyState() {
        if (emptyLayout != null) {
            emptyLayout.setVisibility(View.VISIBLE);
        }
        if (contentLayout != null) {
            contentLayout.setVisibility(View.GONE);
        }
    }
    
    private void hideEmptyState() {
        if (emptyLayout != null) {
            emptyLayout.setVisibility(View.GONE);
        }
        if (contentLayout != null) {
            contentLayout.setVisibility(View.VISIBLE);
        }
    }
}

