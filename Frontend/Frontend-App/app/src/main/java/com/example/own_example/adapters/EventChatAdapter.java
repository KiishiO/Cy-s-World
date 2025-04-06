package com.example.own_example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.EventChat;

import java.util.List;

public class EventChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;
    private static final int VIEW_TYPE_ADMIN_MESSAGE = 3;

    private List<EventChat> chatMessages;
    private String currentUsername;

    public EventChatAdapter(List<EventChat> chatMessages, String currentUsername) {
        this.chatMessages = chatMessages;
        this.currentUsername = currentUsername;
    }

    @Override
    public int getItemViewType(int position) {
        EventChat message = chatMessages.get(position);

        if (message.isAdminMessage()) {
            return VIEW_TYPE_ADMIN_MESSAGE;
        } else if (message.getUsername().equals(currentUsername)) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_my_message, parent, false);
            return new MyMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_ADMIN_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_admin_message, parent, false);
            return new AdminMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_other_message, parent, false);
            return new OtherMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EventChat message = chatMessages.get(position);

        if (holder instanceof MyMessageViewHolder) {
            ((MyMessageViewHolder) holder).bind(message);
        } else if (holder instanceof OtherMessageViewHolder) {
            ((OtherMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AdminMessageViewHolder) {
            ((AdminMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // ViewHolder for messages sent by the current user
    static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        MyMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(EventChat message) {
            messageText.setText(message.getMessage());
            timeText.setText(message.getFormattedTime());
        }
    }

    // ViewHolder for messages sent by other users
    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView usernameText;
        TextView timeText;

        OtherMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            usernameText = itemView.findViewById(R.id.text_message_name);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(EventChat message) {
            messageText.setText(message.getMessage());
            usernameText.setText(message.getUsername());
            timeText.setText(message.getFormattedTime());
        }
    }

    // ViewHolder for admin announcements
    static class AdminMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        AdminMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(EventChat message) {
            messageText.setText(message.getMessage());
            timeText.setText(message.getFormattedTime());
        }
    }
}