package com.example.canteenconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder> {

    private Context context;
    private List<FoodItem> foodItems;
    private OnFoodItemClickListener listener;

    public interface OnFoodItemClickListener {
        void onFoodItemClick(FoodItem foodItem);
    }

    public FoodItemAdapter(Context context, List<FoodItem> foodItems, OnFoodItemClickListener listener) {
        this.context = context;
        this.foodItems = foodItems;
        this.listener = listener;
    }

    @Override
    public FoodItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the food item layout
        View view = LayoutInflater.from(context).inflate(R.layout.food_item, parent, false);
        return new FoodItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FoodItemViewHolder holder, int position) {
        // Bind the food item data to the view holder
        FoodItem foodItem = foodItems.get(position);
        holder.bind(foodItem);
    }

    @Override
    public int getItemCount() {
        // Return the size of the food items list
        return foodItems.size();
    }

    // ViewHolder class to hold references to the views for each food item
    class FoodItemViewHolder extends RecyclerView.ViewHolder {
        private TextView foodName, foodPrice;

        public FoodItemViewHolder(View itemView) {
            super(itemView);
            // Initialize the views
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
        }

        // Bind food item data to the views
        public void bind(final FoodItem foodItem) {
            foodName.setText(foodItem.getName());
            foodPrice.setText("₹ " + foodItem.getPrice());

            // Set click listener for each item
            itemView.setOnClickListener(v -> listener.onFoodItemClick(foodItem));
        }
    }

}
