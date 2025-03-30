package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.java_websocket.handshake.ServerHandshake;

/**
 * Main activity for WebSocket chat application
 * Allows users to connect to a WebSocket server
 */
public class MainActivity extends AppCompatActivity implements WebSocketListener {
    private static final String TAG = "MainActivity";

    private Button connectBtn;
    private EditText serverEtx, usernameEtx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        connectBtn = findViewById(R.id.connectBtn);
        serverEtx = findViewById(R.id.serverEdt);
        usernameEtx = findViewById(R.id.unameEdt);

        // Use laptop's IP with the correct WebSocket path format
        // The server expects /chat/{username} format - username is part of the path
        serverEtx.setText("ws://192.168.1.150:8080/chat/");

        // Connect button listener
        connectBtn.setOnClickListener(view -> {
            String username = usernameEtx.getText().toString().trim();

            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build the WebSocket URL by appending username to the path
            String serverUrl = serverEtx.getText().toString() + username;
            Log.d(TAG, "Connecting to: " + serverUrl);

            // Show connecting message
            Toast.makeText(this, "Connecting to server...", Toast.LENGTH_SHORT).show();

            // Establish WebSocket connection and set listener
            WebSocketManager.getInstance().connectWebSocket(serverUrl);
            WebSocketManager.getInstance().setWebSocketListener(this);
        });
    }

    // WebSocketListener implementations

    @Override
    public void onWebSocketMessage(String message) {
        // Not handling messages in MainActivity
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connection closed: " + reason, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connected to server!", Toast.LENGTH_SHORT).show();

            // Go to chat activity
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connection error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "WebSocket error", ex);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove this activity as the listener when it's destroyed
        WebSocketManager.getInstance().removeWebSocketListener();
    }
}