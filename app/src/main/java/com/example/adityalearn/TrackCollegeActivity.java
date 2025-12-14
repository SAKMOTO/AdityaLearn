package com.example.adityalearn;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrackCollegeActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private Marker userMarker, collegeMarker;
    
    private TextView tvStatus, tvDistance, tvETA, tvRouteInfo;
    private android.widget.ProgressBar pbProgress;
    private Button btnStartTracking, btnStopTracking;
    private LinearLayout trackingInfoContainer;
    
    private boolean isTracking = false;
    private Handler trackingHandler;
    private Runnable trackingRunnable;
    
    // College coordinates (Aditya University - example)
    private static final LatLng COLLEGE_LOCATION = new LatLng(17.3850, 78.4867);
    private static final String COLLEGE_NAME = "Aditya University";
    
    // Simulated route points for demo
    private List<LatLng> routePoints;
    private int currentRouteIndex = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_college);
        
        initializeViews();
        setupMap();
        setupLocationClient();
        generateRoutePoints();
        
        checkLocationPermission();
    }
    
    private void initializeViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvDistance = findViewById(R.id.tv_distance);
        tvETA = findViewById(R.id.tv_eta);
        tvRouteInfo = findViewById(R.id.tv_route_info);
        pbProgress = findViewById(R.id.pb_progress);
        btnStartTracking = findViewById(R.id.btn_start_tracking);
        btnStopTracking = findViewById(R.id.btn_stop_tracking);
        trackingInfoContainer = findViewById(R.id.tracking_info_container);
        
        btnStartTracking.setOnClickListener(v -> startTracking());
        btnStopTracking.setOnClickListener(v -> stopTracking());
        findViewById(R.id.fab_share).setOnClickListener(v -> shareLive());
        
        updateUI();
    }

    private void shareLive() {
        String msg = "Track my bus — Bus A1 — ETA " + tvETA.getText();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg + " (expires in 2 hrs)");
        startActivity(Intent.createChooser(sendIntent, "Share live location"));
    }
    
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    private void setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
    
    private void generateRoutePoints() {
        routePoints = new ArrayList<>();
        // Generate some random route points between user and college
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            double lat = COLLEGE_LOCATION.latitude + (random.nextDouble() - 0.5) * 0.01;
            double lng = COLLEGE_LOCATION.longitude + (random.nextDouble() - 0.5) * 0.01;
            routePoints.add(new LatLng(lat, lng));
        }
    }
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Add college marker
        collegeMarker = mMap.addMarker(new MarkerOptions()
                .position(COLLEGE_LOCATION)
                .title(COLLEGE_NAME)
                .snippet("Your destination"));
        
        // Move camera to college location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COLLEGE_LOCATION, 12));
        
        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }
    
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            
                            // Add user marker
                            if (userMarker != null) {
                                userMarker.remove();
                            }
                            userMarker = mMap.addMarker(new MarkerOptions()
                                    .position(userLocation)
                                    .title("Your Location")
                                    .snippet("Current position"));
                            
                            // Update distance and ETA
                            updateDistanceAndETA();
                        }
                    }
                });
    }
    
    private void updateDistanceAndETA() {
        if (currentLocation != null) {
            float[] distance = new float[1];
            Location.distanceBetween(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    COLLEGE_LOCATION.latitude, COLLEGE_LOCATION.longitude,
                    distance);
            
            float distanceKm = distance[0] / 1000;
            int etaMinutes = (int) (distanceKm * 2); // Assuming 30 km/h average speed
            
            tvDistance.setText(String.format("%.1f km", distanceKm));
            tvETA.setText(String.format("%d min", etaMinutes));
        }
    }
    
    private void startTracking() {
        if (currentLocation == null) {
            Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
            return;
        }
        
        isTracking = true;
        updateUI();
        
        tvStatus.setText("🚗 Tracking to " + COLLEGE_NAME);
        trackingInfoContainer.setVisibility(View.VISIBLE);
        
        // Start simulated tracking
        trackingHandler = new Handler(Looper.getMainLooper());
        trackingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking && currentRouteIndex < routePoints.size()) {
                    simulateMovement();
                    trackingHandler.postDelayed(this, 2000); // Update every 2 seconds
                }
            }
        };
        trackingHandler.post(trackingRunnable);
        
        Toast.makeText(this, "Started tracking to " + COLLEGE_LOCATION, Toast.LENGTH_SHORT).show();
    }
    
    private void stopTracking() {
        isTracking = false;
        if (trackingHandler != null) {
            trackingHandler.removeCallbacks(trackingRunnable);
        }
        
        updateUI();
        tvStatus.setText("📍 Ready to track");
        trackingInfoContainer.setVisibility(View.GONE);
        
        Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();
    }
    
    private void simulateMovement() {
        if (currentRouteIndex < routePoints.size()) {
            LatLng newPosition = routePoints.get(currentRouteIndex);
            
            // Update user marker position
            if (userMarker != null) {
                userMarker.setPosition(newPosition);
            }
            
            // Move camera to follow user
            mMap.animateCamera(CameraUpdateFactory.newLatLng(newPosition));
            
            // Update distance and ETA
            float[] distance = new float[1];
            Location.distanceBetween(
                    newPosition.latitude, newPosition.longitude,
                    COLLEGE_LOCATION.latitude, COLLEGE_LOCATION.longitude,
                    distance);
            
            float distanceKm = distance[0] / 1000;
            int etaMinutes = (int) (distanceKm * 2);
            
            tvDistance.setText(String.format("%.1f km", distanceKm));
            tvETA.setText(String.format("%d min", etaMinutes));
            
            // Update route info
            tvRouteInfo.setText("Route: " + (currentRouteIndex + 1) + "/" + routePoints.size() + " waypoints");
            int progress = (int)(((float)(currentRouteIndex + 1) / (float)routePoints.size()) * 100f);
            pbProgress.setProgress(progress);
            
            currentRouteIndex++;
            
            // Check if reached destination
            if (distanceKm < 0.1) {
                stopTracking();
                tvStatus.setText("🎉 Arrived at " + COLLEGE_NAME);
                Toast.makeText(this, "Welcome to " + COLLEGE_NAME + "!", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void updateUI() {
        btnStartTracking.setVisibility(isTracking ? View.GONE : View.VISIBLE);
        btnStopTracking.setVisibility(isTracking ? View.VISIBLE : View.GONE);
    }
    
    public void onBackClick(View view) {
        stopTracking();
        onBackPressed();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTracking();
    }
}
