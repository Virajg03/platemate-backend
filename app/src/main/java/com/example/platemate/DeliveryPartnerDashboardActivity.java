package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.tabs.TabLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;

public class DeliveryPartnerDashboardActivity extends AppCompatActivity {
    
    private TabLayout tabLayout;
    private FrameLayout fragmentContainer;
    private TextView tvWelcomeText, tvPartnerName;
    
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    
    private DeliveryPartnerOrdersFragment assignedFragment;
    private DeliveryPartnerOrdersFragment availableFragment;
    private DeliveryPartnerOrdersFragment completedFragment;
    private DeliveryPartnerProfileFragment profileFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_partner_dashboard);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        sessionManager = new SessionManager(this);
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        initializeViews();
        setupTabs();
        loadPartnerInfo();
        
        // Show assigned orders by default
        showFragment("ASSIGNED");
    }
    
    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        tvWelcomeText = findViewById(R.id.tvWelcomeText);
        tvPartnerName = findViewById(R.id.tvPartnerName);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
    
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("My Orders"));
        tabLayout.addTab(tabLayout.newTab().setText("Available"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed"));
        tabLayout.addTab(tabLayout.newTab().setText("Profile"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showFragment("ASSIGNED");
                        break;
                    case 1:
                        showFragment("AVAILABLE");
                        break;
                    case 2:
                        showFragment("COMPLETED");
                        break;
                    case 3:
                        showProfileFragment();
                        break;
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void showFragment(String orderType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        Fragment fragment = null;
        
        switch (orderType) {
            case "ASSIGNED":
                if (assignedFragment == null) {
                    assignedFragment = DeliveryPartnerOrdersFragment.newInstance("ASSIGNED");
                }
                fragment = assignedFragment;
                break;
            case "AVAILABLE":
                if (availableFragment == null) {
                    availableFragment = DeliveryPartnerOrdersFragment.newInstance("AVAILABLE");
                }
                fragment = availableFragment;
                break;
            case "COMPLETED":
                if (completedFragment == null) {
                    completedFragment = DeliveryPartnerOrdersFragment.newInstance("COMPLETED");
                }
                fragment = completedFragment;
                break;
        }
        
        if (fragment != null) {
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        }
    }
    
    private void showProfileFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        if (profileFragment == null) {
            profileFragment = new DeliveryPartnerProfileFragment();
        }
        
        transaction.replace(R.id.fragmentContainer, profileFragment);
        transaction.commit();
    }
    
    private void loadPartnerInfo() {
        // Get delivery partner info from session or API
        String username = sessionManager.getUsername();
        if (username != null) {
            tvWelcomeText.setText("Welcome back!");
            // You can fetch delivery partner details from API if needed
            // For now, just show username
            tvPartnerName.setText(username);
        }
    }
    
    // Removed automatic refresh on resume - data loads once on app open
    // Users can refresh manually using swipe-to-refresh in fragments
}


