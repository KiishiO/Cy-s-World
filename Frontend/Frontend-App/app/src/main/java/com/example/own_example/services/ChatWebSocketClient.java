package com.example.own_example.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.own_example.models.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketClient {
    private static final String TAG = "ChatWebSocketClient";
    private WebSocket webSocket;
    private final OkHttpClient client;
    private final String serverUrl;
    private final String username;
    private String currentRoom = "main"; // "main" or "individual"
    private boolean isConnected = false;
    private final List<MessageListener> listeners = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Store messages by localId for local edit/delete operations
    private final Map<String, ChatMessage> messageCache = new HashMap<>();

    public interface MessageListener {
        void onMessageReceived(ChatMessage message);
        void onStatusChange(boolean connected);
        void onUserStatusChange(String username, String status);
        void onMessageEdited(ChatMessage message);
        void onMessageDeleted(ChatMessage message);
    }

    public ChatWebSocketClient(String serverUrl, String username) {
        this.serverUrl = serverUrl;
        this.username = username;

        // Configure OkHttp client with timeouts
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void connectToMainChat() {
        currentRoom = "main";
        connect(serverUrl + "/chat/" + getEncodedUsername(username));
    }

    public void connectToIndividualChat() {
        currentRoom = "individual";
        connect(serverUrl + "/chat/1/" + getEncodedUsername(username));
    }

    private String getEncodedUsername(String username) {
        try {
            // Replace spaces with underscores (simpler approach than URL encoding)
            return username.replace(" ", "_");
        } catch (Exception e) {
            Log.e(TAG, "Error encoding username", e);
            return username;
        }
    }

    private void connect(String endpoint) {
        // Close any existing connection
        if (webSocket != null) {
            webSocket.close(1000, "Switching rooms");
        }

        Log.d(TAG, "Connecting to: " + endpoint);

        Request request = new Request.Builder()
                .url(endpoint)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "Connected to: " + endpoint);
                isConnected = true;
                notifyStatusChange(true);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Received message: " + text);

                ChatMessage chatMessage = ChatMessage.createFromWebSocketMessage(text, username);

                // Store regular chat messages in cache for later edit/delete
                if (chatMessage.getMessageType().equals("CHAT")) {
                    messageCache.put(chatMessage.getLocalId(), chatMessage);
                }

                notifyMessageReceived(chatMessage);

                // Handle status changes
                if (chatMessage.getMessageType().equals("STATUS")) {
                    String[] parts = text.split(" is now ");
                    if (parts.length == 2) {
                        notifyUserStatusChange(parts[0], parts[1]);
                    }
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Connection closed. Code: " + code + ", Reason: " + reason);
                isConnected = false;
                notifyStatusChange(false);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket error", t);
                isConnected = false;
                notifyStatusChange(false);
            }
        });
    }

    public void sendMessage(String message) {
        if (isConnected && webSocket != null) {
            webSocket.send(message);
        } else {
            Log.e(TAG, "Cannot send message. WebSocket is not connected.");
        }
    }

    public void sendDirectMessage(String recipient, String message) {
        sendMessage("@" + recipient + " " + message);
    }

    public void setStatus(String status) {
        if (!status.equals("online") && !status.equals("active") && !status.equals("inactive")) {
            Log.e(TAG, "Invalid status. Use 'online', 'active', or 'inactive'");
            return;
        }
        sendMessage("__status__ " + status);
    }

    // Method to edit a message (frontend-only for now)
    public ChatMessage editMessage(String localId, String newContent) {
        ChatMessage originalMessage = messageCache.get(localId);
        if (originalMessage != null && originalMessage.isMine()) {
            // Create edited message
            ChatMessage editedMessage = ChatMessage.createEditedMessage(originalMessage, newContent);

            // Cache the edited message
            messageCache.put(localId, editedMessage);

            // For future backend support
            // sendMessage("__edit__ " + editedMessage.getId() + " " + newContent);

            // Notify listeners
            notifyMessageEdited(editedMessage);

            return editedMessage;
        }
        return null;
    }

    // Method to delete a message (frontend-only for now)
    public ChatMessage deleteMessage(String localId) {
        ChatMessage originalMessage = messageCache.get(localId);
        if (originalMessage != null && originalMessage.isMine()) {
            // Create deleted message
            ChatMessage deletedMessage = ChatMessage.createDeletedMessage(originalMessage);

            // Cache the deleted message
            messageCache.put(localId, deletedMessage);

            // For future backend support
            // sendMessage("__delete__ " + deletedMessage.getId());

            // Notify listeners
            notifyMessageDeleted(deletedMessage);

            return deletedMessage;
        }
        return null;
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User disconnected");
            webSocket = null;
        }
        isConnected = false;
    }

    public void addMessageListener(MessageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
    }

    private void notifyMessageReceived(final ChatMessage message) {
        mainHandler.post(() -> {
            for (MessageListener listener : listeners) {
                listener.onMessageReceived(message);
            }
        });
    }

    private void notifyStatusChange(final boolean connected) {
        mainHandler.post(() -> {
            for (MessageListener listener : listeners) {
                listener.onStatusChange(connected);
            }
        });
    }

    private void notifyUserStatusChange(final String username, final String status) {
        mainHandler.post(() -> {
            for (MessageListener listener : listeners) {
                listener.onUserStatusChange(username, status);
            }
        });
    }

    private void notifyMessageEdited(final ChatMessage message) {
        mainHandler.post(() -> {
            for (MessageListener listener : listeners) {
                listener.onMessageEdited(message);
            }
        });
    }

    private void notifyMessageDeleted(final ChatMessage message) {
        mainHandler.post(() -> {
            for (MessageListener listener : listeners) {
                listener.onMessageDeleted(message);
            }
        });
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public String getUsername() {
        return username;
    }
}