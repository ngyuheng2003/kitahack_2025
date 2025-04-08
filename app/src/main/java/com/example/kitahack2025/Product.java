package com.example.kitahack2025;

public class Product {
    private String productName;
    private int quantity;

    public Product() {} // Needed for Firebase

    public Product(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }
}
