package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class DeliveryPartnersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DeliveryPartnerAdapter adapter;
    private FloatingActionButton fabAdd;
    private ProgressBar progressBar;
    private android.widget.LinearLayout layoutEmpty;
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    private List<DeliveryPartner> deliveryPartnersList;
    private ActivityResultLauncher<Intent> deliveryPartnerFormLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_partners);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Delivery Partners");
        }

        apiInterface = RetrofitClient.getInstance(this).getApi();
        sessionManager = new SessionManager(this);
        deliveryPartnersList = new ArrayList<>();

        initializeViews();
        setupBackButton();
        setupRecyclerView();
        setupActivityResultLauncher();
        loadDeliveryPartners();

        fabAdd.setOnClickListener(v -> showAddDeliveryPartnerForm());
    }
    
    private void setupBackButton() {
        android.widget.ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Handle back button click - finish activity to go back to previous screen
                finish();
            });
        }
    }
    
    private void setupActivityResultLauncher() {
        deliveryPartnerFormLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Refresh the list when partner is created/updated
                    loadDeliveryPartners();
                }
            }
        );
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        adapter = new DeliveryPartnerAdapter(deliveryPartnersList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadDeliveryPartners() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        Call<List<DeliveryPartner>> call = apiInterface.getProviderDeliveryPartners();
        call.enqueue(new Callback<List<DeliveryPartner>>() {
            @Override
            public void onResponse(Call<List<DeliveryPartner>> call, Response<List<DeliveryPartner>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    deliveryPartnersList.clear();
                    deliveryPartnersList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    ToastUtils.showError(DeliveryPartnersActivity.this, "Failed to load delivery partners");
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<DeliveryPartner>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtils.showError(DeliveryPartnersActivity.this, "Error: " + t.getMessage());
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (deliveryPartnersList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void showAddDeliveryPartnerForm() {
        Intent intent = new Intent(this, DeliveryPartnerFormActivity.class);
        intent.putExtra(DeliveryPartnerFormActivity.EXTRA_IS_EDIT, false);
        deliveryPartnerFormLauncher.launch(intent);
    }

    public void showEditDeliveryPartnerForm(DeliveryPartner partner) {
        Intent intent = new Intent(this, DeliveryPartnerFormActivity.class);
        intent.putExtra(DeliveryPartnerFormActivity.EXTRA_DELIVERY_PARTNER, partner);
        intent.putExtra(DeliveryPartnerFormActivity.EXTRA_IS_EDIT, true);
        deliveryPartnerFormLauncher.launch(intent);
    }

    /**
     * Updates delivery partner - used by toggle switch for quick availability updates
     * For full edits, use showEditDeliveryPartnerForm() which opens the Activity
     */
    public void updateDeliveryPartner(DeliveryPartner partner) {
        progressBar.setVisibility(View.VISIBLE);

        DeliveryPartnerUpdateRequest request = new DeliveryPartnerUpdateRequest();
        request.setFullName(partner.getFullName());
        request.setVehicleType(partner.getVehicleType());
        request.setServiceArea(partner.getServiceArea());
        request.setIsAvailable(partner.getIsAvailable());

        Call<DeliveryPartner> call = apiInterface.updateProviderDeliveryPartner(partner.getId(), request);
        call.enqueue(new Callback<DeliveryPartner>() {
            @Override
            public void onResponse(Call<DeliveryPartner> call, Response<DeliveryPartner> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    // Don't show toast for toggle updates - it's too frequent
                    loadDeliveryPartners(); // Refresh to get updated data
                } else {
                    String errorMessage = "Failed to update delivery partner";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ToastUtils.showError(DeliveryPartnersActivity.this, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<DeliveryPartner> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtils.showError(DeliveryPartnersActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    public void deleteDeliveryPartner(DeliveryPartner partner) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Delivery Partner")
            .setMessage("Are you sure you want to delete " + partner.getFullName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                progressBar.setVisibility(View.VISIBLE);
                Call<Void> call = apiInterface.deleteProviderDeliveryPartner(partner.getId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            ToastUtils.showSuccess(DeliveryPartnersActivity.this, "Delivery partner deleted successfully");
                            loadDeliveryPartners();
                        } else {
                            String errorMessage = "Failed to delete delivery partner";
                            if (response.errorBody() != null) {
                                try {
                                    errorMessage = response.errorBody().string();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            ToastUtils.showError(DeliveryPartnersActivity.this, errorMessage);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        ToastUtils.showError(DeliveryPartnersActivity.this, "Error: " + t.getMessage());
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

