package com.example.platemate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<RatingReview> reviews = new ArrayList<>();

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        RatingReview review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void updateReviews(List<RatingReview> newReviews) {
        this.reviews = newReviews != null ? newReviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCustomerName, tvReviewText, tvReviewDate;
        private RatingBar ratingBar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }

        void bind(RatingReview review) {
            if (review.getCustomer() != null && review.getCustomer().getUser() != null) {
                String username = review.getCustomer().getUser().getUsername();
                tvCustomerName.setText(username != null ? username : "Anonymous");
            } else {
                tvCustomerName.setText("Anonymous");
            }

            ratingBar.setRating(review.getRating() != null ? review.getRating() : 0);

            if (review.getReviewText() != null && !review.getReviewText().isEmpty()) {
                tvReviewText.setText(review.getReviewText());
                tvReviewText.setVisibility(View.VISIBLE);
            } else {
                tvReviewText.setVisibility(View.GONE);
            }

            if (review.getCreatedAt() != null) {
                tvReviewDate.setText(formatDate(review.getCreatedAt()));
                tvReviewDate.setVisibility(View.VISIBLE);
            } else {
                tvReviewDate.setVisibility(View.GONE);
            }
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                // Ignore
            }
            return dateString;
        }
    }
}

