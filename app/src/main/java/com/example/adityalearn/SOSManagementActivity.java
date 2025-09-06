package com.example.adityalearn;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SOSManagementActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private TextView contactsCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_management);

        dbHelper = new DbHelper(this);
        contactsCountText = findViewById(R.id.contacts_count);

        Button addContactBtn = findViewById(R.id.add_contact_btn);
        Button viewContactsBtn = findViewById(R.id.view_contacts_btn);
        Button testSosBtn = findViewById(R.id.test_sos_btn);

        updateContactsCount();

        addContactBtn.setOnClickListener(v -> {
            if (dbHelper.count() < 5) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 1);
            } else {
                Toast.makeText(this, "Maximum 5 contacts allowed", Toast.LENGTH_SHORT).show();
            }
        });

        viewContactsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SOSContactsActivity.class);
            startActivity(intent);
        });

        testSosBtn.setOnClickListener(v -> {
            Toast.makeText(this, "SOS test triggered - Emergency alert created", Toast.LENGTH_SHORT).show();
            triggerTestEmergency();
        });
    }

    private void triggerTestEmergency() {
        Intent serviceIntent = new Intent(this, SensorService.class);
        serviceIntent.putExtra("test_emergency", true);
        startService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                Cursor cursor = null;
                Cursor phoneCursor = null;

                try {
                    cursor = getContentResolver().query(contactUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        // Safely get column indices
                        int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        int hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

                        if (idIndex == -1 || nameIndex == -1 || hasPhoneIndex == -1) {
                            Toast.makeText(this, "Error reading contact information", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String id = cursor.getString(idIndex);
                        String name = cursor.getString(nameIndex);
                        String hasPhone = cursor.getString(hasPhoneIndex);

                        if ("1".equals(hasPhone)) {
                            phoneCursor = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{id},
                                    null
                            );

                            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                                int numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                if (numberIndex == -1) {
                                    Toast.makeText(this, "Error reading phone number", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String phoneNumber = phoneCursor.getString(numberIndex);

                                // Clean phone number
                                if (phoneNumber != null) {
                                    phoneNumber = phoneNumber.replaceAll("[\\s\\-()]", "");

                                    // Add contact to database
                                    dbHelper.addcontact(new ContactModel(0, name, phoneNumber));
                                    updateContactsCount();
                                    Toast.makeText(this, "Contact added: " + name, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "No phone number found for this contact", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Contact has no phone number", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error adding contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } finally {
                    // Always close cursors
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (phoneCursor != null) {
                        phoneCursor.close();
                    }
                }
            }
        }
    }
    // In your onActivityResult method, after adding contact:
    private void saveContactToDatabase(String name, String phoneNumber) {
        ContactModel contact = new ContactModel(0, name, phoneNumber);
        dbHelper.addcontact(contact);

        // Add to emergency numbers list for SMS detection
        EmergencySMSReceiver.addEmergencyNumber(this, phoneNumber);

        updateContactsCount();
        Toast.makeText(this, "Contact added: " + name, Toast.LENGTH_SHORT).show();
    }

    private void updateContactsCount() {
        int count = dbHelper.count();
        contactsCountText.setText("Emergency Contacts: " + count + "/5");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateContactsCount();
    }
}