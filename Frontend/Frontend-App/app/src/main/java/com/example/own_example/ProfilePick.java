package com.example.own_example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfilePick extends AppCompatActivity {

    private MaterialCardView studentCard;
    private MaterialCardView teacherCard;
    private OkHttpClient client;
    private static final String API_URL = "http://coms-3090-017.class.las.iastate.edu:8080/role"; // Replace with actual API URL

    // Simple enum for user roles
    public enum UserRole {
        STUDENT,
        TEACHER
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_landing);

        // Initialize HTTP client
        client = new OkHttpClient();

        // Find views
        studentCard = findViewById(R.id.studentCard);
        teacherCard = findViewById(R.id.teacherCard);

        // Set click listeners
        studentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRole(UserRole.STUDENT);
            }
        });

        teacherCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRole(UserRole.TEACHER);
            }
        });
    }

    private void selectRole(UserRole role) {
        // Show loading state
        setCardsEnabled(false);

        // Make API call to update role
        makeApiCallToUpdateRole(role);
    }

    private void makeApiCallToUpdateRole(final UserRole role) {
        try {
            // Create JSON payload
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("role", role.name().toLowerCase());

            // Create request body
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonObject.toString()
            );

            // Build the request
            Request request = new Request.Builder()
                    .url(API_URL)
                    .put(body)
                    .build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Handle network failure on UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProfilePick.this,
                                    "Network error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            setCardsEnabled(true);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    // Handle response on UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (response.isSuccessful()) {
                                    // Success - save role and navigate
                                    saveUserRoleLocally(role);
                                    navigateToNextScreen(role);
                                } else {
                                    // API returned an error
                                    Toast.makeText(ProfilePick.this,
                                            "Server error: " + response.code() + " " + response.message(),
                                            Toast.LENGTH_SHORT).show();
                                    setCardsEnabled(true);
                                }
                            } catch (Exception e) {
                                Toast.makeText(ProfilePick.this,
                                        "Error processing response: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                setCardsEnabled(true);
                            } finally {
                                response.close();
                            }
                        }
                    });
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            setCardsEnabled(true);
        }
    }

    private void saveUserRoleLocally(UserRole role) {
        // Use SharedPreferences to save the user role
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putString("user_role", role.name())
                .apply();
    }

    private void navigateToNextScreen(UserRole role) {
        Intent intent;

        if (role == UserRole.STUDENT) {
            // Create an intent for the student dashboard
            // Replace StudentDashboardActivity with your actual activity name
            intent = new Intent(this, StudentDashboardActivity.class);
        } else {
            // Create an intent for the teacher dashboard
            // Replace TeacherDashboardActivity with your actual activity name
            intent = new Intent(this, TeacherDashboardActivity.class);
        }

        // Pass the role as an extra if needed
        intent.putExtra("USER_ROLE", role.name());

        startActivity(intent);
        finish(); // Optional: close this activity so user can't go back
    }

    private void setCardsEnabled(boolean enabled) {
        studentCard.setEnabled(enabled);
        teacherCard.setEnabled(enabled);

        // Optionally change appearance to indicate disabled state
        float alpha = enabled ? 1.0f : 0.5f;
        studentCard.setAlpha(alpha);
        teacherCard.setAlpha(alpha);
    }
}