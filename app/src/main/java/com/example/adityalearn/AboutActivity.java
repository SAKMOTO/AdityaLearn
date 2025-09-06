package com.example.adityalearn;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    ImageView imgTeacher;
    TextView tvName, tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        imgTeacher = findViewById(R.id.imgTeacher);
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);

        // Example → Replace with your actual photo & text
        imgTeacher.setImageResource(R.raw.hod); // add photo in res/drawable
        tvName.setText("HEAD OF DEPARTMENT AI ML MADAM");
        tvDescription.setText("Our beloved Head of Department, Madam, is the guiding light of the Artificial Intelligence and Machine Learning Section. With her immense knowledge, visionary leadership, and deep dedication to teaching, she inspires students to think beyond boundaries and innovate with confidence. She believes in empowering every student with strong fundamentals, research-oriented thinking, and real-world problem-solving skills. Her passion for AI & ML not only builds academic excellence but also nurtures creativity, discipline, and confidence in each learner. Madam has always been a source of strength and motivation, constantly encouraging us to aim higher and achieve greatness.");
    }
}
