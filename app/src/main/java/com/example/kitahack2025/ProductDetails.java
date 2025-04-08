package com.example.kitahack2025;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProductDetails extends AppCompatActivity {


    TextView tvProductName, tvBarcode, tvPoints, tvWeight, tvCategory, tvExpiry, tvQuantity, tvProductName2;
    ImageButton btnBack, btnMinus, btnPlus, btnManageStock;

    int currentQuantity = 0;
    String productId;
    DatabaseReference productRef;

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


        // Get current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get product ID (barcodeId)
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
        btnMinus = findViewById(R.id.IB_minus);
        btnPlus = findViewById(R.id.IB_plus);


        productRef = FirebaseDatabase.getInstance()
                .getReference("userStock").child(userId).child(productId);

        // Fetch and display data
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ProductModal product = snapshot.getValue(ProductModal.class);
                if (product != null) {
                    tvProductName.setText(product.productName);
                    tvBarcode.setText(product.barcodeId);
                    tvPoints.setText(String.valueOf(product.points));
                    tvWeight.setText(product.weight);
                    tvCategory.setText(product.category);

                    tvExpiry.setText("Expiry Date:" + product.expiryDate);

                    currentQuantity = product.quantity;
                    tvQuantity.setText(String.valueOf(currentQuantity));
                    tvProductName2.setText(product.productName);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });

        // Back button
        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(ProductDetails.this, StockPage.class);
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


        btnManageStock.setOnClickListener(v -> {
            productRef.child("quantity").setValue(currentQuantity).addOnSuccessListener(unused -> {
                Toast.makeText(ProductDetails.this, "Quantity updated!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(ProductDetails.this, "Failed to update!", Toast.LENGTH_SHORT).show();
            });
        });

    }
}