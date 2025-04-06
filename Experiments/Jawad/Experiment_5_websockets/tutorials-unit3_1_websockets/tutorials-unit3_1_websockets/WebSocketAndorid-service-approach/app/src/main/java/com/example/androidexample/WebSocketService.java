package com.example.androidexample;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebSocketService extends Service {

    // key to WebSocketClient obj mapping - for multiple WebSocket connections
    private final Map<String, WebSocketClient> webSockets = new HashMap<>();
    private static final String TAG = "WebSocketService";
    private Handler mainHandler;

    // Connection timeout in milliseconds (10 seconds)
    private static final int CONNECTION_TIMEOUT = 10000;

    public WebSocketService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("CONNECT".equals(action)) {
                String url = intent.getStringExtra("url");
                String key = intent.getStringExtra("key");
                Log.d(TAG, "Connecting to: " + url + " with key: " + key);
                connectWebSocket(key, url);
            } else if ("DISCONNECT".equals(action)) {
                String key = intent.getStringExtra("key");
                disconnectWebSocket(key);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "WebSocketService created");
        mainHandler = new Handler(Looper.getMainLooper());
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("SendWebSocketMessage"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "WebSocketService destroyed");
        for (WebSocketClient client : webSockets.values()) {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing WebSocket", e);
                }
            }
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void connectWebSocket(final String key, final String url) {
        // Run WebSocket connection in a separate thread to prevent UI freezing
        new Thread(() -> {
            try {
                // Check if there's already a connection for this key
                if (webSockets.containsKey(key) && webSockets.get(key) != null) {
                    WebSocketClient existingClient = webSockets.get(key);
                    if (existingClient.isOpen()) {
                        Log.d(TAG, "WebSocket " + key + " is already connected");
                        return;
                    } else {
                        // Close existing connection
                        try {
                            existingClient.close();
                        } catch (Exception e) {
                            Log.e(TAG, "Error closing existing connection", e);
                        }
                    }
                }

                URI serverUri = new URI(url);
                WebSocketClient webSocketClient = new WebSocketClient(serverUri) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        Log.d(TAG, key + " connection opened");

                        // Send connection status message to the UI
                        sendMessageToActivity(key, "📡 Connected to chat server");
                    }

                    @Override
                    public void onMessage(String message) {
                        Log.d(TAG, key + " received message: " + message);

                        // Forward the message to the UI
                        sendMessageToActivity(key, message);
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        Log.d(TAG, key + " connection closed. Code: " + code + ", Reason: " + reason);

                        // Notify the UI about disconnection
                        sendMessageToActivity(key, "⚠️ Disconnected from server: " + reason);

                        // Remove from active connections
                        webSockets.remove(key);
                    }

                    @Override
                    public void onError(Exception ex) {
                        Log.e(TAG, key + " error: " + ex.getMessage(), ex);

                        // Notify the UI about the error
                        sendMessageToActivity(key, "❌ Connection error: " + ex.getMessage());
                    }
                };

                // Set connection timeout
                webSocketClient.setConnectionLostTimeout(CONNECTION_TIMEOUT);
                webSocketClient.setTcpNoDelay(true);
                webSocketClient.connect();

                // Store the WebSocket client
                webSockets.put(key, webSocketClient);

                // Show toast on main thread
                showToast("Connecting to " + url);

                // Set a timeout for connection
                mainHandler.postDelayed(() -> {
                    if (webSockets.containsKey(key) &&
                            webSockets.get(key) != null &&
                            !webSockets.get(key).isOpen()) {

                        Log.e(TAG, "Connection timeout for " + key);
                        sendMessageToActivity(key, "❌ Connection timeout. Please check server address and try again.");

                        try {
                            webSockets.get(key).close();
                            webSockets.remove(key);
                        } catch (Exception e) {
                            Log.e(TAG, "Error closing timed out connection", e);
                        }
                    }
                }, CONNECTION_TIMEOUT);

            } catch (URISyntaxException e) {
                Log.e(TAG, "Invalid URI syntax: " + url, e);
                sendMessageToActivity(key, "❌ Invalid server address: " + e.getMessage());
                showToast("Invalid server address");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing WebSocket", e);
                sendMessageToActivity(key, "❌ Failed to connect: " + e.getMessage());
                showToast("Connection error: " + e.getMessage());
            }
        }).start();
    }

    private void sendMessageToActivity(String key, String message) {
        Intent intent = new Intent("WebSocketMessageReceived");
        intent.putExtra("key", key);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void showToast(final String message) {
        mainHandler.post(() -> {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    // Listen to messages from Activities and send them via WebSocket
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            String message = intent.getStringExtra("message");
            Log.d(TAG, "Received broadcast to send message via " + key + ": " + message);

            // Run in a separate thread to avoid blocking UI
            new Thread(() -> {
                try {
                    WebSocketClient webSocket = webSockets.get(key);
                    if (webSocket != null && webSocket.isOpen()) {
                        webSocket.send(message);
                        Log.d(TAG, "Message sent successfully via " + key);
                    } else {
                        Log.w(TAG, "Cannot send message - WebSocket " + key + " is not connected");

                        // Notify UI about message failure
                        sendMessageToActivity(key, "⚠️ Message could not be sent - not connected");

                        // Try to reconnect if socket exists but is closed
                        if (webSocket != null && !webSocket.isOpen()) {
                            Log.d(TAG, "Attempting to reconnect " + key);
                            showToast("Attempting to reconnect...");
                            webSocket.reconnect();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error sending message", e);
                    sendMessageToActivity(key, "❌ Error sending message: " + e.getMessage());
                }
            }).start();
        }
    };

    private void disconnectWebSocket(String key) {
        if (webSockets.containsKey(key)) {
            try {
                Log.d(TAG, "Disconnecting WebSocket " + key);
                webSockets.get(key).close();
                webSockets.remove(key);
            } catch (Exception e) {
                Log.e(TAG, "Error disconnecting WebSocket", e);
            }
        }
    }
}