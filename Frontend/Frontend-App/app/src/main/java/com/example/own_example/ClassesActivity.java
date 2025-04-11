package com.example.own_example;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.own_example.adapters.ClassesAdapter;
import com.example.own_example.models.ClassModel;
import com.example.own_example.services.ClassesService;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying the user's classes
 */
public class ClassesActivity extends AppCompatActivity {
    private static final String TAG = "ClassesActivity";

    private RecyclerView classesRecyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private ProgressBar loadingIndicator;
    private TextView classesHeader;

    private ClassesAdapter adapter;
    private ClassesService apiService;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);

        try {
            // Initialize API service
            apiService = new ClassesService(this);

            // Initialize views
            classesRecyclerView = findViewById(R.id.classes_recycler_view);
            swipeRefresh = findViewById(R.id.swipe_refresh);
            emptyState = findViewById(R.id.empty_state);
            loadingIndicator = findViewById(R.id.loading_indicator);
            classesHeader = findViewById(R.id.classes_header);

            // Setup adapter
            adapter = new ClassesAdapter(this, new ArrayList<>());
            classesRecyclerView.setAdapter(adapter);

            // Get user role from shared preferences
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userRole = prefs.getString("user_role", "student");

            // Set header text based on role
            if (userRole.equalsIgnoreCase("teacher")) {
                classesHeader.setText("My Teaching Classes");
            } else {
                classesHeader.setText("My Classes");
            }

            // Setup pull-to-refresh
            swipeRefresh.setOnRefreshListener(this::loadClasses);

            // Load classes
            loadClasses();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showError("Error initializing the Classes screen");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadClasses();
    }

    /**
     * Load classes based on user role
     */
    private void loadClasses() {
        showLoading(true);

        if (userRole.equalsIgnoreCase("teacher")) {
            // Load teacher classes
            apiService.getTeacherClasses(new ClassesService.ApiCallback<List<ClassModel>>() {
                @Override
                public void onSuccess(List<ClassModel> result) {
                    updateUI(result);
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    showError(errorMessage);
                }
            });
        } else {
            // Load student classes
            apiService.getStudentClasses(new ClassesService.ApiCallback<List<ClassModel>>() {
                @Override
                public void onSuccess(List<ClassModel> result) {
                    updateUI(result);
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    showError(errorMessage);
                }
            });
        }
    }

    /**
     * Update the UI with the loaded classes
     */
    private void updateUI(List<ClassModel> classModels) {
        runOnUiThread(() -> {
            showLoading(false);

            if (classModels == null || classModels.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                classesRecyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                classesRecyclerView.setVisibility(View.VISIBLE);
                adapter.updateData(classModels);
            }
        });
    }

    /**
     * Show or hide loading indicators
     */
    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            if (show) {
                loadingIndicator.setVisibility(View.VISIBLE);
                swipeRefresh.setRefreshing(false);
            } else {
                loadingIndicator.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
            }
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
}