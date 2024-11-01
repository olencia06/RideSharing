package com.example.ridesharing;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddRideActivity extends AppCompatActivity {
    private static final String TAG = "AddRideActivity";
    private EditText phoneEditText, seatsEditText, timeEditText, dateEditText, vehicleTypeEditText, vehicleNumberEditText;
    private Button postButton;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);

        phoneEditText = findViewById(R.id.editTextPhone);
        seatsEditText = findViewById(R.id.editTextSeats);
        timeEditText = findViewById(R.id.editTextTime);
        dateEditText = findViewById(R.id.editTextDate);
        vehicleTypeEditText = findViewById(R.id.editTextVehicleType);
        vehicleNumberEditText = findViewById(R.id.editTextVehicleNumber);
        postButton = findViewById(R.id.buttonPost);

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("RideSharing");

        loadDriverDetails();
        loadVehicleType();
        setupDateAndTimePickers();

        postButton.setOnClickListener(v -> showPostRideDialog());
    }

    private void loadDriverDetails() {
        try {
            databaseReference.child("Users").child(userId).child("phone")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String phone = snapshot.getValue(String.class);
                            phoneEditText.setText(phone);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed to fetch user details", error.toException());
                            Toast.makeText(AddRideActivity.this, "Error loading user details", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading driver details: " + e.getMessage());
            Toast.makeText(this, "Failed to load driver details", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadVehicleType() {
        try {
            databaseReference.child("Users").child(userId).child("vehicle_details").child("vehicle_type")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String vehicleType = snapshot.getValue(String.class);
                            vehicleTypeEditText.setText(vehicleType);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed to fetch vehicle type", error.toException());
                            Toast.makeText(AddRideActivity.this, "Error loading vehicle type", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading vehicle type: " + e.getMessage());
            Toast.makeText(this, "Failed to load vehicle type", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDateAndTimePickers() {
        dateEditText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateEditText.setText(dateFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        timeEditText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                timeEditText.setText(timeFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
            timePicker.show();
        });
    }

    private void showPostRideDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_post_ride);

        EditText startingLocationEditText = dialog.findViewById(R.id.editTextStartingLocation);
        EditText endingLocationEditText = dialog.findViewById(R.id.editTextEndingLocation);
        Button postRideButton = dialog.findViewById(R.id.buttonPostRide);

        postRideButton.setOnClickListener(v -> {
            postRide(startingLocationEditText.getText().toString().trim(), endingLocationEditText.getText().toString().trim());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void postRide(String startingLocation, String endingLocation) {
        String phone = phoneEditText.getText().toString().trim();
        String seats = seatsEditText.getText().toString().trim();
        String time = timeEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String vehicleType = vehicleTypeEditText.getText().toString().trim();
        String vehicleNumber = vehicleNumberEditText.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(seats) || TextUtils.isEmpty(time) || TextUtils.isEmpty(date)
                || TextUtils.isEmpty(startingLocation) || TextUtils.isEmpty(endingLocation)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DatabaseReference postRideRef = databaseReference.child("postRides").push();
            Ride ride = new Ride(userId, phone, vehicleType, seats, time, date, vehicleNumber, startingLocation, endingLocation);

            postRideRef.setValue(ride).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Ride posted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to post ride", task.getException());
                    Toast.makeText(this, "Failed to post ride", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error posting ride: " + e.getMessage());
            Toast.makeText(this, "Failed to post ride", Toast.LENGTH_SHORT).show();
        }
    }
}
