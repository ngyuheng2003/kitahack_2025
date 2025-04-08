package com.example.kitahack2025;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {
    private TextInputEditText editName, editPoints, editWeight, editCategory, editExpiry;
    private Button btnAddProduct;
    private String scannedBarcode;
    private String userId = "user123"; // Replace with dynamic user authentication if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Get barcode from intent
        scannedBarcode = getIntent().getStringExtra("barcode");

        // Initialize UI elements
        editName = findViewById(R.id.edit_name);
        editPoints = findViewById(R.id.edit_points);
        editWeight = findViewById(R.id.edit_weight);
        editCategory = findViewById(R.id.edit_category);
        editExpiry = findViewById(R.id.edit_expiry);
        btnAddProduct = findViewById(R.id.btn_add_product);

        // Save product to Firestore
        btnAddProduct.setOnClickListener(v -> saveProductToFirestore());
    }

    private void saveProductToFirestore() {
        String name = editName.getText().toString().trim();
        String points = editPoints.getText().toString().trim();
        String weight = editWeight.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String expiry = editExpiry.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Name and Category are required", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> product = new HashMap<>();
        product.put("barcodeID", scannedBarcode);
        product.put("name", name);
        product.put("points", points);
        product.put("weight", weight);
        product.put("category", category);
        product.put("expiry", expiry);

        db.collection("products").document(scannedBarcode)
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    // Also add to user's stock database
                    Map<String, Object> userProduct = new HashMap<>(product);
                    userProduct.put("quantity", 0); // Default quantity when added

                    db.collection("userStock").document(userId)
                            .collection("items").document(scannedBarcode)
                            .set(userProduct)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Product Added and Stock Updated!", Toast.LENGTH_SHORT).show();

                                // Redirect to ProductDetails
                                Intent intent = new Intent(AddProductActivity.this, ProductDetails.class);
                                intent.putExtra("productId", scannedBarcode);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error updating stock", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error adding product", Toast.LENGTH_SHORT).show());
    }
}
