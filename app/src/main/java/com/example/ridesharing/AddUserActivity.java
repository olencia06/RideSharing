package com.example.ridesharing;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddUserActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPhone;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("RideSharing");

        // Initialize UI elements
        etName = findViewById(R.id.user_name);
        etEmail = findViewById(R.id.user_email);
        etPassword = findViewById(R.id.user_password);
        etPhone = findViewById(R.id.user_ph_number);
        Button btnSignUp = findViewById(R.id.btn_save_vehicle);

        // Set up onClick listener for the sign-up button
        btnSignUp.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get user ID and create database entry
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        addUserToDatabase(userId, name, email, phone);
                    } else {
                        Toast.makeText(AddUserActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUserToDatabase(String userId, String name, String email, String phone) {
        // Create a map to store user data
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("address", "N/A");
        userMap.put("role","passenger");


        Log.d("SignUpActivity", "Adding user to database: UserId = " + userId + ", Data = " + userMap);

        // Save user data under "RideSharing" -> "Users" -> userId
        databaseReference.child("Users").child(userId).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SignUpActivity", "User data saved successfully!");
                        Toast.makeText(AddUserActivity.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the Sign-Up Activity
                    } else {
                        Log.e("SignUpActivity", "Failed to store user data: " + Objects.requireNonNull(task.getException()).getMessage());
                        Toast.makeText(AddUserActivity.this, "Failed to store user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
