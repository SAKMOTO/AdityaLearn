package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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

public class ModelActivity extends AppCompatActivity {

    private static final String TAG = "ModelActivity";
    // Attempt Hunyuan3D-like text-to-3D via Inference API (may return error if unsupported)
    // You can switch this to any 3D-capable model that returns .glb/.obj from Inference API.
    private static final String HF_3D_URL = "https://api-inference.huggingface.co/models/tencent/Hunyuan3D-2";
    private static final String HF_TOKEN = BuildConfig.HUGGINGFACE_API_KEY;

    private EditText promptInput;
    private Button generateBtn;
    private ProgressBar progress;
    private TextView resultText;
    private Button openBtn;

    private OkHttpClient client;
    private File modelFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);

        promptInput = findViewById(R.id.et_prompt);
        generateBtn = findViewById(R.id.btn_generate);
        progress = findViewById(R.id.progress);
        resultText = findViewById(R.id.tv_result);
        openBtn = findViewById(R.id.btn_open);

        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        generateBtn.setOnClickListener(v -> doGenerate3D());
        openBtn.setOnClickListener(v -> openModel());
    }

    private void doGenerate3D() {
        String prompt = promptInput.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "Enter a prompt", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        resultText.setText("");
        openBtn.setVisibility(View.GONE);

        try {
            JSONObject json = new JSONObject();
            json.put("inputs", prompt);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(HF_3D_URL)
                    .addHeader("Authorization", "Bearer " + HF_TOKEN)
                    .addHeader("Accept", "application/octet-stream")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "HF 3D call failed", e);
                    runOnUiThread(() -> {
                        setLoading(false);
                        resultText.setText("Network error: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        final String err = response.body() != null ? response.body().string() : ("HTTP " + response.code());
                        runOnUiThread(() -> {
                            setLoading(false);
                            resultText.setText("API error: " + err + "\nIf this model is not supported via Inference API, consider using the Space via a server, or change to a 3D model endpoint that returns .glb/.obj.");
                        });
                        return;
                    }

                    // Save binary to a .glb file by default
                    byte[] data = response.body().bytes();
                    modelFile = new File(getCacheDir(), "model_output.glb");
                    try (FileOutputStream fos = new FileOutputStream(modelFile)) {
                        fos.write(data);
                    }

                    runOnUiThread(() -> {
                        setLoading(false);
                        resultText.setText("Model saved: " + modelFile.getAbsolutePath() + "\nTap Open to view in an external 3D viewer.");
                        openBtn.setVisibility(View.VISIBLE);
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Request build error", e);
            setLoading(false);
            resultText.setText("Error: " + e.getMessage());
        }
    }

    private void openModel() {
        if (modelFile == null || !modelFile.exists()) {
            Toast.makeText(this, "No model file available", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", modelFile);
            String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            String mime = ext != null && !ext.isEmpty() ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) : "application/octet-stream";

            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(uri, mime);
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(viewIntent, "Open 3D Model"));
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open the model. You can share the file from: " + modelFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        generateBtn.setEnabled(!loading);
    }
}
