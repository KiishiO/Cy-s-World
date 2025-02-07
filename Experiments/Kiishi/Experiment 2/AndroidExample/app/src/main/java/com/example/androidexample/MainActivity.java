package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private TextView messageText;     // define message textview variable
    private Button counterButton;     // define counter button variable


    private Button resetButton;       //define reset button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);             // link to Main activity XML

        /* initialize UI elements */
        messageText = findViewById(R.id.main_msg_txt);      // link to message textview in the Main activity XML
        counterButton = findViewById(R.id.main_counter_btn);// link to counter button in the Main activity XML
        resetButton = findViewById(R.id.main_reset_btn);

        /* extract data passed into this activity from another activity */
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            messageText.setText("Welcome!\n\nPick a dice limit and roll your lucky number!");
        } else {
            String number = extras.getString("NUM");  // this will come from LoginActivity
            messageText.setText("Hurray!\n\nYour lucky number was " + number);
        }

        /* click listener on counter button pressed */
        counterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when counter button is pressed, use intent to switch to Counter Activity */
                Intent intent = new Intent(MainActivity.this, CounterActivity.class);
                startActivity(intent);
            }
        });

        /* click listener on reset button pressed */
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when reset button is pressed, change text displayed back to original */
                messageText.setText("Welcome!\n\nPick a dice limit and roll your lucky number!");
            }
        });
    }
}