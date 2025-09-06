package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmergencyAlertActivity extends AppCompatActivity {

    private String senderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);

        TextView senderText = findViewById(R.id.sender_text);
        TextView messageText = findViewById(R.id.message_text);
        TextView timeText = findViewById(R.id.time_text);

        if (getIntent() != null) {
            senderNumber = getIntent().getStringExtra("sender_number");
            String message = getIntent().getStringExtra("message");

            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String contactName = getContactName(senderNumber);

            senderText.setText("From: " + (contactName != null ? contactName : senderNumber));
            messageText.setText("Message: " + (message != null ? message : "No message content"));
            timeText.setText("Time: " + currentTime);
        }

        // Make emergency sound
        makeEmergencySound();
    }

    private String getContactName(String phoneNumber) {
        DbHelper dbHelper = new DbHelper(this);
        for (ContactModel contact : dbHelper.getAllContacts()) {
            String contactNumber = contact.getPhoneNo().replaceAll("[^0-9+]", "");
            String incomingNumber = phoneNumber != null ? phoneNumber.replaceAll("[^0-9+]", "") : "";

            if (contactNumber.equals(incomingNumber) ||
                    contactNumber.endsWith(incomingNumber) ||
                    incomingNumber.endsWith(contactNumber)) {
                return contact.getName();
            }
        }
        return null;
    }

    private void makeEmergencySound() {
        // Emergency sound will play through notification
        Toast.makeText(this, "🚨 EMERGENCY MESSAGE RECEIVED!", Toast.LENGTH_LONG).show();
    }

    public void onCallBackClick(View view) {
        if (senderNumber != null && !senderNumber.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + senderNumber));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot make call", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
        }
    }

    public void onDismissClick(View view) {
        finish();
    }

    public void onReplyClick(View view) {
        if (senderNumber != null && !senderNumber.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + senderNumber));
                intent.putExtra("sms_body", "I received your emergency message. Are you okay?");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot send message", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop any ongoing sounds if needed
    }
}