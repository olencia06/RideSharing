package com.example.ridesharing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import androidx.appcompat.widget.SwitchCompat;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "HomeActivity"; // Logging tag
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private SwitchCompat toggleButton;
    private boolean isVehicleDetailsStored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Check if vehicle details are already stored
        checkVehicleDetails();

        Log.d(TAG, "onCreate: HomeActivity started");

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d(TAG, "onCreate: MapFragment found, setting async");
        } else {
            Log.e(TAG, "onCreate: MapFragment is null");
        }

        // Initialize BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Toggle button listener
        toggleButton = findViewById(R.id.toggle_role);
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If vehicle details are not stored, prompt the user to enter them
                if (!isVehicleDetailsStored) {
                    Intent vehicleDetailsIntent = new Intent(HomeActivity.this, VehicleDetailsActivity.class);
                    startActivity(vehicleDetailsIntent);
                }
            }
        });
    }

    private void checkVehicleDetails() {
        // This method should query your database to check if vehicle details are stored
        // For example:
        // if (yourDatabase.hasVehicleDetails(userId)) {
        //     isVehicleDetailsStored = true;
        //     Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
        //     startActivity(profileIntent);
        //     finish(); // Close HomeActivity
        // }

        // Simulated check (replace with actual database check)
        boolean vehicleDetailsExist = checkDatabaseForVehicleDetails();
        if (vehicleDetailsExist) {
            isVehicleDetailsStored = true;
            Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
            finish(); // Close HomeActivity
        }
    }

    private boolean checkDatabaseForVehicleDetails() {
        // Simulate a database check. Replace with actual database query.
        // For example:
        // return database.hasVehicleDetails(userId);
        return false; // Change this based on actual check
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Log.d(TAG, "BottomNav: ItemSelected=" + item.getTitle());
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            Log.d(TAG, "BottomNav: Already in HomeActivity");
            return true;
        } else if (itemId == R.id.nav_book) {
            Log.d(TAG, "BottomNav: Navigating to BookActivity");
            startActivity(new Intent(HomeActivity.this, BookActivity.class));
            return true;
        } else if (itemId == R.id.nav_profile) {
            Log.d(TAG, "BottomNav: Navigating to ProfileActivity");
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            return true;
        } else {
            Log.e(TAG, "BottomNav: Unhandled item selected");
            return false;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady: GoogleMap is ready");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onMapReady: Location permission granted");
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            Log.d(TAG, "onMapReady: Requesting location permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation: Fetching last location");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            Log.d(TAG, "getCurrentLocation: Location found: " + currentLocation.toString());
                        } else {
                            Log.e(TAG, "getCurrentLocation: Location is null");
                        }
                    });
        } else {
            Log.e(TAG, "getCurrentLocation: Permission not granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Location permission granted");
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                }
            } else {
                Log.e(TAG, "onRequestPermissionsResult: Location permission denied");
            }
        }
    }
}
