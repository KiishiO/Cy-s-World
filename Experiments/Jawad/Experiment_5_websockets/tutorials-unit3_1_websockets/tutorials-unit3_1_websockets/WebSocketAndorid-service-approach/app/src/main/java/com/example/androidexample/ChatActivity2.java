package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatActivity2 extends AppCompatActivity {

    private Button sendBtn, backMainBtn, emojiBtn, btnHello, btnQuestion, btnAgreement;
    private EditText msgEtx;
    private TextView msgTv;
    private static final String TAG = "ChatActivity2"; // For logging

    // Common emojis for quick access
    private final String[] emojis = {"😊", "👍", "🤔", "❓", "✅", "❌", "🔥", "💡"};
    private int currentEmojiIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        /* initialize UI elements */
        sendBtn = findViewById(R.id.sendBtn2);
        msgEtx = findViewById(R.id.msgEdt2);
        msgTv = findViewById(R.id.tx2);
        backMainBtn = findViewById(R.id.backMainBtn);
        emojiBtn = findViewById(R.id.emojiBtn);
        btnHello = findViewById(R.id.btnHello);
        btnQuestion = findViewById(R.id.btnQuestion);
        btnAgreement = findViewById(R.id.btnAgreement);

        Log.d(TAG, "onCreate: ChatActivity2 initialized.");

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            String message = msgEtx.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "sendBtn clicked but message was empty.");
                return;
            }
            sendMessage(message);
            msgEtx.setText("");
        });

        /* emoji button listener - cycles through emojis */
        emojiBtn.setOnClickListener(v -> {
            currentEmojiIndex = (currentEmojiIndex + 1) % emojis.length;
            emojiBtn.setText(emojis[currentEmojiIndex]);

            // Insert emoji at current cursor position
            int cursorPosition = msgEtx.getSelectionStart();
            String currentText = msgEtx.getText().toString();
            String newText = currentText.substring(0, cursorPosition) + emojis[currentEmojiIndex] +
                    currentText.substring(cursorPosition);
            msgEtx.setText(newText);
            msgEtx.setSelection(cursorPosition + emojis[currentEmojiIndex].length());
        });

        /* Quick reply buttons */
        btnHello.setOnClickListener(v -> sendMessage("👋 Hello everyone!"));
        btnQuestion.setOnClickListener(v -> sendMessage("❓ I have a question..."));
        btnAgreement.setOnClickListener(v -> sendMessage("✅ I agree with that!"));

        /* back button listener */
        backMainBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Helper method to send message with timestamp
    private void sendMessage(String message) {
        Intent intent = new Intent("SendWebSocketMessage");
        intent.putExtra("key", "chat2");
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent for message: " + message);

        // Add the outgoing message to the chat display with timestamp and styling
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String formattedMessage = "\n[" + timestamp + "] You: " + message;
        String s = msgTv.getText().toString();
        msgTv.setText(s + formattedMessage);
    }

    // For receiving messages
    // only react to messages with tag "chat2"
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            if ("chat2".equals(key)){
                String message = intent.getStringExtra("message");
                runOnUiThread(() -> {
                    // Don't add the message if it already appears to be from the local user
                    // This is to avoid duplication since we're already adding outgoing messages
                    if (!message.startsWith("You:")) {
                        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                        String formattedMessage = "\n[" + timestamp + "] " + message;
                        String s = msgTv.getText().toString();
                        msgTv.setText(s + formattedMessage);
                    }
                });
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
                new IntentFilter("WebSocketMessageReceived"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }
}