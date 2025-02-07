package com.example.androidexample;

import static kotlin.random.RandomKt.Random;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import kotlin.random.Random;


public class DiceActivity extends AppCompatActivity {
    // Declare UI elements (but don't initialize them here)
    private Button homeButton;
    private TextView displayText;
    private TextView numberText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        // Initialize UI elements after setContentView
        homeButton = findViewById(R.id.dice_back_btn);
        displayText = findViewById(R.id.dice_display_txt);
        numberText = findViewById(R.id.dice_number);

        // Extract data passed into this activity
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            displayText.setText("Your lucky number is...");
            numberText.setText("?");
        } else {
            String number = extras.getString("NUM");
            Random rand = Random(Integer.parseInt(number));
            displayText.setText("Your lucky number is...");
            numberText.setText(rand.toString());
        }

        // Set click listener for home button
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DiceActivity.this, MainActivity.class);
                // Be careful here - extras might be null
                if (extras != null) {
                    String number = extras.getString("NUM");
                    intent.putExtra("NUM", number);
                }
                startActivity(intent);
            }
        });
    }
}