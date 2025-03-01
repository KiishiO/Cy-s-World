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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
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

public class SignUpActivity extends AppCompatActivity{

    private ProgressBar passwordStrengthBar;
    private CircularProgressIndicator loadingProgress;
    //private MaterialCheckBox rememberMeCheckbox;
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/signup";
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
        MaterialButton btnNext = findViewById(R.id.signUp_btnNext);
        TextView tvAlreadyHaveAccount = findViewById(R.id.signUp_tvAlreadyHaveAccount);
        ImageView logo = findViewById(R.id.signUp_ivLogo);

        passwordStrengthBar = findViewById(R.id.signUp_passwordStrengthBar);
        loadingProgress = findViewById(R.id.signUp_loadingProgress);
        //rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

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
            } else if (isPasswordMatch(password, passwordConfirmed) == false){
                showError("Passwords do not match");
            } else{
                    showSignUpAnimation(btnNext);
                    performSignUp(username, password, email);
            }
        });

        // Add click listener for forgot password
        tvAlreadyHaveAccount.setOnClickListener(v -> {

            /* when pressed, use intent to switch to Login Activity */
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);

            //String username = etUsername.getText().toString();
            //if (username.isEmpty()) {
              //  showError("Please enter your Net-ID first");
                //shakeView(etUsername);
            //} else {
            //    sendPasswordResetRequest(username);
            //}
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

    private Boolean isPasswordMatch(String password, String confirm){
        if(password.equals(confirm)){
            return true;
        }
        return false;
    }

    private void performSignUp(String username, String password, String email) {
        String url = BASE_URL;
        Log.d("Sign Up", "Attempting sign up to: " + url);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            jsonBody.put("email", email);

            Log.d("Sign Up", "Sending data: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        Log.d("Sign Up", "Success response: " + response.toString());
                        try {
                            String message = response.getString("message");
                            showSuccess(message);
                        } catch (JSONException e) {
                            showError("Error parsing response");
                        }
                    },
                    error -> {
                        Log.e("Sign Up", "Error: " + error.toString());
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String errorResponse = new String(networkResponse.data);
                            Log.e("Sign Up", "Error response: " + errorResponse);
                        }
                        showError("Sign up failed: " + getVolleyErrorMessage(error));
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

    private void showSignUpAnimation(MaterialButton button) {
        button.setEnabled(false);
        button.setText("");
        loadingProgress.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadingProgress.setVisibility(View.GONE);
            button.setText("Next");
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
