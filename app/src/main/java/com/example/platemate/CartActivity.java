package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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
        });
        rvCartItems.setAdapter(cartAdapter);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        btnProceedToCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CartActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }
            
            @Override
            public void onFailure(Call<CartSummary> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CartActivity.this, "Failed to update cart", Toast.LENGTH_SHORT).show();
                    loadCart(); // Reload to revert changes
                }
            }
            
            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CartActivity.this, "Item removed from cart", Toast.LENGTH_SHORT).show();
                    loadCart();
                } else {
                    Toast.makeText(CartActivity.this, "Failed to remove item", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateOrderSummary(Double subtotal) {
        if (subtotal == null) subtotal = 0.0;
        
        double tax = subtotal * TAX_RATE;
        double total = subtotal + DELIVERY_FEE + tax;
        
        tvSubtotal.setText("₹" + String.format("%.2f", subtotal));
        tvDeliveryFee.setText("₹" + String.format("%.2f", DELIVERY_FEE));
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
        
        // TODO: Get delivery address from user profile or address management
        String deliveryAddress = "Your Address Here"; // Replace with actual address
        
        CreateOrderRequest request = new CreateOrderRequest(cartItemIds, deliveryAddress, DELIVERY_FEE);
        
        Call<Order> call = apiInterface.createOrder(request);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to order details or order history
                    Intent intent = new Intent(CartActivity.this, CustomerHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CartActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCart(); // Reload cart when returning to this activity
    }
}

