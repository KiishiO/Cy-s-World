package com.example.own_example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.own_example.BusActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class StudentDashboardActivity extends AppCompatActivity {

    private static final String TAG = "StudentDashboard";
    private TextView welcomeText;
    private MaterialCardView friendRequestsCard;
    private MaterialCardView classesCard;
    private MaterialCardView busTrackerCard;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_student_dashboard);
            Log.d(TAG, "StudentDashboardActivity onCreate");

            // Initialize views with null checks
            welcomeText = findViewById(R.id.welcome_text);
            friendRequestsCard = findViewById(R.id.friends_request_card);
            classesCard = findViewById(R.id.classes_card);
            busTrackerCard = findViewById(R.id.bus_tracker_card);
            bottomNavigationView = findViewById(R.id.bottom_navigation);

            // Make sure bottom navigation isn't null before using it
            if (bottomNavigationView != null) {
                try {
                    bottomNavigationView.inflateMenu(R.menu.student_bottom_navigation_menu);
                } catch (Exception e) {
                    Log.e(TAG, "Error inflating navigation menu: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Bottom navigation view is null!");
            }

            // Set welcome text
            String username = "Student"; // Default value
            try {
                // First try user_prefs
                SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                username = userPrefs.getString("username", "");

                // If empty, try LoginPrefs
                if (username.isEmpty()) {
                    SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    username = loginPrefs.getString("username", "Student");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading SharedPreferences: " + e.getMessage());
            }

            // Set welcome text if view exists
            if (welcomeText != null) {
                welcomeText.setText("Welcome, " + username + "!");
                Log.d(TAG, "Set welcome text for: " + username);
            }

            // Set click listeners for cards (with null checks)
            if (friendRequestsCard != null) {
                friendRequestsCard.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(StudentDashboardActivity.this, FriendsActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to FriendsActivity: " + e.getMessage());
                    }
                });
            }

            if (classesCard != null) {
                classesCard.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(StudentDashboardActivity.this, ClassesActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to ClassesActivity: " + e.getMessage());
                    }
                });
            }

            // Set click listener for bus tracker card
            if (busTrackerCard != null) {
                busTrackerCard.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(StudentDashboardActivity.this, BusActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to BusActivity: " + e.getMessage());
                    }
                });
            }

            // Set up bottom navigation
            if (bottomNavigationView != null) {
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    try {
                        int itemId = item.getItemId();

                        if (itemId == R.id.nav_home) {
                            // Already on home
                            return true;
                        } else if (itemId == R.id.nav_friends) {
                            Intent friendsIntent = new Intent(StudentDashboardActivity.this, FriendsActivity.class);
                            startActivity(friendsIntent);
                            return true;
                        } else if (itemId == R.id.nav_classes) {
                            Intent classesIntent = new Intent(StudentDashboardActivity.this, ClassesActivity.class);
                            startActivity(classesIntent);
                            return true;
                        } else if (itemId == R.id.nav_bus) {
                            Intent busIntent = new Intent(StudentDashboardActivity.this, BusActivity.class);
                            startActivity(busIntent);
                            return true;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in navigation selection: " + e.getMessage());
                    }
                    return false;
                });
            }

            // Set up featured cards (with null checks)
            MaterialCardView firstInfoCard = findViewById(R.id.first_info_card);
            if (firstInfoCard != null) {
                firstInfoCard.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(StudentDashboardActivity.this, CampusEventsActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to CampusNewsActivity: " + e.getMessage());
                    }
                });
            }

            MaterialCardView secondInfoCard = findViewById(R.id.second_info_card);
            if (secondInfoCard != null) {
                secondInfoCard.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(StudentDashboardActivity.this, StudyGroupsActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to StudyGroupsActivity: " + e.getMessage());
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Fatal error in onCreate: " + e.getMessage(), e);
            // If we can't even load the layout, return to MainActivity
            try {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e2) {
                Log.e(TAG, "Could not even navigate back to MainActivity: " + e2.getMessage());
            }
        }
    }

    // Method to handle back button presses
    @Override
    public void onBackPressed() {
        // Don't allow going back to login screen
        moveTaskToBack(true);
    }
}