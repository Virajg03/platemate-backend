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
        
        // Find the position of the item in the list to preserve order
        int itemPosition = -1;
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId().equals(item.getId())) {
                itemPosition = i;
                break;
            }
        }
        
        if (itemPosition == -1) {
            // Item not found, reload cart
            loadCart();
            return;
        }
        
        // Make final copy for use in inner class
        final int finalItemPosition = itemPosition;
        final Long itemId = item.getId();
        
        // Optimistically update the item locally to preserve position
        CartItem localItem = cartItems.get(finalItemPosition);
        localItem.setQuantity(newQuantity);
        // Update item total (price * quantity)
        if (localItem.getItemPrice() != null) {
            localItem.setItemTotal(localItem.getItemPrice() * newQuantity);
        }
        
        // Update the specific item in the adapter without reloading entire list
        cartAdapter.notifyItemChanged(finalItemPosition);
        
        // Recalculate summary with local data
        double subtotal = cartItems.stream()
            .mapToDouble(cartItem -> cartItem.getItemTotal() != null ? cartItem.getItemTotal() : 0.0)
            .sum();
        updateOrderSummary(subtotal);
        
        // Update on server
        UpdateCartRequest request = new UpdateCartRequest(newQuantity, item.getSpecialInstructions());
        Call<CartItem> call = apiInterface.updateCartItem(item.getId(), request);
        call.enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update the item with server response (to get any server-side calculations)
                    CartItem updatedItem = response.body();
                    if (finalItemPosition < cartItems.size() && 
                        cartItems.get(finalItemPosition).getId().equals(updatedItem.getId())) {
                        // Update the item in place
                        cartItems.set(finalItemPosition, updatedItem);
                        cartAdapter.notifyItemChanged(finalItemPosition);
                        
                        // Recalculate summary with server data
                        loadCartSummary();
                    }
                } else {
                    ToastUtils.showError(CartActivity.this, "Failed to update cart");
                    // Reload to get correct state from server
                    loadCart();
                }
            }
            
            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                ToastUtils.showError(CartActivity.this, "Error: " + t.getMessage());
                // Reload to get correct state from server
                loadCart();
            }
        });
    }
    
    /**
     * Load only cart summary (subtotal) without reloading the entire cart list
     * This preserves the order of items
     */
    private void loadCartSummary() {
        Call<CartSummary> call = apiInterface.getCart();
        call.enqueue(new Callback<CartSummary>() {
            @Override
            public void onResponse(Call<CartSummary> call, Response<CartSummary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CartSummary cartSummary = response.body();
                    updateOrderSummary(cartSummary.getSubtotal());
                }
            }
            
            @Override
            public void onFailure(Call<CartSummary> call, Throwable t) {
                // Silently fail - we already have local data
            }
        });
    }
    
    private void removeCartItem(CartItem item) {
        // Find the position of the item in the list
        int itemPosition = -1;
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId().equals(item.getId())) {
                itemPosition = i;
                break;
            }
        }
        
        if (itemPosition == -1) {
            // Item not found, reload cart
            loadCart();
            return;
        }
        
        // Make final copy for use in inner class
        final int finalItemPosition = itemPosition;
        final Long itemId = item.getId();
        
        // Optimistically remove the item locally to preserve order of remaining items
        cartItems.remove(finalItemPosition);
        cartAdapter.notifyItemRemoved(finalItemPosition);
        
        // Update summary with local data
        double subtotal = cartItems.stream()
            .mapToDouble(cartItem -> cartItem.getItemTotal() != null ? cartItem.getItemTotal() : 0.0)
            .sum();
        updateOrderSummary(subtotal);
        showEmptyState(cartItems.isEmpty());
        
        // Remove from server
        Call<Void> call = apiInterface.removeCartItem(itemId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(CartActivity.this, "Item removed from cart");
                    // Reload summary to ensure accuracy
                    loadCartSummary();
                } else {
                    ToastUtils.showError(CartActivity.this, "Failed to remove item");
                    // Reload to get correct state from server
                    loadCart();
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                ToastUtils.showError(CartActivity.this, "Error: " + t.getMessage());
                // Reload to get correct state from server
                loadCart();
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
        // Navigate to checkout activity
        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCart(); // Reload cart when returning to this activity
    }
}


