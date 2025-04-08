package com.example.kitahack2025;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.MultiFormatWriter;

import java.util.ArrayList;
import java.util.List;

public class OrderPending extends AppCompatActivity implements ppOrderAdapter.OnOrderActionListener {

    private RecyclerView recyclerView;
    private ppOrderAdapter adapter;
    private List<OrderModal> orderList;
    private DatabaseReference ordersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_pending);

        recyclerView = findViewById(R.id.orderPendingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new ppOrderAdapter(this, orderList, this, false); // false = show orderDate
        recyclerView.setAdapter(adapter);
        ImageButton btnPacked = findViewById(R.id.IB_Packed);
        btnPacked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderPending.this, OrderPacked.class);
                startActivity(intent);
            }
        });
        ImageButton btnBack = findViewById(R.id.IB_Back3);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderPending.this, Home.class);
                startActivity(intent);
            }
        });

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders").child(userId);
        loadPendingOrders();
    }

    private void loadPendingOrders() {
        Query query = ordersRef.orderByChild("status").equalTo("pending");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    OrderModal order = data.getValue(OrderModal.class);
                    if (order != null) {
                        order.setRequestId(data.getKey());
                        orderList.add(order);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onItemClick(OrderModal order) {
        Intent intent = new Intent(this, OrderDetails.class);
        intent.putExtra("orderId", order.getRequestId());
        startActivity(intent);
    }

    @Override
    public void onPackClick(OrderModal order) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        AlertDialog dialog = new AlertDialog.Builder(OrderPending.this)
                .setTitle("Confirm")
                .setMessage("Are you sure the order is packed?")
                .setPositiveButton("Yes", (dialogInterface, which) -> {

                    FirebaseDatabase.getInstance().getReference("orders")
                            .child(userId)
                            .child(order.getRequestId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String existingCode = snapshot.child("redeemCode").getValue(String.class);
                                        String customerEmail = snapshot.child("customerEmail").getValue(String.class);

                                        if (existingCode != null && !existingCode.isEmpty()) {
                                            Bitmap qrBitmap = generateQRCodeBitmap(existingCode);
                                            showQRCodeDialog(existingCode);
                                            if (qrBitmap != null) {
                                                sendEmailWithQRCode(customerEmail, existingCode, qrBitmap);
                                            }
                                        } else {
                                            String redeemCode = java.util.UUID.randomUUID().toString();

                                            FirebaseDatabase.getInstance().getReference("orders")
                                                    .child(userId)
                                                    .child(order.getRequestId())
                                                    .child("status")
                                                    .setValue("packed");
                                            FirebaseDatabase.getInstance().getReference("orders")
                                                    .child(userId)
                                                    .child(order.getRequestId())
                                                    .child("orderPackedDate")
                                                    .setValue(getCurrentDateTime());
                                            FirebaseDatabase.getInstance().getReference("orders")
                                                    .child(userId)
                                                    .child(order.getRequestId())
                                                    .child("redeemCode")
                                                    .setValue(redeemCode)
                                                    .addOnSuccessListener(unused -> {
                                                        Bitmap qrBitmap = generateQRCodeBitmap(redeemCode);
                                                        showQRCodeDialog(redeemCode);
                                                        if (qrBitmap != null) {
                                                            sendEmailWithQRCode(customerEmail, redeemCode, qrBitmap);
                                                        }
                                                    });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(OrderPending.this, "Failed to check code.", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(android.R.color.black));
        });

        dialog.show();
    }


    private android.graphics.Bitmap generateQRCodeBitmap(String content) {
        try {
            com.google.zxing.MultiFormatWriter writer = new com.google.zxing.MultiFormatWriter();
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(
                    content,
                    com.google.zxing.BarcodeFormat.QR_CODE,
                    600,
                    600
            );
            com.journeyapps.barcodescanner.BarcodeEncoder encoder = new com.journeyapps.barcodescanner.BarcodeEncoder();
            return encoder.createBitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void showQRCodeDialog(String code) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(code,
                    com.google.zxing.BarcodeFormat.QR_CODE, 600, 600);

            com.journeyapps.barcodescanner.BarcodeEncoder encoder = new com.journeyapps.barcodescanner.BarcodeEncoder();
            android.graphics.Bitmap bitmap = encoder.createBitmap(bitMatrix);

            android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(32, 32, 32, 32);

            android.widget.ImageView qrImage = new android.widget.ImageView(this);
            qrImage.setImageBitmap(bitmap);
            layout.addView(qrImage);

            android.widget.TextView message = new android.widget.TextView(this);
            message.setText("Please redeem your item by showing this QR code within 7 days.");
            message.setPadding(0, 16, 0, 0);
            message.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
            layout.addView(message);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Scan to Redeem")
                    .setView(layout)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        startActivity(new Intent(OrderPending.this, OrderPacked.class));
                        finish();
                    })
                    .create();

            dialog.setOnShowListener(d -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(android.graphics.Color.BLACK);  // ✅ force black using Color.BLACK
            });

            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendEmailWithQRCode(String toEmail, String redeemCode, android.graphics.Bitmap qrBitmap) {
        try {
            // Step 1: Save QR bitmap to cache as PNG
            java.io.File cachePath = new java.io.File(getCacheDir(), "qr");
            cachePath.mkdirs(); // make dir if not exists
            java.io.File file = new java.io.File(cachePath, "redeem_qr.png");

            java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
            qrBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Step 2: Get content URI using FileProvider
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider", // 👈 must match manifest
                    file
            );

            // Step 3: Create email intent with attachment
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("application/image");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{toEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your Redeem QR Code");
            emailIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hi! Please redeem your item by showing the attached QR code within 7 days at the locker.");
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(emailIntent, "Send email using:"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send QR email.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDateTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}