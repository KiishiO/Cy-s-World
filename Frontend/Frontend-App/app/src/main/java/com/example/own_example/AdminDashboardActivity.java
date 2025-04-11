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

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private MaterialCardView classesManagementCard;
    private MaterialCardView eventManagementCard;
    private MaterialCardView diningManagementCard;
    private MaterialCardView busRouteCard;
    private RecyclerView recentActivityRecycler;

    // Sample data structure for recent activities
    private List<RecentActivity> recentActivities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user has admin role
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userRoleStr = prefs.getString("user_role", "STUDENT");

        try {
            UserRoles userRole = UserRoles.valueOf(userRoleStr);
            if (userRole != UserRoles.ADMIN) {
                // User is not an admin, redirect to appropriate dashboard
                Toast.makeText(this, "You don't have permission to access the admin dashboard", Toast.LENGTH_SHORT).show();
                Intent intent;

                if (userRole == UserRoles.TEACHER) {
                    intent = new Intent(this, TeacherDashboardActivity.class);
                } else {
                    intent = new Intent(this, StudentDashboardActivity.class);
                }

                startActivity(intent);
                finish();
                return; // Important to return here to prevent loading the admin layout
            }
        } catch (IllegalArgumentException e) {
            // If role can't be parsed, default to student dashboard
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        welcomeText = findViewById(R.id.welcome_text);
        classesManagementCard = findViewById(R.id.admin_classes_management_card);
        eventManagementCard = findViewById(R.id.admin_event_management_card);
        diningManagementCard = findViewById(R.id.admin_dining_management_card);
        busRouteCard = findViewById(R.id.admin_bus_management_card);
        recentActivityRecycler = findViewById(R.id.recent_activity_recycler);

        // Set up click listeners for action cards
        setupCardClickListeners();

        // Initialize recent activities
        populateRecentActivities();

        // Set up RecyclerView for recent activities
        setupRecentActivitiesRecyclerView();
    }


    private void setupCardClickListeners() {
        classesManagementCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to class management activity
                Intent intent = new Intent(AdminDashboardActivity.this, AdminClassesActivity.class);
                startActivity(intent);
            }
        });

        eventManagementCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to grading activity
                Intent intent = new Intent(AdminDashboardActivity.this, AdminEventsActivity.class);
                startActivity(intent);
            }
        });

        diningManagementCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to attendance activity
                Intent intent = new Intent(AdminDashboardActivity.this, AdminDiningHallActivity.class);
                startActivity(intent);
            }
        });

        busRouteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to office hours activity
                Intent intent = new Intent(AdminDashboardActivity.this, OfficeHoursActivity.class);//need to change this to map to bus management java class
                startActivity(intent);
            }
        });
    }

    private void populateRecentActivities() {
        // Sample data - just a placeholder for now, this would come from the server database or an API
        recentActivities.add(new RecentActivity("Created new Campus Event: Cyclone Cinema - Dune", "Today, 10:30 AM"));
        recentActivities.add(new RecentActivity("Created new class: COM S 288", "Yesterday, 3:45 PM"));
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
                    Toast.makeText(AdminDashboardActivity.this,
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