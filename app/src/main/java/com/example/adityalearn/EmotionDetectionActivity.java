package com.example.adityalearn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;
import java.util.Locale;

public class EmotionDetectionActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 101;
    private static final int CAMERA_CAPTURE_REQUEST = 102;

    private Button detectEmotionButton;
    private Button emulatorTestButton;
    private TextView emotionResult;
    private ImageView capturedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion);

        detectEmotionButton = findViewById(R.id.detect_emotion_button);
        emulatorTestButton = findViewById(R.id.test_emulator_button);
        emotionResult = findViewById(R.id.emotion_result);
        capturedImage = findViewById(R.id.captured_image);

        detectEmotionButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        emulatorTestButton.setOnClickListener(v -> simulateEmotionTest());
    }

    // ---------- Permission helpers ----------
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
    }

    // ---------- Camera ----------
    private void openCamera() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_CAPTURE_REQUEST);
            } else {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_REQUEST && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data"); // thumbnail
                if (imageBitmap != null) {
                    capturedImage.setImageBitmap(imageBitmap);
                    analyzeEmotion(imageBitmap);
                } else {
                    emotionResult.setText("Failed to capture image.");
                }
            } else {
                emotionResult.setText("No image data returned.");
            }
        } else if (requestCode == CAMERA_CAPTURE_REQUEST) {
            Toast.makeText(this, "Camera operation cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- ML Kit Face Detection ----------
    private void analyzeEmotion(Bitmap bitmap) {
        // Configure face detector
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.1f)
                        .enableTracking()
                        .build();

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetector detector = FaceDetection.getClient(options);

        detector.process(image)
                .addOnSuccessListener(faces -> handleFaces(faces))
                .addOnFailureListener(e -> emotionResult.setText("Detection failed: " + e.getMessage()));
    }

    private void handleFaces(List<Face> faces) {
        if (faces == null || faces.isEmpty()) {
            emotionResult.setText("No face detected. Try again with a clearer image.");
            return;
        }

        // We'll analyze the first face for simplicity
        Face face = faces.get(0);

        Float smileProbObj = face.getSmilingProbability();
        Float leftEyeOpen = face.getLeftEyeOpenProbability();
        Float rightEyeOpen = face.getRightEyeOpenProbability();

        String summary = "";

        float smileProb = (smileProbObj != null) ? smileProbObj : -1f;
        float leftEye = (leftEyeOpen != null) ? leftEyeOpen : -1f;
        float rightEye = (rightEyeOpen != null) ? rightEyeOpen : -1f;

        // Heuristic mapping: adjust thresholds to suit your data
        String detectedEmotion = mapToEmotion(smileProb, leftEye, rightEye);

        summary += String.format(Locale.getDefault(),
                "Detected: %s\n\n(Details)\nSmilingProb: %s\nLeftEyeOpen: %s\nRightEyeOpen: %s",
                detectedEmotion,
                (smileProb >= 0 ? String.format("%.2f", smileProb) : "N/A"),
                (leftEye >= 0 ? String.format("%.2f", leftEye) : "N/A"),
                (rightEye >= 0 ? String.format("%.2f", rightEye) : "N/A")
        );

        // Add friendly suggestions
        switch (detectedEmotion) {
            case "Happy 😊":
                summary += "\n\nGreat! Keep smiling 🙂";
                break;
            case "Sad 😢":
                summary += "\n\nTry listening to some music or talk to a friend.";
                break;
            case "Tired 😴":
                summary += "\n\nConsider a short break or a power nap.";
                break;
            case "Angry 😠":
                summary += "\n\nTake deep breaths and relax.";
                break;
            default:
                summary += "\n\nStay focused!";
        }

        emotionResult.setText(summary);
    }

    /**
     * Map smiling probability + eye openness heuristics to an emotion label.
     * This is a simple mapping — for better accuracy use a trained model (TFLite).
     */
    private String mapToEmotion(float smileProb, float leftEyeOpen, float rightEyeOpen) {
        // If smile probability available, use it
        if (smileProb >= 0) {
            if (smileProb > 0.65f) {
                return "Happy 😊";
            } else if (smileProb > 0.35f) {
                return "Neutral 😐";
            } else {
                // low smile probability -> maybe sad or tired or angry
                // decide using eyes: if eyes mostly closed -> tired, if wide open but not smiling -> angry/sad
                if ((leftEyeOpen >= 0 && rightEyeOpen >= 0) && (leftEyeOpen < 0.4f && rightEyeOpen < 0.4f)) {
                    return "Tired 😴";
                } else {
                    return "Sad 😢";
                }
            }
        }

        // Fallback when smilingProbability is unavailable: infer from eyes
        if (leftEyeOpen >= 0 && rightEyeOpen >= 0) {
            float eyesAvg = (leftEyeOpen + rightEyeOpen) / 2f;
            if (eyesAvg < 0.35f) return "Tired 😴";
            if (eyesAvg > 0.8f) return "Surprised/Alert 😯";
        }

        return "Neutral 😐";
    }

    // ---------- Emulator test ----------
    private void simulateEmotionTest() {
        String[] emotions = {"Happy 😊", "Sad 😢", "Tired 😴", "Angry 😠", "Neutral 😐"};
        String detectedEmotion = emotions[(int) (Math.random() * emotions.length)];
        String response = "Test Emotion: " + detectedEmotion + "\n\nSuggestion:\n";

        switch (detectedEmotion) {
            case "Happy 😊":
                response += "Great to see you happy! Keep up the good mood!";
                break;
            case "Sad 😢":
                response += "Listen to some uplifting music or call a friend.";
                break;
            case "Tired 😴":
                response += "Take a short nap or drink water.";
                break;
            case "Angry 😠":
                response += "Take deep breaths and calm down.";
                break;
            default:
                response += "Focus on your work and learn something new!";
        }

        emotionResult.setText(response);
        capturedImage.setImageResource(android.R.color.transparent);
    }

    // ---------- Permission result ----------
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied. Using test mode.", Toast.LENGTH_SHORT).show();
                simulateEmotionTest();
            }
        }
    }
}
