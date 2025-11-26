package com.example.platemate;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
        setupRecyclerView();
        loadDeliveryPartners();

        fabAdd.setOnClickListener(v -> showAddDeliveryPartnerDialog());
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

    public void showAddDeliveryPartnerDialog() {
        // Get logged-in user ID from session
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            ToastUtils.showError(this, "User ID not found. Please login again.");
            return;
        }
        
        DeliveryPartnerDialog dialog = new DeliveryPartnerDialog(this, null, userId, (partner, isEdit) -> {
            if (isEdit) {
                updateDeliveryPartner(partner);
            } else {
                createDeliveryPartner(partner);
            }
        });
        dialog.show();
    }

    public void showEditDeliveryPartnerDialog(DeliveryPartner partner) {
        // Get logged-in user ID from session (for consistency, though not used in edit mode)
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            ToastUtils.showError(this, "User ID not found. Please login again.");
            return;
        }
        
        DeliveryPartnerDialog dialog = new DeliveryPartnerDialog(this, partner, userId, (updatedPartner, isEdit) -> {
            updateDeliveryPartner(updatedPartner);
        });
        dialog.show();
    }

    private void createDeliveryPartner(DeliveryPartner partner) {
        progressBar.setVisibility(View.VISIBLE);

        DeliveryPartnerCreateRequest request = new DeliveryPartnerCreateRequest(
            partner.getUserId(),
            partner.getFullName(),
            partner.getVehicleType(),
            partner.getCommissionRate(),
            partner.getServiceArea()
        );

        Call<DeliveryPartner> call = apiInterface.createProviderDeliveryPartner(request);
        call.enqueue(new Callback<DeliveryPartner>() {
            @Override
            public void onResponse(Call<DeliveryPartner> call, Response<DeliveryPartner> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(DeliveryPartnersActivity.this, "Delivery partner created successfully");
                    loadDeliveryPartners();
                } else {
                    String errorMessage = "Failed to create delivery partner";
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

    public void updateDeliveryPartner(DeliveryPartner partner) {
        progressBar.setVisibility(View.VISIBLE);

        DeliveryPartnerUpdateRequest request = new DeliveryPartnerUpdateRequest();
        request.setFullName(partner.getFullName());
        request.setVehicleType(partner.getVehicleType());
        request.setCommissionRate(partner.getCommissionRate());
        request.setServiceArea(partner.getServiceArea());
        request.setIsAvailable(partner.getIsAvailable());

        Call<DeliveryPartner> call = apiInterface.updateProviderDeliveryPartner(partner.getId(), request);
        call.enqueue(new Callback<DeliveryPartner>() {
            @Override
            public void onResponse(Call<DeliveryPartner> call, Response<DeliveryPartner> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    ToastUtils.showSuccess(DeliveryPartnersActivity.this, "Delivery partner updated successfully");
                    loadDeliveryPartners();
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

