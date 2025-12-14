package com.example.adityalearn;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpeechActivity extends AppCompatActivity {

    private static final String TAG = "SpeechActivity";
    // Using Kokoro model via Hugging Face Inference API (stable HTTP interface)
    private static final String HF_TTS_URL = "https://api-inference.huggingface.co/models/hexgrad/Kokoro-82M";
    private static final String HF_TOKEN = BuildConfig.HUGGINGFACE_API_KEY;

    private EditText textInput;
    private Button speakBtn;
    private ProgressBar progress;

    private OkHttpClient client;
    private MediaPlayer mediaPlayer;
    private File audioFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        textInput = findViewById(R.id.et_text);
        speakBtn = findViewById(R.id.btn_speak);
        progress = findViewById(R.id.progress);

        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        speakBtn.setOnClickListener(v -> doSynthesize());
    }

    private void doSynthesize() {
        String text = textInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Enter text to speak", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);

        try {
            JSONObject json = new JSONObject();
            json.put("inputs", text);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(HF_TTS_URL)
                    .addHeader("Authorization", "Bearer " + HF_TOKEN)
                    .addHeader("Accept", "audio/mpeg")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "HF TTS call failed", e);
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(SpeechActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        final String err = response.body() != null ? response.body().string() : ("HTTP " + response.code());
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(SpeechActivity.this, "API error: " + err, Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    byte[] data = response.body().bytes();
                    audioFile = new File(getCacheDir(), "tts_output.mp3");
                    try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                        fos.write(data);
                    }

                    runOnUiThread(() -> {
                        try {
                            setLoading(false);
                            playAudio(audioFile);
                        } catch (Exception e) {
                            Toast.makeText(SpeechActivity.this, "Playback error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Request build error", e);
            setLoading(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void playAudio(File file) throws IOException {
        stopIfPlaying();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(file.getAbsolutePath());
        mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        mediaPlayer.setOnCompletionListener(mp -> Toast.makeText(SpeechActivity.this, "Done", Toast.LENGTH_SHORT).show());
        mediaPlayer.prepareAsync();
    }

    private void stopIfPlaying() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        speakBtn.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopIfPlaying();
    }
}
