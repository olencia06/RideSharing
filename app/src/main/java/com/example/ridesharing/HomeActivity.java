package com.example.ridesharing;

import android.view.View; // For View
import com.google.firebase.database.DatabaseError; // For DatabaseError
import com.google.firebase.database.ValueEventListener; // For ValueEventListener

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "HomeActivity"; // Logging tag
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseUser currentUser;
    private FloatingActionButton fabAddRide; // Declare FAB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();




        if (currentUser != null) {
            String userId = currentUser.getUid(); // Use user's UID as userId
            checkUserRole(userId); // Check vehicle details for the logged-in user
        } else {
            Log.e(TAG, "User not logged in");
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish(); // Close HomeActivity if user is not logged in
            return; // Ensure no further execution if user is not logged in
        }

        Log.d(TAG, "onCreate: HomeActivity started");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fabAddRide = findViewById(R.id.fab_add_ride); // Initialize FAB

        fabAddRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddRideActivity.class);
                startActivity(intent);
            }
        });

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
    }

    private void checkUserRole(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.child("role").getValue(String.class);
                if ("Driver".equals(role)) {
                    fabAddRide.setVisibility(View.VISIBLE); // Show FAB for drivers
                } else {
                    fabAddRide.setVisibility(View.GONE); // Hide FAB for passengers
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read user role", error.toException());
            }
        });
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
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "getCurrentLocation: Failed to fetch location", e);
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
                Toast.makeText(this, "Location permission is required to show your current location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
