package com.example.canteenconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private ArrayList<Order> ordersList;

    public OrderAdapter(Context context, ArrayList<Order> ordersList) {
        this.context = context;
        this.ordersList = ordersList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = ordersList.get(position);

        // Set Order ID
        holder.orderIdTextView.setText("Order ID: " + order.getOrderId());

        // Format items with their quantities
        StringBuilder itemsText = new StringBuilder();
        for (Map.Entry<String, Integer> entry : order.getItemsWithQuantity().entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            itemsText.append(itemName).append(": ").append(quantity).append("\n");
        }
        holder.orderItemsTextView.setText(itemsText.toString().trim());
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, orderItemsTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            orderItemsTextView = itemView.findViewById(R.id.orderItemsTextView);
        }
    }
}
