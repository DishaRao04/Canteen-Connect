package com.example.canteenconnect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements FoodItemAdapter.OnFoodItemClickListener {

    private RecyclerView recyclerView;
    private FoodItemAdapter foodItemAdapter;
    private ArrayList<FoodItem> foodItemsInCart;
    private TextView totalPriceTextView;
    private Button makePaymentButton;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerViewCart);
        totalPriceTextView = findViewById(R.id.totalPrice);
        makePaymentButton = findViewById(R.id.makePaymentButton);
        foodItemsInCart = new ArrayList<>();
        userId = getIntent().getStringExtra("userId");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodItemAdapter = new FoodItemAdapter(this, foodItemsInCart, this);
        recyclerView.setAdapter(foodItemAdapter);

        // Swipe to remove food item
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                foodItemsInCart.remove(position);
                foodItemAdapter.notifyItemRemoved(position);
                updateTotalPrice();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        makePaymentButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                // Check if cart has items
                if (foodItemsInCart.isEmpty()) {
                    Toast.makeText(CartActivity.this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare data to store in Firebase
                databaseReference = FirebaseDatabase.getInstance().getReference("CanteenConnect")
                        .child("RegisteredUsers").child(userId).child("order");
                String orderId = databaseReference.push().getKey();

                if (orderId != null) {
                    Map<String, Integer> quantityMap = new HashMap<>();

                    // Calculate total quantity for each item
                    for (FoodItem item : foodItemsInCart) {
                        String itemName = item.getName();
                        quantityMap.put(itemName, quantityMap.getOrDefault(itemName, 0) + 1);
                    }

                    // Store each item with its quantity in Firebase under the orderId
                    Map<String, Object> orderMap = new HashMap<>();
                    for (Map.Entry<String, Integer> entry : quantityMap.entrySet()) {
                        String itemName = entry.getKey();
                        int quantity = entry.getValue();
                        orderMap.put(itemName, quantity); // Store directly under orderId
                    }

                    databaseReference.child(orderId).setValue(orderMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Pass data to PaymentActivity
                            Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                            intent.putExtra("cartItems", foodItemsInCart);
                            intent.putExtra("totalPrice", totalPriceTextView.getText().toString());
                            intent.putExtra("orderId", orderId);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CartActivity.this, "Failed to store order", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Retrieve cart data from the Intent
        ArrayList<FoodItem> cartItems = getIntent().getParcelableArrayListExtra("cartItems");
        if (cartItems != null) {
            foodItemsInCart.addAll(cartItems);
            foodItemAdapter.notifyDataSetChanged();
            updateTotalPrice();
        }
    }

    private void updateTotalPrice() {
        double total = 0;
        for (FoodItem item : foodItemsInCart) {
            total += item.getPrice();
        }
        totalPriceTextView.setText("Total: ₹" + total);
    }

    @Override
    public void onFoodItemClick(FoodItem foodItem) {
        Toast.makeText(this, foodItem.getName() + " clicked", Toast.LENGTH_SHORT).show();
    }

}
