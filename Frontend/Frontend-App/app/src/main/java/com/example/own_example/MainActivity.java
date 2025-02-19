package com.example.own_example;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu/"; // Update with server URL

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
    }

    private void performLogin(String username, String password) {
        String url = BASE_URL + "users/login";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            showSuccess(message);
                            // Handle successful login
                            // You might get a token from response
                            // String token = response.getString("token");
                        } catch (JSONException e) {
                            showError("Error parsing response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showError("Login failed: " + error.getMessage());
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
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
}