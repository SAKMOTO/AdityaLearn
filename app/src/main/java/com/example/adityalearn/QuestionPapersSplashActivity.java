package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionPapersSplashActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_papers_splash);

        videoView = findViewById(R.id.splashVideo);

        // Play video from raw folder
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.question_papers_intro);
        videoView.setVideoURI(uri);
        videoView.start();

        // After video finishes → go to QuestionPapersActivity
        videoView.setOnCompletionListener(mp -> {
            Intent intent = new Intent(QuestionPapersSplashActivity.this, QuestionPapersActivity.class);
            startActivity(intent);
            finish(); // close splash
        });
    }
}
