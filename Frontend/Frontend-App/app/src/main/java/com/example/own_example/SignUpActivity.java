package com.example.own_example;

import android.content.Intent;
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

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private ProgressBar passwordStrengthBar;
    private CircularProgressIndicator loadingProgress;
    private MaterialButton btnNext;

    // Let's try the Login endpoint as a last resort
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/Logins/new";
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        MaterialCardView signUpCard = findViewById(R.id.signUpCard);
        TextInputEditText etUsername = findViewById(R.id.signUp_etUsername);
        TextInputEditText etConfirmPassword = findViewById(R.id.signUp_et_Confirm_Password);
        TextInputEditText etPassword = findViewById((R.id.signUp_etPassword));
        TextInputEditText etEmail = findViewById(R.id.signUp_etEmail);
        btnNext = findViewById(R.id.signUp_btnNext);
        TextView tvAlreadyHaveAccount = findViewById(R.id.signUp_tvAlreadyHaveAccount);
        ImageView logo = findViewById(R.id.signUp_ivLogo);

        passwordStrengthBar = findViewById(R.id.signUp_passwordStrengthBar);
        loadingProgress = findViewById(R.id.signUp_loadingProgress);

        // Set initial states
        signUpCard.setTranslationY(1000f);
        logo.setScaleX(0f);
        logo.setScaleY(0f);

        // Animate logo
        logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() ->
                        // Animate login card after logo animation
                        signUpCard.animate()
                                .translationY(0f)
                                .setDuration(1000)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                );

        // Password strength watcher
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
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

        // Add click listener for next button
        btnNext.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            String passwordConfirmed = etConfirmPassword.getText().toString();
            String email = etEmail.getText().toString();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || passwordConfirmed.isEmpty()) {
                showError("Please fill in all fields");
                shakeView(username.isEmpty() ? etUsername : etPassword);
            } else if (!isPasswordMatch(password, passwordConfirmed)) {
                showError("Passwords do not match");
            } else {
                showSignUpAnimation();
                performSignUp(username, password, email);
            }
        });

        // Add click listener for already have account
        tvAlreadyHaveAccount.setOnClickListener(v -> {
            /* when pressed, use intent to switch to Login Activity */
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
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

    private Boolean isPasswordMatch(String password, String confirm) {
        return password.equals(confirm);
    }

    private void performSignUp(String username, String password, String email) {
        // Use a background thread for network operations
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                JSONObject jsonBody = new JSONObject();
                // Using Login endpoint field names since we're using that endpoint
                jsonBody.put("name", username);
                jsonBody.put("password", password);
                jsonBody.put("emailId", email);
                // Add ifActive field which may be required
                jsonBody.put("ifActive", true);

                Log.d(TAG, "Sending data to " + BASE_URL + ": " + jsonBody.toString());

                URL requestUrl = new URL(BASE_URL);
                urlConnection = (HttpURLConnection) requestUrl.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                // Write the JSON to the request body
                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonBody.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Get the response code
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                // Read response
                StringBuilder response = new StringBuilder();
                try {
                    InputStream is;
                    if (responseCode >= 200 && responseCode < 400) {
                        is = urlConnection.getInputStream();
                    } else {
                        is = urlConnection.getErrorStream();
                    }

                    if (is != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        br.close();
                    }
                    Log.d(TAG, "Response: " + response.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Error reading response", e);
                }

                // Update UI on main thread
                final String finalResponse = response.toString();
                final int finalResponseCode = responseCode;

                runOnUiThread(() -> {
                    if (finalResponseCode >= 200 && finalResponseCode < 300) {
                        showSuccess("Account created successfully");

                        // Navigate to login screen after successful signup
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // Close this activity
                        }, 1500);
                    } else {
                        resetSignUpAnimation();
                        String errorMsg = "Sign up failed with code " + finalResponseCode;
                        if (!finalResponse.isEmpty()) {
                            errorMsg += ": " + finalResponse;
                        }
                        showError(errorMsg);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error during signup", e);
                final String errorMessage = e.getMessage();

                runOnUiThread(() -> {
                    resetSignUpAnimation();
                    showError("Sign up failed: " + errorMessage);
                });
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
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

    private void showSignUpAnimation() {
        btnNext.setEnabled(false);
        btnNext.setText("Creating Account...");
        loadingProgress.setVisibility(View.VISIBLE);
    }

    private void resetSignUpAnimation() {
        loadingProgress.setVisibility(View.GONE);
        btnNext.setText("Next");
        btnNext.setEnabled(true);
    }

    private void showError(String message) {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG
        );
        snackbar.setBackgroundTint(getColor(R.color.cardinal_red));
        snackbar.show();
    }

    private void showSuccess(String message) {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG
        );
        snackbar.setBackgroundTint(Color.parseColor("#4CAF50"));
        snackbar.show();
    }

    private void showInfo(String message) {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG
        );
        snackbar.setBackgroundTint(Color.parseColor("#2196F3"));
        snackbar.show();
    }
}
