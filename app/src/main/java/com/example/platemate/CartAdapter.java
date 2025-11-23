package com.example.platemate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveClick(CartItem item);
        void onItemClick(CartItem item);
    }
    
    public CartAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public void updateList(List<CartItem> newList) {
        this.cartItems = newList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }
    
    class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItemName, tvItemPrice, tvQuantity, tvItemTotal, tvProviderName;
        private ImageView btnDecrease, btnIncrease, btnRemove, ivProductImage;
        private ApiInterface apiInterface;
        
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvProductName);
            tvItemPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvItemTotal = itemView.findViewById(R.id.tvSubtotal);
            tvProviderName = itemView.findViewById(R.id.tvProviderName);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            
            apiInterface = RetrofitClient.getInstance(itemView.getContext()).getApi();
            
            // Make entire item clickable to view details
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(cartItems.get(getAdapterPosition()));
                }
            });
            
            btnDecrease.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    CartItem item = cartItems.get(getAdapterPosition());
                    int newQuantity = (item.getQuantity() != null ? item.getQuantity() : 1) - 1;
                    listener.onQuantityChanged(item, newQuantity);
                }
            });
            
            btnIncrease.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    CartItem item = cartItems.get(getAdapterPosition());
                    int newQuantity = (item.getQuantity() != null ? item.getQuantity() : 1) + 1;
                    listener.onQuantityChanged(item, newQuantity);
                }
            });
            
            btnRemove.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onRemoveClick(cartItems.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(CartItem item) {
            tvItemName.setText(item.getItemName());
            tvItemPrice.setText("₹" + String.format("%.2f", item.getItemPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity() != null ? item.getQuantity() : 1));
            tvItemTotal.setText("₹" + String.format("%.2f", item.getItemTotal()));
            
            // Set provider name if available
            if (item.getProviderName() != null && !item.getProviderName().isEmpty()) {
                tvProviderName.setText(item.getProviderName());
                tvProviderName.setVisibility(View.VISIBLE);
            } else {
                tvProviderName.setVisibility(View.GONE);
            }
            
            // Load menu item image using menuItemId
            if (item.getMenuItemId() != null) {
                loadMenuItemImage(item.getMenuItemId());
            } else {
                ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
        
        private void loadMenuItemImage(Long menuItemId) {
            Call<MenuItem> call = apiInterface.getCustomerMenuItemById(menuItemId);
            call.enqueue(new Callback<MenuItem>() {
                @Override
                public void onResponse(Call<MenuItem> call, Response<MenuItem> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MenuItem menuItem = response.body();
                        String base64Image = menuItem.getFirstImageBase64();
                        
                        if (base64Image != null && !base64Image.isEmpty()) {
                            try {
                                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                if (bitmap != null) {
                                    ivProductImage.setImageBitmap(bitmap);
                                } else {
                                    ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                                }
                            } catch (Exception e) {
                                ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                        } else {
                            ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<MenuItem> call, Throwable t) {
                    ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            });
        }
    }
}

