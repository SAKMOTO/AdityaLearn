package com.example.adityalearn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class StudentDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
    }

    public void onBackClick(View view) {
        onBackPressed();
    }

    public void onWebsiteClick(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
    }

    public void onCodingClick(View view) {
        Intent intent = new Intent(this, CodingLanguagesActivity.class);
        startActivity(intent);
    }

    public void onLearnClick(View view) {
        Intent intent = new Intent(this, LearningActivity.class);
        startActivity(intent);
    }

    public void onProblemsClick(View view) {
        Intent intent = new Intent(this, ProblemActivity.class);
        startActivity(intent);
    }
}
