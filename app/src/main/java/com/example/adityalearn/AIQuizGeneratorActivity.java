package com.example.adityalearn;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIQuizGeneratorActivity extends AppCompatActivity {
    
    private static final String API_KEY = "AIzaSyDOhRmTo7ycJ-cUaDf2SyBHbcZHGvYtCqg";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;
    
    private EditText etTopic, etDifficulty, etQuestionCount;
    private Button btnGenerateQuiz, btnSubmitAnswer;
    private ProgressBar progressBar;
    private LinearLayout quizContainer, answerContainer;
    private TextView tvQuestion, tvScore, tvTimer;
    
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int timeLeft = 30;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private SharedPreferences gamePrefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_quiz_generator);
        
        initializeViews();
        gamePrefs = getSharedPreferences("game_stats", MODE_PRIVATE);
    }
    
    private void initializeViews() {
        etTopic = findViewById(R.id.et_topic);
        etDifficulty = findViewById(R.id.et_difficulty);
        etQuestionCount = findViewById(R.id.et_question_count);
        btnGenerateQuiz = findViewById(R.id.btn_generate_quiz);
        btnSubmitAnswer = findViewById(R.id.btn_submit_answer);
        progressBar = findViewById(R.id.progress_bar);
        quizContainer = findViewById(R.id.quiz_container);
        answerContainer = findViewById(R.id.answer_container);
        tvQuestion = findViewById(R.id.tv_question);
        tvScore = findViewById(R.id.tv_score);
        tvTimer = findViewById(R.id.tv_timer);
        
        btnGenerateQuiz.setOnClickListener(v -> generateQuiz());
        btnSubmitAnswer.setOnClickListener(v -> submitAnswer());
        
        // Set default values
        etTopic.setText("Android Development");
        etDifficulty.setText("Medium");
        etQuestionCount.setText("5");
    }
    
    private void generateQuiz() {
        String topic = etTopic.getText().toString().trim();
        String difficulty = etDifficulty.getText().toString().trim();
        String questionCount = etQuestionCount.getText().toString().trim();
        
        if (topic.isEmpty() || difficulty.isEmpty() || questionCount.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnGenerateQuiz.setEnabled(false);
        
        String prompt = String.format(
            "Generate %s multiple choice questions about %s with %s difficulty level. " +
            "Format each question as JSON with: question, options (array of 4 options), correct_answer (index 0-3), explanation. " +
            "Return only valid JSON array format.",
            questionCount, topic, difficulty
        );
        
        generateQuizWithAI(prompt);
    }
    
    private void generateQuizWithAI(String prompt) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                
                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                
                part.put("text", prompt);
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
                requestBody.put("contents", contents);
                
                RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.get("application/json")
                );
                
                Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();
                
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnGenerateQuiz.setEnabled(true);
                            Toast.makeText(AIQuizGeneratorActivity.this, "Failed to generate quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                    
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body().string();
                        
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnGenerateQuiz.setEnabled(true);
                            
                            if (response.isSuccessful()) {
                                parseAndDisplayQuiz(responseBody);
                            } else {
                                Toast.makeText(AIQuizGeneratorActivity.this, "API Error: " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnGenerateQuiz.setEnabled(true);
                    Toast.makeText(AIQuizGeneratorActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void parseAndDisplayQuiz(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            String text = parts.getJSONObject(0).getString("text");
            
            // Clean the response text
            text = text.replace("```json", "").replace("```", "").trim();
            
            JSONArray questionsArray = new JSONArray(text);
            questions = new ArrayList<>();
            
            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject q = questionsArray.getJSONObject(i);
                QuizQuestion question = new QuizQuestion();
                question.question = q.getString("question");
                question.correctAnswer = q.getInt("correct_answer");
                question.explanation = q.getString("explanation");
                
                JSONArray options = q.getJSONArray("options");
                question.options = new String[4];
                for (int j = 0; j < 4; j++) {
                    question.options[j] = options.getString(j);
                }
                
                questions.add(question);
            }
            
            startQuiz();
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed to parse quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void startQuiz() {
        currentQuestionIndex = 0;
        score = 0;
        timeLeft = 30;
        
        quizContainer.setVisibility(View.VISIBLE);
        answerContainer.setVisibility(View.GONE);
        
        displayQuestion();
        startTimer();
    }
    
    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showResults();
            return;
        }
        
        QuizQuestion question = questions.get(currentQuestionIndex);
        tvQuestion.setText(question.question);
        tvScore.setText("Score: " + score + "/" + questions.size());
        
        // Clear previous options
        answerContainer.removeAllViews();
        
        // Create radio group for options
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        
        for (int i = 0; i < question.options.length; i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(question.options[i]);
            radioButton.setId(i);
            radioButton.setTextSize(16);
            radioButton.setPadding(16, 16, 16, 16);
            radioGroup.addView(radioButton);
        }
        
        answerContainer.addView(radioGroup);
        answerContainer.setVisibility(View.VISIBLE);
        
        // Reset timer
        timeLeft = 30;
        updateTimer();
    }
    
    private void startTimer() {
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timeLeft > 0) {
                    timeLeft--;
                    updateTimer();
                    timerHandler.postDelayed(this, 1000);
                } else {
                    // Time's up
                    submitAnswer();
                }
            }
        };
        timerHandler.post(timerRunnable);
    }
    
    private void updateTimer() {
        tvTimer.setText("Time: " + timeLeft + "s");
    }
    
    private void submitAnswer() {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        
        RadioGroup radioGroup = (RadioGroup) answerContainer.getChildAt(0);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        
        QuizQuestion question = questions.get(currentQuestionIndex);
        boolean isCorrect = selectedId == question.correctAnswer;
        
        if (isCorrect) {
            score++;
            Toast.makeText(this, "Correct! ✅", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! ❌", Toast.LENGTH_SHORT).show();
        }
        
        // Show explanation
        TextView explanation = new TextView(this);
        explanation.setText("Explanation: " + question.explanation);
        explanation.setTextSize(14);
        explanation.setPadding(16, 16, 16, 16);
        explanation.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        answerContainer.addView(explanation);
        
        // Move to next question after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 3000);
    }
    
    private void showResults() {
        quizContainer.setVisibility(View.GONE);
        answerContainer.removeAllViews();
        
        TextView results = new TextView(this);
        results.setText("Quiz Complete!\n\nScore: " + score + "/" + questions.size() + 
                       "\nPercentage: " + (score * 100 / questions.size()) + "%");
        results.setTextSize(20);
        results.setPadding(32, 32, 32, 32);
        results.setGravity(android.view.Gravity.CENTER);
        answerContainer.addView(results);
        answerContainer.setVisibility(View.VISIBLE);
        
        // Add XP based on score
        int xpEarned = score * 20;
        addXP(xpEarned);
        
        Toast.makeText(this, "Quiz Complete! +" + xpEarned + " XP earned!", Toast.LENGTH_LONG).show();
    }
    
    private void addXP(int points) {
        int currentXP = gamePrefs.getInt("xp_points", 1250);
        int newXP = currentXP + points;
        gamePrefs.edit().putInt("xp_points", newXP).apply();
    }
    
    public void onBackClick(View view) {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        onBackPressed();
    }
    
    // Quiz Question class
    private static class QuizQuestion {
        String question;
        String[] options;
        int correctAnswer;
        String explanation;
    }
}
