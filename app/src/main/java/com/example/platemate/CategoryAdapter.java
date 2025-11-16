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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categoryList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Category> newList) {
        this.categoryList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryName;
        private ImageView ivCategoryIcon;
        private View itemView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCategoryClick(categoryList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Category category) {
            tvCategoryName.setText(category.getName());

            // Load icon from resource or URL
            if (category.getIconResId() != 0) {
                ivCategoryIcon.setImageResource(category.getIconResId());
            } else if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(category.getIconUrl())
                    .placeholder(R.drawable.neubrutal_card)
                    .error(R.drawable.neubrutal_card)
                    .into(ivCategoryIcon);
            } else {
                // Default icon
                ivCategoryIcon.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}

