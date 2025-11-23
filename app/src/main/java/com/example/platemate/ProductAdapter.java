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
            // Safely handle name - use "Unnamed Product" if null
            String name = product.getName();
            if (name == null || name.isEmpty()) {
                name = "Unnamed Product";
            }
            tvName.setText(name);
            
            // Safely handle description - use empty string if null
            String description = product.getDescription();
            if (description == null) {
                description = "";
            }
            tvDescription.setText(description);
            
            // Safely handle price - use 0.0 if null
            Double price = product.getPrice();
            if (price == null) {
                price = 0.0;
            }
            tvPrice.setText("₹" + price);
            
            // Safely handle category - use "N/A" if null
            String category = product.getCategory();
            if (category == null || category.isEmpty()) {
                category = "N/A";
            }
            tvCategory.setText(category);
            
            // Safely handle quantity - use 0 if null
            Integer quantity = product.getQuantity();
            if (quantity == null) {
                quantity = 0;
            }
            tvQuantity.setText("Qty: " + quantity);
            
            // Load image using Glide - handle null and construct full URL if needed
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Construct full URL if it's a relative path
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    // Get base URL from RetrofitClient
                    String baseUrl = "https://trypanosomal-annalise-stenographic.ngrok-free.dev";
                    imageUrl = baseUrl + imageUrl;
                }
                Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.neubrutal_card)
                    .error(R.drawable.neubrutal_card)
                    .into(ivProduct);
            } else {
                ivProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}

