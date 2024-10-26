package com.example.ridesharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog; // Import for dialog

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, profileEmail, phoneNumber, address, userRole;
    private TextView vehicleType, vehicleNumber, vehicleCapacity;
    private LinearLayout vehicle;
    private Button addVehicleButton;
    private DatabaseReference dbRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        setFieldsVisibility(View.GONE); // Initially hide all fields
        dbRef = FirebaseDatabase.getInstance().getReference("RideSharing/Users");

        // Get current user ID
        String userId = getCurrentUserId();

        if (userId != null) {
            getUserData(userId);
        } else {
            Log.d("ProfileActivity", "No user is currently logged in");
            // Optionally, redirect to login activity
        }

        // Set onClickListener for the add vehicle button
        addVehicleButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, VehicleDetailsActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        phoneNumber = findViewById(R.id.user_ph_number);
        address = findViewById(R.id.tv_support);
        userRole = findViewById(R.id.current_user_role);
        vehicleType = findViewById(R.id.user_vehicle_type);
        vehicleNumber = findViewById(R.id.user_vehicle_number);
        vehicleCapacity = findViewById(R.id.user_vehicle_capacity);
        addVehicleButton = findViewById(R.id.btn_add_vehicle);
        vehicle = findViewById(R.id.vehicle);

        // Set OnClickListener for userRole TextView
        userRole.setOnClickListener(v -> showRoleSelectionDialog());
    }

    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null); // Get user ID from SharedPreferences

        // Fallback to FirebaseAuth user ID if SharedPreferences is null
        if (userId == null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
            }
        }
        return userId;
    }

    private void getUserData(String userId) {
        Log.d("ProfileActivity", "Fetching user data for user ID: " + userId);
        dbRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("ProfileActivity", "User data snapshot exists.");
                    populateUserData(dataSnapshot);
                    fetchVehicleDetails(userId);
                } else {
                    Log.d("ProfileActivity", "No such user data found for user ID: " + userId);
                    setFieldsVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ProfileActivity", "Error fetching user data: " + databaseError.getMessage());
            }
        });
    }

    private void populateUserData(DataSnapshot dataSnapshot) {
        String name = dataSnapshot.child("name").getValue(String.class);
        String email = dataSnapshot.child("email").getValue(String.class);
        String phone = dataSnapshot.child("phone").getValue(String.class);
        String userAddress = dataSnapshot.child("address").getValue(String.class);
        String role = dataSnapshot.child("role").getValue(String.class);

        profileName.setText(name != null ? name : "N/A");
        profileEmail.setText(email != null ? email : "N/A");
        phoneNumber.setText(phone != null ? phone : "N/A");
        address.setText(userAddress != null ? userAddress : "N/A");
        userRole.setText(role != null ? role : "N/A");

        setFieldsVisibility(View.VISIBLE);
    }

    private void fetchVehicleDetails(String userId) {
        Log.d("ProfileActivity", "Fetching vehicle details for user ID: " + userId);
        dbRef.child(userId).child("vehicle_details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot vehicleSnapshot) {
                if (vehicleSnapshot.exists()) {
                    Log.d("ProfileActivity", "Vehicle details snapshot exists for user ID: " + userId);
                    populateVehicleDetails(vehicleSnapshot);
                } else {
                    Log.d("ProfileActivity", "No vehicle details found for user ID: " + userId);
                    showAddVehicleButton();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ProfileActivity", "Error fetching vehicle details: " + databaseError.getMessage());
            }
        });
    }

    private void populateVehicleDetails(DataSnapshot vehicleSnapshot) {
        String model = vehicleSnapshot.child("vehicle_type").getValue(String.class);
        String license = vehicleSnapshot.child("vehicle_number").getValue(String.class);
        String capacity = vehicleSnapshot.child("vehicle_capacity").getValue(String.class);

        Log.d("ProfileActivity", "Vehicle Type: " + model);
        Log.d("ProfileActivity", "Vehicle Number: " + license);
        Log.d("ProfileActivity", "Vehicle Capacity: " + capacity);

        vehicle.setVisibility(View.VISIBLE);
        vehicleType.setVisibility(View.VISIBLE);
        vehicleNumber.setVisibility(View.VISIBLE);
        vehicleCapacity.setVisibility(View.VISIBLE);

        vehicleType.setText(model != null ? model : "N/A");
        vehicleNumber.setText(license != null ? license : "N/A");
        vehicleCapacity.setText(capacity != null ? capacity : "N/A");

        addVehicleButton.setVisibility(View.GONE);
    }

    private void showAddVehicleButton() {
        vehicleType.setText("N/A");
        vehicleNumber.setText("N/A");
        vehicleCapacity.setText("N/A");
        addVehicleButton.setVisibility(View.VISIBLE); // Show button to add vehicle details
        setVehicleFieldsVisibility(); // Hide vehicle detail fields
    }

    private void setFieldsVisibility(int visibility) {
        profileName.setVisibility(visibility);
        profileEmail.setVisibility(visibility);
        phoneNumber.setVisibility(visibility);
        address.setVisibility(visibility);
        userRole.setVisibility(visibility);
        vehicle.setVisibility(visibility);
    }

    private void setVehicleFieldsVisibility() {
        vehicleType.setVisibility(View.GONE);
        vehicleNumber.setVisibility(View.GONE);
        vehicleCapacity.setVisibility(View.GONE);
        vehicle.setVisibility(View.GONE);
    }

    private void showRoleSelectionDialog() {
        final String[] roles = {"Driver", "Passenger"}; // Define roles here
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Role")
                .setItems(roles, (dialog, which) -> {
                    String selectedRole = roles[which];
                    updateUserRole(getCurrentUserId(), selectedRole); // Update role in the database
                })
                .create()
                .show();
    }

    public void updateUserRole(String userId, String newRole) {
        dbRef.child(userId).child("role").setValue(newRole).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userRole.setText(newRole); // Update user role TextView
                Log.d("ProfileActivity", "User role updated successfully");
            } else {
                Log.d("ProfileActivity", "Error updating user role: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }
}
