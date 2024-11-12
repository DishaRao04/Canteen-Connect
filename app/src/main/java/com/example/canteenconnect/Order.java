package com.example.canteenconnect;

import java.util.HashMap;

public class Order {
    private String customerId;
    private String orderId;
    private HashMap<String, Integer> itemsWithQuantity;

    public Order(String customerId, String orderId, HashMap<String, Integer> itemsWithQuantity) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.itemsWithQuantity = itemsWithQuantity;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public HashMap<String, Integer> getItemsWithQuantity() {
        return itemsWithQuantity;
    }

}

