package com.example.platemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class CustomerHomeActivity extends AppCompatActivity {
    
    private TextView welcomeText, userNameText;
    private ImageView logoutBtn, filterBtn, cartBtn, searchIcon;
    private EditText searchEditText;
    private RecyclerView bestFoodRecyclerView, categoryRecyclerView;
    private ProgressBar progressBarBestFood, progressBarCategory;
    private TextView todayBestFoodTitle, viewAllText, chooseCategoryTitle;
    
    private SessionManager sessionManager;
    private ApiInterface apiInterface;
    
    // TODO: Create adapters for best food and categories
    // private BestFoodAdapter bestFoodAdapter;
    // private CategoryAdapter categoryAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_home);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        sessionManager = new SessionManager(this);
        apiInterface = RetrofitClient.getInstance(this).getApi();
        
        initializeViews();
        setupClickListeners();
        loadUserData();
        setupRecyclerViews();
        loadBestFoods();
        loadCategories();
    }
    
    private void initializeViews() {
        welcomeText = findViewById(R.id.textView14);
        userNameText = findViewById(R.id.textView13);
        logoutBtn = findViewById(R.id.logoutbtn);
        filterBtn = findViewById(R.id.filter);
        cartBtn = findViewById(R.id.cartbtn);
        searchIcon = findViewById(R.id.searchfilter);
        searchEditText = findViewById(R.id.editTextText);
        bestFoodRecyclerView = findViewById(R.id.bestfoodview);
        categoryRecyclerView = findViewById(R.id.catogoryview);
        progressBarBestFood = findViewById(R.id.progressbarbestfood);
        progressBarCategory = findViewById(R.id.progressBarcatogory);
        todayBestFoodTitle = findViewById(R.id.textView10);
        viewAllText = findViewById(R.id.textView11);
        chooseCategoryTitle = findViewById(R.id.textView12);
    }
    
    private void setupClickListeners() {
        logoutBtn.setOnClickListener(v -> handleLogout());
        cartBtn.setOnClickListener(v -> {
            // TODO: Navigate to cart activity
            Toast.makeText(this, "Cart feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        filterBtn.setOnClickListener(v -> {
            // TODO: Show filter dialog
            Toast.makeText(this, "Filter feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        viewAllText.setOnClickListener(v -> {
            // TODO: Navigate to all foods activity
            Toast.makeText(this, "View all foods coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadUserData() {
        String username = sessionManager.getUsername();
        if (username != null && !username.isEmpty()) {
            userNameText.setText(username);
        } else {
            userNameText.setText("Guest");
        }
    }
    
    private void setupRecyclerViews() {
        // Setup best food RecyclerView
        bestFoodRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        // bestFoodAdapter = new BestFoodAdapter(new ArrayList<>(), this);
        // bestFoodRecyclerView.setAdapter(bestFoodAdapter);
        
        // Setup category RecyclerView
        categoryRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        // categoryAdapter = new CategoryAdapter(new ArrayList<>(), this);
        // categoryRecyclerView.setAdapter(categoryAdapter);
    }
    
    private void loadBestFoods() {
        progressBarBestFood.setVisibility(View.VISIBLE);
        bestFoodRecyclerView.setVisibility(View.GONE);
        
        // TODO: Implement API call to get best foods
        // Call<List<Product>> call = apiInterface.getBestFoods();
        // call.enqueue(new Callback<List<Product>>() {
        //     @Override
        //     public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
        //         progressBarBestFood.setVisibility(View.GONE);
        //         bestFoodRecyclerView.setVisibility(View.VISIBLE);
        //         if (response.isSuccessful() && response.body() != null) {
        //             bestFoodAdapter.updateList(response.body());
        //         }
        //     }
        //     @Override
        //     public void onFailure(Call<List<Product>> call, Throwable t) {
        //         progressBarBestFood.setVisibility(View.GONE);
        //         bestFoodRecyclerView.setVisibility(View.VISIBLE);
        //         Toast.makeText(CustomerHomeActivity.this, "Failed to load foods", Toast.LENGTH_SHORT).show();
        //     }
        // });
        
        // Temporary: Hide progress bar after delay
        bestFoodRecyclerView.postDelayed(() -> {
            progressBarBestFood.setVisibility(View.GONE);
            bestFoodRecyclerView.setVisibility(View.VISIBLE);
        }, 1000);
    }
    
    private void loadCategories() {
        progressBarCategory.setVisibility(View.VISIBLE);
        categoryRecyclerView.setVisibility(View.GONE);
        
        // TODO: Implement API call to get categories
        // Similar to loadBestFoods()
        
        // Temporary: Hide progress bar after delay
        categoryRecyclerView.postDelayed(() -> {
            progressBarCategory.setVisibility(View.GONE);
            categoryRecyclerView.setVisibility(View.VISIBLE);
        }, 1000);
    }
    
    private void handleLogout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add menu items if needed
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
