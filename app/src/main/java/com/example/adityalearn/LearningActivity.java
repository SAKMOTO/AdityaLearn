package com.example.adityalearn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.adityalearn.adapters.LearningAdapter;
import com.example.adityalearn.models.LearningItem;
import java.util.ArrayList;

public class LearningActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<LearningItem> list;
    LearningAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        recyclerView = findViewById(R.id.recyclerLearning);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        list.add(new LearningItem("DSA Basics", "https://www.geeksforgeeks.org/dsa/dsa-tutorial-learn-data-structures-and-algorithms/"));
        list.add(new LearningItem("Cloud Computing", "https://www.geeksforgeeks.org/cloud-computing/cloud-computing/"));
        list.add(new LearningItem("Machine Learning", "https://www.coursera.org/learn/machine-learning"));

        adapter = new LearningAdapter(list, url -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}
