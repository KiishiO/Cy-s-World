/**
 * Client for managing WebSocket connections for the chat functionality in CyWorld.
 * This service handles real-time communication between users, including messages
 * and user status updates.
 *
 * @author Jawad Ali
 */
package com.example.own_example.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.own_example.models.ChatMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ChatWebSocketClient {
    /** Tag for logging purposes */
    private static final String TAG = "ChatWebSocketClient";

    /** The active WebSocket connection */
    private WebSocket webSocket;

    /** HTTP client for establishing WebSocket connections */
    private final OkHttpClient client;

    /** URL of the chat server */
    private final String serverUrl;

    /** Username of the current user */
    private final String username;

    /** Current chat room ("main" or "individual") */
    private String currentRoom = "main";

    /** Connection status flag */
    private boolean isConnected = false;

    /** List of listeners for message and status events */
    private final List<MessageListener> listeners = new ArrayList<>();

    /** Handler for executing callbacks on the main thread */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Interface for listening to chat events.
     * Implementors can receive messages, connection status changes,
     * and user status updates.
     */
    public interface MessageListener {
        /**
         * Called when a new chat message is received.
         *
         * @param message The received ChatMessage object
         */
        void onMessageReceived(ChatMessage message);

        /**
         * Called when the connection status changes.
         *
         * @param connected true if connected, false otherwise
         */
        void onStatusChange(boolean connected);

        /**
         * Called when a user's status changes (online, away, etc.).
         *
         * @param username The username of the user whose status changed
         * @param status The new status
         */
        void onUserStatusChange(String username, String status);
    }

    /**
     * Creates a new ChatWebSocketClient with specified server URL and username.
     *
     * @param serverUrl The URL of the chat server
     * @param username The username of the current user
     */
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

    /**
     * Connects to the main chat room.
     */
    public void connectToMainChat() {
        currentRoom = "main";
        connect(serverUrl + "/chat/" + getEncodedUsername(username));
    }

    /**
     * Connects to an individual chat room.
     */
    public void connectToIndividualChat() {
        currentRoom = "individual";
        connect(serverUrl + "/chat/1/" + getEncodedUsername(username));
    }

    /**
     * Encodes the username for use in URLs.
     * Replaces spaces with underscores.
     *
     * @param username The username to encode
     * @return The encoded username
     */
    private String getEncodedUsername(String username) {
        try {
            // Replace spaces with underscores (simpler approach than URL encoding)
            return username.replace(" ", "_");
        } catch (Exception e) {
            Log.e(TAG, "Error encoding username", e);
            return username;
        }
    }

    /**
     * Establishes a WebSocket connection to the specified endpoint.
     *
     * @param endpoint The complete WebSocket URL to connect to
     */
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
            /**
             * Called when the connection is successfully established.
             */
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "Connected to: " + endpoint);
                isConnected = true;
                notifyStatusChange(true);
            }

            /**
             * Called when a text message is received from the server.
             */
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Received message: " + text);

                ChatMessage chatMessage = ChatMessage.createFromWebSocketMessage(text, username);
                notifyMessageReceived(chatMessage);

                // Handle status changes
                if (chatMessage.getMessageType().equals("STATUS")) {
                    String[] parts = text.split(" is now ");
                    if (parts.length == 2) {
                        notifyUserStatusChange(parts[0], parts[1]);
                    }
                }
            }

            /**
             * Called when the connection is closed.
             */
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Connection closed. Code: " + code + ", Reason: " + reason);
                isConnected = false;
                notifyStatusChange(false);
            }

            /**
             * Called when a connection error occurs.
             */
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket error", t);
                isConnected = false;
                notifyStatusChange(false);
            }
        });
    }

    /**
     * Sends a message to the current chat room.
     *
     * @param message The message text to send
     */
    public void sendMessage(String message) {
        if (isConnected && webSocket != null) {
            webSocket.send(message);
        } else {
            Log.e(TAG, "Cannot send message. WebSocket is not connected.");
        }
    }

    /**
     * Sends a direct message to a specific user.
     *
     * @param recipient The username of the recipient
     * @param message The message text to send
     */
    public void sendDirectMessage(String recipient, String message) {
        sendMessage("@" + recipient + " " + message);
    }

    /**
     * Sets the user's status.
     *
     * @param status The status to set ('online', 'active', or 'inactive')
     */
    public void setStatus(String status) {
        if (!status.equals("online") && !status.equals("active") && !status.equals("inactive")) {
            Log.e(TAG, "Invalid status. Use 'online', 'active', or 'inactive'");
            return;
        }
        sendMessage("__status__ " + status);
    }

    /**
     * Disconnects from the chat server.
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User disconnected");
            webSocket = null;
        }
        isConnected = false;
    }

    /**
     * Adds a listener for chat events.
     *
     * @param listener The listener to add
     */
    public void addMessageListener(MessageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for chat events.
     *
     * @param listener The listener to remove
     */
    public void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners about a received message.
     *
     * @param message The received ChatMessage
     */
    private void notifyMessageReceived(final ChatMessage message) {
        mainHandler.post(() -> {
            for (MessageListener listener : listeners) {
                listener.onMessageReceived(message);
            }
        });
    }

    /**
     * Notifies all listeners about a connection status change.
     *
     * @param connected The new connection status
     */
    private void notifyStatusChange(final boolean connected) {
        mainHandler.post(() -> {
            for (MessageListener listener : listeners) {
                listener.onStatusChange(connected);
            }
        });
    }

    /**
     * Notifies all listeners about a user status change.
     *
     * @param username The username of the user
     * @param status The new status
     */
    private void notifyUserStatusChange(final String username, final String status) {
        mainHandler.post(() -> {
            for (MessageListener listener : listeners) {
                listener.onUserStatusChange(username, status);
            }
        });
    }

    /**
     * Checks if the client is currently connected to the server.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Gets the current chat room.
     *
     * @return The current room ('main' or 'individual')
     */
    public String getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Gets the username of the current user.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }
}