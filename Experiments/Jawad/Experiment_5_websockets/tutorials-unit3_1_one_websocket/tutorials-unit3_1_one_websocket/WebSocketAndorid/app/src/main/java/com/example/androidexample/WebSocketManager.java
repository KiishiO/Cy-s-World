package com.example.androidexample;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Singleton WebSocketManager instance used for managing WebSocket connections
 * in the Android application.
 */
public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;
    private MyWebSocketClient webSocketClient;
    private WebSocketListener webSocketListener;

    private WebSocketManager() {}

    /**
     * Retrieves a synchronized instance of the WebSocketManager
     * @return A synchronized instance of WebSocketManager
     */
    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    /**
     * Sets the WebSocketListener for this WebSocketManager instance
     * @param listener The WebSocketListener to be set
     */
    public void setWebSocketListener(WebSocketListener listener) {
        this.webSocketListener = listener;
    }

    /**
     * Removes the currently set WebSocketListener
     */
    public void removeWebSocketListener() {
        this.webSocketListener = null;
    }

    /**
     * Initiates a WebSocket connection to the specified server URL
     * @param serverUrl The URL of the WebSocket server to connect to
     */
    public void connectWebSocket(String serverUrl) {
        try {
            Log.d(TAG, "Connecting to: " + serverUrl);
            URI serverUri = URI.create(serverUrl);
            webSocketClient = new MyWebSocketClient(serverUri);
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to WebSocket", e);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketError(e);
            }
        }
    }

    /**
     * Sends a WebSocket message to the connected server
     * @param message The message to be sent
     */
    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        } else {
            Log.e(TAG, "Cannot send message - WebSocket is not connected");
        }
    }

    /**
     * Disconnects the WebSocket connection
     */
    public void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    /**
     * A private inner class that extends WebSocketClient
     */
    private class MyWebSocketClient extends WebSocketClient {

        private MyWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d(TAG, "WebSocket Connected");
            if (webSocketListener != null) {
                webSocketListener.onWebSocketOpen(handshakedata);
            }
        }

        @Override
        public void onMessage(String message) {
            Log.d(TAG, "Received message: " + message);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketMessage(message);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d(TAG, "WebSocket Closed. Code: " + code + ", Reason: " + reason + ", Remote: " + remote);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketClose(code, reason, remote);
            }
        }

        @Override
        public void onError(Exception ex) {
            Log.e(TAG, "WebSocket Error", ex);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketError(ex);
            }
        }
    }
}