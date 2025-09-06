package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.adityalearn.adapters.ProblemsAdapter;
import com.example.adityalearn.models.ProblemItem;
import java.util.ArrayList;

public class ProblemActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<ProblemItem> list;
    ProblemsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem);

        recyclerView = findViewById(R.id.recyclerProblem);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add problems
        list = new ArrayList<>();
        list.add(new ProblemItem("Two Sum", "Easy", "https://leetcode.com/problems/two-sum/"));
        list.add(new ProblemItem("Reverse String", "Easy", "https://leetcode.com/problems/reverse-string/"));
        list.add(new ProblemItem("Longest Substring", "Medium", "https://leetcode.com/problems/longest-substring-without-repeating-characters/"));

        // Pass click listener as 3rd argument
        adapter = new ProblemsAdapter(this, list, url -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}
