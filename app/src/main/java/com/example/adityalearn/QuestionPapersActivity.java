package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class QuestionPapersActivity extends AppCompatActivity {

    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_papers);

        container = findViewById(R.id.qpapers_container);

        // List of titles and URLs
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> urls = new ArrayList<>();

        titles.add("AI UNIT 1");
        urls.add("https://drive.google.com/file/d/11khDEeWJRAetMplNYnSziO1zA-A4zGML/view?usp=sharing");

        titles.add("DMGT UNIT 1");
        urls.add("https://drive.google.com/file/d/1DgFeuj3qkiQQUyIlQgTGBsHpZlMAc0DO/view?usp=sharing");

        titles.add("JAVA UNIT 1");
        urls.add("https://drive.google.com/file/d/1JlBfYojCK5fQwZz4FBxYwIxVmNOEwN5j/view?usp=sharing");

        // Dynamically add buttons
        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            String url = urls.get(i);

            Button btn = new Button(this);
            btn.setText(title);
            btn.setAllCaps(false);
            btn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            btn.setTextColor(getResources().getColor(android.R.color.white));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 16);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Cannot open link", Toast.LENGTH_SHORT).show();
                }
            });

            container.addView(btn);
        }
    }
}
