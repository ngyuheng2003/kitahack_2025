package com.example.kitahack2025;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;

public class OrderDetails extends AppCompatActivity {

    private TextView tvOrderId, tvOrderId2, tvCustomerName, tvCustomerId, tvCustomerEmail, tvOrderDate;
    private RecyclerView recyclerView;
    private OrderProductAdapter adapter;
    private List<Product> productList;
    private String orderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get orderId from Intent
        orderId = getIntent().getStringExtra("orderId");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Init views
        tvOrderId = findViewById(R.id.TV_orderId);
        tvOrderId2 = findViewById(R.id.TV_orderId2);
        tvCustomerName = findViewById(R.id.TV_C_name);
        tvCustomerId = findViewById(R.id.TV_C_Id);
        tvCustomerEmail = findViewById(R.id.TV_C_email);
        tvOrderDate = findViewById(R.id.TV_orderDate);

        recyclerView = findViewById(R.id.orderProductList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        adapter = new OrderProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        ImageButton backBtn = findViewById(R.id.IB_Back5);
        backBtn.setOnClickListener(v -> finish());

        ImageButton donePackingBtn = findViewById(R.id.IB_donePacking);
        donePackingBtn.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(OrderDetails.this)
                    .setTitle("Confirm")
                    .setMessage("Are you sure the order is packed?")
                    .setPositiveButton("Yes", (dialogInterface, which) -> {

                        FirebaseDatabase.getInstance().getReference("orders")
                                .child(userId)
                                .child(orderId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String existingCode = snapshot.child("redeemCode").getValue(String.class);
                                            String customerEmail = snapshot.child("customerEmail").getValue(String.class);

                                            if (existingCode != null && !existingCode.isEmpty()) {
                                                android.graphics.Bitmap qrBitmap = generateQRCodeBitmap(existingCode);
                                                showQRCodeDialog(existingCode, () -> {
                                                    startActivity(new Intent(OrderDetails.this, OrderPacked.class));
                                                    finish();
                                                });

                                                if (qrBitmap != null) {
                                                    sendEmailWithQRCode(customerEmail, existingCode, qrBitmap);
                                                }
                                            } else {
                                                String redeemCode = java.util.UUID.randomUUID().toString();

                                                FirebaseDatabase.getInstance().getReference("orders")
                                                        .child(userId)
                                                        .child(orderId)
                                                        .child("status")
                                                        .setValue("packed");
                                                FirebaseDatabase.getInstance().getReference("orders")
                                                        .child(userId)
                                                        .child(orderId)
                                                        .child("orderPackedDate")
                                                        .setValue(getCurrentDateTime());

                                                FirebaseDatabase.getInstance().getReference("orders")
                                                        .child(userId)
                                                        .child(orderId)
                                                        .child("redeemCode")
                                                        .setValue(redeemCode)
                                                        .addOnSuccessListener(unused -> {
                                                            android.graphics.Bitmap qrBitmap = generateQRCodeBitmap(redeemCode);
                                                            if (qrBitmap != null) {
                                                                sendEmailWithQRCode(customerEmail, existingCode, qrBitmap);
                                                            }

                                                            showQRCodeDialog(existingCode, () -> {
                                                                startActivity(new Intent(OrderDetails.this, OrderPacked.class));
                                                                finish();
                                                            });

                                                        });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(OrderDetails.this, "Failed to check code.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("No", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(android.R.color.black));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(android.R.color.black));
            });

            dialog.show();
        });


        loadOrderDetails(orderId);
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


    private void showQRCodeDialog(String code, Runnable onOkClick) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(code, BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(32, 32, 32, 32);

            ImageView qrImage = new ImageView(this);
            qrImage.setImageBitmap(bitmap);
            layout.addView(qrImage);

            TextView message = new TextView(this);
            message.setText("Please redeem your item by showing this QR code within 7 days.");
            message.setPadding(0, 16, 0, 0);
            message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(message);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Scan to Redeem")
                    .setView(layout)
                    .setCancelable(false) // optional: prevent closing accidentally
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        if (onOkClick != null) onOkClick.run(); // <- safely navigate now
                    })
                    .create();

            dialog.setOnShowListener(d -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(android.graphics.Color.BLACK);
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
                    "Hi! Please redeem your item by showing the attached QR code within 7 days at the counter.");
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(emailIntent, "Send email using:"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send QR email.", Toast.LENGTH_SHORT).show();
        }
    }



    private void loadOrderDetails(String orderId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("orders")
                .child(userId)
                .child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get order/customer info
                            String name = snapshot.child("customerName").getValue(String.class);
                            String customerId = snapshot.child("customerId").getValue(String.class);
                            String email = snapshot.child("customerEmail").getValue(String.class);
                            String orderDate = snapshot.child("orderDate").getValue(String.class);

                            tvOrderId.setText(orderId);
                            tvOrderId2.setText("Order Id: " + orderId);
                            tvCustomerName.setText("Customer name: " + name);
                            tvCustomerId.setText("Customer ID: " + customerId);
                            tvCustomerEmail.setText("Email: " + email);
                            tvOrderDate.setText("Order date: " + orderDate);

                            // Load product list
                            productList.clear();
                            for (DataSnapshot itemSnap : snapshot.child("products").getChildren()) {
                                Product product = itemSnap.getValue(Product.class);
                                if (product != null) {
                                    productList.add(product);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private String getCurrentDateTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

}