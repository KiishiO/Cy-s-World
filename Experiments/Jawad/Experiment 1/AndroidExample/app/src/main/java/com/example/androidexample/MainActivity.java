package com.example.androidexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final String PREFS_NAME = "AppPreferences";
    private static final String LAST_MESSAGE_KEY = "LastMessage";
    private static final int ANIMATION_DURATION = 1000;

    // UI Elements
    private TextView messageText;
    private Button updateButton;

    // State variables
    private boolean isAnimating = false;
    private int clickCount = 0;
    private Handler autoUpdateHandler;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize preferences
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI components
        initializeViews();
        setupListeners();
        loadLastMessage();

        // Start auto-update mechanism
        setupAutoUpdate();
    }

    private void initializeViews() {
        messageText = findViewById(R.id.main_msg_txt);
        updateButton = findViewById(R.id.update_button);

        // Set initial styles
        messageText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        messageText.setTextSize(18);
    }

    private void setupListeners() {
        updateButton.setOnClickListener(v -> {
            clickCount++;
            updateMessage("Click count: " + clickCount);
            if (clickCount >= 5) {
                animateDisplay();
            }
        });

        messageText.setOnLongClickListener(v -> {
            resetState();
            return true;
        });
    }

    private void updateMessage(String message) {
        messageText.setText(message);
        saveMessage(message);
        Toast.makeText(this, "Message updated!", Toast.LENGTH_SHORT).show();
    }

    private void animateDisplay() {
        if (!isAnimating) {
            isAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(messageText, "rotationY", 0f, 360f);
            animator.setDuration(ANIMATION_DURATION);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();

            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    isAnimating = false, ANIMATION_DURATION);
        }
    }

    private void resetState() {
        clickCount = 0;
        messageText.setText("Reset Complete");
        messageText.setRotationY(0f);
        isAnimating = false;
    }

    private void setupAutoUpdate() {
        autoUpdateHandler = new Handler(Looper.getMainLooper());
        autoUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAnimating) {
                    messageText.setAlpha(messageText.getAlpha() == 1f ? 0.5f : 1f);
                }
                autoUpdateHandler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    private void saveMessage(String message) {
        preferences.edit().putString(LAST_MESSAGE_KEY, message).apply();
    }

    private void loadLastMessage() {
        String lastMessage = preferences.getString(LAST_MESSAGE_KEY, "Hello World");
        messageText.setText(lastMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoUpdateHandler != null) {
            autoUpdateHandler.removeCallbacksAndMessages(null);
        }
    }
}