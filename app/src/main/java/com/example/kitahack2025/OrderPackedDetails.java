package com.example.kitahack2025;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class OrderPackedDetails extends AppCompatActivity {

    private TextView tvOrderId, tvOrderId2, tvCustomerName, tvCustomerId, tvCustomerEmail, tvOrderDate;
    private RecyclerView recyclerView;
    private OrderProductAdapter adapter;
    private List<Product> productList;
    private String orderId;
    private String redeemCode;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_packed_details);

        orderId = getIntent().getStringExtra("orderId");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvOrderId = findViewById(R.id.TV_orderId);
        tvOrderId2 = findViewById(R.id.TV_orderId2);
        tvCustomerName = findViewById(R.id.TV_C_name);
        tvCustomerId = findViewById(R.id.TV_C_Id);
        tvCustomerEmail = findViewById(R.id.TV_C_email);
        tvOrderDate = findViewById(R.id.TV_orderDate);
        recyclerView = findViewById(R.id.orderProductList);

        productList = new ArrayList<>();
        adapter = new OrderProductAdapter(this, productList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ImageButton backBtn = findViewById(R.id.IB_Back5);
        backBtn.setOnClickListener(v -> finish());

        ImageButton showQRBtn = findViewById(R.id.IB_showqr);
        showQRBtn.setOnClickListener(v -> {
            if (redeemCode != null) {
                showQRCodeDialog(redeemCode);
            }
        });

        loadOrderDetails(orderId);

    }

    private void loadOrderDetails(String orderId) {
        FirebaseDatabase.getInstance().getReference("orders").child(userId).child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            tvOrderId.setText(orderId);
                            tvOrderId2.setText("Order ID: " + orderId);
                            tvCustomerName.setText("Customer name: " + snapshot.child("customerName").getValue(String.class));
                            tvCustomerId.setText("Customer ID: " + snapshot.child("customerId").getValue(String.class));
                            tvCustomerEmail.setText("Email: " + snapshot.child("customerEmail").getValue(String.class));
                            tvOrderDate.setText("Order Packed Date: " + snapshot.child("orderPackedDate").getValue(String.class));
                            redeemCode = snapshot.child("redeemCode").getValue(String.class);

                            productList.clear();
                            for (DataSnapshot item : snapshot.child("products").getChildren()) {
                                Product p = item.getValue(Product.class);
                                if (p != null) productList.add(p);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showQRCodeDialog(String code) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(code, BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            ImageView qrView = new ImageView(this);
            qrView.setImageBitmap(bitmap);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Redeem QR Code")
                    .setView(qrView)
                    .setPositiveButton("OK", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
            });

            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}