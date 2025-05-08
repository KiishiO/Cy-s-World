package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;
    private static final int VIEW_TYPE_STATUS_MESSAGE = 3;

    private List<ChatMessage> messageList;
    private final Map<String, Integer> messagePositions; // Track message positions
    private Context context;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private OnMessageActionListener actionListener;

    public interface OnMessageActionListener {
        void onEditMessage(ChatMessage message, int position);
        void onDeleteMessage(ChatMessage message, int position);
    }

    public ChatMessageAdapter(Context context) {
        this.context = context;
        this.messageList = new ArrayList<>();
        this.messagePositions = new HashMap<>();
    }

    public void setOnMessageActionListener(OnMessageActionListener listener) {
        this.actionListener = listener;
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

        // Update the message position map
        messagePositions.put(message.getLocalId(), position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MY_MESSAGE:
                ((SentMessageHolder) holder).bind(message, position);
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
        messagePositions.put(message.getLocalId(), messageList.size() - 1);
        notifyItemInserted(messageList.size() - 1);
    }

    public void updateMessage(ChatMessage message) {
        Integer position = messagePositions.get(message.getLocalId());
        if (position != null) {
            messageList.set(position, message);
            notifyItemChanged(position);
        }
    }

    public void addMessages(List<ChatMessage> messages) {
        int startPosition = messageList.size();
        messageList.addAll(messages);

        for (int i = 0; i < messages.size(); i++) {
            messagePositions.put(messages.get(i).getLocalId(), startPosition + i);
        }

        notifyItemRangeInserted(startPosition, messages.size());
    }

    public void clear() {
        messageList.clear();
        messagePositions.clear();
        notifyDataSetChanged();
    }

    public ChatMessage getMessage(int position) {
        if (position >= 0 && position < messageList.size()) {
            return messageList.get(position);
        }
        return null;
    }

    // View holder for sent messages (with options menu)
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, editedTag;
        ImageButton messageOptions;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);

            // These may not exist in your original layout
            try {
                editedTag = itemView.findViewById(R.id.text_edited_tag);
                messageOptions = itemView.findViewById(R.id.message_options);
            } catch (Exception e) {
                // If the views don't exist in the layout, handle gracefully
            }
        }

        void bind(ChatMessage message, int position) {
            // Set message content
            if (message.isDeleted()) {
                messageText.setText("This message has been deleted");
                messageText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                if (messageOptions != null) {
                    messageOptions.setVisibility(View.GONE);
                }
            } else {
                messageText.setText(message.getContent());
                messageText.setTextColor(context.getResources().getColor(android.R.color.white));
                if (messageOptions != null) {
                    messageOptions.setVisibility(View.VISIBLE);

                    // Setup options menu on click
                    messageOptions.setOnClickListener(v -> {
                        PopupMenu popup = new PopupMenu(context, messageOptions);
                        popup.inflate(R.menu.menu_message_actions);
                        popup.setOnMenuItemClickListener(item -> {
                            int id = item.getItemId();
                            if (id == R.id.action_edit && actionListener != null) {
                                actionListener.onEditMessage(message, getAdapterPosition());
                                return true;
                            } else if (id == R.id.action_delete && actionListener != null) {
                                actionListener.onDeleteMessage(message, getAdapterPosition());
                                return true;
                            }
                            return false;
                        });
                        popup.show();
                    });
                }
            }

            // Show edited tag if needed
            if (editedTag != null) {
                editedTag.setVisibility(message.isEdited() ? View.VISIBLE : View.GONE);
            }

            // Set time
            if (message.getSent() != null) {
                timeText.setText(timeFormat.format(message.getSent()));
            }
        }
    }

    // View holder for received messages
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText, editedTag;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);

            try {
                editedTag = itemView.findViewById(R.id.text_edited_tag);
            } catch (Exception e) {
                // If the view doesn't exist in the layout, handle gracefully
            }
        }

        void bind(ChatMessage message) {
            // Set message content
            if (message.isDeleted()) {
                messageText.setText("This message has been deleted");
                messageText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            } else {
                messageText.setText(message.getContent());
                messageText.setTextColor(context.getResources().getColor(android.R.color.white));
            }

            // Show edited tag if needed and if it exists
            if (editedTag != null) {
                editedTag.setVisibility(message.isEdited() ? View.VISIBLE : View.GONE);
            }

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