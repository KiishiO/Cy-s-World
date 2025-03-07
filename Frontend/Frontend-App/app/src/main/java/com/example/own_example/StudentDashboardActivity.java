package com.example.own_example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private MaterialCardView friendRequestsCard;
    private MaterialCardView classesCard;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize views
        welcomeText = findViewById(R.id.welcome_text);
        friendRequestsCard = findViewById(R.id.friends_request_card);
        classesCard = findViewById(R.id.classes_card);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Get username from SharedPreferences (if available)
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = preferences.getString("username", "Student");
        welcomeText.setText("Welcome, " + username + "!");

        // Set click listener for Friend Requests card
        friendRequestsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Friend Request activity
                Intent intent = new Intent(StudentDashboardActivity.this, FriendsActivity.class); //will come from Jawad's code
                startActivity(intent);
            }
        });

        // Set click listener for Classes card
        classesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Classes activity
                Intent intent = new Intent(StudentDashboardActivity.this, ClassesActivity.class);
                startActivity(intent);
            }
        });

        // Set up bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        // Already on home
                        return true;
                    case R.id.nav_friends:
                        // Navigate to Friends section
                        Intent friendsIntent = new Intent(StudentDashboardActivity.this, FriendsActivity.class);
                        startActivity(friendsIntent);
                        return true;
                    case R.id.nav_classes:
                        // Navigate to Classes section
                        Intent classesIntent = new Intent(StudentDashboardActivity.this, ClassesActivity.class);
                        startActivity(classesIntent);
                        return true;
//                    case R.id.nav_profile:
//                        // Navigate to Profile section
//                        Intent profileIntent = new Intent(StudentDashboardActivity.this, StudentProfileActivity.class);
//                        startActivity(profileIntent);
//                        return true;
                }
                return false;
            }
        });

        // Set up featured cards click listeners
        MaterialCardView firstInfoCard = findViewById(R.id.first_info_card);
        firstInfoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Campus News activity
                Intent intent = new Intent(StudentDashboardActivity.this, CampusNewsActivity.class);
                startActivity(intent);
            }
        });

        MaterialCardView secondInfoCard = findViewById(R.id.second_info_card);
        secondInfoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Study Groups activity
                Intent intent = new Intent(StudentDashboardActivity.this, StudyGroupsActivity.class);
                startActivity(intent);
            }
        });
    }

    // Method to handle back button presses
    @Override
    public void onBackPressed() {
        // Ask if the user really wants to exit the app
        // I may implement a dialog here if needed
        super.onBackPressed();
    }
}