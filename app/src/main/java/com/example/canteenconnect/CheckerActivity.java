package com.example.canteenconnect;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class CheckerActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private ArrayList<Order> ordersList = new ArrayList<>();
    private DatabaseReference ordersRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checker);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, ordersList);
        ordersRecyclerView.setAdapter(orderAdapter);

        ordersRef = FirebaseDatabase.getInstance().getReference("CanteenConnect/RegisteredUsers");

        loadOrders();

        // Swipe to delete an order
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Order order = ordersList.get(position);

                // Remove orderId from database
                deleteOrder(order.getCustomerId(), order.getOrderId());

                // Remove from list and notify adapter
                ordersList.remove(position);
                orderAdapter.notifyItemRemoved(position);
            }
        }).attachToRecyclerView(ordersRecyclerView);
    }

    private void loadOrders() {
        // Set up a ChildEventListener to listen for real-time updates
        ordersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot userSnapshot, String previousChildName) {

                // Ensure the snapshot is valid
                if (userSnapshot.child("order").exists()) {
                    for (DataSnapshot orderSnapshot : userSnapshot.child("order").getChildren()) {
                        String orderId = orderSnapshot.getKey();
                        HashMap<String, Integer> itemsWithQuantity = new HashMap<>();

                        // Retrieve item name and quantity from the snapshot
                        for (DataSnapshot itemSnapshot : orderSnapshot.getChildren()) {
                            String itemName = itemSnapshot.getKey();
                            Integer quantity = itemSnapshot.getValue(Integer.class);
                            if (itemName != null && quantity != null) {
                                itemsWithQuantity.put(itemName, quantity);
                            }
                        }

                        // Add the new order to the list and notify adapter
                        ordersList.add(new Order(userSnapshot.getKey(), orderId, itemsWithQuantity));
                        orderAdapter.notifyItemInserted(ordersList.size() - 1);  // Notify adapter of new item
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String customerId = snapshot.getKey();

                // Remove order if deleted
                if (snapshot.child("order").exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.child("order").getChildren()) {
                        String orderId = orderSnapshot.getKey();
                        // Remove the order from the list if deleted
                        for (int i = 0; i < ordersList.size(); i++) {
                            if (ordersList.get(i).getOrderId().equals(orderId)) {
                                ordersList.remove(i);
                                orderAdapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CheckerActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteOrder(String customerId, String orderId) {
        // Remove  orderId node under the specific customerId
        ordersRef.child(customerId).child("order").child(orderId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CheckerActivity.this, "Order deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CheckerActivity.this, "Failed to delete order", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
