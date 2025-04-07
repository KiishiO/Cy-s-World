package com.example.own_example.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.own_example.models.ChatMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

public class ChatWebSocketClient {
    private static final String TAG = "ChatWebSocketClient";
    private JavaWebSocketClient webSocketClient;
    private final String serverUrl;
    private final String username;
    private String currentRoom = "main"; // "main" or "individual"
    private boolean isConnected = false;
    private final List<MessageListener> listeners = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface MessageListener {
        void onMessageReceived(ChatMessage message);
        void onStatusChange(boolean connected);
        void onUserStatusChange(String username, String status);
    }

    public ChatWebSocketClient(String serverUrl, String username) {
        this.serverUrl = serverUrl;
        this.username = username;
    }

    public void connectToMainChat() {
        currentRoom = "main";
        connect(serverUrl + "/chat/" + username);
    }

    public void connectToIndividualChat() {
        currentRoom = "individual";
        connect(serverUrl + "/chat/1/" + username);
    }

    private void connect(String endpoint) {
        // Close any existing connection
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close();
        }

        try {
            // URL encode the username part to handle spaces and special characters
            String encodedEndpoint = getEncodedEndpoint(endpoint);

            URI uri = new URI(encodedEndpoint);
            webSocketClient = new JavaWebSocketClient(uri, new WebSocketListener() {
                @Override
                public void onOpen() {
                    Log.d(TAG, "Connected to: " + uri.toString());
                    isConnected = true;
                    notifyStatusChange(true);
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Received message: " + message);

                    ChatMessage chatMessage = ChatMessage.createFromWebSocketMessage(message, username);
                    notifyMessageReceived(chatMessage);

                    // Handle status changes
                    if (chatMessage.getMessageType().equals("STATUS")) {
                        String[] parts = message.split(" is now ");
                        if (parts.length == 2) {
                            notifyUserStatusChange(parts[0], parts[1]);
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "Connection closed. Code: " + code + ", Reason: " + reason + ", Remote: " + remote);
                    isConnected = false;
                    notifyStatusChange(false);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);
                    isConnected = false;
                    notifyStatusChange(false);
                }
            });

            // Perform connection in background
            executorService.execute(() -> {
                try {
                    webSocketClient.connect();
                } catch (Exception e) {
                    Log.e(TAG, "Connection error", e);
                    mainHandler.post(() -> notifyStatusChange(false));
                }
            });

        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid URI: " + endpoint, e);
        }
    }

    private String getEncodedEndpoint(String endpoint) {
        try {
            // URL encode the username part to handle spaces and special characters
            String encodedEndpoint = endpoint;

            // If the endpoint contains a username (which would be after the last slash)
            int lastSlashIndex = endpoint.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < endpoint.length() - 1) {
                String baseUrl = endpoint.substring(0, lastSlashIndex + 1);
                String username = endpoint.substring(lastSlashIndex + 1);

                // URL encode just the username portion
                String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
                encodedEndpoint = baseUrl + encodedUsername;
            }

            return encodedEndpoint;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error encoding username", e);
            return endpoint; // fallback to original endpoint if encoding fails
        }
    }

    public void sendMessage(String message) {
        if (isConnected && webSocketClient != null && webSocketClient.isOpen()) {
            // Send message in background
            executorService.execute(() -> {
                try {
                    webSocketClient.send(message);
                } catch (Exception e) {
                    Log.e(TAG, "Error sending message", e);
                }
            });
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

    public void disconnect() {
        if (webSocketClient != null) {
            // Disconnect in background
            executorService.execute(() -> {
                webSocketClient.close();
                webSocketClient = null;
            });
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

    public boolean isConnected() {
        return isConnected;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public String getUsername() {
        return username;
    }

    // Cleanup method to shutdown executor service
    public void shutdown() {
        executorService.shutdown();
    }

    // Listener interface for WebSocket events
    private interface WebSocketListener {
        void onOpen();
        void onMessage(String message);
        void onClose(int code, String reason, boolean remote);
        void onError(Exception ex);
    }

    // Custom WebSocket client implementation
    private static class JavaWebSocketClient {
        private final URI serverUri;
        private final WebSocketListener listener;
        private java.net.Socket socket;
        private java.io.BufferedReader reader;
        private java.io.PrintWriter writer;
        private boolean isOpen = false;
        private Thread receiveThread;

        public JavaWebSocketClient(URI serverUri, WebSocketListener listener) {
            this.serverUri = serverUri;
            this.listener = listener;
        }

        public void connect() {
            try {
                String host = serverUri.getHost();
                int port = serverUri.getPort() == -1 ? 80 : serverUri.getPort();

                socket = new java.net.Socket(host, port);
                reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
                writer = new java.io.PrintWriter(socket.getOutputStream(), true);

                // Send WebSocket handshake
                String path = serverUri.getPath();
                if (path == null || path.isEmpty()) {
                    path = "/";
                }
                writer.println("GET " + path + " HTTP/1.1");
                writer.println("Host: " + host + ":" + port);
                writer.println("Upgrade: websocket");
                writer.println("Connection: Upgrade");
                writer.println("Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ=="); // Dummy key
                writer.println("Sec-WebSocket-Version: 13");
                writer.println();

                // Read handshake response
                String line;
                boolean upgraded = false;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        break;
                    }
                    if (line.startsWith("HTTP/1.1 101")) {
                        upgraded = true;
                    }
                }

                if (upgraded) {
                    isOpen = true;
                    listener.onOpen();

                    // Start receive thread
                    receiveThread = new Thread(this::receiveMessages);
                    receiveThread.start();
                } else {
                    listener.onError(new Exception("WebSocket handshake failed"));
                    close();
                }
            } catch (Exception e) {
                listener.onError(e);
                try {
                    close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        private void receiveMessages() {
            try {
                String message;
                while (isOpen && (message = reader.readLine()) != null) {
                    listener.onMessage(message);
                }
            } catch (Exception e) {
                if (isOpen) {
                    listener.onError(e);
                    close();
                }
            }
        }

        public void send(String message) {
            if (isOpen && writer != null) {
                writer.println(message);
            }
        }

        public void close() {
            isOpen = false;
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
                if (receiveThread != null) {
                    receiveThread.interrupt();
                    receiveThread = null;
                }
                listener.onClose(1000, "Connection closed normally", false);
            } catch (Exception e) {
                listener.onError(e);
            }
        }

        public boolean isOpen() {
            return isOpen;
        }
    }
}