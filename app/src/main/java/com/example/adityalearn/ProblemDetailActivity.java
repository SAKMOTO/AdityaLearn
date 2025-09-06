package com.example.adityalearn;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.*;

public class ProblemDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvStatement;
    private EditText etCode;
    private Spinner spLanguage;
    private Button btnRun;
    private TextView tvOutput;

    // Replace with your run API endpoint
    private static final String RUN_ENDPOINT = "https://your-run-api.com/api/run";
    private static final String RUN_API_KEY = ""; // optional for headers

    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService ex = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();
    private static final String TAG = "ProblemDetail";

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_problem_detail);

        tvTitle = findViewById(R.id.tvTitle);
        tvStatement = findViewById(R.id.tvStatement);
        etCode = findViewById(R.id.etCode);
        spLanguage = findViewById(R.id.spLanguage);
        btnRun = findViewById(R.id.btnRun);
        tvOutput = findViewById(R.id.tvOutput);

        String id = getIntent().getStringExtra("problem_id");
        String title = getIntent().getStringExtra("problem_title");
        String statement = getIntent().getStringExtra("problem_statement");

        tvTitle.setText(title);
        tvStatement.setText(statement);

        String[] langs = new String[] {"c", "cpp", "java", "python3"};
        spLanguage.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, langs));

        etCode.setText(getStarterCodeFor("python3"));

        btnRun.setOnClickListener(v -> {
            String code = etCode.getText().toString();
            String lang = spLanguage.getSelectedItem().toString();
            runCode(code, lang, id);
        });
    }

    private String getStarterCodeFor(String lang) {
        switch (lang) {
            case "c":
                return "#include <stdio.h>\nint main(){\n    // write your code\n    return 0;\n}";
            case "cpp":
                return "#include <bits/stdc++.h>\nusing namespace std;\nint main(){\n    // write your code\n    return 0;\n}";
            case "java":
                return "public class Main {\n    public static void main(String[] args) {\n        // write your code\n    }\n}";
            default:
                return "print('Hello')";
        }
    }

    private void runCode(String sourceCode, String language, String problemId) {
        tvOutput.setText("Running...");
        ex.execute(() -> {
            try {
                // Build a JSON payload compatible with your run API.
                // Example generic payload:
                // { "language": "python3", "source_code": "<raw source>", "stdin": "" }
                // If your run API requires base64_source, encode here.

                var payload = new RunRequest(language, sourceCode, problemId);
                String json = gson.toJson(payload);

                RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
                Request.Builder b = new Request.Builder().url(RUN_ENDPOINT).post(body);
                if (!RUN_API_KEY.isEmpty()) {
                    b.header("x-api-key", RUN_API_KEY);
                }
                Request req = b.build();

                try (Response resp = client.newCall(req).execute()) {
                    if (!resp.isSuccessful()) {
                        final String msg = "Run API error: " + resp.message();
                        runOnUiThread(() -> tvOutput.setText(msg));
                        return;
                    }
                    String respBody = resp.body().string();
                    // For now show the raw JSON; later parse fields like stdout, stderr.
                    runOnUiThread(() -> tvOutput.setText(respBody));
                }
            } catch (IOException e) {
                Log.e(TAG, "runCode error", e);
                runOnUiThread(() -> tvOutput.setText("Run error: " + e.getMessage()));
            }
        });
    }

    static class RunRequest {
        String language;
        String source_code;
        String problem_id;
        RunRequest(String l, String s, String pid) { language = l; source_code = s; problem_id = pid; }
    }
}
