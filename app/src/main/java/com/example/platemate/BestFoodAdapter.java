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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class BestFoodAdapter extends RecyclerView.Adapter<BestFoodAdapter.BestFoodViewHolder> {
    private List<MenuItem> menuItemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAddToCartClick(MenuItem menuItem);
        void onItemClick(MenuItem menuItem);
    }

    public BestFoodAdapter(List<MenuItem> menuItemList) {
        this.menuItemList = menuItemList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<MenuItem> newList) {
        this.menuItemList = newList;
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
        MenuItem menuItem = menuItemList.get(position);
        holder.bind(menuItem);
    }

    @Override
    public int getItemCount() {
        return menuItemList != null ? menuItemList.size() : 0;
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
                    listener.onItemClick(menuItemList.get(getAdapterPosition()));
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onAddToCartClick(menuItemList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(MenuItem menuItem) {
            tvProductName.setText(menuItem.getItemName());
            tvProductPrice.setText("₹" + String.format("%.2f", menuItem.getPrice()));

            // Load image from base64
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
}

