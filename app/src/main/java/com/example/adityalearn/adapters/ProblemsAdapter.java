package com.example.adityalearn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adityalearn.R;
import com.example.adityalearn.models.ProblemItem;

import java.util.List;

public class ProblemsAdapter extends RecyclerView.Adapter<ProblemsAdapter.ProblemViewHolder> {

    private Context context;
    private List<ProblemItem> problems;
    private OnProblemClickListener listener;

    // Custom interface for clicks
    public interface OnProblemClickListener {
        void onProblemClick(String url);
    }

    public ProblemsAdapter(Context context, List<ProblemItem> problems, OnProblemClickListener listener) {
        this.context = context;
        this.problems = problems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_problem, parent, false);
        return new ProblemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
        ProblemItem item = problems.get(position);
        holder.title.setText(item.getTitle());
        holder.difficulty.setText(item.getDifficulty());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProblemClick(item.getLink());
            }
        });
    }

    @Override
    public int getItemCount() {
        return problems.size();
    }

    public static class ProblemViewHolder extends RecyclerView.ViewHolder {
        TextView title, difficulty;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtProblemTitle);
            difficulty = itemView.findViewById(R.id.txtProblemDifficulty);
        }
    }
}
