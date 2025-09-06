package com.example.adityalearn;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import java.util.List;

public class SensorService extends Service implements ShakeDetector.OnShakeListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private FusedLocationProviderClient fusedLocationClient;
    private DbHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DbHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupShakeDetection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check if this is a test emergency
        if (intent != null && intent.getBooleanExtra("test_emergency", false)) {
            triggerEmergencyAlert(null); // Test without location
        }

        // Start foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
        }
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.example.adityalearn.permanence";
        String channelName = "Shake Detection Service";

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null && manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            NotificationChannel chan = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, channelName,
                    NotificationManager.IMPORTANCE_MIN
            );
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("SOS Protection Active")
                .setContentText("Shake detection is enabled")
                .setSmallIcon(R.drawable.ic_sos)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(2, notification);
    }

    private void setupShakeDetection() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(this);

        if (mAccelerometer != null) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onShake(int count) {
        if (count >= 3) {
            vibrate();
            getLocationAndTriggerEmergency();
        }
    }

    public void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                VibrationEffect vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);
                vibrator.vibrate(vibEff);
            } else {
                vibrator.vibrate(500);
            }
        }
    }

    private void getLocationAndTriggerEmergency() {
        try {
            fusedLocationClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    new CancellationToken() {
                        @Override
                        public boolean isCancellationRequested() { return false; }
                        @NonNull
                        @Override
                        public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                            return null;
                        }
                    }
            ).addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    triggerEmergencyAlert(location);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    triggerEmergencyAlert(null);
                }
            });
        } catch (SecurityException e) {
            triggerEmergencyAlert(null);
        }
    }

    private void triggerEmergencyAlert(Location location) {
        // Create emergency notification (instead of SMS)
        createEmergencyNotification(location);

        // Show toast
        Toast.makeText(this, "🚨 EMERGENCY ALERT TRIGGERED!", Toast.LENGTH_LONG).show();

        // Vibrate aggressively
        vibrateEmergency();
    }

    private void createEmergencyNotification(Location location) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create emergency channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "emergency_channel", "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Emergency SOS alerts");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationManager.createNotificationChannel(channel);
        }

        String locationText = (location != null) ?
                "Location: http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude() :
                "Location: Unable to get location";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "emergency_channel")
                .setSmallIcon(R.drawable.ic_sos)
                .setContentTitle("🚨 EMERGENCY ALERT")
                .setContentText("SOS triggered! Help needed.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("EMERGENCY! I need immediate help!\n" + locationText + "\n\nPlease check on me!"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 1000, 500, 1000});

        notificationManager.notify(999, builder.build());
    }

    private void vibrateEmergency() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 500, 1000, 500, 1000};
            vibrator.vibrate(pattern, 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mShakeDetector);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}