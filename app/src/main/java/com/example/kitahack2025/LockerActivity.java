package com.example.kitahack2025;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LockerActivity extends AppCompatActivity implements OrderAdapter.OnMapClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private RecyclerView orderListRecyclerView;
    private FirebaseFirestore db;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    private Geocoder geocoder;

    private List<FoodBank> foodBankList = new ArrayList<>(); // Store food banks here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker);

        db = FirebaseFirestore.getInstance();
        geocoder = new Geocoder(this, Locale.getDefault());

        // Initialize RecyclerView
        orderListRecyclerView = findViewById(R.id.order_list);
        orderListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load food banks from Firestore
        loadFoodBanks();

        // Load orders from Firestore
        retrieveOrders();

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadFoodBanks() {
        // Load all food banks from Firestore
        db.collection("foodbanks").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                foodBankList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    double foodBankLat = document.getDouble("latitude");
                    double foodBankLon = document.getDouble("longitude");
                    String foodBankName = document.getString("name");

                    foodBankList.add(new FoodBank(foodBankName, foodBankLat, foodBankLon));
                }
            } else {
                Log.e("LockerActivity", "Error loading food banks: ", task.getException());
            }
        });
    }

    private void retrieveOrders() {
        db.collection("orders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                orderList.clear();
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    String requestID = document.getString("requestID");
                    double latitude = document.getDouble("latitude");
                    double longitude = document.getDouble("longitude");

                    // Get user location name from lat/lng
                    String userLocationName = getUserLocationName(latitude, longitude);
                    LatLng userLocation = new LatLng(latitude, longitude);

                    // Find the nearest food bank
                    String nearestFoodBank = getNearestFoodBank(userLocation);
                    Log.d("NearestFoodBank", "Nearest food bank for requestID " + requestID + ": " + nearestFoodBank);

                    // Create an order and add it to the list
                    orderList.add(new Order(requestID, userLocationName, userLocation, nearestFoodBank));

                    // Query Firestore to find the document by requestID and update the foodBank field
                    db.collection("orders").whereEqualTo("requestID", requestID).get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    db.collection("orders").document(doc.getId())
                                            .update("foodBank", nearestFoodBank)
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Food bank updated successfully for requestID " + requestID))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error updating food bank", e));
                                }
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error finding order by requestID", e));
                }

                // Initialize the adapter if it hasn't been initialized yet
                if (orderAdapter == null) {
                    orderAdapter = new OrderAdapter(orderList, this,this);
                    orderListRecyclerView.setAdapter(orderAdapter);
                } else {
                    // If the adapter is already set, update the data
                    orderAdapter.notifyDataSetChanged();
                }
            } else {
                // Handle failure
                Log.e("LockerActivity", "Error getting orders: ", task.getException());
            }
        });
    }

    private String getUserLocationName(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0); // Get full address
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Failed to get address", e);
        }
        return "Unknown Location";
    }

    private String getNearestFoodBank(LatLng userLocation) {
        double minDistance = Double.MAX_VALUE;
        String nearestFoodBank = "";

        for (FoodBank foodBank : foodBankList) {
            double distance = calculateDistance(userLocation.latitude, userLocation.longitude, foodBank.getLatitude(), foodBank.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearestFoodBank = foodBank.getName();
            }
        }

        return nearestFoodBank;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onClick(LatLng location) {
        if (mMap != null) {
            mMap.clear(); // Clear previous markers
            mMap.addMarker(new MarkerOptions().position(location).title("User Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12)); // Zoom in on the location
        }
    }



}
