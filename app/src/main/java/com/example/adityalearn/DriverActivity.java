package com.example.adityalearn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOCATION = 2011;
    private GoogleMap mMap;
    private Marker selfMarker;
    private Button btnStart, btnEnd;
    public static final String BUS_ID = "bus1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        btnStart = findViewById(R.id.btn_start_trip);
        btnEnd = findViewById(R.id.btn_end_trip);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_driver);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnStart.setOnClickListener(v -> startTrip());
        btnEnd.setOnClickListener(v -> endTrip());

        checkLocationPerms();
    }

    private void checkLocationPerms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
        }
    }

    private void startTrip() {
        Intent svc = new Intent(this, DriverLocationService.class);
        svc.putExtra("busId", BUS_ID);
        if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(svc); else startService(svc);
        Toast.makeText(this, "Trip started", Toast.LENGTH_SHORT).show();
    }

    private void endTrip() {
        stopService(new Intent(this, DriverLocationService.class));
        Toast.makeText(this, "Trip ended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        LatLng init = new LatLng(17.3850, 78.4867);
        selfMarker = mMap.addMarker(new MarkerOptions().position(init).title("You"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(init, 15));
    }

    public void onBackClick(View v) { finish(); }

    public void updateSelfMarker(LatLng latLng) {
        if (selfMarker != null) selfMarker.setPosition(latLng);
        if (mMap != null) mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && mMap != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }
}




