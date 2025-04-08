package com.example.kitahack2025;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageButton signupBtn, goLoginBtn;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        etEmail = findViewById(R.id.ET_email);
        etPassword = findViewById(R.id.ET_password);
        signupBtn = findViewById(R.id.IB_signup);
        goLoginBtn = findViewById(R.id.IB_goSignin);
        mAuth = FirebaseAuth.getInstance();

        signupBtn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = mAuth.getCurrentUser().getUid();
                        FirebaseDatabase.getInstance().getReference("users").child(uid)
                                .setValue(new UserModel(uid, email))
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, Login.class));
                                    finish();
                                });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        goLoginBtn.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));

    }
}