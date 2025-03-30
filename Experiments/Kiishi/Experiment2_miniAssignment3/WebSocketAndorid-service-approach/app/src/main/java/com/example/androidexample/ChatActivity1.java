package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.java_websocket.handshake.ServerHandshake;

public class ChatActivity1 extends AppCompatActivity {

    private Button sendBtn;
    private ImageButton backMainBtn;
    private EditText msgEtx;
    private TextView msgTv;
    private TextView userText;
    private static final String TAG = "ChatActivity1";
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat1);
        Log.d(TAG, "onCreate: ChatActivity1 initialized.");


        /* initialize UI elements */
        sendBtn = (Button) findViewById(R.id.sendBtn);
        backMainBtn = (ImageButton) findViewById(R.id.backMainBtn);
        msgEtx = (EditText) findViewById(R.id.msgEdt);
        msgTv = (TextView) findViewById(R.id.tx1);
        userText = (TextView) findViewById(R.id.username);

        //Retrieving saved data, if any
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        // Retrieve and display saved input
        String receivedInput = sharedPreferences.getString("USER_INPUT_1", "Username");

        /* extract data passed into this activity from another activity */
        if(receivedInput == null) {
            userText.setText("Username");
        } else {
            userText.setText(receivedInput);
        }

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            String message = msgEtx.getText().toString().trim();
            // broadcast this message to the WebSocketService
            // tag it with the key - to specify which WebSocketClient (connection) to send
            // in this case: "chat1"
            if (message.isEmpty()) {
                Log.w(TAG, "sendBtn clicked but message was empty.");
                return;
            }
            Intent intent = new Intent("SendWebSocketMessage");
            intent.putExtra("key", "chat1");
            intent.putExtra("message", msgEtx.getText().toString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d(TAG, "Broadcast sent for message: " + message);
            msgEtx.setText("");
            Log.d(TAG, "Message input cleared.");
        });

        /* back button listener */
        backMainBtn.setOnClickListener(view -> {
            // got to chat activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // For receiving messages
    // only react to messages with tag "chat1"
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            if ("chat1".equals(key)){
                String message = intent.getStringExtra("message");
                runOnUiThread(() -> {
                    String s = msgTv.getText().toString();
                    msgTv.setText(s + "\n" + message);
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