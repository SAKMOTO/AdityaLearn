package com.example.adityalearn;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class AIVoiceTeacherActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyDOhRmTo7ycJ-cUaDf2SyBHbcZHGvYtCqg"; // replace with your Gemini key

    private TextView responseText;
    private FloatingActionButton micButton;
    private LottieAnimationView idleAnimation, listeningAnimation;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private GeminiApi geminiApi;
    private TextToSpeech tts;  // 👈 Added TTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_teacher);

        responseText = findViewById(R.id.response_text);
        micButton = findViewById(R.id.mic_button);
        idleAnimation = findViewById(R.id.idle_animation);
        listeningAnimation = findViewById(R.id.listening_animation);

        // Runtime mic permission
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        }

        // ✅ Init TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setPitch(1.0f);
                tts.setSpeechRate(1.0f);
            }
        });

        // Setup Retrofit + Gemini API
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geminiApi = retrofit.create(GeminiApi.class);

        // Setup SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        // Play idle animation once
        idleAnimation.setRepeatCount(0);
        idleAnimation.playAnimation();

        // Recognition listener
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) {
                responseText.setText("⚠️ Error listening. Try again.");
            }

            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String question = matches.get(0);
                    responseText.setText("You said: " + question + "\n\n🤖 Thinking...");
                    askGemini(question); // Call Gemini API
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        // Mic button hold listener
        micButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    responseText.setText("🎤 Listening...");
                    idleAnimation.setVisibility(View.GONE);
                    listeningAnimation.setVisibility(View.VISIBLE);
                    listeningAnimation.playAnimation();
                    speechRecognizer.startListening(recognizerIntent);
                    return true;

                case MotionEvent.ACTION_UP:
                    speechRecognizer.stopListening();
                    listeningAnimation.cancelAnimation();
                    listeningAnimation.setVisibility(View.GONE);
                    return true;
            }
            return false;
        });
    }

    // Call Gemini API
    private void askGemini(String question) {
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", question);
        parts.add(part);
        JsonObject contentObj = new JsonObject();
        contentObj.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(contentObj);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);

        geminiApi.getGeminiResponse(requestBody, API_KEY).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String aiResponse = response.body()
                                .getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();

                        responseText.setText("👨‍🏫 Aditya says:\n\n" + aiResponse);

                        // ✅ Speak AI response
                        if (tts != null) {
                            tts.speak(aiResponse, TextToSpeech.QUEUE_FLUSH, null, "AI_RESPONSE");
                        }

                    } catch (Exception e) {
                        responseText.setText("⚠️ Parsing error.");
                    }
                } else {
                    responseText.setText("⚠️ No response. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                responseText.setText("⚠️ API Error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    // Retrofit interface
    interface GeminiApi {
        @Headers("Content-Type: application/json")
        @POST("v1beta/models/gemini-1.5-flash-latest:generateContent")
        Call<JsonObject> getGeminiResponse(@Body JsonObject body, @Query("key") String apiKey);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
