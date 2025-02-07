package com.example.androidexample;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private TextView messageText;
    private TextView usernameText;
    private TextView lastLoginText;
    private Button loginButton;
    private Button signupButton;
    private Button logoutButton;
    private Button profileButton;
    private ProgressBar loadingSpinner;

    // Constants
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LAST_LOGIN = "lastLogin";
    private static final int ANIMATION_DURATION = 500;

    // State Variables
    private SharedPreferences prefs;
    private boolean isAnimating = false;
    private Handler handler;

    // Activity Result Launcher
    private final ActivityResultLauncher<Intent> loginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String username = result.getData().getStringExtra("USERNAME");
                    handleLoginSuccess(username);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize components
        initializeViews();
        setupHandlerAndPrefs();
        setupClickListeners();
        checkPreviousLogin();
        handleIntent(getIntent());
    }

    private void initializeViews() {
        // Find views
        messageText = findViewById(R.id.main_msg_txt);
        usernameText = findViewById(R.id.main_username_txt);
        lastLoginText = findViewById(R.id.main_last_login_txt);
        loginButton = findViewById(R.id.main_login_btn);
        signupButton = findViewById(R.id.main_signup_btn);
        logoutButton = findViewById(R.id.main_logout_btn);
        profileButton = findViewById(R.id.main_profile_btn);
        loadingSpinner = findViewById(R.id.loading_spinner);

        // Set initial visibility
        usernameText.setVisibility(View.GONE);
        lastLoginText.setVisibility(View.GONE);
        logoutButton.setVisibility(View.GONE);
        profileButton.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);

        // Set initial text
        messageText.setText("Welcome to My App");
    }

    private void setupHandlerAndPrefs() {
        handler = new Handler(Looper.getMainLooper());
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> showLoginAnimation());
        signupButton.setOnClickListener(v -> startSignupActivity());
        logoutButton.setOnClickListener(v -> showLogoutDialog());
        profileButton.setOnClickListener(v -> showProfileDialog());

        messageText.setOnLongClickListener(v -> {
            showAppInfo();
            return true;
        });
    }

    private void showLoginAnimation() {
        loadingSpinner.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            loadingSpinner.setVisibility(View.GONE);
            startLoginActivity();
        }, 500);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        loginLauncher.launch(intent);
    }

    private void startSignupActivity() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    private void handleLoginSuccess(String username) {
        if (username != null && !username.isEmpty()) {
            // Save login data
            long currentTime = System.currentTimeMillis();
            prefs.edit()
                    .putString(KEY_USERNAME, username)
                    .putLong(KEY_LAST_LOGIN, currentTime)
                    .apply();

            // Update UI with animation
            animateLoginSuccess(username, currentTime);
        }
    }

    private void animateLoginSuccess(String username, long loginTime) {
        if (!isAnimating) {
            isAnimating = true;

            // Fade out current content
            AnimatorSet fadeOutSet = new AnimatorSet();
            ObjectAnimator fadeOutButtons = ObjectAnimator.ofFloat(loginButton, "alpha", 1f, 0f);
            ObjectAnimator fadeOutSignup = ObjectAnimator.ofFloat(signupButton, "alpha", 1f, 0f);
            fadeOutSet.playTogether(fadeOutButtons, fadeOutSignup);
            fadeOutSet.setDuration(ANIMATION_DURATION);

            fadeOutSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Update visibility
                    loginButton.setVisibility(View.GONE);
                    signupButton.setVisibility(View.GONE);
                    usernameText.setVisibility(View.VISIBLE);
                    lastLoginText.setVisibility(View.VISIBLE);
                    logoutButton.setVisibility(View.VISIBLE);
                    profileButton.setVisibility(View.VISIBLE);

                    // Update text
                    messageText.setText("Welcome Back!");
                    usernameText.setText(username);
                    updateLastLoginText(loginTime);

                    // Fade in new content
                    AnimatorSet fadeInSet = new AnimatorSet();
                    ObjectAnimator fadeInUsername = ObjectAnimator.ofFloat(usernameText, "alpha", 0f, 1f);
                    ObjectAnimator fadeInLastLogin = ObjectAnimator.ofFloat(lastLoginText, "alpha", 0f, 1f);
                    ObjectAnimator fadeInLogout = ObjectAnimator.ofFloat(logoutButton, "alpha", 0f, 1f);
                    ObjectAnimator fadeInProfile = ObjectAnimator.ofFloat(profileButton, "alpha", 0f, 1f);

                    fadeInSet.playTogether(fadeInUsername, fadeInLastLogin, fadeInLogout, fadeInProfile);
                    fadeInSet.setDuration(ANIMATION_DURATION);
                    fadeInSet.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            isAnimating = false;
                        }
                    });
                    fadeInSet.start();
                }
            });

            fadeOutSet.start();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        // Clear preferences
        prefs.edit().clear().apply();

        // Reset UI with animation
        if (!isAnimating) {
            isAnimating = true;

            AnimatorSet fadeOutSet = new AnimatorSet();
            ObjectAnimator fadeOutUsername = ObjectAnimator.ofFloat(usernameText, "alpha", 1f, 0f);
            ObjectAnimator fadeOutLastLogin = ObjectAnimator.ofFloat(lastLoginText, "alpha", 1f, 0f);
            ObjectAnimator fadeOutLogout = ObjectAnimator.ofFloat(logoutButton, "alpha", 1f, 0f);
            ObjectAnimator fadeOutProfile = ObjectAnimator.ofFloat(profileButton, "alpha", 1f, 0f);

            fadeOutSet.playTogether(fadeOutUsername, fadeOutLastLogin, fadeOutLogout, fadeOutProfile);
            fadeOutSet.setDuration(ANIMATION_DURATION);

            fadeOutSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Update visibility
                    usernameText.setVisibility(View.GONE);
                    lastLoginText.setVisibility(View.GONE);
                    logoutButton.setVisibility(View.GONE);
                    profileButton.setVisibility(View.GONE);
                    loginButton.setVisibility(View.VISIBLE);
                    signupButton.setVisibility(View.VISIBLE);

                    // Update text
                    messageText.setText("Welcome to My App");

                    // Fade in login/signup buttons
                    AnimatorSet fadeInSet = new AnimatorSet();
                    ObjectAnimator fadeInLogin = ObjectAnimator.ofFloat(loginButton, "alpha", 0f, 1f);
                    ObjectAnimator fadeInSignup = ObjectAnimator.ofFloat(signupButton, "alpha", 0f, 1f);

                    fadeInSet.playTogether(fadeInLogin, fadeInSignup);
                    fadeInSet.setDuration(ANIMATION_DURATION);
                    fadeInSet.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            isAnimating = false;
                        }
                    });
                    fadeInSet.start();
                }
            });

            fadeOutSet.start();
        }

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showProfileDialog() {
        String username = usernameText.getText().toString();
        String lastLogin = lastLoginText.getText().toString();

        new AlertDialog.Builder(this)
                .setTitle("User Profile")
                .setMessage("Username: " + username + "\n" + lastLogin)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAppInfo() {
        new AlertDialog.Builder(this)
                .setTitle("About App")
                .setMessage("Enhanced Login Example\nVersion 2.0\nDeveloped with ❤️")
                .setPositiveButton("OK", null)
                .show();
    }

    private void checkPreviousLogin() {
        String savedUsername = prefs.getString(KEY_USERNAME, null);
        long lastLoginTime = prefs.getLong(KEY_LAST_LOGIN, 0);

        if (savedUsername != null) {
            handleLoginSuccess(savedUsername);
        }
    }

    private void updateLastLoginText(long loginTime) {
        String formattedDate = android.text.format.DateFormat.format(
                "MMM dd, yyyy hh:mm a", loginTime).toString();
        lastLoginText.setText("Last login: " + formattedDate);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String username = extras.getString("USERNAME");
                if (username != null && !username.isEmpty()) {
                    handleLoginSuccess(username);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_data) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}