package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Random;


public class DiceActivity extends AppCompatActivity {
    // Declare UI elements (but don't initialize them here)
    private Button homeButton;
    private TextView displayText;
    private TextView numberText;

    private int randNum;

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
            Random randomGenerator = new Random();
            int randomInt = randomGenerator.nextInt(Integer.parseInt(number));
            randNum = randomInt;
            String text = String.valueOf(randomInt);
            displayText.setText("Your lucky number is...");
            numberText.setText(text);
        }

        // Set click listener for home button
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DiceActivity.this, MainActivity.class);
                // Be careful here - extras might be null
                if (extras != null) {
                    //String number = extras.getString("NUM");
                    //Random randomGenerator = new Random();
                    //int randomInt = randomGenerator.nextInt(Integer.parseInt(number));
                    //String number = extras.getString("NUM");
                    intent.putExtra("NUM", String.valueOf(randNum));
                }
                startActivity(intent);
            }
        });
    }
}