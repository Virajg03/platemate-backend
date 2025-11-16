package com.example.platemate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class BestFoodAdapter extends RecyclerView.Adapter<BestFoodAdapter.BestFoodViewHolder> {
    private List<Product> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAddToCartClick(Product product);
        void onItemClick(Product product);
    }

    public BestFoodAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BestFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_best_food, parent, false);
        return new BestFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BestFoodViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    class BestFoodViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName, tvProductPrice;
        private ImageView ivProductImage;
        private ImageButton btnAddToCart;
        private View itemView;

        public BestFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(productList.get(getAdapterPosition()));
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onAddToCartClick(productList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText("$" + String.format("%.2f", product.getPrice()));

            // Load image using Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.neubrutal_card)
                    .error(R.drawable.neubrutal_card)
                    .centerCrop()
                    .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}

