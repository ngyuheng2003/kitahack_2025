package com.example.kitahack2025;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AssignLockerActivity extends AppCompatActivity {

    private TextView requestIDText, userLocationText, foodBankText, assignedLockerText;
    private Button assignLockerButton;
    private FirebaseFirestore db;
    private String requestID, userLocation, foodBank, assignedLockerID;
    private List<String> availableLockers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_locker);

        db = FirebaseFirestore.getInstance();

        // Get data from intent
        Intent intent = getIntent();
        requestID = intent.getStringExtra("requestID");
        userLocation = intent.getStringExtra("userLocation");
        foodBank = intent.getStringExtra("foodBank");

        // Initialize UI elements
        requestIDText = findViewById(R.id.tvRequestID);
        userLocationText = findViewById(R.id.tvUserLocation);
        foodBankText = findViewById(R.id.tvFoodBank);
        assignedLockerText = findViewById(R.id.tvAssignedLocker);
        assignLockerButton = findViewById(R.id.btnAssignLocker);

        // Display order details
        requestIDText.setText("Request ID: " + requestID);
        userLocationText.setText("User Location: " + userLocation);
        foodBankText.setText("Food Bank: " + foodBank);

        // Check if order already has an assigned locker
        checkIfLockerAssigned();

        // Assign locker on button click
        assignLockerButton.setOnClickListener(view -> assignLocker());
    }

    private void checkIfLockerAssigned() {
        db.collection("orders").document(requestID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        assignedLockerID = documentSnapshot.getString("lockerID");

                        if (assignedLockerID != null && !assignedLockerID.isEmpty()) {
                            // Order already has a locker assigned
                            assignedLockerText.setText("Assigned Locker: " + assignedLockerID);
                            assignLockerButton.setEnabled(false); // Disable button
                        } else {
                            // Load available lockers if no locker assigned
                            loadAvailableLockers();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AssignLocker", "Error checking assigned locker", e));
    }

    private void loadAvailableLockers() {
        db.collection("locker").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                availableLockers.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String lockerID = document.getId();
                    availableLockers.add(lockerID);
                }
                Log.d("AssignLocker", "Available lockers: " + availableLockers);
            } else {
                Log.e("AssignLocker", "Error getting lockers", task.getException());
            }
        });
    }

    private void assignLocker() {
        if (availableLockers.isEmpty()) {
            Toast.makeText(this, "No available lockers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Randomly select a locker
        String selectedLocker = availableLockers.get(new Random().nextInt(availableLockers.size()));
        assignedLockerText.setText("Assigned Locker: " + selectedLocker);

        // Check if the order already has a locker assigned
        db.collection("orders").document(requestID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("locker_ID")) {
                        String existingLocker = documentSnapshot.getString("locker_ID");
                        if (existingLocker != null && !existingLocker.isEmpty()) {
                            Toast.makeText(this, "This order already has a locker assigned!", Toast.LENGTH_SHORT).show();
                            assignLockerButton.setEnabled(false);
                            assignedLockerText.setText("Assigned Locker: " + existingLocker);
                            return;
                        }
                    }

                    // If no locker assigned, update orders collection
                    db.collection("orders").document(requestID)
                            .update("locker_ID", selectedLocker)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AssignLocker", "Updated orders collection: locker_ID assigned.");

                                // Update locker collection with requestID and foodBank
                                db.collection("locker").document(selectedLocker)
                                        .update("requestID", requestID, "foodBank", foodBank)
                                        .addOnSuccessListener(lockerUpdate -> {
                                            Toast.makeText(this, "Locker assigned successfully", Toast.LENGTH_SHORT).show();
                                            assignLockerButton.setEnabled(false); // Disable button after assignment
                                            Log.d("AssignLocker", "Locker " + selectedLocker + " assigned to request " + requestID);
                                        })
                                        .addOnFailureListener(e -> Log.e("AssignLocker", "Error updating locker data", e));
                            })
                            .addOnFailureListener(e -> Log.e("AssignLocker", "Error updating orders collection", e));
                })
                .addOnFailureListener(e -> Log.e("AssignLocker", "Error fetching order data", e));
    }

}
