package com.example.own_example;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.ChatMessageAdapter;
import com.example.own_example.models.ChatMessage;
import com.example.own_example.models.Friend;
import com.example.own_example.services.ChatWebSocketClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

public class ChatActivity extends AppCompatActivity implements
        ChatWebSocketClient.MessageListener,
        ChatMessageAdapter.OnMessageActionListener {

    private static final String TAG = "ChatActivity";

    // WebSocket server URL
    private static final String SERVER_URL = "ws://coms-3090-017.class.las.iastate.edu:8080";

    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private TabLayout tabLayout;
    private TextView connectionStatus;

    private ChatMessageAdapter adapter;
    private ChatWebSocketClient chatClient;
    private String username;
    private Friend selectedFriend; // If chat is with a specific friend

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get username from SharedPreferences (adjust this to match your app's login logic)
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        username = prefs.getString("username", "user" + System.currentTimeMillis());

        // Get selected friend if any
        if (getIntent() != null && getIntent().hasExtra("friendId")) {
            int friendId = getIntent().getIntExtra("friendId", -1);
            String friendName = getIntent().getStringExtra("friendName");
            String friendStatus = getIntent().getStringExtra("friendStatus");

            if (friendId != -1) {
                selectedFriend = new Friend(friendId, friendName, friendStatus);
            }
        }

        initializeViews();
        setupRecyclerView();
        setupWebSocketClient();
        setupListeners();
    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        tabLayout = findViewById(R.id.tabLayout);
        connectionStatus = findViewById(R.id.connectionStatus);

        // Update title if direct chat
        if (selectedFriend != null) {
            TextView titleChat = findViewById(R.id.title_chat);
            titleChat.setText("Chat with " + selectedFriend.getName());

            // Select DM tab
            tabLayout.getTabAt(1).select();
        }
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(this);
        adapter.setOnMessageActionListener(this); // Set this activity as action listener
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(adapter);
    }

    private void setupWebSocketClient() {
        chatClient = new ChatWebSocketClient(SERVER_URL, username);
        chatClient.addMessageListener(this);

        // Connect to appropriate chat based on tab or selected friend
        if (selectedFriend != null || tabLayout.getSelectedTabPosition() == 1) {
            chatClient.connectToIndividualChat();
        } else {
            chatClient.connectToMainChat();
        }
    }

    private void setupListeners() {
        // Send button click
        sendButton.setOnClickListener(v -> sendMessage());

        // Tab selection changes
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Clear messages
                adapter.clear();

                // Switch chat room
                if (tab.getPosition() == 0) {
                    chatClient.disconnect();
                    chatClient.connectToMainChat();
                } else {
                    chatClient.disconnect();
                    chatClient.connectToIndividualChat();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        if (chatClient.isConnected()) {
            // Send direct message if appropriate
            if (selectedFriend != null && tabLayout.getSelectedTabPosition() == 1) {
                chatClient.sendDirectMessage(selectedFriend.getName(), messageText);
            } else {
                chatClient.sendMessage(messageText);
            }

            // Clear input
            messageInput.setText("");
        } else {
            Toast.makeText(this, "Not connected to chat server", Toast.LENGTH_SHORT).show();
        }
    }

    // ChatWebSocketClient.MessageListener implementations
    @Override
    public void onMessageReceived(ChatMessage message) {
        runOnUiThread(() -> {
            adapter.addMessage(message);
            // Scroll to the bottom
            messagesRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        });
    }

    @Override
    public void onStatusChange(boolean connected) {
        runOnUiThread(() -> {
            if (connected) {
                connectionStatus.setText("Connected");
                connectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            } else {
                connectionStatus.setText("Disconnected");
                connectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
        });
    }

    @Override
    public void onUserStatusChange(String username, String status) {
        // We could update a list of online users here if needed
    }

    @Override
    public void onMessageEdited(ChatMessage message) {
        runOnUiThread(() -> {
            adapter.updateMessage(message);
        });
    }

    @Override
    public void onMessageDeleted(ChatMessage message) {
        runOnUiThread(() -> {
            adapter.updateMessage(message);
        });
    }

    // ChatMessageAdapter.OnMessageActionListener implementations
    @Override
    public void onEditMessage(ChatMessage message, int position) {
        // Show edit dialog
        showEditMessageDialog(message);
    }

    @Override
    public void onDeleteMessage(ChatMessage message, int position) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ChatMessage deletedMessage = chatClient.deleteMessage(message.getLocalId());
                    if (deletedMessage != null) {
                        adapter.updateMessage(deletedMessage);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditMessageDialog(ChatMessage message) {
        // Create edit text for dialog
        final EditText editText = new EditText(this);
        editText.setText(message.getContent());

        // Build dialog
        new AlertDialog.Builder(this)
                .setTitle("Edit Message")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newContent = editText.getText().toString().trim();
                    if (!newContent.isEmpty()) {
                        ChatMessage editedMessage = chatClient.editMessage(message.getLocalId(), newContent);
                        if (editedMessage != null) {
                            adapter.updateMessage(editedMessage);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disconnect from WebSocket
        if (chatClient != null) {
            chatClient.disconnect();
        }
    }
}