package com.example.ridesharing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationAccessActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "LocationAccessActivity"; // Logging tag
    private Button allowBtn, dismissBtn;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_access);

        Log.d(TAG, "onCreate: LocationAccessActivity started");

        allowBtn = findViewById(R.id.allowBtn);
        dismissBtn = findViewById(R.id.dismissBtn);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Handle allow button click
        allowBtn.setOnClickListener(v -> {
            Log.d(TAG, "Allow button clicked");
            requestLocationPermission();
        });

        // Handle dismiss button click
        dismissBtn.setOnClickListener(v -> {
            Log.d(TAG, "Dismiss button clicked");
            Toast.makeText(LocationAccessActivity.this, "Location permission denied", Toast.LENGTH_SHORT).show();
            navigateToHomeScreen(null); // Pass null here
        });
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting location permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "Location permission already granted");
            getLocationAndProceed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted");
                getLocationAndProceed();
            } else {
                Log.e(TAG, "Location permission denied");
                navigateToHomeScreen(null); // Pass null here
            }
        }
    }

    private void getLocationAndProceed() {
        Log.d(TAG, "Getting location");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.d(TAG, "Location: " + latitude + ", " + longitude);
                            Toast.makeText(LocationAccessActivity.this,
                                    "Location: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
                            navigateToHomeScreen(location); // Pass the location
                        } else {
                            Log.e(TAG, "Location is null");
                            navigateToHomeScreen(null); // Pass null if location is not available
                        }
                    });
        } else {
            Log.e(TAG, "Location permission not granted");
        }
    }

    private void navigateToHomeScreen(Location location) {
        Log.d(TAG, "Navigating to HomeActivity");
        Intent intent = new Intent(LocationAccessActivity.this, HomeActivity.class);
        if (location != null) {
            intent.putExtra("latitude", location.getLatitude());
            intent.putExtra("longitude", location.getLongitude());
        }
        startActivity(intent);
        finish(); // Close the location access activity
    }
}
