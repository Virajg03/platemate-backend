package com.example.platemate;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    
    private RecyclerView rvCartItems;
    private LinearLayout emptyCartLayout;
    private TextView tvSubtotal, tvDeliveryFee, tvTax, tvTotal;
    private Button btnProceedToCheckout;
    private ImageView backButton;
    private ProgressBar progressBar;
    
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    
    private static final double DELIVERY_FEE = 30.0;
    private static final double TAX_RATE = 0.05; // 5% GST
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        
        sessionManager = new SessionManager(this);
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadCart();
    }
    
    private void initializeViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        btnProceedToCheckout = findViewById(R.id.btnProceedToCheckout);
        backButton = findViewById(R.id.backButton);
        cartItems = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems);
        cartAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                updateCartItem(item, newQuantity);
            }
            
            @Override
            public void onRemoveClick(CartItem item) {
                removeCartItem(item);
            }
            
            @Override
            public void onItemClick(CartItem item) {
                // Navigate to product detail page
                if (item.getMenuItemId() != null) {
                    Intent intent = new Intent(CartActivity.this, ProductDetailActivity.class);
                    intent.putExtra("menuItemId", item.getMenuItemId());
                    startActivity(intent);
                }
            }
        });
        rvCartItems.setAdapter(cartAdapter);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        btnProceedToCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                ToastUtils.showInfo(this, "Your cart is empty");
                return;
            }
            proceedToCheckout();
        });
    }
    
    private void loadCart() {
        Call<CartSummary> call = apiInterface.getCart();
        call.enqueue(new Callback<CartSummary>() {
            @Override
            public void onResponse(Call<CartSummary> call, Response<CartSummary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CartSummary cartSummary = response.body();
                    if (cartSummary.getItems() != null) {
                        cartItems.clear();
                        cartItems.addAll(cartSummary.getItems());
                        cartAdapter.notifyDataSetChanged();
                        updateOrderSummary(cartSummary.getSubtotal());
                        showEmptyState(cartItems.isEmpty());
                    }
                } else {
                    ToastUtils.showError(CartActivity.this, "Failed to load cart");
                    showEmptyState(true);
                }
            }
            
            @Override
            public void onFailure(Call<CartSummary> call, Throwable t) {
                ToastUtils.showError(CartActivity.this, "Error: " + t.getMessage());
                showEmptyState(true);
            }
        });
    }
    
    private void updateCartItem(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            removeCartItem(item);
            return;
        }
        
        UpdateCartRequest request = new UpdateCartRequest(newQuantity, item.getSpecialInstructions());
        Call<CartItem> call = apiInterface.updateCartItem(item.getId(), request);
        call.enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful()) {
                    loadCart(); // Reload cart to get updated totals
                } else {
                    ToastUtils.showError(CartActivity.this, "Failed to update cart");
                    loadCart(); // Reload to revert changes
                }
            }
            
            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                ToastUtils.showError(CartActivity.this, "Error: " + t.getMessage());
                loadCart(); // Reload to revert changes
            }
        });
    }
    
    private void removeCartItem(CartItem item) {
        Call<Void> call = apiInterface.removeCartItem(item.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(CartActivity.this, "Item removed from cart");
                    loadCart();
                } else {
                    ToastUtils.showError(CartActivity.this, "Failed to remove item");
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                ToastUtils.showError(CartActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    private void updateOrderSummary(Double subtotal) {
        if (subtotal == null) subtotal = 0.0;
        
        // Only show delivery fee if cart is not empty
        boolean isEmpty = cartItems.isEmpty();
        double deliveryFee = isEmpty ? 0.0 : DELIVERY_FEE;
        double tax = subtotal * TAX_RATE;
        double total = subtotal + deliveryFee + tax;
        
        tvSubtotal.setText("₹" + String.format("%.2f", subtotal));
        
        // Hide delivery fee section if cart is empty
        View deliveryFeeLayout = findViewById(R.id.deliveryFeeLayout);
        if (deliveryFeeLayout != null) {
            deliveryFeeLayout.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        
        tvDeliveryFee.setText("₹" + String.format("%.2f", deliveryFee));
        tvTax.setText("₹" + String.format("%.2f", tax));
        tvTotal.setText("₹" + String.format("%.2f", total));
    }
    
    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvCartItems.setVisibility(View.GONE);
            emptyCartLayout.setVisibility(View.VISIBLE);
            btnProceedToCheckout.setEnabled(false);
            btnProceedToCheckout.setAlpha(0.5f);
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            emptyCartLayout.setVisibility(View.GONE);
            btnProceedToCheckout.setEnabled(true);
            btnProceedToCheckout.setAlpha(1.0f);
        }
    }
    
    private void proceedToCheckout() {
        // Check if user has delivery address
        String deliveryAddress = sessionManager.getFullDeliveryAddress();
        
        if (deliveryAddress == null || deliveryAddress.isEmpty()) {
            // Show address dialog
            showAddressDialog();
        } else {
            // Proceed with order creation
            createOrder(deliveryAddress);
        }
    }
    
    private void showAddressDialog() {
        // Get address from session manager (address is loaded with user profile)
        Address existingAddress = null;
        SessionManager sm = new SessionManager(this);
        if (sm.hasDeliveryAddress()) {
            existingAddress = new Address();
            existingAddress.setStreet(sm.getDeliveryStreet());
            existingAddress.setCity(sm.getDeliveryCity());
            existingAddress.setState(sm.getDeliveryState());
            existingAddress.setZipCode(sm.getDeliveryZipCode());
        }
        
        // Show dialog with existing address if available
        AddressDialog.show(CartActivity.this, existingAddress, new AddressDialog.AddressDialogListener() {
            @Override
            public void onAddressSaved(String street, String city, String state, String zipCode) {
                // Address is already saved to backend by AddressDialog
                // Create full address string
                String fullAddress = street + ", " + city + ", " + state + " " + zipCode;
                
                // Proceed with order creation
                createOrder(fullAddress);
            }
            
            @Override
            public void onCancel() {
                ToastUtils.showInfo(CartActivity.this, "Order cancelled. Please add address to proceed.");
            }
        });
    }
    
    private void createOrder(String deliveryAddress) {
        // Get all cart item IDs
        List<Long> cartItemIds = new ArrayList<>();
        for (CartItem item : cartItems) {
            cartItemIds.add(item.getId());
        }
        
        // Calculate delivery fee and total
        double subtotal = cartItems.isEmpty() ? 0.0 : 
            cartItems.stream().mapToDouble(CartItem::getItemTotal).sum();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + DELIVERY_FEE + tax;
        
        CreateOrderRequest request = new CreateOrderRequest(cartItemIds, deliveryAddress, DELIVERY_FEE);
        
        Call<Order> call = apiInterface.createOrder(request);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    ToastUtils.showSuccess(CartActivity.this, "Order placed successfully!");
                    
                    // Navigate to order details or order history
                    Intent intent = new Intent(CartActivity.this, CustomerHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtils.showError(CartActivity.this, "Failed to place order");
                }
            }
            
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                ToastUtils.showError(CartActivity.this, "Error: " + t.getMessage());
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCart(); // Reload cart when returning to this activity
    }
}


