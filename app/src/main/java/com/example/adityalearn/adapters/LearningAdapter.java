package com.example.adityalearn.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.adityalearn.R;
import com.example.adityalearn.models.LearningItem;
import java.util.List;

public class LearningAdapter extends RecyclerView.Adapter<LearningAdapter.LearningViewHolder> {

    public interface OnLearningClickListener {
        void onLearningClick(String url);
    }

    private List<LearningItem> list;
    private OnLearningClickListener listener;

    public LearningAdapter(List<LearningItem> list, OnLearningClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LearningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_learning, parent, false);
        return new LearningViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LearningViewHolder holder, int position) {
        LearningItem item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onLearningClick(item.getUrl()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class LearningViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        LearningViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtLearning);
        }
    }
}
