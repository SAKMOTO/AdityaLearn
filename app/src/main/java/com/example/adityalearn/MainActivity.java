package com.example.adityalearn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int IGNORE_BATTERY_OPTIMIZATION_REQUEST = 1002;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity created");

        // Check permissions first
        checkAndRequestPermissions();

        // Menu button functionality
        FloatingActionButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            Log.d(TAG, "Bottom navigation found");

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_notes) {
                    startActivity(new Intent(MainActivity.this, NotesSplashActivity.class));
                    return true;
                } else if (id == R.id.nav_question_papers) {
                    startActivity(new Intent(MainActivity.this, QuestionPapersSplashActivity.class));
                    return true;
                } else if (id == R.id.nav_timetable) {
                    startActivity(new Intent(MainActivity.this, TimetableActivity.class));
                    return true;
                } else if (id == R.id.nav_ai_chat) {
                    startActivity(new Intent(MainActivity.this, AiChatSplashActivity.class));
                    return true;
                } else if (id == R.id.nav_quiz) {
                    startActivity(new Intent(MainActivity.this, QuizActivity.class));
                    return true;
                }

                return false;
            });
        } else {
            Log.e(TAG, "Bottom navigation not found");
        }
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Check all required permissions
        String[] requiredPermissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECEIVE_SMS,    // ADD THIS
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,    // ADD THIS
                Manifest.permission.READ_SMS,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.RECORD_AUDIO
        };

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            // All permissions granted
            checkBatteryOptimization();
        }
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                askIgnoreOptimization();
            } else {
                startSensorService();
            }
        } else {
            startSensorService();
        }
    }

    @SuppressLint("BatteryLife")
    private void askIgnoreOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST);
        }
    }

    private void startSensorService() {
        if (hasRequiredPermissions()) {
            Intent serviceIntent = new Intent(this, SensorService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Log.d(TAG, "SOS Service started");
        }
    }

    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
                checkBatteryOptimization();
            } else {
                Toast.makeText(this, "Some permissions denied. SOS may not work properly.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IGNORE_BATTERY_OPTIMIZATION_REQUEST) {
            startSensorService();
        }
    }

    // Method to show popup menu
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.ai_features_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_ai_teacher) {
                    startActivity(new Intent(MainActivity.this, AIVoiceTeacherActivity.class));
                    return true;
                } else if (id == R.id.nav_about) {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    return true;
                } else if (id == R.id.nav_emotion) {
                    startActivity(new Intent(MainActivity.this, EmotionDetectionActivity.class));
                    return true;
                } else if (id == R.id.nav_sos) {
                    startActivity(new Intent(MainActivity.this, SOSManagementActivity.class));
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }

    // --- Student Login ---
    public void onStudentLoginClick(View view) {
        Log.d(TAG, "Student login clicked");
        try {
            Intent intent = new Intent(MainActivity.this, StudentLoginSplashActivity.class);
            startActivity(intent);
            Log.d(TAG, "Student dashboard intent started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting student dashboard: " + e.getMessage());
            Toast.makeText(this, "Error opening student dashboard", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Teacher Login ---
    public void onTeacherLoginClick(View view) {
        Log.d(TAG, "Teacher login clicked");
        try {
            Intent intent = new Intent(this, TeacherLoginActivity.class);
            startActivity(intent);
            Log.d(TAG, "Teacher login intent started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting teacher login: " + e.getMessage());
            Toast.makeText(this, "Error opening teacher login", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Feature Click Handlers ---
    public void onAIChatClick(View view) {
        Log.d(TAG, "Multi-AI Chat clicked");
        try {
            Intent intent = new Intent(this, MultiAIChatActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting Multi-AI Chat: " + e.getMessage());
            Toast.makeText(this, "Error opening Multi-AI Chat", Toast.LENGTH_SHORT).show();
        }
    }

    public void onQuizClick(View view) {
        Log.d(TAG, "Quiz clicked");
        try {
            Intent intent = new Intent(this, QuizActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting Quiz: " + e.getMessage());
            Toast.makeText(this, "Error opening Quiz", Toast.LENGTH_SHORT).show();
        }
    }

    public void onFlashcardsClick(View view) {
        Log.d(TAG, "Flashcards clicked");
        try {
            Intent intent = new Intent(this, FlashcardsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting Flashcards: " + e.getMessage());
            Toast.makeText(this, "Error opening Flashcards", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStudyPlannerClick(View view) {
        Log.d(TAG, "Study Planner clicked");
        Toast.makeText(this, "Study Planner coming soon! 📅", Toast.LENGTH_SHORT).show();
    }

    public void onTrackCollegeClick(View view) {
        Log.d(TAG, "Track College clicked");
        try {
            Intent intent = new Intent(this, TrackCollegeActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting Track College: " + e.getMessage());
            Toast.makeText(this, "Error opening Track College", Toast.LENGTH_SHORT).show();
        }
    }

    public void onAIQuizGeneratorClick(View view) {
        Log.d(TAG, "AI Quiz Generator clicked");
        try {
            Intent intent = new Intent(this, AIQuizGeneratorActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting AI Quiz Generator: " + e.getMessage());
            Toast.makeText(this, "Error opening AI Quiz Generator", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if service is running and restart if needed
        if (!isMyServiceRunning(SensorService.class) && hasRequiredPermissions()) {
            startSensorService();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
