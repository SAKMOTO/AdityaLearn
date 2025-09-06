package com.example.adityalearn.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adityalearn.R;
import com.example.adityalearn.models.PlatformModel;

import java.util.List;

public class PlatformAdapter extends RecyclerView.Adapter<PlatformAdapter.ViewHolder> {
    private List<PlatformModel> platforms;
    private OnPlatformClickListener listener;

    public interface OnPlatformClickListener {
        void onPlatformClick(PlatformModel platform);
    }

    public PlatformAdapter(List<PlatformModel> platforms, View.OnClickListener context, OnPlatformClickListener listener) {
        this.platforms = platforms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_platform, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlatformModel platform = platforms.get(position);
        holder.tvName.setText(platform.getName());
        holder.itemView.setOnClickListener(v -> listener.onPlatformClick(platform));
    }

    @Override
    public int getItemCount() {
        return platforms.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPlatformName);
        }
    }
}
