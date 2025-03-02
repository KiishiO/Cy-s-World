package com.example.own_example;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ProgressBar passwordStrengthBar;
    private CircularProgressIndicator loadingProgress;
    private MaterialCheckBox rememberMeCheckbox;
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/Logins";

    private List<JSONObject> usersList = new ArrayList<>();
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "Starting login activity...");

        // Initialize views
        MaterialCardView loginCard = findViewById(R.id.loginCard);
        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnSignIn);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        ImageView logo = findViewById(R.id.ivLogo);

        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        loadingProgress = findViewById(R.id.loadingProgress);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        // Make sure loading indicator is properly initialized
        if (loadingProgress != null) {
            loadingProgress.setVisibility(View.GONE);
            Log.d(TAG, "Loading indicator initialized and set to GONE");
        } else {
            Log.e(TAG, "Loading indicator is null! Check your layout XML");
        }

        // Change button text
        btnLogin.setText("Login");

        // Set initial animations
        loginCard.setTranslationY(1000f);
        logo.setScaleX(0f);
        logo.setScaleY(0f);

        logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() ->
                        loginCard.animate()
                                .translationY(0f)
                                .setDuration(1000)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                );

        // Password strength watcher
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                if (password.isEmpty()) {
                    passwordStrengthBar.setVisibility(View.GONE);
                } else {
                    passwordStrengthBar.setVisibility(View.VISIBLE);
                    updatePasswordStrength(password);
                }
            }
        });

        // Fetch users data on startup
        fetchAllUsers();

        // Login button click listener
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                showError("Please fill in all fields");
                shakeView(username.isEmpty() ? etUsername : etPassword);
            } else {
                showLoginAnimation(btnLogin);
                if (isDataLoaded) {
                    validateLogin(username, password);
                } else {
                    fetchAllUsers(username, password);
                }
            }
        });

        // Forgot password click listener
        tvForgotPassword.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            if (username.isEmpty()) {
                showError("Please enter your Net-ID first");
                shakeView(etUsername);
            } else {
                showSuccess("Password reset instructions sent");
            }
        });
    }

    private void fetchAllUsers() {
        fetchAllUsers(null, null);
    }

    private void fetchAllUsers(String username, String password) {
        showInfo("Connecting to server...");
        Log.d(TAG, "Fetching users from: " + BASE_URL);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    Log.d(TAG, "Users response received, length: " + response.length());
                    usersList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject user = response.getJSONObject(i);
                            usersList.add(user);

                            // Log user data to Logcat
                            String name = user.optString("name", "N/A");
                            String email = user.optString("emailId", "N/A");
                            String pass = user.optString("password", "N/A");
                            Log.d(TAG, "User " + (i+1) + ": Name=" + name + ", Email=" + email + ", Pass=" + pass);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing user at index " + i, e);
                        }
                    }

                    isDataLoaded = true;
                    Log.d(TAG, "Successfully loaded " + usersList.size() + " users");
                    showInfo("Connected to server");

                    if (username != null && password != null) {
                        validateLogin(username, password);
                    }
                },
                error -> {
                    isDataLoaded = false;
                    Log.e(TAG, "Error fetching users: " + error.toString());

                    if (error.networkResponse != null) {
                        Log.e(TAG, "Network error code: " + error.networkResponse.statusCode);
                    }

                    showError("Cannot connect to server. Please check your internet connection.");
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                1,     // 1 retry
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void validateLogin(String username, String password) {
        Log.d(TAG, "Validating login for username: " + username);
        boolean loginSuccess = false;
        String fullName = "";

        for (JSONObject user : usersList) {
            try {
                // Check for matching emailId and password
                String storedNetId = user.getString("emailId").trim();
                String storedPassword = user.getString("password");
                String storedName = user.getString("name").trim();

                Log.d(TAG, "Comparing with user: " + storedName + ", email: " + storedNetId);

                // Try matching by email or name
                boolean credentialsMatch =
                        (storedNetId.equalsIgnoreCase(username.trim()) ||
                                storedName.equalsIgnoreCase(username.trim())) &&
                                storedPassword.equals(password);

                if (credentialsMatch) {
                    loginSuccess = true;
                    Log.d(TAG, "Login match found for user: " + storedName);
                    if (user.has("person")) {
                        fullName = user.getJSONObject("person").getString("name");
                        Log.d(TAG, "Found person name: " + fullName);
                    }
                    break;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error checking user credentials", e);
            }
        }

        if (loginSuccess) {
            final String welcomeName = fullName.isEmpty() ? username : fullName;
            Log.d(TAG, "Login successful for user: " + welcomeName);
            showSuccess("Login successful! Welcome, " + welcomeName);
            // TODO: Navigate to main activity
        } else {
            Log.d(TAG, "Login failed - invalid credentials");
            showError("Invalid username or password. Please check your credentials.");
        }
    }

    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);
        passwordStrengthBar.setProgress(strength);

        int color;
        if (strength < 33) {
            color = Color.RED;
        } else if (strength < 66) {
            color = Color.YELLOW;
        } else {
            color = Color.GREEN;
        }
        passwordStrengthBar.setProgressTintList(ColorStateList.valueOf(color));
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score += 20;
        if (password.matches(".*[A-Z].*")) score += 20;
        if (password.matches(".*[a-z].*")) score += 20;
        if (password.matches(".*\\d.*")) score += 20;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 20;
        return score;
    }

    private void shakeView(View view) {
        view.animate()
                .translationX(20f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .translationX(-20f)
                                .setDuration(100)
                                .withEndAction(() ->
                                        view.animate()
                                                .translationX(0f)
                                                .setDuration(100)
                                )
                );
    }

    private void showLoginAnimation(MaterialButton button) {
        Log.d(TAG, "Starting login animation");

        // Save original text to restore later
        final String originalText = button.getText().toString();

        // Disable button and show logging in text
        button.setEnabled(false);
        button.setText("Logging in...");

        // Show loading indicator if available
        if (loadingProgress != null) {
            loadingProgress.setVisibility(View.VISIBLE);
            Log.d(TAG, "Loading indicator set to VISIBLE");
        }

        // After delay, restore button state
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (loadingProgress != null) {
                loadingProgress.setVisibility(View.GONE);
                Log.d(TAG, "Loading indicator set back to GONE");
            }
            button.setText("Login");
            button.setEnabled(true);
            Log.d(TAG, "Login animation completed");
        }, 2000);
    }

    private void showError(String message) {
        Log.e(TAG, "Error: " + message);
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.cardinal_red))
                .show();
    }

    private void showSuccess(String message) {
        Log.d(TAG, "Success: " + message);
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.parseColor("#4CAF50"))
                .show();
    }

    private void showInfo(String message) {
        Log.i(TAG, "Info: " + message);
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.parseColor("#2196F3"))
                .show();
    }
}