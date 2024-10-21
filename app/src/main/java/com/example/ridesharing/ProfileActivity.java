package com.example.ridesharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, profileEmail, phoneNumber, address, userRole;
    private TextView vehicleType, vehicleNumber; // New TextViews for vehicle details
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        phoneNumber = findViewById(R.id.user_ph_number);
        address = findViewById(R.id.tv_support);
        userRole = findViewById(R.id.cuurent_user_role);
        vehicleType = findViewById(R.id.user_vehicle_type); // Initialize vehicle type TextView
        vehicleNumber = findViewById(R.id.user_vehicle_number); // Initialize vehicle number TextView

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch user data from Firestore
        String userId = "user1@gmail.com"; // Replace with dynamic ID based on user session
        getUserData(userId);
    }

    private void getUserData(String userId) {
        // Fetch user data
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Fetch and display user data
                    String name = document.getString("name");
                    String email = document.getString("email");
                    String phone = document.getString("phone_no");
                    String userAddress = document.getString("address");
                    String role = document.getString("role");

                    // Set data to TextViews
                    profileName.setText(name);
                    profileEmail.setText(email);
                    phoneNumber.setText(phone);
                    address.setText(userAddress);
                    userRole.setText(role);

                    // Fetch vehicle details
                    fetchVehicleDetails(userId);
                } else {
                    Log.d("ProfileActivity", "No such document");
                }
            } else {
                Log.d("ProfileActivity", "Get failed with ", task.getException());
            }
        });
    }

    private void fetchVehicleDetails(String userId) {
        // Fetch vehicle details from the subcollection
        db.collection("users").document(userId).collection("vehicle_details")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot vehicleDoc : task.getResult()) {
                            String model = vehicleDoc.getString("model");
                            String license = vehicleDoc.getString("license");

                            // Assuming you want to set the model as vehicle type
                            vehicleType.setText(model);
                            vehicleNumber.setText(license);
                        }
                    } else {
                        Log.d("ProfileActivity", "Get vehicle details failed with ", task.getException());
                    }
                });
    }
}
