package com.example.adityalearn;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerNotes;
    private NotesAdapter adapter;
    private final ArrayList<NoteModel> notesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        recyclerNotes = findViewById(R.id.recyclerNotes);
        adapter = new NotesAdapter(notesList, this);
        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotes.setAdapter(adapter);

        loadNotes();
    }

    private void loadNotes() {
        notesList.clear();

        // 🔹 Replace YOUR_FILE_ID_X with actual Google Drive file IDs
        notesList.add(new NoteModel("AI UNIT 1",
                "https://drive.google.com/file/d/11khDEeWJRAetMplNYnSziO1zA-A4zGML/view?usp=sharing"));
        notesList.add(new NoteModel("DMGT UNIT 1",
                "https://drive.google.com/file/d/1DgFeuj3qkiQQUyIlQgTGBsHpZlMAc0DO/view?usp=sharing"));
        notesList.add(new NoteModel("JAVA UNIT 1",
                "https://drive.google.com/file/d/1JlBfYojCK5fQwZz4FBxYwIxVmNOEwN5j/view?usp=sharing"));
        notesList.add(new NoteModel("UHV UNIT 1",
                "https://drive.google.com/file/d/17B8ahVuOIhqwwu0xJqNFtU80Dl_QRxnC/view?usp=sharing"));
        notesList.add(new NoteModel("ADS UNIT 2",
                "https://drive.google.com/file/d/1pQLjmjQQPm7AM6Simcs0S9Se72FkQIWB/view?usp=sharing"));
        notesList.add(new NoteModel("AI UNIT 2",
                "https://drive.google.com/file/d/1hBtrDXAop9LD4Y1ELq4EltzWmaYqA-jP/view?usp=sharing"));
        notesList.add(new NoteModel("UHV UNIT 2",
                "https://drive.google.com/file/d/1n6zwY27MYKAwxzSKyik1jvkHuoH6Ed_O/view?usp=sharing"));
        notesList.add(new NoteModel("JAVA UNIT 2",
                "https://drive.google.com/file/d/1xDS1cIXQus9gTiyBlnAtIZBeL4WJppVp/view?usp=sharing"));






        adapter.notifyDataSetChanged();
    }
}
