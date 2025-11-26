package com.example.platemate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    
    private List<Order> orders;
    private OnOrderClickListener listener;
    
    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }
    
    public OrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders != null ? orders : new ArrayList<>();
        this.listener = listener;
    }
    
    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders != null ? newOrders : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }
    
    @Override
    public int getItemCount() {
        return orders.size();
    }
    
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId, tvProviderName, tvOrderDate, tvOrderStatus, tvTotalAmount;
        
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvProviderName = itemView.findViewById(R.id.tvProviderName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTotalAmount = itemView.findViewById(R.id.tvOrderTotal);
        }
        
        public void bind(Order order, OnOrderClickListener listener) {
            // Order ID
            if (order.getId() != null) {
                tvOrderId.setText("Order #" + order.getId());
            } else {
                tvOrderId.setText("Order #N/A");
            }
            
            // Provider name
            if (order.getProviderName() != null) {
                tvProviderName.setText(order.getProviderName());
            } else {
                tvProviderName.setText("Provider");
            }
            
            // Order date
            if (order.getOrderTime() != null) {
                tvOrderDate.setText(formatDate(order.getOrderTime()));
            } else {
                tvOrderDate.setText("N/A");
            }
            
            // Order status
            if (order.getOrderStatus() != null) {
                tvOrderStatus.setText(order.getOrderStatus());
                // Set status color
                int statusColor = getStatusColor(order.getOrderStatus());
                tvOrderStatus.setTextColor(statusColor);
            } else {
                tvOrderStatus.setText("UNKNOWN");
            }
            
            // Total amount
            if (order.getTotalAmount() != null) {
                tvTotalAmount.setText("₹" + String.format("%.2f", order.getTotalAmount()));
            } else {
                tvTotalAmount.setText("₹0.00");
            }
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }
        
        private String formatDate(String dateString) {
            try {
                // Parse ISO 8601 format from backend
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
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
                    return itemView.getContext().getColor(R.color.orange);
                case "CONFIRMED":
                case "PREPARING":
                    return itemView.getContext().getColor(R.color.blue);
                case "READY":
                case "OUT_FOR_DELIVERY":
                    return itemView.getContext().getColor(R.color.purple);
                case "DELIVERED":
                    return itemView.getContext().getColor(R.color.green);
                case "CANCELLED":
                    return itemView.getContext().getColor(R.color.red);
                default:
                    return itemView.getContext().getColor(R.color.black);
            }
        }
    }
}

