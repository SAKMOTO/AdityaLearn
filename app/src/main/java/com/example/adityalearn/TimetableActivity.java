package com.example.adityalearn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class TimetableActivity extends AppCompatActivity {

    private LinearLayout timetableContainer;
    private FloatingActionButton fabAddTimetable;
    private boolean isTeacher = false;

    private ArrayList<File> pdfFiles = new ArrayList<>();
    private ActivityResultLauncher<String> pdfPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        timetableContainer = findViewById(R.id.timetable_container);
        fabAddTimetable = findViewById(R.id.fab_add_timetable);

        // Check if teacher from shared prefs
        SharedPreferences prefs = getSharedPreferences(TeacherLoginActivity.PREFS_NAME, MODE_PRIVATE);
        isTeacher = prefs.getBoolean(TeacherLoginActivity.KEY_IS_LOGGED_IN, false);

        // Also check intent extra
        if (getIntent() != null && getIntent().hasExtra("isTeacher")) {
            isTeacher = getIntent().getBooleanExtra("isTeacher", isTeacher);
        }

        // Show FAB for teacher
        fabAddTimetable.setVisibility(isTeacher ? android.view.View.VISIBLE : android.view.View.GONE);

        // PDF picker
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) savePdf(uri);
                });

        fabAddTimetable.setOnClickListener(v -> pdfPickerLauncher.launch("application/pdf"));

        loadPDFs();
    }

    private void savePdf(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File dir = new File(getFilesDir(), "timetables");
            if (!dir.exists()) dir.mkdirs();

            String name = "timetable_" + System.currentTimeMillis() + ".pdf";
            File file = new File(dir, name);

            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) fos.write(buffer, 0, len);

            fos.close();
            is.close();

            Toast.makeText(this, "PDF uploaded", Toast.LENGTH_SHORT).show();
            loadPDFs();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPDFs() {
        timetableContainer.removeAllViews();
        pdfFiles.clear();

        File dir = new File(getFilesDir(), "timetables");
        if (!dir.exists()) dir.mkdirs();

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".pdf")) pdfFiles.add(f);
            }
        }

        if (pdfFiles.isEmpty()) {
            TextView tvNo = new TextView(this);
            tvNo.setText("No timetable available");
            tvNo.setTextSize(16);
            tvNo.setPadding(0, 32, 0, 0);
            timetableContainer.addView(tvNo);
        } else {
            for (File f : pdfFiles) addPdfToView(f);
        }
    }

    private void addPdfToView(File file) {
        CardView card = (CardView) getLayoutInflater().inflate(R.layout.item_pdf, null);

        TextView tvName = card.findViewById(R.id.tv_pdf_name);
        ImageButton btnDelete = card.findViewById(R.id.btn_delete_pdf);

        tvName.setText(file.getName());

        // Delete only for teacher
        if (isTeacher) {
            btnDelete.setVisibility(android.view.View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                if (file.delete()) {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    loadPDFs();
                } else Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
            });
        }

        // Open PDF externally
        card.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
            }
        });

        timetableContainer.addView(card);
    }
}
