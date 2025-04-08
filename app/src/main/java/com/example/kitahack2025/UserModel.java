package com.example.kitahack2025;

public class UserModel {
    public String uid;
    public String email;

    public UserModel() {}  // Required for Firebase

    public UserModel(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }
}
