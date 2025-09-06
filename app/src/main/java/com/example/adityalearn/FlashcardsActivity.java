package com.example.adityalearn;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlashcardsActivity extends AppCompatActivity {
    
    private LinearLayout flashcardContainer;
    private TextView tvQuestion, tvAnswer, tvCardCount;
    private Button btnShowAnswer, btnNext, btnPrevious;
    private FloatingActionButton fabAddCard;
    
    private List<Flashcard> flashcards;
    private int currentCardIndex = 0;
    private boolean showingAnswer = false;
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);
        
        initializeViews();
        loadFlashcards();
        displayCurrentCard();
    }
    
    private void initializeViews() {
        flashcardContainer = findViewById(R.id.flashcard_container);
        tvQuestion = findViewById(R.id.tv_question);
        tvAnswer = findViewById(R.id.tv_answer);
        tvCardCount = findViewById(R.id.tv_card_count);
        btnShowAnswer = findViewById(R.id.btn_show_answer);
        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_previous);
        fabAddCard = findViewById(R.id.fab_add_card);
        
        prefs = getSharedPreferences("flashcards", MODE_PRIVATE);
        flashcards = new ArrayList<>();
        
        // Set click listeners
        btnShowAnswer.setOnClickListener(v -> toggleAnswer());
        btnNext.setOnClickListener(v -> nextCard());
        btnPrevious.setOnClickListener(v -> previousCard());
        fabAddCard.setOnClickListener(v -> addNewCard());
    }
    
    private void loadFlashcards() {
        // Load sample flashcards or from preferences
        if (flashcards.isEmpty()) {
            // Add sample flashcards
            flashcards.add(new Flashcard("What is Android?", "Android is a mobile operating system based on Linux."));
            flashcards.add(new Flashcard("What is Java?", "Java is a programming language and computing platform."));
            flashcards.add(new Flashcard("What is XML?", "XML stands for eXtensible Markup Language."));
            flashcards.add(new Flashcard("What is API?", "API stands for Application Programming Interface."));
            flashcards.add(new Flashcard("What is SQL?", "SQL stands for Structured Query Language."));
        }
    }
    
    private void displayCurrentCard() {
        if (flashcards.isEmpty()) {
            tvQuestion.setText("No flashcards available");
            tvAnswer.setText("");
            tvCardCount.setText("0 / 0");
            return;
        }
        
        Flashcard currentCard = flashcards.get(currentCardIndex);
        tvQuestion.setText(currentCard.getQuestion());
        tvAnswer.setText(showingAnswer ? currentCard.getAnswer() : "Tap 'Show Answer' to reveal");
        tvCardCount.setText((currentCardIndex + 1) + " / " + flashcards.size());
        
        btnShowAnswer.setText(showingAnswer ? "Hide Answer" : "Show Answer");
        btnPrevious.setEnabled(currentCardIndex > 0);
        btnNext.setEnabled(currentCardIndex < flashcards.size() - 1);
    }
    
    private void toggleAnswer() {
        showingAnswer = !showingAnswer;
        displayCurrentCard();
    }
    
    private void nextCard() {
        if (currentCardIndex < flashcards.size() - 1) {
            currentCardIndex++;
            showingAnswer = false;
            displayCurrentCard();
        }
    }
    
    private void previousCard() {
        if (currentCardIndex > 0) {
            currentCardIndex--;
            showingAnswer = false;
            displayCurrentCard();
        }
    }
    
    private void addNewCard() {
        // Simple way to add new cards - in a real app, you'd have a dialog
        flashcards.add(new Flashcard("New Question", "New Answer"));
        Toast.makeText(this, "New flashcard added!", Toast.LENGTH_SHORT).show();
        displayCurrentCard();
    }
    
    public void onBackClick(View view) {
        onBackPressed();
    }
    
    public void onShuffleClick(View view) {
        Collections.shuffle(flashcards);
        currentCardIndex = 0;
        showingAnswer = false;
        displayCurrentCard();
        Toast.makeText(this, "Cards shuffled!", Toast.LENGTH_SHORT).show();
    }
    
    // Simple Flashcard class
    private static class Flashcard {
        private String question;
        private String answer;
        
        public Flashcard(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
        
        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
    }
}
