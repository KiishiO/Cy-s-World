package com.example.own_example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private MaterialCardView classesManagementCard;
    private MaterialCardView gradingCard;
    private MaterialCardView attendanceCard;
    private MaterialCardView officeHoursCard;
    private RecyclerView recentActivityRecycler;
    private BottomNavigationView bottomNavigationView;

    // Sample data structure for recent activities
    private List<RecentActivity> recentActivities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialize views
        welcomeText = findViewById(R.id.welcome_text);
        classesManagementCard = findViewById(R.id.classes_management_card);
        officeHoursCard = findViewById(R.id.office_hours_card);
        recentActivityRecycler = findViewById(R.id.recent_activity_recycler);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up teacher name from SharedPreferences (assuming you have stored it)
        setupTeacherName();

        // Set up click listeners for action cards
        setupCardClickListeners();

        // Set up bottom navigation
        setupBottomNavigation();

        // Initialize recent activities
        populateRecentActivities();

        // Set up RecyclerView for recent activities
        setupRecentActivitiesRecyclerView();
    }

    private void setupTeacherName() {
        SharedPreferences sharedPreferences = getSharedPreferences("TeacherPrefs", MODE_PRIVATE);
        String teacherName = sharedPreferences.getString("teacherName", "");
        if (!teacherName.isEmpty()) {
            welcomeText.setText("Welcome, Professor " + teacherName);
        }
    }

    private void setupCardClickListeners() {
        classesManagementCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to class management activity
                Intent intent = new Intent(TeacherDashboardActivity.this, ClassManagementActivity.class);
                startActivity(intent);
            }
        });

        officeHoursCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to office hours activity
                Intent intent = new Intent(TeacherDashboardActivity.this, OfficeHoursActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Handle navigation item clicks
                if (itemId == R.id.nav_home) {
                    // Already on dashboard, do nothing
                    return true;
                } else if (itemId == R.id.nav_dining) {
                    Intent intent = new Intent(TeacherDashboardActivity.this, DiningHallActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_buses) {
                    Intent intent = new Intent(TeacherDashboardActivity.this, BusActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void populateRecentActivities() {
        // Sample data - just a placeholder for now, this would come from the server database or an API
        recentActivities.add(new RecentActivity("Graded 10 assignments for CS101", "Today, 10:30 AM"));
        recentActivities.add(new RecentActivity("Posted new syllabus for CS205", "Yesterday, 3:45 PM"));
        recentActivities.add(new RecentActivity("Updated office hours schedule", "Yesterday, 1:15 PM"));
        recentActivities.add(new RecentActivity("Uploaded lecture notes for CS350", "Mar 4, 2025, 9:20 AM"));
        recentActivities.add(new RecentActivity("Marked attendance for CS101", "Mar 3, 2025, 11:00 AM"));
    }

    private void setupRecentActivitiesRecyclerView() {
        RecentActivityAdapter adapter = new RecentActivityAdapter(recentActivities);
        recentActivityRecycler.setLayoutManager(new LinearLayoutManager(this));
        recentActivityRecycler.setAdapter(adapter);
    }

    // Inner class to represent a recent activity
    public static class RecentActivity {
        private String activityTitle;
        private String activityTime;

        public RecentActivity(String activityTitle, String activityTime) {
            this.activityTitle = activityTitle;
            this.activityTime = activityTime;
        }

        public String getActivityTitle() {
            return activityTitle;
        }

        public String getActivityTime() {
            return activityTime;
        }
    }

    // Adapter for the RecentActivity RecyclerView
    private class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
        private List<RecentActivity> activities;

        public RecentActivityAdapter(List<RecentActivity> activities) {
            this.activities = activities;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_recent_activity, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecentActivity activity = activities.get(position);
            holder.titleTextView.setText(activity.getActivityTitle());
            holder.timeTextView.setText(activity.getActivityTime());

            // Set click listener for the item
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle item click - perhaps navigate to a detail view
                    Toast.makeText(TeacherDashboardActivity.this,
                            "Selected: " + activity.getActivityTitle(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return activities.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView timeTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.activity_title);
                timeTextView = itemView.findViewById(R.id.activity_timestamp);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        populateRecentActivities();
        setupRecentActivitiesRecyclerView();
    }
}