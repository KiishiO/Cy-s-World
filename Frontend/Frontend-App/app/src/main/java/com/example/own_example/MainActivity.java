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
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ProgressBar passwordStrengthBar;
    private CircularProgressIndicator loadingProgress;
    private MaterialCheckBox rememberMeCheckbox;
    private static final String BASE_URL = "http://localhost:8080/new";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        MaterialCardView loginCard = findViewById(R.id.loginCard);
        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        ImageView logo = findViewById(R.id.ivLogo);

        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        loadingProgress = findViewById(R.id.loadingProgress);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        // Set initial states
        loginCard.setTranslationY(1000f);
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

        // Add click listener for sign in button
        btnSignIn.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                showError("Please fill in all fields");
                shakeView(username.isEmpty() ? etUsername : etPassword);
            } else {
                showLoginAnimation(btnSignIn);
                performLogin(username, password);
            }
        });

        // Add click listener for forgot password
        tvForgotPassword.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            if (username.isEmpty()) {
                showError("Please enter your Net-ID first");
                shakeView(etUsername);
            } else {
                sendPasswordResetRequest(username);
            }
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

    private void performLogin(String username, String password) {
        String url = BASE_URL + "users/login";
        Log.d("Login", "Attempting login to: " + url);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);

            Log.d("Login", "Sending data: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        Log.d("Login", "Success response: " + response.toString());
                        try {
                            String message = response.getString("message");
                            showSuccess(message);
                        } catch (JSONException e) {
                            showError("Error parsing response");
                        }
                    },
                    error -> {
                        Log.e("Login", "Error: " + error.toString());
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String errorResponse = new String(networkResponse.data);
                            Log.e("Login", "Error response: " + errorResponse);
                        }
                        showError("Login failed: " + getVolleyErrorMessage(error));
                    }
            );

            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000, // 10 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (JSONException e) {
            e.printStackTrace();
            showError("Error creating request");
        }
    }

    private void sendPasswordResetRequest(String username) {
        String url = BASE_URL + "users/forgot-password";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> showSuccess("Password reset instructions sent"),
                    error -> showError("Failed to send reset instructions: " + getVolleyErrorMessage(error))
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (JSONException e) {
            e.printStackTrace();
            showError("Error processing request");
        }
    }

    private String getVolleyErrorMessage(VolleyError error) {
        if (error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case 404: return "Server not found";
                case 401: return "Invalid credentials";
                case 400: return "Invalid request";
                case 500: return "Server error";
                default: return "Network error " + error.networkResponse.statusCode;
            }
        }
        return error.getMessage() != null ? error.getMessage() : "Unknown error occurred";
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
        button.setEnabled(false);
        button.setText("");
        loadingProgress.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadingProgress.setVisibility(View.GONE);
            button.setText("Sign In");
            button.setEnabled(true);
        }, 2000);
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