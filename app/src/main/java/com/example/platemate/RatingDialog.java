package com.example.platemate;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class RatingDialog extends Dialog {
    private RatingBar ratingBar;
    private EditText etReview;
    private Button btnSubmit, btnCancel;
    private TextView tvTitle;
    private OnRatingSubmitListener listener;
    private String title;

    public RatingDialog(@NonNull Context context, String title, OnRatingSubmitListener listener) {
        super(context);
        this.title = title;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rating);

        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        tvTitle = findViewById(R.id.tvTitle);

        if (title != null) {
            tvTitle.setText(title);
        }

        btnSubmit.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String review = etReview.getText().toString().trim();
            
            if (rating == 0) {
                ToastUtils.showInfo(getContext(), "Please select a rating");
                return;
            }
            
            if (listener != null) {
                listener.onRatingSubmit(rating, review);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    public interface OnRatingSubmitListener {
        void onRatingSubmit(int rating, String review);
    }
}

