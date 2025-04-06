package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ChatActivity for handling WebSocket chat messages
 */
public class ChatActivity extends AppCompatActivity implements WebSocketListener {
    private static final String TAG = "ChatActivity";

    private Button sendBtn, clearBtn, disconnectBtn;
    private EditText msgEtx;
    private TextView msgTv;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize UI elements
        sendBtn = findViewById(R.id.sendBtn);
        msgEtx = findViewById(R.id.msgEdt);
        msgTv = findViewById(R.id.tx1);
        scrollView = findViewById(R.id.scrollView);

        // Add clear and disconnect buttons if they exist in your layout
        try {
            clearBtn = findViewById(R.id.clearBtn);
            disconnectBtn = findViewById(R.id.disconnectBtn);

            clearBtn.setOnClickListener(v -> {
                msgTv.setText("");
            });

            disconnectBtn.setOnClickListener(v -> {
                WebSocketManager.getInstance().disconnectWebSocket();
                finish(); // Close this activity and return to the main activity
            });
        } catch (Exception e) {
            Log.w(TAG, "Buttons not found in layout: " + e.getMessage());
        }

        // Connect this activity to the WebSocket instance
        WebSocketManager.getInstance().setWebSocketListener(this);

        // Send button listener
        sendBtn.setOnClickListener(v -> {
            try {
                String message = msgEtx.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Create JSON message
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("message", message);

                    // Send message
                    WebSocketManager.getInstance().sendMessage(jsonMessage.toString());

                    // Clear input after sending
                    msgEtx.setText("");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending message", e);
                Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
            }
        });

        // Add welcome message
        appendToChat("Connected to WebSocket chat server");
    }

    /**
     * Helper method to add text to the chat box
     */
    private void appendToChat(String message) {
        String currentText = msgTv.getText().toString();
        if (!currentText.isEmpty() && !currentText.equals("The conversation will appear here:")) {
            currentText += "\n";
        } else {
            currentText = "";
        }

        // Format with timestamp
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        msgTv.setText(currentText + "[" + timestamp + "] " + message);

        // Scroll to bottom
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            try {
                // Try to parse as JSON
                JSONObject jsonMessage = new JSONObject(message);
                String formattedMessage = jsonMessage.optString("message", message);
                appendToChat(formattedMessage);
            } catch (JSONException e) {
                // Not JSON, just show the raw message
                appendToChat(message);
            }
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            appendToChat("--- Connection closed by " + closedBy + " (Reason: " + reason + ") ---");

            // Disable sending
            sendBtn.setEnabled(false);
            msgEtx.setEnabled(false);

            Toast.makeText(this, "WebSocket disconnected", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connected to chat server", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onWebSocketError(Exception ex) {
        runOnUiThread(() -> {
            appendToChat("--- Error: " + ex.getMessage() + " ---");
            Toast.makeText(this, "WebSocket error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove this activity as the listener when it's destroyed
        WebSocketManager.getInstance().removeWebSocketListener();
    }
}