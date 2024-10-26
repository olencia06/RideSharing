package com.example.ridesharing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class VehicleDetailsActivity extends AppCompatActivity {

    private DatabaseReference db; // Change to DatabaseReference for Realtime Database
    private EditText vehicleModel, vehicleLicense, vehicleCapacity;
    private Button saveButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);

        // Initialize Realtime Database
        db = FirebaseDatabase.getInstance().getReference("RideSharing/Users");

        // Initialize views
        vehicleModel = findViewById(R.id.et_vehicle_model);
        vehicleLicense = findViewById(R.id.et_vehicle_license);
        vehicleCapacity = findViewById(R.id.et_vehicle_capacity);
        saveButton = findViewById(R.id.btn_save_vehicle);

        // Save vehicle details to Realtime Database on button click
        saveButton.setOnClickListener(v -> saveVehicleDetails());
    }

    private void saveVehicleDetails() {
        String model = vehicleModel.getText().toString().trim();
        String license = vehicleLicense.getText().toString().trim();
        String capacity = vehicleCapacity.getText().toString().trim();

        if (!model.isEmpty() && !license.isEmpty() && !capacity.isEmpty()) {
            // Validate capacity if needed (optional)
            if (!isValidCapacity(capacity)) {
                Toast.makeText(VehicleDetailsActivity.this, "Please enter a valid capacity", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the current user's ID
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid(); // Use UID for the document reference

                // Create a map for vehicle details
                Map<String, Object> vehicle = new HashMap<>();
                vehicle.put("vehicle_type", model);
                vehicle.put("vehicle_number", license);
                vehicle.put("vehicle_capacity", capacity);

                // Save vehicle details in Realtime Database
                db.child(userId).child("vehicle_details").setValue(vehicle)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(VehicleDetailsActivity.this, "Vehicle details saved", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(VehicleDetailsActivity.this, ProfileActivity.class));
                            finish();// Close the activity after saving
                        })
                        .addOnFailureListener(e -> Toast.makeText(VehicleDetailsActivity.this, "Error saving details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(VehicleDetailsActivity.this, "No user is currently logged in", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(VehicleDetailsActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidCapacity(String capacity) {
        try {
            int cap = Integer.parseInt(capacity);
            return cap > 0; // Capacity must be positive
        } catch (NumberFormatException e) {
            return false; // Not a valid integer
        }
    }
}
