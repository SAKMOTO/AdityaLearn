package com.example.adityalearn;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class SOSContactsActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_contacts);

        dbHelper = new DbHelper(this);
        ListView listView = findViewById(R.id.contacts_list);
        TextView emptyText = findViewById(R.id.empty_text);

        List<ContactModel> contacts = dbHelper.getAllContacts();
        customAdapter = new CustomAdapter(this, contacts);
        listView.setAdapter(customAdapter);

        if (contacts.isEmpty()) {
            emptyText.setText("No emergency contacts added");
        } else {
            emptyText.setText("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list
        List<ContactModel> contacts = dbHelper.getAllContacts();
        customAdapter.refresh(contacts);
    }
}