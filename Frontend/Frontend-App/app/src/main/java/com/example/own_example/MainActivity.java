package com.example.own_example;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button signUpButton;
    private ImageView studentsIcon;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Initialize UI elements
        loginButton = findViewById(R.id.main_btnLogIn);
        signUpButton = findViewById(R.id.main_btnSignUp);
        studentsIcon = findViewById(R.id.landing_students_icon);

        // Hide elements initially
        studentsIcon.setAlpha(0f);
        loginButton.setAlpha(0f);
        signUpButton.setAlpha(0f);

        // Set click listeners with animation
        loginButton.setOnClickListener(v -> {
            animateButtonPress(loginButton);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 200);
        });

        signUpButton.setOnClickListener(v -> {
            animateButtonPress(signUpButton);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 200);
        });

        // Delay to let views initialize fully
        new Handler(Looper.getMainLooper()).postDelayed(this::startAdvancedAnimations, 100);
    }

    private void animateButtonPress(View button) {
        button.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(70)
                .withEndAction(() ->
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                );
    }

    private void startAdvancedAnimations() {
        // Setup icon animation
        studentsIcon.setScaleX(0.2f);
        studentsIcon.setScaleY(0.2f);
        studentsIcon.setRotation(-15f);

        // Prepare buttons
        loginButton.setTranslationY(200f);
        signUpButton.setTranslationY(200f);

        // First animate the icon with a complex animation
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(studentsIcon, "scaleX", 0.2f, 1.2f, 1f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(studentsIcon, "scaleY", 0.2f, 1.2f, 1f);
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(studentsIcon, "rotation", -15f, 15f, 0f);
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(studentsIcon, "alpha", 0f, 1f);

        AnimatorSet iconSet = new AnimatorSet();
        iconSet.playTogether(scaleXAnim, scaleYAnim, rotateAnim, alphaAnim);
        iconSet.setDuration(1300);
        iconSet.setInterpolator(new AnticipateOvershootInterpolator(1.4f));
        iconSet.start();

        // After icon animation, start button animations
        new Handler(Looper.getMainLooper()).postDelayed(this::animateButtons, 1000);
    }

    private void animateButtons() {
        // Animate login button with bounce effect
        loginButton.setAlpha(1f);
        ObjectAnimator loginMoveAnim = ObjectAnimator.ofFloat(loginButton, "translationY", 200f, -20f, 0f);
        loginMoveAnim.setDuration(800);
        loginMoveAnim.setInterpolator(new OvershootInterpolator(1.2f));
        loginMoveAnim.start();

        // Animate signup button with slight delay and different bounce
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            signUpButton.setAlpha(1f);
            ObjectAnimator signupMoveAnim = ObjectAnimator.ofFloat(signUpButton, "translationY", 200f, -15f, 0f);
            signupMoveAnim.setDuration(800);
            signupMoveAnim.setInterpolator(new OvershootInterpolator(1.1f));
            signupMoveAnim.start();

            // Add final glow/highlight effect after all animations
            new Handler(Looper.getMainLooper()).postDelayed(this::addFinalTouchEffects, 800);
        }, 200);
    }

    private void addFinalTouchEffects() {
        // Add a subtle pulse effect to draw attention to buttons
        pulseViewRepeatedly(loginButton, 3);

        // Slight delay for staggered effect
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            pulseViewRepeatedly(signUpButton, 3);
        }, 300);
    }

    private void pulseViewRepeatedly(View view, int repeatCount) {
        if (repeatCount <= 0) return;

        view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(300)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(300)
                                .withEndAction(() -> pulseViewRepeatedly(view, repeatCount - 1))
                );
    }
}