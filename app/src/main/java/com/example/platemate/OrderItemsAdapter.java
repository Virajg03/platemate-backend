package com.example.platemate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {
    
    private List<Order.OrderItem> items;
    
    public OrderItemsAdapter(List<Order.OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
    
    public void updateItems(List<Order.OrderItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        Order.OrderItem item = items.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItemName, tvQuantity, tvItemPrice, tvItemTotal;
        
        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
        }
        
        public void bind(Order.OrderItem item) {
            if (item.getItemName() != null) {
                tvItemName.setText(item.getItemName());
            } else {
                tvItemName.setText("Item");
            }
            
            if (item.getQuantity() != null) {
                tvQuantity.setText("Qty: " + item.getQuantity());
            } else {
                tvQuantity.setText("Qty: 0");
            }
            
            if (item.getItemPrice() != null) {
                tvItemPrice.setText("₹" + String.format("%.2f", item.getItemPrice()) + " each");
            } else {
                tvItemPrice.setText("₹0.00");
            }
            
            if (item.getItemTotal() != null) {
                tvItemTotal.setText("₹" + String.format("%.2f", item.getItemTotal()));
            } else {
                tvItemTotal.setText("₹0.00");
            }
        }
    }
}

