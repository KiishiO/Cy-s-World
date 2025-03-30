package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity{

    private Button connectBtn1, connectBtn2, backBtn1, backBtn2;
    private EditText serverEtx1, usernameEtx1, serverEtx2, usernameEtx2;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* initialize UI elements */
        connectBtn1 = (Button) findViewById(R.id.connectBtn1);
        connectBtn2 = (Button) findViewById(R.id.connectBtn2);
        backBtn1 = (Button) findViewById(R.id.backBtn1);
        backBtn2 = (Button) findViewById(R.id.backBtn2);
        serverEtx1 = (EditText) findViewById(R.id.server1Edt);
        usernameEtx1 = (EditText) findViewById(R.id.uname1Edt);
        serverEtx2 = (EditText) findViewById(R.id.server2Edt);
        usernameEtx2 = (EditText) findViewById(R.id.uname2Edt);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        //Code modified to save User input in case Return button is clicked
        // Retrieve saved data if available
        String savedInput1 = sharedPreferences.getString("USER_INPUT_1", "");
        usernameEtx1.setText(savedInput1);
        String savedInput2 = sharedPreferences.getString("USER_INPUT_2", "");
        usernameEtx2.setText(savedInput2);

        /* connect1 button listener */
        connectBtn1.setOnClickListener(view -> {
            // Save user input
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("USER_INPUT_1", usernameEtx1.getText().toString());
            editor.apply();  // Asynchronous save

            String serverUrl = serverEtx1.getText().toString() + usernameEtx1.getText().toString();

            // start Websocket service with key "chat1"
            Intent serviceIntent = new Intent(this, WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", "chat1");
            serviceIntent.putExtra("url", serverUrl);
            startService(serviceIntent);

            // got to chat activity #1
            Intent intent = new Intent(this, ChatActivity1.class);
            startActivity(intent);
        });

        /* connect2 button listener */
        connectBtn2.setOnClickListener(view -> {
            // Save user input
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("USER_INPUT_2", usernameEtx2.getText().toString());
            editor.apply();  // Asynchronous save

            String serverUrl = serverEtx2.getText().toString() + usernameEtx2.getText().toString();

            // start Websocket service with key "chat2"
            Intent serviceIntent = new Intent(this, WebSocketService.class);
            serviceIntent.setAction("CONNECT");
            serviceIntent.putExtra("key", "chat2");
            serviceIntent.putExtra("url", serverUrl);
            startService(serviceIntent);

            // got to chat activity #2
            Intent intent = new Intent(this, ChatActivity2.class);
            startActivity(intent);
        });

        /* back button listener */
        backBtn1.setOnClickListener(view -> {
            // got to chat activity
            Intent intent = new Intent(this, ChatActivity1.class);
            startActivity(intent);
        });

        /* back2 button listener */
        backBtn2.setOnClickListener(view -> {
            // got to chat activity
            Intent intent = new Intent(this, ChatActivity2.class);
            startActivity(intent);
        });
    }
}