package com.example.kitahack2025;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ScanResult extends AppCompatActivity {

    TextView tvProductName, tvBarcode, tvPoints, tvWeight, tvCategory, tvExpiry, tvQuantity, tvProductName2;
    ImageButton btnBack, btnMinus, btnPlus, btnManageStock;
    String userId = "user123"; // Replace with actual user authentication
    int currentQuantity = 0;
    String productId;
    FirebaseFirestore db;
    DocumentReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get product ID (barcodeId) passed from previous activity
        productId = getIntent().getStringExtra("productId");

        // Find Views
        tvProductName = findViewById(R.id.TV_productName);
        tvProductName2 = findViewById(R.id.TV_productName2);
        tvBarcode = findViewById(R.id.TV_barcodeId);
        tvPoints = findViewById(R.id.TV_points);
        tvWeight = findViewById(R.id.TV_weight);
        tvCategory = findViewById(R.id.TV_category);
        tvExpiry = findViewById(R.id.TV_expiredDate);
        tvQuantity = findViewById(R.id.TV_quantity);
        btnBack = findViewById(R.id.IB_Back2);
        btnManageStock = findViewById(R.id.IB_manageStock);

        // Firebase reference
        db = FirebaseFirestore.getInstance();
        productRef = db.collection("userStock").document(userId).collection("items").document(productId);

        // Fetch and display data from Firestore
        productRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    tvProductName.setText(snapshot.getString("name"));
                    tvBarcode.setText(snapshot.getString("barcodeID"));
                    tvPoints.setText(snapshot.getString("points"));
                    tvWeight.setText(snapshot.getString("weight"));
                    tvCategory.setText(snapshot.getString("category"));
                    tvExpiry.setText(snapshot.getString("expiry"));
                    currentQuantity = snapshot.getLong("quantity").intValue();
                    tvQuantity.setText(String.valueOf(currentQuantity));
                    tvProductName2.setText(snapshot.getString("name"));
                } else {
                    Toast.makeText(ScanResult.this, "Product not found in user stock", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ScanResult.this, "Error fetching product details", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button
        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(ScanResult.this, StockPage.class);
            startActivity(i);
            finish();
        });

        btnMinus = findViewById(R.id.IB_minus);
        btnPlus = findViewById(R.id.IB_plus);

        // Button: Minus
        btnMinus.setOnClickListener(v -> {
            if (currentQuantity > 0) {
                currentQuantity--;
                tvQuantity.setText(String.valueOf(currentQuantity));
            }
        });

        // Button: Plus
        btnPlus.setOnClickListener(v -> {
            currentQuantity++;
            tvQuantity.setText(String.valueOf(currentQuantity));
        });

        // Manage Stock button (save updated quantity back to Firestore)
        btnManageStock.setOnClickListener(v -> {
            productRef.update("quantity", currentQuantity).addOnSuccessListener(aVoid -> {
                Toast.makeText(ScanResult.this, "Quantity updated!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(ScanResult.this, "Failed to update quantity!", Toast.LENGTH_SHORT).show();
            });
        });
    }
}