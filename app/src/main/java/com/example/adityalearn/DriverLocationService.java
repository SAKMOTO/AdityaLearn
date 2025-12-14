package com.example.adityalearn;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLocationService extends Service {

    private static final String CH_ID = "driver_tracking";
    private static final int NOTIF_ID = 12002;

    private FusedLocationProviderClient fused;
    private LocationCallback callback;
    private DatabaseReference ref;
    private String busId;

    @Override
    public void onCreate() {
        super.onCreate();
        fused = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        busId = intent != null ? intent.getStringExtra("busId") : DriverActivity.BUS_ID;
        ref = FirebaseDatabase.getInstance().getReference("buses").child(busId).child("location");
        startForeground(NOTIF_ID, buildNotification());
        startUpdates();
        return START_STICKY;
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CH_ID, "Driver Tracking", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, DriverActivity.class), Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, CH_ID)
                .setSmallIcon(R.drawable.ic_bus)
                .setContentTitle("Trip running")
                .setContentText("Sharing live location")
                .setOngoing(true)
                .setContentIntent(pi)
                .build();
    }

    private void startUpdates() {
        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build();
        callback = new LocationCallback() {
            @Override public void onLocationResult(LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc == null) return;
                ref.setValue(new LatLngDTO(loc.getLatitude(), loc.getLongitude()));
            }
        };
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fused.requestLocationUpdates(req, callback, getMainLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fused != null && callback != null) fused.removeLocationUpdates(callback);
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }

    public static class LatLngDTO {
        public double lat; public double lng;
        public LatLngDTO() {}
        public LatLngDTO(double lat, double lng) { this.lat = lat; this.lng = lng; }
    }
}




