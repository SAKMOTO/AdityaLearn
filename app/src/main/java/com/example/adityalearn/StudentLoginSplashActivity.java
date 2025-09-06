package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;

public class StudentLoginSplashActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login_splash);

        videoView = findViewById(R.id.videoView);

        // Set video from raw folder
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.student_login_anim);
        videoView.setVideoURI(videoUri);

        // Start playing
        videoView.start();

        // When video finishes, go to Student Dashboard
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent = new Intent(StudentLoginSplashActivity.this, StudentDashboardActivity.class);
                startActivity(intent);
                finish(); // Close animation activity
            }
        });
    }
}
