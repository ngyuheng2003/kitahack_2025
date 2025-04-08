package com.example.kitahack2025;


import com.google.android.gms.maps.model.LatLng;

public class Order {
    private String requestID;
    private String locationName;
    private LatLng userLocation;
    private String nearestFoodBank;

    public Order(String requestID, String locationName, LatLng userLocation, String nearestFoodBank) {
        this.requestID = requestID;
        this.locationName = locationName;
        this.userLocation = userLocation;
        this.nearestFoodBank = nearestFoodBank;
    }

    public String getRequestID() {
        return requestID;
    }

    public String getLocationName() {
        return locationName;
    }

    public LatLng getUserLocation() {
        return userLocation;
    }

    public String getNearestFoodBank() {
        return nearestFoodBank;
    }
}
