package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CodingLanguagesActivity extends AppCompatActivity {

    Button btnC, btnCpp, btnJava, btnPython;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coding_language);

        btnC = findViewById(R.id.btnC);
        btnCpp = findViewById(R.id.btnCpp);
        btnJava = findViewById(R.id.btnJava);
        btnPython = findViewById(R.id.btnPython);

        btnC.setOnClickListener(v -> openCompiler("https://www.programiz.com/c-programming/online-compiler/"));
        btnCpp.setOnClickListener(v -> openCompiler("https://www.programiz.com/cpp-programming/online-compiler/"));
        btnJava.setOnClickListener(v -> openCompiler("https://www.programiz.com/java-programming/online-compiler/"));
        btnPython.setOnClickListener(v -> openCompiler("https://www.programiz.com/python-programming/online-compiler/"));
    }

    private void openCompiler(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
