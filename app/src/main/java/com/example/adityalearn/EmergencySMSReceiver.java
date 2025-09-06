package com.example.adityalearn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class EmergencySMSReceiver extends BroadcastReceiver {

    private static final String EMERGENCY_KEYWORDS = "SOS|EMERGENCY|HELP|DANGER|URGENT|ACCIDENT|SAVE|RESCUE|911|112";
    private static final String PREFS_NAME = "EmergencyPrefs";
    private static final String KEY_EMERGENCY_NUMBERS = "emergency_numbers";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            // Check if we have SMS permissions
            if (!hasSmsPermissions(context)) {
                // Don't process SMS if we don't have permissions
                return;
            }

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            // Use reflection to avoid direct permission issues
                            SmsMessage smsMessage = createSmsMessageFromPdu(pdu, bundle.getString("format"));
                            if (smsMessage != null) {
                                String senderNumber = smsMessage.getOriginatingAddress();
                                String messageBody = smsMessage.getMessageBody();

                                // Check if this is an emergency message
                                if (isEmergencyMessage(senderNumber, messageBody, context)) {
                                    // Show emergency notification
                                    showEmergencyNotification(context, senderNumber, messageBody);

                                    // Show toast alert
                                    Toast.makeText(context, "🚨 EMERGENCY from " + getContactName(context, senderNumber), Toast.LENGTH_LONG).show();

                                    // Launch emergency activity
                                    launchEmergencyActivity(context, senderNumber, messageBody);
                                }
                            }
                        }
                    }
                } catch (SecurityException e) {
                    // Handle permission denied gracefully
                    Toast.makeText(context, "SMS permission required for emergency detection", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean hasSmsPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private SmsMessage createSmsMessageFromPdu(Object pdu, String format) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                return SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                return SmsMessage.createFromPdu((byte[]) pdu);
            }
        } catch (SecurityException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isEmergencyMessage(String senderNumber, String messageBody, Context context) {
        if (senderNumber == null || messageBody == null) return false;

        // Check if message contains emergency keywords (case insensitive)
        boolean hasEmergencyKeyword = Pattern.compile(EMERGENCY_KEYWORDS, Pattern.CASE_INSENSITIVE)
                .matcher(messageBody)
                .find();

        // Check if sender is in emergency contacts list
        boolean isEmergencyContact = isEmergencyContact(senderNumber, context);

        return hasEmergencyKeyword || isEmergencyContact;
    }

    private boolean isEmergencyContact(String phoneNumber, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> emergencyNumbers = prefs.getStringSet(KEY_EMERGENCY_NUMBERS, new HashSet<>());

        if (phoneNumber == null) return false;

        // Clean the phone number for comparison
        String cleanedNumber = phoneNumber.replaceAll("[^0-9+]", "");

        for (String emergencyNumber : emergencyNumbers) {
            String cleanedEmergencyNumber = emergencyNumber.replaceAll("[^0-9+]", "");
            if (cleanedNumber.equals(cleanedEmergencyNumber) ||
                    cleanedNumber.endsWith(cleanedEmergencyNumber) ||
                    cleanedEmergencyNumber.endsWith(cleanedNumber)) {
                return true;
            }
        }
        return false;
    }

    private String getContactName(Context context, String phoneNumber) {
        // Try to get contact name from saved contacts
        try {
            DbHelper dbHelper = new DbHelper(context);
            for (ContactModel contact : dbHelper.getAllContacts()) {
                String contactNumber = contact.getPhoneNo().replaceAll("[^0-9+]", "");
                String incomingNumber = phoneNumber.replaceAll("[^0-9+]", "");

                if (contactNumber.equals(incomingNumber) ||
                        contactNumber.endsWith(incomingNumber) ||
                        incomingNumber.endsWith(contactNumber)) {
                    return contact.getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phoneNumber; // Return number if name not found
    }

    private void showEmergencyNotification(Context context, String senderNumber, String message) {
        try {
            String contactName = getContactName(context, senderNumber);

            // Create notification channel for Android O+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                android.app.NotificationChannel channel = new android.app.NotificationChannel(
                        "emergency_sms_channel",
                        "Emergency SMS Alerts",
                        android.app.NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Emergency messages from contacts");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                android.app.NotificationManager manager = context.getSystemService(android.app.NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "emergency_sms_channel")
                    .setSmallIcon(R.drawable.ic_sos)
                    .setContentTitle("🚨 EMERGENCY from " + contactName)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 1000, 500, 1000});

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(1002, builder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchEmergencyActivity(Context context, String senderNumber, String message) {
        try {
            Intent intent = new Intent(context, EmergencyAlertActivity.class);
            intent.putExtra("sender_number", senderNumber);
            intent.putExtra("message", message);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to add emergency numbers (call this when user adds contacts)
    public static void addEmergencyNumber(Context context, String phoneNumber) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Set<String> emergencyNumbers = new HashSet<>(prefs.getStringSet(KEY_EMERGENCY_NUMBERS, new HashSet<>()));
            emergencyNumbers.add(phoneNumber);
            prefs.edit().putStringSet(KEY_EMERGENCY_NUMBERS, emergencyNumbers).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to remove emergency number
    public static void removeEmergencyNumber(Context context, String phoneNumber) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Set<String> emergencyNumbers = new HashSet<>(prefs.getStringSet(KEY_EMERGENCY_NUMBERS, new HashSet<>()));
            emergencyNumbers.remove(phoneNumber);
            prefs.edit().putStringSet(KEY_EMERGENCY_NUMBERS, emergencyNumbers).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to get all emergency numbers
    public static Set<String> getEmergencyNumbers(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getStringSet(KEY_EMERGENCY_NUMBERS, new HashSet<>());
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }
}