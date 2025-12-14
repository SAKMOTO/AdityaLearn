package com.example.adityalearn;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SpaceLauncherActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "url";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "Missing Space URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Try to open in Chrome app if installed, else default browser
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Prefer Chrome if available
        try {
            if (isPackageInstalled("com.android.chrome")) {
                intent.setPackage("com.android.chrome");
            }
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Fallback: remove package constraint and try again
            intent.setPackage(null);
            try {
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "No browser found to open Space", Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
