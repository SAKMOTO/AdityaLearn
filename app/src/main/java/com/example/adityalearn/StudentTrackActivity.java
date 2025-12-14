package com.example.adityalearn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentTrackActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOC = 3011;
    private GoogleMap mMap;
    private Marker busMarker;
    private Polyline routeLine;
    private FusedLocationProviderClient fused;
    private TextView bannerTitle, bannerEta, tvBusNum, tvDriver, tvOcc;
    private DatabaseReference locRef, occRef, driverRef;
    private String busId = DriverActivity.BUS_ID;
    private LatLng college = new LatLng(17.3850, 78.4867);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_track);

        fused = LocationServices.getFusedLocationProviderClient(this);
        bannerTitle = findViewById(R.id.banner_title);
        bannerEta = findViewById(R.id.banner_eta);
        tvBusNum = findViewById(R.id.tv_bus_num);
        tvDriver = findViewById(R.id.tv_driver);
        tvOcc = findViewById(R.id.tv_occ);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_student);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        findViewById(R.id.btn_call_driver).setOnClickListener(v -> callDriver());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareLink());
        findViewById(R.id.fab_recenter).setOnClickListener(v -> recenter());

        DatabaseReference busRef = FirebaseDatabase.getInstance().getReference("buses").child(busId);
        locRef = busRef.child("location");
        occRef = busRef.child("occupancy");
        driverRef = busRef.child("driver");

        occRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) { tvOcc.setText(snapshot.getValue() != null ? snapshot.getValue().toString() : "0/40"); }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
        driverRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) { tvDriver.setText(snapshot.getValue() != null ? snapshot.getValue().toString() : "Mr. Ramesh"); }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        tvBusNum.setText(busId.toUpperCase());

        locRef.addValueEventListener(new ValueEventListener() {
            LatLng last;
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double lat = snapshot.child("lat").getValue(Double.class);
                Double lng = snapshot.child("lng").getValue(Double.class);
                if (lat == null || lng == null) return;
                LatLng now = new LatLng(lat, lng);
                updateBusMarkerAnimated(last, now);
                last = now;
                updateRouteAndEta(now);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { bannerTitle.setText("Bus location unavailable"); }
        });

        checkPerms();
    }

    private void checkPerms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);
        }
    }

    @Override public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(college, 13));
    }

    private void updateBusMarkerAnimated(LatLng from, LatLng to) {
        if (busMarker == null) {
            busMarker = mMap.addMarker(new MarkerOptions().position(to).title("Bus").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            return;
        }
        if (from == null) { busMarker.setPosition(to); return; }
        final long duration = 800;
        final long start = System.currentTimeMillis();
        Handler h = new Handler();
        h.post(new Runnable() {
            @Override public void run() {
                float t = Math.min(1f, (System.currentTimeMillis() - start) / (float) duration);
                double lat = from.latitude + (to.latitude - from.latitude) * t;
                double lng = from.longitude + (to.longitude - from.longitude) * t;
                busMarker.setPosition(new LatLng(lat, lng));
                if (t < 1f) h.postDelayed(this, 16);
            }
        });
    }

    private void updateRouteAndEta(LatLng bus) {
        if (routeLine != null) routeLine.remove();
        routeLine = mMap.addPolyline(new PolylineOptions().add(bus, college).color(0xFF2E7D32).width(8));

        fused.getLastLocation().addOnSuccessListener(loc -> {
            Location busLoc = new Location("bus"); busLoc.setLatitude(bus.latitude); busLoc.setLongitude(bus.longitude);
            Location collegeLoc = new Location("college"); collegeLoc.setLatitude(college.latitude); collegeLoc.setLongitude(college.longitude);
            float meters = busLoc.distanceTo(collegeLoc);
            int etaMin = (int) Math.max(1, Math.round((meters/1000f) / 25f * 60f)); // avg 25km/h
            bannerTitle.setText("Bus is on the way 🚍");
            bannerEta.setText("Arriving in " + etaMin + " minutes");

            if (meters <= 500) Toast.makeText(this, "Bus is arriving in 2 minutes", Toast.LENGTH_LONG).show();
        });
    }

    private void recenter() {
        if (busMarker != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busMarker.getPosition(), 15));
    }

    private void callDriver() {
        String num = tvDriver.getText() != null ? "tel:" + "9999999999" : "tel:9999999999";
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(num)));
    }

    private void shareLink() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "Track my bus — " + busId + " — live location link (demo)");
        startActivity(Intent.createChooser(i, "Share location"));
    }

    public void onBackClick(View v) { finish(); }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && mMap != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) mMap.setMyLocationEnabled(true);
        }
    }
}




