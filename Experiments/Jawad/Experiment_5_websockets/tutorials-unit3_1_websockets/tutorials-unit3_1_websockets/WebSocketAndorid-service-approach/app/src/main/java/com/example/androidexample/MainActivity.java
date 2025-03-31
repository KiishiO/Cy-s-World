package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button connectBtn1, connectBtn2, backBtn1, backBtn2;
    private EditText serverEtx1, usernameEtx1, serverEtx2, usernameEtx2;
    private static final String TAG = "MainActivity";

    // Change this to your laptop's actual IP address on your network
    // For example: "192.168.1.5" (not localhost or 10.0.2.2)
    // Found IP by running "ipconfig" on Windows
    private static final String LAPTOP_IP = "192.168.1.150";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "MainActivity created");

        /* initialize UI elements */
        connectBtn1 = findViewById(R.id.connectBtn);
        connectBtn2 = findViewById(R.id.connectBtn2);
        backBtn1 = findViewById(R.id.backBtn);
        backBtn2 = findViewById(R.id.backBtn2);
        serverEtx1 = findViewById(R.id.serverEdt);
        usernameEtx1 = findViewById(R.id.unameEdt);
        serverEtx2 = findViewById(R.id.serverEdt2);
        usernameEtx2 = findViewById(R.id.unameEdt2);

        // Set correct server URLs with your laptop's actual IP address
        serverEtx1.setText("ws://" + LAPTOP_IP + ":8080/chat/1/");
        serverEtx2.setText("ws://" + LAPTOP_IP + ":8080/chat/2/");

        // Set default usernames
        if (TextUtils.isEmpty(usernameEtx1.getText())) {
            usernameEtx1.setText("AndroidUser1");
        }

        if (TextUtils.isEmpty(usernameEtx2.getText())) {
            usernameEtx2.setText("AndroidUser2");
        }

        /* connect1 button listener */
        connectBtn1.setOnClickListener(view -> {
            String username = usernameEtx1.getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            String serverUrl = serverEtx1.getText().toString() + username;
            Log.d(TAG, "Connecting to Chat Room 1: " + serverUrl);

            // start Websocket service with key "chat1"
            Intent serviceIntent = new Intent(this, WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", "chat1");
            serviceIntent.putExtra("url", serverUrl);
            startService(serviceIntent);

            // go to chat activity #1
            Intent intent = new Intent(this, ChatActivity1.class);
            startActivity(intent);

            Toast.makeText(this, "Connecting to Chat Room 1...", Toast.LENGTH_SHORT).show();
        });

        /* connect2 button listener */
        connectBtn2.setOnClickListener(view -> {
            String username = usernameEtx2.getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            String serverUrl = serverEtx2.getText().toString() + username;
            Log.d(TAG, "Connecting to Chat Room 2: " + serverUrl);

            // start Websocket service with key "chat2"
            Intent serviceIntent = new Intent(this, WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", "chat2");
            serviceIntent.putExtra("url", serverUrl);
            startService(serviceIntent);

            // go to chat activity #2
            Intent intent = new Intent(this, ChatActivity2.class);
            startActivity(intent);

            Toast.makeText(this, "Connecting to Chat Room 2...", Toast.LENGTH_SHORT).show();
        });

        /* back button listener */
        backBtn1.setOnClickListener(view -> {
            // go to chat activity
            Intent intent = new Intent(this, ChatActivity1.class);
            startActivity(intent);
        });

        /* back2 button listener */
        backBtn2.setOnClickListener(view -> {
            // go to chat activity
            Intent intent = new Intent(this, ChatActivity2.class);
            startActivity(intent);
        });
    }
}