package com.example.adityalearn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private LinearLayout quizContainer;
    private FloatingActionButton fabAddQuiz;
    private boolean isTeacher = false;

    private ArrayList<File> quizFiles = new ArrayList<>();
    private ActivityResultLauncher<String> filePickerLauncher;
    
    // Gamification elements
    private TextView tvXpPoints, tvStreak, tvLevel, tvRank;
    private SharedPreferences gamePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_activity);

        quizContainer = findViewById(R.id.quiz_container);
        fabAddQuiz = findViewById(R.id.fab_add_quiz);
        
        // Initialize gamification elements
        tvXpPoints = findViewById(R.id.tv_xp_points);
        tvStreak = findViewById(R.id.tv_streak);
        tvLevel = findViewById(R.id.tv_level);
        tvRank = findViewById(R.id.tv_rank);
        
        // Initialize game preferences
        gamePrefs = getSharedPreferences("game_stats", MODE_PRIVATE);
        updateGameStats();

        SharedPreferences prefs = getSharedPreferences(TeacherLoginActivity.PREFS_NAME, MODE_PRIVATE);
        isTeacher = prefs.getBoolean(TeacherLoginActivity.KEY_IS_LOGGED_IN, false);
        if (isTeacher) fabAddQuiz.setVisibility(View.VISIBLE);

        // File picker (PDF or Excel)
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) saveQuizFile(uri);
                });

        fabAddQuiz.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        loadQuizFiles();
    }

    private void saveQuizFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String name = "quiz_" + System.currentTimeMillis();
            if (uri.toString().endsWith(".pdf")) name += ".pdf";
            else name += ".xlsx";

            File file = new File(getFilesDir(), name);
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) fos.write(buffer, 0, len);

            fos.close();
            inputStream.close();

            Toast.makeText(this, "Quiz uploaded", Toast.LENGTH_SHORT).show();
            loadQuizFiles();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadQuizFiles() {
        quizContainer.removeAllViews();
        quizFiles.clear();

        File[] files = getFilesDir().listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().startsWith("quiz_")) quizFiles.add(f);
            }
        }

        if (quizFiles.isEmpty()) {
            TextView tvNo = new TextView(this);
            tvNo.setText("No quizzes available");
            tvNo.setTextSize(16);
            tvNo.setPadding(0, 32, 0, 0);
            quizContainer.addView(tvNo);
        } else {
            for (File f : quizFiles) addQuizToView(f);
        }
    }

    private void addQuizToView(File file) {
        CardView card = (CardView) getLayoutInflater().inflate(R.layout.item_mcq_question, null);
        TextView tvQuestion = card.findViewById(R.id.tv_question);
        RadioGroup rgOptions = card.findViewById(R.id.rg_options);

        if (file.getName().endsWith(".pdf")) {
            tvQuestion.setText(file.getName());
            card.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                try { startActivity(intent); }
                catch (Exception e) { Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show(); }
            });
        } else if (file.getName().endsWith(".xlsx")) {
            List<String[]> mcqs = readExcel(file);
            if (!mcqs.isEmpty()) {
                Collections.shuffle(mcqs);
                String[] q = mcqs.get(0); // Random question
                tvQuestion.setText(q[0]); // Question text
                rgOptions.removeAllViews();
                for (int i = 1; i < q.length; i++) {
                    RadioButton rb = new RadioButton(this);
                    rb.setText(q[i]);
                    rgOptions.addView(rb);
                }
            }
        }

        quizContainer.addView(card);
    }

    private List<String[]> readExcel(File file) {
        List<String[]> questions = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                for (Row row : workbook.getSheetAt(s)) {
                    int cellCount = row.getPhysicalNumberOfCells();
                    String[] q = new String[cellCount];
                    int i = 0;
                    for (Cell cell : row) q[i++] = cell.toString();
                    questions.add(q);
                }
            }
            workbook.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questions;
    }
    
    // Gamification Methods
    private void updateGameStats() {
        int xp = gamePrefs.getInt("xp_points", 1250);
        int streak = gamePrefs.getInt("streak", 7);
        int level = gamePrefs.getInt("level", 5);
        int rank = gamePrefs.getInt("rank", 12);
        
        tvXpPoints.setText(String.valueOf(xp));
        tvStreak.setText(String.valueOf(streak));
        tvLevel.setText("Level " + level);
        tvRank.setText("#" + rank);
    }
    
    private void addXP(int points) {
        int currentXP = gamePrefs.getInt("xp_points", 1250);
        int newXP = currentXP + points;
        gamePrefs.edit().putInt("xp_points", newXP).apply();
        updateGameStats();
        
        // Show XP gain animation
        Toast.makeText(this, "+" + points + " XP!", Toast.LENGTH_SHORT).show();
    }
    
    private void updateStreak() {
        int currentStreak = gamePrefs.getInt("streak", 7);
        gamePrefs.edit().putInt("streak", currentStreak + 1).apply();
        updateGameStats();
    }
    
    // Click Handlers for new quiz types
    public void onBackClick(View view) {
        onBackPressed();
    }
    
    public void onQuickQuizClick(View view) {
        // Start quick quiz (5 questions, 2 minutes)
        Toast.makeText(this, "Quick Quiz: 5 questions, 2 minutes, +50 XP! 🚀", Toast.LENGTH_LONG).show();
        addXP(50);
        updateStreak();
    }
    
    public void onTimedChallengeClick(View view) {
        // Start timed challenge (20 questions, 10 minutes)
        Toast.makeText(this, "Timed Challenge: 20 questions, 10 minutes, +200 XP! ⏰", Toast.LENGTH_LONG).show();
        addXP(200);
        updateStreak();
    }
    
    public void onCodingChallengeClick(View view) {
        // Start coding challenge
        Toast.makeText(this, "Coding Challenge: Solve problems, +300 XP! 💻", Toast.LENGTH_LONG).show();
        addXP(300);
        updateStreak();
    }
    
    public void onLeaderboardClick(View view) {
        // Show leaderboard
        Toast.makeText(this, "Leaderboard: You're currently #12! 🏆", Toast.LENGTH_LONG).show();
    }
}
