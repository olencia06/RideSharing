package com.example.ridesharing;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VehicleDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText vehicleModel, vehicleLicense, vehicleCapacity;
    private Button saveButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        vehicleModel = findViewById(R.id.et_vehicle_model);
        vehicleLicense = findViewById(R.id.et_vehicle_license);
        vehicleCapacity = findViewById(R.id.et_vehicle_capacity);
        saveButton = findViewById(R.id.btn_save_vehicle);

        // Save vehicle details to Firestore on button click
        saveButton.setOnClickListener(v -> saveVehicleDetails());
    }

    private void saveVehicleDetails() {
        String model = vehicleModel.getText().toString();
        String license = vehicleLicense.getText().toString();
        String capacity = vehicleCapacity.getText().toString();

        if (!model.isEmpty() && !license.isEmpty() && !capacity.isEmpty()) {
            // Store vehicle details in Firestore
            Map<String, Object> vehicle = new HashMap<>();
            vehicle.put("model", model);
            vehicle.put("license", license);
            vehicle.put("capacity", capacity);

            // Replace "userId" with the actual user ID
            db.collection("users").document("user1@gmail.com").collection("vehicle_details")
                    .add(vehicle)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(VehicleDetailsActivity.this, "Vehicle details saved", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after saving
                    })
                    .addOnFailureListener(e -> Toast.makeText(VehicleDetailsActivity.this, "Error saving details", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(VehicleDetailsActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
