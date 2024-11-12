package com.example.canteenconnect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomerMenuActivity extends AppCompatActivity {

    private ImageButton btnSideBar;
    private Button btnViewCart;
    private LinearLayout navDrawer;
    private TextView profile, logout, appUserId, appUserName;
    private LinearLayout profileDetails;
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerViewCategory, recyclerViewFoodItems;
    private CategoryAdapter categoryAdapter;
    private FoodItemAdapter foodItemAdapter;
    private ArrayList<Category> categoryList = new ArrayList<>();
    private ArrayList<FoodItem> foodItemList = new ArrayList<>();
    private ArrayList<FoodItem> cartList = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private FirebaseDatabase database;
    private DatabaseReference menuRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_menu);

        btnSideBar = findViewById(R.id.btnSideBar);
        navDrawer = findViewById(R.id.navDrawer);
        profile = findViewById(R.id.profile);
        logout = findViewById(R.id.logout);
        appUserId = findViewById(R.id.appUserId);
        appUserName = findViewById(R.id.appUserName);
        profileDetails = findViewById(R.id.profileDetails);
        recyclerViewCategory = findViewById(R.id.recyclerViewCategory);
        recyclerViewFoodItems = findViewById(R.id.recyclerViewFoodItems);
        btnViewCart = findViewById(R.id.btnLogout);

        sharedPreferences = getSharedPreferences("CanteenConnectPrefs", MODE_PRIVATE);

        database = FirebaseDatabase.getInstance();
        menuRef = database.getReference("CanteenConnect/Menu");

        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFoodItems.setLayoutManager(new LinearLayoutManager(this));

        categoryAdapter = new CategoryAdapter(categories, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(String category) {
                loadFoodItems(category);
            }
        });
        recyclerViewCategory.setAdapter(categoryAdapter);

        foodItemAdapter = new FoodItemAdapter(CustomerMenuActivity.this, new ArrayList<>(), new FoodItemAdapter.OnFoodItemClickListener() {
            @Override
            public void onFoodItemClick(FoodItem foodItem) {
                addToCart(foodItem);
            }
        });
        recyclerViewFoodItems.setAdapter(foodItemAdapter);

        setupSideNavigation();
        loadCategories();
        setupViewCart();
        setupLogout();
    }

    private void setupSideNavigation() {
        btnSideBar.setOnClickListener(view -> {
            if (navDrawer.getVisibility() == View.GONE) {
                navDrawer.setVisibility(View.VISIBLE);
            } else {
                navDrawer.setVisibility(View.GONE);
            }
        });

        profile.setOnClickListener(view -> {
            String userId = sharedPreferences.getString("userId", "N/A");
            String userName = sharedPreferences.getString("userName", "N/A");

            appUserId.setText("User ID: " + userId);
            appUserName.setText("User Name: " + userName);
            profileDetails.setVisibility(View.VISIBLE);
        });
    }

    private void setupViewCart() {
        btnViewCart.setOnClickListener(view -> {
            String userId = sharedPreferences.getString("userId", "");
            Intent intent = new Intent(CustomerMenuActivity.this, CartActivity.class);
            intent.putExtra("userId", userId);
            intent.putParcelableArrayListExtra("cartItems", cartList);
            startActivity(intent);
        });
    }

    private void setupLogout() {
        logout.setOnClickListener(view -> {
            sharedPreferences.edit().clear().apply();
            Intent intent = new Intent(CustomerMenuActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadCategories() {
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryName = categorySnapshot.getKey();
                    categoryList.add(new Category(categoryName));
                }
                List<String> categoryNames = new ArrayList<>();
                for (Category category : categoryList) {
                    categoryNames.add(category.getName());
                }
                categoryAdapter = new CategoryAdapter(categoryNames, category -> loadFoodItems(category));
                recyclerViewCategory.setAdapter(categoryAdapter);
                if (!categoryList.isEmpty()) {
                    loadFoodItems(categoryList.get(0).getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerMenuActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFoodItems(String category) {
        menuRef.child(category).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodItemList.clear();
                for (DataSnapshot foodItemSnapshot : snapshot.getChildren()) {
                    String name = foodItemSnapshot.getKey();
                    int price = foodItemSnapshot.getValue(Integer.class);
                    foodItemList.add(new FoodItem(name, price));
                }
                foodItemAdapter = new FoodItemAdapter(CustomerMenuActivity.this, foodItemList, foodItem -> addToCart(foodItem));
                recyclerViewFoodItems.setAdapter(foodItemAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerMenuActivity.this, "Failed to load food items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart(FoodItem foodItem) {
        cartList.add(foodItem);
        Toast.makeText(this, foodItem.getName() + " added to cart", Toast.LENGTH_SHORT).show();
    }

}
