package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class AiChatSplashActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat_splash);

        videoView = findViewById(R.id.splashVideo);

        // Play video from raw folder
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ai_intro);
        videoView.setVideoURI(uri);
        videoView.start();

        // After video finishes → go to AIChatActivity
        videoView.setOnCompletionListener(mp -> {
            Intent intent = new Intent(AiChatSplashActivity.this, AIChatActivity.class);
            startActivity(intent);
            finish(); // close splash so user can’t return
        });
    }
}
