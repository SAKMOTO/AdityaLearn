package com.example.adityalearn;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HandwritingRecognitionActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView capturedImage;
    private TextView resultText;
    private Button captureButton, processButton;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handwriting);

        capturedImage = findViewById(R.id.captured_image);
        resultText = findViewById(R.id.result_text);
        captureButton = findViewById(R.id.capture_button);
        processButton = findViewById(R.id.process_button);

        captureButton.setOnClickListener(v -> dispatchTakePictureIntent());
        processButton.setOnClickListener(v -> processHandwriting());
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            capturedImage.setImageBitmap(imageBitmap);
        }
    }

    private void processHandwriting() {
        if (imageBitmap != null) {
            resultText.setText("Processing handwriting...\n" +
                    "This would use Tesseract OCR + AI model integration");
        } else {
            resultText.setText("Please capture an image first");
        }
    }
}