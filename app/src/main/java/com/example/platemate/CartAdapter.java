package com.example.platemate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveClick(CartItem item);
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
        private TextView tvItemName, tvItemPrice, tvQuantity, tvItemTotal;
        private android.widget.ImageView btnDecrease, btnIncrease, btnRemove;
        
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvProductName);
            tvItemPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvItemTotal = itemView.findViewById(R.id.tvSubtotal);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            
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
        }
    }
}

