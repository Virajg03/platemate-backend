package com.example.platemate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private ProviderDashboardActivity activity;

    public ProductAdapter(List<Product> productList, ProviderDashboardActivity activity) {
        this.productList = productList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDescription, tvPrice, tvCategory, tvQuantity;
        private ImageView ivProduct;
        private View itemView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvName = itemView.findViewById(R.id.tvProductName);
            tvDescription = itemView.findViewById(R.id.tvProductDescription);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvCategory = itemView.findViewById(R.id.tvProductCategory);
            tvQuantity = itemView.findViewById(R.id.tvProductQuantity);
            ivProduct = itemView.findViewById(R.id.ivProductImage);
        }

        public void bind(Product product) {
            tvName.setText(product.getName());
            tvDescription.setText(product.getDescription());
            tvPrice.setText("₹" + product.getPrice());
            tvCategory.setText(product.getCategory());
            tvQuantity.setText("Qty: " + product.getQuantity());
            
            // Load image using Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.neubrutal_card)
                    .error(R.drawable.neubrutal_card)
                    .into(ivProduct);
            } else {
                ivProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}

