package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class NotesSplashActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_splash);

        videoView = findViewById(R.id.splashVideo);

        // Play video from raw folder (replace "notes_intro" with your video filename)
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notes_intro);
        videoView.setVideoURI(uri);
        videoView.start();

        // After video finishes → go to NotesActivity
        videoView.setOnCompletionListener(mp -> {
            Intent intent = new Intent(NotesSplashActivity.this, NotesActivity.class);
            startActivity(intent);
            finish(); // close splash
        });
    }
}
