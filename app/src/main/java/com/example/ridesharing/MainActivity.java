package com.example.ridesharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ridesharing.LocationAccessActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailET, passET;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();  // Initialize Firestore

        emailET = findViewById(R.id.email);
        passET = findViewById(R.id.pass);
        loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> loginUser());
    }

    // Login user verification
    private void loginUser() {
        String email = emailET.getText().toString();
        String password = passET.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if the user is an admin
                            if (isAdmin(email)) {
                                navigateToAdminScreen();
                            } else {
                                // Check if the user exists in the Realtime Database
                                checkUserInRealtimeDB(user);
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Check if the email belongs to an admin
    private boolean isAdmin(String email) {
        // Define admin email(s) or a condition (e.g., specific domain)
        return email.equals("admin@gmail.com"); // Replace with your admin email
    }

    // Navigate to admin management screen
    private void navigateToAdminScreen() {
        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
        startActivity(intent);
        finish();
    }


    // Check if the user exists in Realtime Database
    private void checkUserInRealtimeDB(FirebaseUser user) {
        String userId = user.getUid();

        // Reference to the Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("RideSharing").child("Users").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User exists in Realtime Database
                    String email = user.getEmail();
                    String role = dataSnapshot.child("role").getValue(String.class); // Assume the role is stored in the Realtime DB
                    saveUserData(email, role, userId); // Save user data in SharedPreferences
                    navigateToLocationAccessScreen();
                } else {
                    // User does not exist, create new document in Firestore
                    createUserInFirestore(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error accessing Realtime Database: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Create new user in Firestore
    private void createUserInFirestore(FirebaseUser user) {
        String userId = user.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("role", "Passenger");  // Default role is Passenger

        // Save user in Firestore
        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "User added to Firestore", Toast.LENGTH_SHORT).show();
                    saveUserData(user.getEmail(), "Passenger", userId); // Save user data in SharedPreferences
                    navigateToLocationAccessScreen();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                });
    }

    // Navigate to the screen asking for location access
    private void navigateToLocationAccessScreen() {
        Intent intent = new Intent(MainActivity.this, LocationAccessActivity.class);
        startActivity(intent);
        finish();
    }

    // Save user data in SharedPreferences
    private void saveUserData(String email, String role, String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("role", role);
        editor.putString("userId", userId);  // Save user ID
        editor.apply();
    }
}
