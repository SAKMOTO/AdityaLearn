package com.example.adityalearn;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class AIChatFragment extends Fragment {

    private static final String TAG = "AIChatFragment";
    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private ImageButton sendButton;
    private ProgressBar typingIndicator;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private RecyclerView.AdapterDataObserver adapterDataObserver; // Store the observer

    private static final String GEMINI_MODEL = "gemini-2.5-flash";
    private GeminiApi geminiApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        inputMessage = view.findViewById(R.id.inputMessage);
        sendButton = view.findViewById(R.id.sendButton);
        typingIndicator = view.findViewById(R.id.typingIndicator);

        // Initialize adapter FIRST
        chatAdapter = new ChatAdapter(messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        setupRetrofit();
        setupKeyboardBehavior();

        sendButton.setOnClickListener(v -> {
            final String text = inputMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            addMessage(new ChatMessage("user", text));
            inputMessage.setText("");
            requestGemini(text);
        });

        // Add focus listener to auto-scroll when typing
        inputMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollToBottom();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupKeyboardListeners();
    }

    private void setupKeyboardListeners() {
        final View rootView = getView();
        if (rootView != null) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Check if keyboard is visible and scroll to bottom
                    scrollToBottom();
                }
            });
        }
    }

    private void setupKeyboardBehavior() {
        // Create and store the adapter data observer
        adapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                scrollToBottom();
            }
        };

        // Register the observer
        chatAdapter.registerAdapterDataObserver(adapterDataObserver);

        // Scroll to bottom when layout changes (keyboard shows/hides)
        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    // Keyboard is shown, scroll to bottom
                    scrollToBottom();
                }
            }
        });
    }

    private void scrollToBottom() {
        if (chatRecyclerView != null && messages.size() > 0) {
            chatRecyclerView.postDelayed(() -> {
                chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
            }, 100);
        }
    }

    private void addMessage(ChatMessage m) {
        if (chatAdapter == null) {
            Log.e(TAG, "ChatAdapter is null!");
            return;
        }
        messages.add(m);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void setupRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .callTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geminiApi = retrofit.create(GeminiApi.class);
    }

    private void requestGemini(String userText) {
        typingIndicator.setVisibility(View.VISIBLE);
        Log.d(TAG, "Sending request: " + userText);

        // Build request
        Part p = new Part(userText);
        Content c = new Content(new Part[]{p});
        GenerateContentRequest body = new GenerateContentRequest(new Content[]{c}, 4096);

        String apiKey = BuildConfig.GEMINI_API_KEY;

        if (apiKey == null || apiKey.isEmpty()) {
            typingIndicator.setVisibility(View.GONE);
            Toast.makeText(getContext(), "API key is missing", Toast.LENGTH_LONG).show();
            addMessage(new ChatMessage("assistant", "Error: API key is not configured"));
            return;
        }

        Log.d(TAG, "API Key present, making request...");

        geminiApi.generateContent(GEMINI_MODEL, apiKey, body)
                .enqueue(new Callback<GenerateContentResponse>() {
                    @Override
                    public void onResponse(Call<GenerateContentResponse> call, Response<GenerateContentResponse> response) {
                        typingIndicator.setVisibility(View.GONE);
                        Log.d(TAG, "Response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Response successful");
                            GenerateContentResponse resp = response.body();

                            // Debug the response structure
                            if (resp.candidates != null && resp.candidates.length > 0) {
                                Log.d(TAG, "Candidates found: " + resp.candidates.length);
                                if (resp.candidates[0].content != null && resp.candidates[0].content.parts != null &&
                                        resp.candidates[0].content.parts.length > 0) {
                                    String responseText = resp.candidates[0].content.parts[0].text;
                                    Log.d(TAG, "AI Response: " + responseText);
                                    addMessage(new ChatMessage("assistant", responseText));
                                } else {
                                    Log.d(TAG, "No content in candidate");
                                    addMessage(new ChatMessage("assistant", "No response content"));
                                }
                            } else {
                                Log.d(TAG, "No candidates in response");
                                addMessage(new ChatMessage("assistant", "No response generated"));
                            }
                        } else {
                            String errorMsg = "Error: " + response.code();
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                    Log.e(TAG, "Error response: " + errorMsg);
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading error body", e);
                            }
                            addMessage(new ChatMessage("assistant", errorMsg));
                            Toast.makeText(getContext(), "API Error: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GenerateContentResponse> call, Throwable t) {
                        typingIndicator.setVisibility(View.GONE);
                        Log.e(TAG, "Network error", t);
                        addMessage(new ChatMessage("assistant", "Network error: " + t.getMessage()));
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up listeners
        if (chatAdapter != null && adapterDataObserver != null) {
            chatAdapter.unregisterAdapterDataObserver(adapterDataObserver);
        }
    }

    // ---- Retrofit API ----
    interface GeminiApi {
        @POST("models/{model}:generateContent")
        Call<GenerateContentResponse> generateContent(
                @Path("model") String model,
                @Query("key") String apiKey,
                @Body GenerateContentRequest request
        );
    }

    // ---- Request/Response Models ----
    static class GenerateContentRequest {
        @SerializedName("contents")
        Content[] contents;

        @SerializedName("generationConfig")
        GenerationConfig generationConfig;

        GenerateContentRequest(Content[] contents, int maxTokens) {
            this.contents = contents;
            this.generationConfig = new GenerationConfig(maxTokens, 0.7);
        }
    }

    static class GenerationConfig {
        @SerializedName("maxOutputTokens")
        Integer maxOutputTokens;

        @SerializedName("temperature")
        Double temperature;

        GenerationConfig(int maxOutputTokens, double temperature) {
            this.maxOutputTokens = maxOutputTokens;
            this.temperature = temperature;
        }
    }

    static class Content {
        @SerializedName("parts")
        Part[] parts;

        Content(Part[] parts) {
            this.parts = parts;
        }
    }

    static class Part {
        @SerializedName("text")
        String text;

        Part(String text) {
            this.text = text;
        }
    }

    static class GenerateContentResponse {
        @SerializedName("candidates")
        Candidate[] candidates;

        @Override
        public String toString() {
            return "GenerateContentResponse{" +
                    "candidates=" + (candidates != null ? candidates.length : 0) +
                    '}';
        }
    }

    static class Candidate {
        @SerializedName("content")
        Content content;
    }

    // ---- Chat Message ----
    public static class ChatMessage {
        private final String role;
        private final String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}