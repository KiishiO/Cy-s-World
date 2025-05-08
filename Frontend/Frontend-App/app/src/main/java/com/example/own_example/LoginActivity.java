package com.example.own_example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.own_example.services.UserService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ProgressBar passwordStrengthBar;
    private CircularProgressIndicator loadingProgress;
    private MaterialCheckBox rememberMeCheckbox;
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/Logins";
    private UserRoles userRole = UserRoles.STUDENT; // Default value
    private List<JSONObject> usersList = new ArrayList<>();
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "Starting login activity...");

        // Initialize UserService
        UserService.getInstance().initialize(getApplicationContext());

        // Initialize views
        MaterialCardView loginCard = findViewById(R.id.loginCard);
        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnSignIn);
        MaterialButton btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
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

        // Delete Account button click listener
        btnDeleteAccount.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                showError("Please enter your credentials first");
                shakeView(username.isEmpty() ? etUsername : etPassword);
            } else {
                // First authenticate the user
                authenticateAndDeleteUser(username, password);
            }
        });

        // Forgot password click listener
        tvForgotPassword.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            if (username.isEmpty()) {
                showError("Please enter your Net-ID first");
                shakeView(etUsername);
            } else {
                showChangePasswordDialog(username);
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
        String userId = "";
        UserRoles userRole = UserRoles.STUDENT; // Default role

        for (JSONObject user : usersList) {
            try {
                // Log the entire user object to see what we're working with
                Log.d(TAG, "Examining user: " + user.toString());

                // Check for matching emailId and password
                String storedNetId = user.getString("emailId").trim();
                String storedPassword = user.getString("password");
                String storedName = user.getString("name").trim();
                String storedId = user.getString("id");

                // Extract role directly from the user object
                String roleString = "STUDENT"; // Default
                if (user.has("role")) {
                    roleString = user.getString("role").trim().toUpperCase();
                    Log.d(TAG, "Found role in user object: " + roleString);
                }

                // Try matching by email or name
                boolean credentialsMatch =
                        (storedNetId.equalsIgnoreCase(username.trim()) ||
                                storedName.equalsIgnoreCase(username.trim())) &&
                                storedPassword.equals(password);

                if (credentialsMatch) {
                    loginSuccess = true;
                    userId = storedId;

                    // Convert role string to enum
                    try {
                        userRole = UserRoles.valueOf(roleString);
                        Log.d(TAG, "Successfully converted role to enum: " + userRole);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Invalid role format: " + roleString + ". Using default STUDENT role.");
                        userRole = UserRoles.STUDENT;
                    }

                    Log.d(TAG, "Login match found for user: " + storedName + " with role: " + userRole);

                    if (user.has("person")) {
                        fullName = user.getJSONObject("person").getString("name");
                        Log.d(TAG, "Found person name: " + fullName);

                        // Double-check if role exists in person object
                        if (user.getJSONObject("person").has("role")) {
                            String personRole = user.getJSONObject("person").getString("role").trim().toUpperCase();
                            Log.d(TAG, "Found role in person object: " + personRole);

                            // Use person role if available
                            try {
                                userRole = UserRoles.valueOf(personRole);
                                Log.d(TAG, "Using role from person object: " + userRole);
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "Invalid person role format: " + personRole + ". Keeping previously found role.");
                            }
                        }
                    } else {
                        fullName = storedName;
                    }
                    break;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error checking user credentials", e);
            }
        }

        if (loginSuccess) {
            final String welcomeName = fullName.isEmpty() ? username : fullName;
            final String finalUserId = userId;
            final UserRoles finalUserRole = userRole;

            Log.d(TAG, "Login successful for user: " + welcomeName + " with ID: " + finalUserId + " and role: " + finalUserRole);
            showSuccess("Login successful! Welcome, " + welcomeName);

            // Save user data to both SharedPreferences
            // Save to user_prefs for StudentDashboardActivity
            SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor userEditor = userPrefs.edit();
            userEditor.putString("username", welcomeName);
            userEditor.putLong("user_id", Long.parseLong(finalUserId));
            userEditor.putString("user_role", finalUserRole.toString());
            userEditor.putBoolean("is_logged_in", true);
            userEditor.apply();

            // Save to LoginPrefs for other activities
            SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor loginEditor = loginPrefs.edit();
            loginEditor.putString("username", welcomeName);
            loginEditor.putLong("user_id", Long.parseLong(finalUserId));
            loginEditor.putString("user_role", finalUserRole.toString());
            loginEditor.putBoolean("is_logged_in", true);

            // Save the remember me preference if checkbox is checked
            if (rememberMeCheckbox != null && rememberMeCheckbox.isChecked()) {
                loginEditor.putBoolean("remember_me", true);
            }

            loginEditor.apply();

            // Navigate to appropriate dashboard based on role
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent;

                // Check role and navigate accordingly
                switch (finalUserRole) {
                    case ADMIN:
                        intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        Log.d(TAG, "Navigating to Admin Dashboard");
                        break;
                    case TEACHER:
                        intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
                        Log.d(TAG, "Navigating to Teacher Dashboard");
                        break;
                    case STUDENT:
                    default:
                        intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                        Log.d(TAG, "Navigating to Student Dashboard");
                        break;
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Close login activity
            }, 1000); // Short delay for success message to be visible
        } else {
            Log.d(TAG, "Login failed - invalid credentials");
            showError("Invalid username or password. Please check your credentials.");
        }
    }

    private void authenticateAndDeleteUser(String username, String password) {
        // First we need to find the user ID by authenticating
        for (JSONObject user : usersList) {
            try {
                String storedNetId = user.getString("emailId").trim();
                String storedName = user.getString("name").trim();
                String storedPassword = user.getString("password");

                boolean credentialsMatch =
                        (storedNetId.equalsIgnoreCase(username.trim()) ||
                                storedName.equalsIgnoreCase(username.trim())) &&
                                storedPassword.equals(password);

                if (credentialsMatch) {
                    // Found the user, get the ID
                    String userId = user.getString("id");

                    // Show confirmation dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Account")
                            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                            .setPositiveButton("Delete", (dialog, which) -> deleteCurrentUser(userId))
                            .setNegativeButton("Cancel", null)
                            .show();

                    return;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error checking user credentials", e);
            }
        }

        // If we got here, authentication failed
        showError("Invalid username or password");
    }

    private void deleteCurrentUser(String userId) {
        // Show loading indicator
        loadingProgress.setVisibility(View.VISIBLE);

        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                String deleteUrl = BASE_URL + "/" + userId;
                Log.d(TAG, "Attempting to delete user at: " + deleteUrl);

                URL requestUrl = new URL(deleteUrl);
                urlConnection = (HttpURLConnection) requestUrl.openConnection();
                urlConnection.setRequestMethod("DELETE");

                // Get the response code
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Delete response code: " + responseCode);

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
                    Log.d(TAG, "Delete response: " + response.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Error reading response", e);
                }

                // Update UI on main thread
                final int finalResponseCode = responseCode;
                final String finalResponse = response.toString();

                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (finalResponseCode >= 200 && finalResponseCode < 300) {
                        showSuccess("Account deleted successfully");

                        // Refresh the user list
                        fetchAllUsers();
                    } else {
                        String errorMsg = "Failed to delete account: " + finalResponseCode;
                        if (!finalResponse.isEmpty()) {
                            errorMsg += " - " + finalResponse;
                        }
                        showError(errorMsg);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error during account deletion", e);
                final String errorMessage = e.getMessage();

                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    showError("Failed to delete account: " + errorMessage);
                });
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
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

    private void showChangePasswordDialog(String username) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        ProgressBar passwordStrengthDialogBar = dialogView.findViewById(R.id.passwordStrengthDialogBar);
        passwordStrengthDialogBar.setVisibility(View.GONE);

        // Add TextWatcher to new password field
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                if (password.isEmpty()) {
                    passwordStrengthDialogBar.setVisibility(View.GONE);
                } else {
                    passwordStrengthDialogBar.setVisibility(View.VISIBLE);
                    int strength = calculatePasswordStrength(password);
                    passwordStrengthDialogBar.setProgress(strength);

                    int color;
                    if (strength < 33) {
                        color = Color.RED;
                    } else if (strength < 66) {
                        color = Color.YELLOW;
                    } else {
                        color = Color.GREEN;
                    }
                    passwordStrengthDialogBar.setProgressTintList(ColorStateList.valueOf(color));
                }
            }
        });

        // Create themed alert dialog that matches the app's dark theme
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update", null) // Set null to prevent auto-dismiss
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .create();

        // Style dialog to match app theme
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.cardinal_red));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.white));
        });

        dialog.show();

        // Override the positive button to handle password validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showError("Please fill in all password fields");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showError("New passwords don't match");
                return;
            }

            // Validate current password
            boolean isCurrentPasswordValid = validateCurrentPassword(username, currentPassword);
            if (!isCurrentPasswordValid) {
                showError("Current password is incorrect");
                return;
            }

            // Password strength check
            int strength = calculatePasswordStrength(newPassword);
            if (strength < 60) {
                showError("New password is too weak. Include uppercase, lowercase, numbers, and special characters.");
                return;
            }

            // All validations passed, update the password
            updatePassword(username, newPassword, dialog);
        });
    }

    private boolean validateCurrentPassword(String username, String password) {
        for (JSONObject user : usersList) {
            try {
                String storedNetId = user.getString("emailId").trim();
                String storedName = user.getString("name").trim();
                String storedPassword = user.getString("password");

                // Check if username matches either email or name, and password matches
                if ((storedNetId.equalsIgnoreCase(username.trim()) ||
                        storedName.equalsIgnoreCase(username.trim())) &&
                        storedPassword.equals(password)) {
                    return true;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error validating current password", e);
            }
        }
        return false;
    }

    private void updatePassword(String username, String newPassword, AlertDialog dialog) {
        // Find the user ID to update
        String userId = null;
        JSONObject userToUpdate = null;

        for (JSONObject user : usersList) {
            try {
                String storedNetId = user.getString("emailId").trim();
                String storedName = user.getString("name").trim();

                if (storedNetId.equalsIgnoreCase(username.trim()) ||
                        storedName.equalsIgnoreCase(username.trim())) {
                    userId = user.getString("id");
                    userToUpdate = user;
                    break;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error finding user ID", e);
            }
        }

        if (userId == null || userToUpdate == null) {
            showError("User not found. Please try again.");
            return;
        }

        // Show loading indicator
        loadingProgress.setVisibility(View.VISIBLE);

        try {
            // Create a copy of the user object with the updated password
            JSONObject updatedUser = new JSONObject(userToUpdate.toString());
            updatedUser.put("password", newPassword);

            // Prepare the API endpoint URL for the specific user
            String updateUrl = BASE_URL + "/" + userId;
            Log.d(TAG, "Updating password at URL: " + updateUrl);

            // Make the PUT request to update the password
            JsonObjectRequest updateRequest = new JsonObjectRequest(
                    Request.Method.PUT,
                    updateUrl,
                    updatedUser,
                    response -> {
                        loadingProgress.setVisibility(View.GONE);
                        Log.d(TAG, "Password updated successfully: " + response.toString());
                        showSuccess("Password updated successfully!");
                        dialog.dismiss();

                        // Refresh the user list to get updated data
                        fetchAllUsers();
                    },
                    error -> {
                        loadingProgress.setVisibility(View.GONE);
                        Log.e(TAG, "Error updating password: " + error.toString());

                        if (error.networkResponse != null) {
                            Log.e(TAG, "Network error code: " + error.networkResponse.statusCode);
                        }

                        showError("Failed to update password. Please try again later.");
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            updateRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000, // 10 seconds timeout
                    1,     // 1 retry
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(this).addToRequestQueue(updateRequest);

        } catch (JSONException e) {
            loadingProgress.setVisibility(View.GONE);
            Log.e(TAG, "Error preparing user data for update", e);
            showError("An error occurred. Please try again.");
        }
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