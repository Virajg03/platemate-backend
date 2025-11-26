package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity implements PaymentResultListener {
    
    private RecyclerView rvOrderItems;
    private TextView tvFullAddress, tvSubtotal, tvDeliveryFee, tvTax, tvTotal;
    private LinearLayout addressLayout, noAddressLayout;
    private Button btnAddEditAddress, btnEditAddress, btnPlaceOrder;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbRazorpay;
    private Spinner spinnerDeliveryTime;
    private EditText etSpecialInstructions;
    private ImageView backButton;
    
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    private List<CartItem> cartItems;
    private CheckoutAdapter checkoutAdapter;
    
    private static final double DELIVERY_FEE = 30.0;
    private static final double TAX_RATE = 0.05; // 5% GST
    
    private String selectedPaymentMethod = "CASH"; // Default to COD
    private double orderTotal = 0.0;
    private Long createdOrderId = null;
    private String storedRazorpayOrderId = null; // Store Razorpay order ID for verification
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        
        // Initialize Razorpay
        Checkout.preload(getApplicationContext());
        
        sessionManager = new SessionManager(this);
        apiInterface = RetrofitClient.getInstance(this).getApi();
        cartItems = new ArrayList<>();
        
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadCartItems();
        setupAddressDisplay();
        setupDeliveryTimeSpinner();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        tvFullAddress = findViewById(R.id.tvFullAddress);
        addressLayout = findViewById(R.id.addressLayout);
        noAddressLayout = findViewById(R.id.noAddressLayout);
        btnAddEditAddress = findViewById(R.id.btnAddEditAddress);
        btnEditAddress = findViewById(R.id.btnEditAddress);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCOD = findViewById(R.id.rbCOD);
        rbRazorpay = findViewById(R.id.rbRazorpay);
        spinnerDeliveryTime = findViewById(R.id.spinnerDeliveryTime);
        etSpecialInstructions = findViewById(R.id.etSpecialInstructions);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
    }
    
    private void setupRecyclerView() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        checkoutAdapter = new CheckoutAdapter(cartItems);
        rvOrderItems.setAdapter(checkoutAdapter);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        btnAddEditAddress.setOnClickListener(v -> showAddressDialog());
        btnEditAddress.setOnClickListener(v -> showAddressDialog());
        
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCOD) {
                selectedPaymentMethod = "CASH";
            } else if (checkedId == R.id.rbRazorpay) {
                selectedPaymentMethod = "UPI"; // Razorpay handles UPI/Card/Wallet
            }
        });
        
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }
    
    private void setupAddressDisplay() {
        if (sessionManager.hasDeliveryAddress()) {
            String fullAddress = sessionManager.getFullDeliveryAddress();
            tvFullAddress.setText(fullAddress);
            addressLayout.setVisibility(View.VISIBLE);
            noAddressLayout.setVisibility(View.GONE);
            btnAddEditAddress.setText("CHANGE ADDRESS");
        } else {
            addressLayout.setVisibility(View.GONE);
            noAddressLayout.setVisibility(View.VISIBLE);
            btnAddEditAddress.setText("ADD ADDRESS");
        }
    }
    
    private void showAddressDialog() {
        Address existingAddress = null;
        if (sessionManager.hasDeliveryAddress()) {
            existingAddress = new Address();
            existingAddress.setStreet(sessionManager.getDeliveryStreet());
            existingAddress.setCity(sessionManager.getDeliveryCity());
            existingAddress.setState(sessionManager.getDeliveryState());
            existingAddress.setZipCode(sessionManager.getDeliveryZipCode());
        }
        
        AddressDialog.show(CheckoutActivity.this, existingAddress, new AddressDialog.AddressDialogListener() {
            @Override
            public void onAddressSaved(String street, String city, String state, String zipCode) {
                // Address is saved to backend by AddressDialog
                // Update display
                setupAddressDisplay();
            }
            
            @Override
            public void onCancel() {
                // User cancelled
            }
        });
    }
    
    private void setupDeliveryTimeSpinner() {
        String[] timeSlots = {
            "30-45 minutes",
            "45-60 minutes",
            "1-2 hours",
            "2-3 hours"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeliveryTime.setAdapter(adapter);
    }
    
    private void loadCartItems() {
        Call<CartSummary> call = apiInterface.getCart();
        call.enqueue(new Callback<CartSummary>() {
            @Override
            public void onResponse(Call<CartSummary> call, Response<CartSummary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CartSummary cartSummary = response.body();
                    if (cartSummary.getItems() != null) {
                        cartItems.clear();
                        cartItems.addAll(cartSummary.getItems());
                        checkoutAdapter.notifyDataSetChanged();
                        updateOrderSummary(cartSummary.getSubtotal());
                    }
                } else {
                    ToastUtils.showError(CheckoutActivity.this, "Failed to load cart items");
                    finish();
                }
            }
            
            @Override
            public void onFailure(Call<CartSummary> call, Throwable t) {
                ToastUtils.showError(CheckoutActivity.this, "Error: " + t.getMessage());
                finish();
            }
        });
    }
    
    private void updateOrderSummary(Double subtotal) {
        if (subtotal == null) subtotal = 0.0;
        
        double deliveryFee = DELIVERY_FEE;
        double tax = subtotal * TAX_RATE;
        orderTotal = subtotal + deliveryFee + tax;
        
        tvSubtotal.setText("₹" + String.format("%.2f", subtotal));
        tvDeliveryFee.setText("₹" + String.format("%.2f", deliveryFee));
        tvTax.setText("₹" + String.format("%.2f", tax));
        tvTotal.setText("₹" + String.format("%.2f", orderTotal));
    }
    
    private void placeOrder() {
        // Validate address
        if (!sessionManager.hasDeliveryAddress()) {
            ToastUtils.showInfo(this, "Please add a delivery address");
            showAddressDialog();
            return;
        }
        
        // Get cart item IDs
        List<Long> cartItemIds = new ArrayList<>();
        for (CartItem item : cartItems) {
            cartItemIds.add(item.getId());
        }
        
        if (cartItemIds.isEmpty()) {
            ToastUtils.showInfo(this, "Your cart is empty");
            finish();
            return;
        }
        
        String deliveryAddress = sessionManager.getFullDeliveryAddress();
        
        // Create order request
        CreateOrderRequest request = new CreateOrderRequest(
            cartItemIds, 
            deliveryAddress, 
            DELIVERY_FEE,
            selectedPaymentMethod
        );
        
        // Create order first
        Call<Order> createOrderCall = apiInterface.createOrder(request);
        createOrderCall.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    createdOrderId = order.getId();
                    
                    // Handle payment based on selected method
                    if ("CASH".equals(selectedPaymentMethod)) {
                        // COD - Order is already created, just show success
                        handleOrderSuccess(order);
                    } else {
                        // Razorpay payment
                        initiateRazorpayPayment(order.getId());
                    }
                } else {
                    String errorMessage = "Failed to create order";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                            // Check for multiple providers error
                            if (errorMessage.contains("same provider") || errorMessage.contains("All cart items must be")) {
                                errorMessage = "All items in your cart must be from the same provider. Please remove items from other providers or create separate orders.";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(CheckoutActivity.this, errorMessage);
                }
            }
            
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                ToastUtils.showError(CheckoutActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    private void initiateRazorpayPayment(Long orderId) {
        // Get Razorpay order ID from backend
        Call<PaymentOrderResponse> paymentCall = apiInterface.createPaymentOrder(orderId);
        paymentCall.enqueue(new Callback<PaymentOrderResponse>() {
            @Override
            public void onResponse(Call<PaymentOrderResponse> call, Response<PaymentOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PaymentOrderResponse paymentResponse = response.body();
                    String razorpayOrderId = paymentResponse.getRazorpayOrderId();
                    
                    // Store razorpayOrderId for verification
                    storedRazorpayOrderId = razorpayOrderId;
                    
                    // Start Razorpay checkout
                    startRazorpayCheckout(razorpayOrderId);
                } else {
                    ToastUtils.showError(CheckoutActivity.this, "Failed to initialize payment");
                }
            }
            
            @Override
            public void onFailure(Call<PaymentOrderResponse> call, Throwable t) {
                ToastUtils.showError(CheckoutActivity.this, "Payment error: " + t.getMessage());
            }
        });
    }
    
    
    private void startRazorpayCheckout(String razorpayOrderId) {
        Checkout checkout = new Checkout();
        // Razorpay Key ID - must match the key ID in your backend application.properties
        checkout.setKeyID("rzp_test_RkT4jplwd1NaBg");
        
        try {
            JSONObject options = new JSONObject();
            options.put("name", "PlateMate");
            options.put("description", "Order Payment");
            options.put("order_id", razorpayOrderId);
            options.put("theme.color", "#FF6B35");
            options.put("currency", "INR");
            options.put("amount", (int)(orderTotal * 100)); // Amount in paise
            options.put("prefill.email", sessionManager.getUsername());
            options.put("prefill.contact", ""); // Add phone if available
            
            checkout.open(this, options);
        } catch (Exception e) {
            ToastUtils.showError(this, "Error in payment: " + e.getMessage());
        }
    }
    
    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        // Payment successful - verify with backend
        if (createdOrderId != null && storedRazorpayOrderId != null) {
            // Create verification request
            VerifyPaymentRequest verifyRequest = new VerifyPaymentRequest(
                razorpayPaymentID,
                storedRazorpayOrderId,
                ""  // Signature will be verified by webhook, but we mark payment as success here
            );
            
            Call<Order> verifyCall = apiInterface.verifyPayment(createdOrderId, verifyRequest);
            verifyCall.enqueue(new Callback<Order>() {
                @Override
                public void onResponse(Call<Order> call, Response<Order> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        handleOrderSuccess(response.body());
                    } else {
                        String errorMsg = "Payment verification failed";
                        if (response.errorBody() != null) {
                            try {
                                errorMsg = response.errorBody().string();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        ToastUtils.showError(CheckoutActivity.this, errorMsg);
                    }
                }
                
                @Override
                public void onFailure(Call<Order> call, Throwable t) {
                    ToastUtils.showError(CheckoutActivity.this, "Verification error: " + t.getMessage());
                }
            });
        } else {
            ToastUtils.showError(this, "Payment verification failed: Missing order information");
        }
    }
    
    @Override
    public void onPaymentError(int code, String response) {
        ToastUtils.showError(this, "Payment failed: " + response);
        // Order is already created, but payment failed
        // Backend should handle this (order status = PENDING or FAILED)
    }
    
    private void handleOrderSuccess(Order order) {
        ToastUtils.showSuccess(this, "Order placed successfully!");
        
        // Navigate to order details or home
        Intent intent = new Intent(CheckoutActivity.this, CustomerHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    
    // Simple adapter for checkout items
    private static class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {
        private List<CartItem> items;
        
        public CheckoutAdapter(List<CartItem> items) {
            this.items = items;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CartItem item = items.get(position);
            holder.text1.setText(item.getItemName());
            holder.text2.setText("Qty: " + item.getQuantity() + " × ₹" + 
                String.format("%.2f", item.getItemPrice()) + " = ₹" + 
                String.format("%.2f", item.getItemTotal()));
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            
            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}

