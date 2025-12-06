package com.example.platemate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DeliveryPartnerAdapter extends RecyclerView.Adapter<DeliveryPartnerAdapter.DeliveryPartnerViewHolder> {
    private List<DeliveryPartner> deliveryPartnersList;
    private DeliveryPartnersActivity activity;

    public DeliveryPartnerAdapter(List<DeliveryPartner> deliveryPartnersList, DeliveryPartnersActivity activity) {
        this.deliveryPartnersList = deliveryPartnersList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public DeliveryPartnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_delivery_partner, parent, false);
        return new DeliveryPartnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryPartnerViewHolder holder, int position) {
        DeliveryPartner partner = deliveryPartnersList.get(position);
        holder.bind(partner);
    }

    @Override
    public int getItemCount() {
        return deliveryPartnersList.size();
    }

    class DeliveryPartnerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvVehicleType, tvCommissionRate, tvServiceArea;
        private Switch switchAvailable;
        private ImageView btnEdit, btnDelete;
        private View itemView;

        public DeliveryPartnerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvName = itemView.findViewById(R.id.tvName);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvCommissionRate = itemView.findViewById(R.id.tvCommissionRate);
            tvServiceArea = itemView.findViewById(R.id.tvServiceArea);
            switchAvailable = itemView.findViewById(R.id.switchAvailable);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(DeliveryPartner partner) {
            tvName.setText(partner.getFullName() != null ? partner.getFullName() : "N/A");
            tvVehicleType.setText("Vehicle: " + (partner.getVehicleType() != null ? partner.getVehicleType() : "N/A"));
            
            Double commissionRate = partner.getCommissionRate();
            if (commissionRate != null) {
                tvCommissionRate.setText("Commission: " + commissionRate + "%");
            } else {
                tvCommissionRate.setText("Commission: 0%");
            }
            
            tvServiceArea.setText("Area: " + (partner.getServiceArea() != null ? partner.getServiceArea() : "N/A"));
            
            Boolean isAvailable = partner.getIsAvailable();
            switchAvailable.setChecked(isAvailable != null && isAvailable);
            
            // Toggle availability - prevent triggering during binding
            switchAvailable.setOnCheckedChangeListener(null);
            switchAvailable.setChecked(isAvailable != null && isAvailable);
            switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                partner.setIsAvailable(isChecked);
                // Create update request with all fields
                DeliveryPartnerUpdateRequest request = new DeliveryPartnerUpdateRequest();
                request.setFullName(partner.getFullName());
                request.setVehicleType(partner.getVehicleType());
                request.setCommissionRate(partner.getCommissionRate());
                request.setServiceArea(partner.getServiceArea());
                request.setIsAvailable(isChecked);
                
                // Update via activity
                activity.updateDeliveryPartner(partner);
            });
            
            // Edit button
            btnEdit.setOnClickListener(v -> activity.showEditDeliveryPartnerForm(partner));
            
            // Delete button
            btnDelete.setOnClickListener(v -> activity.deleteDeliveryPartner(partner));
        }
    }
}

