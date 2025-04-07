package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;
    private static final int VIEW_TYPE_STATUS_MESSAGE = 3;

    private List<ChatMessage> messageList;
    private Context context;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public ChatMessageAdapter(Context context) {
        this.context = context;
        this.messageList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_OTHER_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_status, parent, false);
            return new StatusMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MY_MESSAGE:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_OTHER_MESSAGE:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_STATUS_MESSAGE:
                ((StatusMessageHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);

        if (message.getMessageType().equals("STATUS") ||
                message.getMessageType().equals("JOIN") ||
                message.getMessageType().equals("LEAVE")) {
            return VIEW_TYPE_STATUS_MESSAGE;
        } else if (message.isMine()) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public void addMessages(List<ChatMessage> messages) {
        int startPosition = messageList.size();
        messageList.addAll(messages);
        notifyItemRangeInserted(startPosition, messages.size());
    }

    public void clear() {
        messageList.clear();
        notifyDataSetChanged();
    }

    // View holder for sent messages
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getContent());

            if (message.getSent() != null) {
                timeText.setText(timeFormat.format(message.getSent()));
            }
        }
    }

    // View holder for received messages
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getContent());
            nameText.setText(message.getUserName());

            if (message.getSent() != null) {
                timeText.setText(timeFormat.format(message.getSent()));
            }
        }
    }

    // View holder for status messages
    private class StatusMessageHolder extends RecyclerView.ViewHolder {
        TextView statusText;

        StatusMessageHolder(View itemView) {
            super(itemView);
            statusText = itemView.findViewById(R.id.text_status_message);
        }

        void bind(ChatMessage message) {
            statusText.setText(message.getContent());
        }
    }
}