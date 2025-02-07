package com.example.androidexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView messageText;     // define message textview variable
    private Button counterButton;     // define counter button variable
    private int clickCount = 0;       // counter for button clicks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);             // link to Main activity XML

        /* initialize UI elements */
        messageText = findViewById(R.id.main_msg_txt);      // link to message textview in the Main activity XML
        counterButton = findViewById(R.id.main_counter_btn);// link to counter button in the Main activity XML

        /* extract data passed into this activity from another activity */
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            messageText.setText("Intent Example");
        } else {
            String number = extras.getString("NUM");  // this will come from LoginActivity
            messageText.setText("The number was " + number);
        }

        /* click listener on counter button pressed */
        counterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++;
                updateClickDisplay();

                /* when counter button is pressed, use intent to switch to Counter Activity */
                Intent intent = new Intent(MainActivity.this, CounterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateClickDisplay() {
        Toast.makeText(this, "Button clicked: " + clickCount + " times", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu resource
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            // Handle refresh
            messageText.setText("Intent Example");
            clickCount = 0;
            Toast.makeText(this, "Refreshed!", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.action_settings) {
            // Handle settings
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.action_about) {
            // Handle about
            messageText.setText("About: Intent Example App v1.0");
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}