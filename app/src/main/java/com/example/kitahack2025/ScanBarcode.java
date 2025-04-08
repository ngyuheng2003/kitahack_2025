package com.example.kitahack2025;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import android.Manifest;


public class ScanBarcode extends AppCompatActivity {
    SurfaceView cameraView;
    BarcodeDetector barcode;
    CameraSource cameraSource;
    SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        cameraView = (SurfaceView) findViewById(R.id.cameraView);
        cameraView.setZOrderMediaOverlay(true);
        holder = cameraView.getHolder();
        barcode = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();
        if(!barcode.isOperational()){
            Toast.makeText(getApplicationContext(), "Sorry, Couldn't setup the detector", Toast.LENGTH_LONG).show();
            this.finish();
        }
        cameraSource = new CameraSource.Builder(this, barcode)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1920,1024)
                .build();
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try{
                    if(ContextCompat.checkSelfPermission(ScanBarcode.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        cameraSource.start(cameraView.getHolder());
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        barcode.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }


            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {
                    String scannedBarcode = barcodes.valueAt(0).displayValue;

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("products").document(scannedBarcode).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // If product exists, go to ProductDetails instead of ScanResultActivity
                                    Intent intent = new Intent(ScanBarcode.this, ScanResult.class);
                                    intent.putExtra("productId", scannedBarcode);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If product not found, ask user to enter details
                                    Intent intent = new Intent(ScanBarcode.this, AddProductActivity.class);
                                    intent.putExtra("barcode", scannedBarcode);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(ScanBarcode.this, "Error fetching data", Toast.LENGTH_SHORT).show());
                }
            }



           /* private void addNewProductToFirestore(String barcode) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Example default product data (replace with user input or API data)
                ProductModal newProduct = new ProductModal(
                        barcode,  // barcodeID
                        "Unknown Product", // name
                        "0", // points
                        "N/A", // weight
                        "Unknown", // category
                        "N/A", // expiry
                        "https://example.com/default-image.png" // Default image URL
                );

                db.collection("products").document(barcode)
                        .set(newProduct)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(ScanBarcode.this, "New product added!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(ScanBarcode.this, "Error adding product", Toast.LENGTH_SHORT).show());
            }*/


        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

}