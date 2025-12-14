package com.example.adityalearn;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

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

public class VideoActivity extends AppCompatActivity {

    private static final String TAG = "VideoActivity";
    private static final String HF_SVD_URL = "https://api-inference.huggingface.co/models/stabilityai/stable-video-diffusion";
    private static final String HF_TOKEN = BuildConfig.HUGGINGFACE_API_KEY;

    private EditText promptInput;
    private EditText imageUrlInput;
    private Button generateBtn;
    private ProgressBar progress;
    private VideoView videoView;

    private OkHttpClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        promptInput = findViewById(R.id.et_prompt);
        imageUrlInput = findViewById(R.id.et_image_url);
        generateBtn = findViewById(R.id.btn_generate);
        progress = findViewById(R.id.progress);
        videoView = findViewById(R.id.video_result);

        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        generateBtn.setOnClickListener(v -> doGenerateVideo());
    }

    private void doGenerateVideo() {
        String prompt = promptInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        if (TextUtils.isEmpty(imageUrl)) {
            Toast.makeText(this, "Stable Video Diffusion requires an image URL. Please provide one.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        try {
            JSONObject json = new JSONObject();
            json.put("inputs", imageUrl); // For image-to-video, send the image URL as inputs
            JSONObject params = new JSONObject();
            if (!prompt.isEmpty()) params.put("prompt", prompt);
            json.put("parameters", params);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(HF_SVD_URL)
                    .addHeader("Authorization", "Bearer " + HF_TOKEN)
                    .addHeader("Accept", "video/mp4")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "HF SVD call failed", e);
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(VideoActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        final String err = response.body() != null ? response.body().string() : ("HTTP " + response.code());
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(VideoActivity.this, "API error: " + err, Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    // Save MP4 to cache and play
                    byte[] data = response.body().bytes();
                    File out = new File(getCacheDir(), "svd_output.mp4");
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        fos.write(data);
                    }

                    runOnUiThread(() -> {
                        setLoading(false);
                        videoView.setVideoURI(Uri.fromFile(out));
                        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
                        videoView.start();
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
