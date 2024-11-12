package com.example.canteenconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity {


    private TextView orderDetailsTextView;
    private Button backToMenuButton;
    private ArrayList<FoodItem> cartItems;
    private String totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        orderDetailsTextView = findViewById(R.id.orderDetails);
        backToMenuButton = findViewById(R.id.backToMenuButton);

        cartItems = getIntent().getParcelableArrayListExtra("cartItems");
        totalPrice = getIntent().getStringExtra("totalPrice");
        String orderId = getIntent().getStringExtra("orderId");

        // Display order details
        StringBuilder orderDetails = new StringBuilder("Your Order: "+orderId+"\n\n");
        for (FoodItem item : cartItems) {
            orderDetails.append(item.getName()).append(" - ₹").append(item.getPrice()).append("\n");
        }
        orderDetails.append("\n").append(totalPrice);
        orderDetailsTextView.setText(orderDetails.toString());

        // Handle back to menu button
        backToMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, CustomerMenuActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
