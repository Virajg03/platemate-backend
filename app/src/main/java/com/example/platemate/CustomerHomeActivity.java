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
    
    private BestFoodAdapter bestFoodAdapter;
    private CategoryAdapter categoryAdapter;
    
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
        bestFoodAdapter = new BestFoodAdapter(new ArrayList<>());
        bestFoodAdapter.setOnItemClickListener(new BestFoodAdapter.OnItemClickListener() {
            @Override
            public void onAddToCartClick(Product product) {
                // TODO: Add product to cart
                Toast.makeText(CustomerHomeActivity.this, 
                    "Added " + product.getName() + " to cart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(Product product) {
                // TODO: Navigate to product detail activity
                Toast.makeText(CustomerHomeActivity.this, 
                    "Product: " + product.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        bestFoodRecyclerView.setAdapter(bestFoodAdapter);
        
        // Setup category RecyclerView
        categoryRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        categoryAdapter.setOnItemClickListener(category -> {
            // TODO: Filter products by category
            Toast.makeText(CustomerHomeActivity.this, 
                "Selected category: " + category.getName(), Toast.LENGTH_SHORT).show();
        });
        categoryRecyclerView.setAdapter(categoryAdapter);
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
        
        // Temporary: Load sample data for testing
        List<Product> sampleProducts = new ArrayList<>();
        sampleProducts.add(new Product("Pepperoni Pizza", "Delicious pepperoni pizza", 13.10, "Pizza"));
        sampleProducts.add(new Product("Cheese Burger", "Juicy cheese burger", 13.10, "Burger"));
        sampleProducts.add(new Product("Vegetable Pizza", "Fresh vegetable pizza", 12.50, "Pizza"));
        sampleProducts.add(new Product("Chicken Burger", "Tasty chicken burger", 14.00, "Burger"));
        
        bestFoodRecyclerView.postDelayed(() -> {
            progressBarBestFood.setVisibility(View.GONE);
            bestFoodRecyclerView.setVisibility(View.VISIBLE);
            bestFoodAdapter.updateList(sampleProducts);
        }, 1000);
    }
    
    private void loadCategories() {
        progressBarCategory.setVisibility(View.VISIBLE);
        categoryRecyclerView.setVisibility(View.GONE);
        
        // TODO: Implement API call to get categories
        // Call<List<Category>> call = apiInterface.getCategories();
        // call.enqueue(new Callback<List<Category>>() {
        //     @Override
        //     public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
        //         progressBarCategory.setVisibility(View.GONE);
        //         categoryRecyclerView.setVisibility(View.VISIBLE);
        //         if (response.isSuccessful() && response.body() != null) {
        //             categoryAdapter.updateList(response.body());
        //         }
        //     }
        //     @Override
        //     public void onFailure(Call<List<Category>> call, Throwable t) {
        //         progressBarCategory.setVisibility(View.GONE);
        //         categoryRecyclerView.setVisibility(View.VISIBLE);
        //         Toast.makeText(CustomerHomeActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
        //     }
        // });
        
        // Temporary: Load sample data for testing
        List<Category> sampleCategories = new ArrayList<>();
        sampleCategories.add(new Category("Pizza", android.R.drawable.ic_menu_gallery)); // Replace with actual icon
        sampleCategories.add(new Category("Burger", android.R.drawable.ic_menu_gallery));
        sampleCategories.add(new Category("Hotdog", android.R.drawable.ic_menu_gallery));
        sampleCategories.add(new Category("Drink", android.R.drawable.ic_menu_gallery));
        
        categoryRecyclerView.postDelayed(() -> {
            progressBarCategory.setVisibility(View.GONE);
            categoryRecyclerView.setVisibility(View.VISIBLE);
            categoryAdapter.updateList(sampleCategories);
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
