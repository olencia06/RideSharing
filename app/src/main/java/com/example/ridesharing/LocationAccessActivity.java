package com.example.ridesharing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationAccessActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Button allowBtn, dismissBtn;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_access);

        allowBtn = findViewById(R.id.allowBtn);
        dismissBtn = findViewById(R.id.dismissBtn);

        // Initialize FusedLocationProviderClient for getting location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Handle allow button click
        allowBtn.setOnClickListener(v -> requestLocationPermission());

        // Handle dismiss button click
        dismissBtn.setOnClickListener(v -> {
            Toast.makeText(LocationAccessActivity.this, "Location permission denied", Toast.LENGTH_SHORT).show();
            navigateToHomeScreen(); // Move to home screen even if location is denied
        });
    }

    // Request location permission
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            getLocationAndProceed();
        }
    }

    // Handle the permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                getLocationAndProceed();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                navigateToHomeScreen();
            }
        }
    }

    // Get the user's location if permission is granted
    private void getLocationAndProceed() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Do something with the location
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                Toast.makeText(LocationAccessActivity.this,
                                        "Location: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
                            }
                            navigateToHomeScreen();
                        }
                    });
        }
    }

    // Navigate to the home screen
    private void navigateToHomeScreen() {
        Intent intent = new Intent(LocationAccessActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close the location access activity
    }
}