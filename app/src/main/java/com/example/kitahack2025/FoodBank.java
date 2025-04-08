package com.example.kitahack2025;


public class FoodBank {
    private String name;
    private double latitude;
    private double longitude;

    public FoodBank(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

