package com.example.own_example;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.StudentsAdapter;
import com.example.own_example.services.ClassesService;
import com.example.own_example.models.Student;
import com.example.own_example.models.ClassModel;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying class details
 */
public class ClassDetailActivity extends AppCompatActivity implements StudentsAdapter.OnGradeUpdatedListener {
    private static final String TAG = "ClassDetailActivity";

    private TextView classDetailHeader;
    private TextView teacherName;
    private TextView scheduleDetails;
    private TextView locationDetails;
    private RecyclerView studentsRecyclerView;
    private MaterialCardView studentsCard;
    private ProgressBar loadingIndicator;

    private ClassesService apiService;
    private StudentsAdapter studentsAdapter;
    private int classId;
    private ClassModel currentClassModel;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        try {
            // Get class ID from intent
            classId = getIntent().getIntExtra("CLASS_ID", -1);
            if (classId == -1) {
                showError("Invalid class selected");
                finish();
                return;
            }

            // Initialize API service
            apiService = new ClassesService(this);

            // Initialize views
            classDetailHeader = findViewById(R.id.class_detail_header);
            teacherName = findViewById(R.id.teacher_name);
            scheduleDetails = findViewById(R.id.schedule_details);
            locationDetails = findViewById(R.id.location_details);
            studentsRecyclerView = findViewById(R.id.students_recycler_view);
            studentsCard = findViewById(R.id.students_card);
            loadingIndicator = findViewById(R.id.loading_indicator);

            // Get user role from shared preferences
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userRole = prefs.getString("user_role", "student");

            // Setup students adapter
            studentsAdapter = new StudentsAdapter(this, new ArrayList<>(), classId, this);
            studentsRecyclerView.setAdapter(studentsAdapter);

            // Show students list only for teachers
            if (userRole.equalsIgnoreCase("teacher")) {
                studentsCard.setVisibility(View.VISIBLE);
            } else {
                studentsCard.setVisibility(View.GONE);
            }

            // Load class details
            loadClassDetails();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showError("Error initializing the Class Details screen");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadClassDetails();
    }

    /**
     * Load class details from API
     */
    private void loadClassDetails() {
        showLoading(true);

        apiService.getClassDetails(classId, new ClassesService.ApiCallback<ClassModel>() {
            @Override
            public void onSuccess(ClassModel result) {
                currentClassModel = result;
                updateUI(result);

                // Load students list if user is teacher
                if (userRole.equalsIgnoreCase("teacher")) {
                    loadStudents();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showError(errorMessage);
            }
        });
    }

    /**
     * Load students enrolled in the class
     */
    private void loadStudents() {
        apiService.getStudentsInClass(classId, new ClassesService.ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> result) {
                updateStudentsUI(result);
            }

            @Override
            public void onError(String errorMessage) {
                showError("Error loading students: " + errorMessage);
            }
        });
    }

    /**
     * Update the UI with class details
     */
    private void updateUI(ClassModel classModel) {
        runOnUiThread(() -> {
            showLoading(false);

            // Update header
            classDetailHeader.setText(classModel.getClassName());

            // Update teacher info
            teacherName.setText(classModel.getTeacherName());

            // Update schedule
            scheduleDetails.setText(classModel.getFormattedSchedule());

            // Update location
            locationDetails.setText(classModel.getLocation());
        });
    }

    /**
     * Update the students list
     */
    private void updateStudentsUI(List<Student> students) {
        runOnUiThread(() -> {
            if (students != null && !students.isEmpty()) {
                studentsAdapter.updateData(students);
            }
        });
    }

    /**
     * Show or hide loading indicators
     */
    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * Show an error message
     */
    private void showError(String message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    /**
     * Handle grade updated event
     */
    @Override
    public void onGradeUpdated(int studentId, String grade) {
        // Show confirmation message
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage("Grade updated successfully")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }
}