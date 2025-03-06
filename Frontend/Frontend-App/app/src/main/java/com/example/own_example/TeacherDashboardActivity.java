package com.example.own_example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
        gradingCard = findViewById(R.id.grading_card);
        attendanceCard = findViewById(R.id.attendance_card);
        officeHoursCard = findViewByI
