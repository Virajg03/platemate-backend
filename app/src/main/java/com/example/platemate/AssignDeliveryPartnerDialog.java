package com.example.platemate;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class AssignDeliveryPartnerDialog {
    
    public interface OnPartnerSelectedListener {
        void onPartnerSelected(DeliveryPartner partner);
        void onCancel();
    }
    
    public static void show(Context context, OnPartnerSelectedListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_assign_delivery_partner);
        dialog.setCancelable(true);
        
        RecyclerView rvDeliveryPartners = dialog.findViewById(R.id.rvDeliveryPartners);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        LinearLayout emptyLayout = dialog.findViewById(R.id.emptyLayout);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        
        ApiInterface apiInterface = RetrofitClient.getInstance(context).getApi();
        List<DeliveryPartner> deliveryPartners = new ArrayList<>();
        
        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rvDeliveryPartners.setLayoutManager(layoutManager);
        
        // Create a simplified adapter for selection
        DeliveryPartnerSelectionAdapter selectionAdapter = 
            new DeliveryPartnerSelectionAdapter(deliveryPartners, partner -> {
                android.util.Log.d("AssignDeliveryPartner", "Adapter callback received for partner: " + partner.getFullName());
                dialog.dismiss();
                if (listener != null) {
                    android.util.Log.d("AssignDeliveryPartner", "Calling main listener.onPartnerSelected");
                    listener.onPartnerSelected(partner);
                } else {
                    android.util.Log.e("AssignDeliveryPartner", "Main listener is null!");
                }
            });
        rvDeliveryPartners.setAdapter(selectionAdapter);
        
        // Load available delivery partners
        progressBar.setVisibility(View.VISIBLE);
        android.util.Log.d("AssignDeliveryPartner", "Loading available delivery partners...");
        Call<List<DeliveryPartner>> call = apiInterface.getAvailableDeliveryPartners();
        call.enqueue(new Callback<List<DeliveryPartner>>() {
            @Override
            public void onResponse(Call<List<DeliveryPartner>> call, Response<List<DeliveryPartner>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("AssignDeliveryPartner", "Loaded " + response.body().size() + " delivery partners");
                    deliveryPartners.clear();
                    // Filter only available partners
                    for (DeliveryPartner partner : response.body()) {
                        if (partner.getIsAvailable() != null && partner.getIsAvailable()) {
                            deliveryPartners.add(partner);
                            android.util.Log.d("AssignDeliveryPartner", "Added available partner: " + partner.getFullName());
                        }
                    }
                    
                    selectionAdapter.notifyDataSetChanged();
                    
                    if (deliveryPartners.isEmpty()) {
                        android.util.Log.w("AssignDeliveryPartner", "No available delivery partners found");
                        emptyLayout.setVisibility(View.VISIBLE);
                        rvDeliveryPartners.setVisibility(View.GONE);
                    } else {
                        android.util.Log.d("AssignDeliveryPartner", "Showing " + deliveryPartners.size() + " available partners");
                        emptyLayout.setVisibility(View.GONE);
                        rvDeliveryPartners.setVisibility(View.VISIBLE);
                    }
                } else {
                    android.util.Log.e("AssignDeliveryPartner", "Failed to load delivery partners. Code: " + response.code());
                    ToastUtils.showError(context, "Failed to load delivery partners");
                    emptyLayout.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onFailure(Call<List<DeliveryPartner>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                android.util.Log.e("AssignDeliveryPartner", "Error loading delivery partners", t);
                ToastUtils.showError(context, "Error: " + t.getMessage());
                emptyLayout.setVisibility(View.VISIBLE);
            }
        });
        
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    // Simplified adapter for partner selection (no edit/delete)
    private static class DeliveryPartnerSelectionAdapter extends RecyclerView.Adapter<DeliveryPartnerSelectionAdapter.ViewHolder> {
        private List<DeliveryPartner> partners;
        private OnSelectionListener listener;
        
        public interface OnSelectionListener {
            void onPartnerSelected(DeliveryPartner partner);
        }
        
        public DeliveryPartnerSelectionAdapter(List<DeliveryPartner> partners, OnSelectionListener listener) {
            this.partners = partners;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery_partner_selection, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DeliveryPartner partner = partners.get(position);
            holder.bind(partner, listener);
        }
        
        @Override
        public int getItemCount() {
            return partners.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvName, tvVehicleType, tvServiceArea;
            private DeliveryPartner currentPartner;
            private OnSelectionListener currentListener;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
                tvServiceArea = itemView.findViewById(R.id.tvServiceArea);
                
                // Set click listener once in constructor
                itemView.setOnClickListener(v -> {
                    android.util.Log.d("AssignDeliveryPartner", "ItemView clicked at position: " + getAdapterPosition());
                    if (currentListener != null && currentPartner != null) {
                        android.util.Log.d("AssignDeliveryPartner", "Partner clicked: " + currentPartner.getFullName() + " (ID: " + currentPartner.getId() + ")");
                        android.util.Log.d("AssignDeliveryPartner", "Calling listener.onPartnerSelected");
                        currentListener.onPartnerSelected(currentPartner);
                    } else {
                        android.util.Log.e("AssignDeliveryPartner", "Listener or partner is null! listener=" + currentListener + ", partner=" + currentPartner);
                    }
                });
                
                // Ensure the view is clickable
                itemView.setClickable(true);
                itemView.setFocusable(true);
            }
            
            public void bind(DeliveryPartner partner, OnSelectionListener listener) {
                // Store current partner and listener
                this.currentPartner = partner;
                this.currentListener = listener;
                
                tvName.setText(partner.getFullName() != null ? partner.getFullName() : "N/A");
                tvVehicleType.setText("Vehicle: " + (partner.getVehicleType() != null ? partner.getVehicleType() : "N/A"));
                tvServiceArea.setText("Area: " + (partner.getServiceArea() != null ? partner.getServiceArea() : "N/A"));
            }
        }
    }
}

