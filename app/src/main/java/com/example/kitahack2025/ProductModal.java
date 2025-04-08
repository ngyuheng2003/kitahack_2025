package com.example.kitahack2025;

public class ProductModal {
    public String productId; // ID key from Firebase
    public String productName;
    public String barcodeId;
    public String expiryDate;
    public String weight;
    public String category;
    public int points;
    public int quantity;
    public String imageUrl;

    public ProductModal() {
        // Default constructor required for calls to DataSnapshot.getValue(ProductModal.class)
    }

    public ProductModal(String barcode, String unknownProduct, String number, String s, String unknown, String string, String url) {}

    public ProductModal(String productName, String barcodeId, int points, String expiryDate,
                        String weight, int quantity, String category, String imageUrl) {
        this.productName = productName;
        this.barcodeId = barcodeId;
        this.points = points;
        this.expiryDate = expiryDate;
        this.weight = weight;
        this.quantity = quantity;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getProductId() {
        return productId;
    }
}
