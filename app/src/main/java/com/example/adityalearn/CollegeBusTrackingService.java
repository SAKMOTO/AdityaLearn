package com.example.adityalearn;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class CollegeBusTrackingService extends Service {

    public static final String CHANNEL_ID = "college_bus_tracking";
    public static final int NOTIF_ID = 11001;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String etaText = intent != null ? intent.getStringExtra("eta") : null;
        if (etaText == null) etaText = "Arriving soon";

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, TrackCollegeActivity.class),
                Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bus)
                .setContentTitle("College Bus Tracking")
                .setContentText(etaText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("On time | " + etaText))
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentIntent)
                .build();

        startForeground(NOTIF_ID, notification);
        return START_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "College Bus Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}




