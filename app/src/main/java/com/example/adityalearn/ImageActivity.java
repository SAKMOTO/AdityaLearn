package com.example.adityalearn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = "ImageActivity";
    // Hugging Face Inference API endpoint for Stable Diffusion
    private static final String HF_SD_URL = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion";
    // Placeholder token (replace via secure storage in production)
    private static final String HF_TOKEN = BuildConfig.HUGGINGFACE_API_KEY;

    private EditText promptInput;
    private Button generateBtn;
    private ProgressBar progress;
    private ImageView imageView;

    private OkHttpClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // Initialize views
        promptInput = findViewById(R.id.et_prompt);
        generateBtn = findViewById(R.id.btn_generate);
        progress = findViewById(R.id.progress);
        imageView = findViewById(R.id.image_result);

        // Simple OkHttp client
        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        // Generate button handler
        generateBtn.setOnClickListener(v -> doGenerateImage());
    }

    // Trigger Stable Diffusion image generation using Hugging Face Inference API
    private void doGenerateImage() {
        String prompt = promptInput.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "Enter a prompt", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        try {
            // Request body: JSON with the prompt
            JSONObject json = new JSONObject();
            json.put("inputs", prompt);

            RequestBody body = RequestBody.create(
                    json.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(HF_SD_URL)
                    .addHeader("Authorization", "Bearer " + HF_TOKEN)
                    .addHeader("Accept", "image/png")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "HF SD call failed", e);
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(ImageActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        final String err = response.body() != null ? response.body().string() : ("HTTP " + response.code());
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(ImageActivity.this, "API error: " + err, Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    // Read image bytes and decode to Bitmap
                    byte[] data = response.body().bytes();
                    final Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                    runOnUiThread(() -> {
                        setLoading(false);
                        if (bmp != null) {
                            imageView.setImageBitmap(bmp);
                        } else {
                            Toast.makeText(ImageActivity.this, "Failed to decode image", Toast.LENGTH_LONG).show();
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

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        generateBtn.setEnabled(!loading);
    }
}
