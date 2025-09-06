package com.example.adityalearn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private List<AIChatFragment.ChatMessage> messages;

    public ChatAdapter(List<AIChatFragment.ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages == null || messages.size() <= position) {
            return VIEW_TYPE_AI; // Default to AI type if messages is null or position is invalid
        }
        AIChatFragment.ChatMessage message = messages.get(position);
        return "user".equals(message.getRole()) ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_user_message, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_bot_message, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (messages == null || messages.size() <= position) {
            return;
        }

        AIChatFragment.ChatMessage message = messages.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof AIViewHolder) {
            ((AIViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    // ViewHolder for user messages
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userMsg;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userMsg = itemView.findViewById(R.id.userMsg);
        }

        void bind(AIChatFragment.ChatMessage message) {
            userMsg.setText(message.getContent());
        }
    }

    // ViewHolder for AI messages
    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView aiMsg;

        AIViewHolder(@NonNull View itemView) {
            super(itemView);
            aiMsg = itemView.findViewById(R.id.aiMsg);
        }

        void bind(AIChatFragment.ChatMessage message) {
            aiMsg.setText(message.getContent());
        }
    }
}