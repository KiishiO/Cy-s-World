package com.example.own_example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.ExamInfoAdapter;
import com.example.own_example.models.ExamInfo;
import com.example.own_example.models.TestingCenter;
import com.example.own_example.services.TestingCenterService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminTestingCenterDetailActivity extends AppCompatActivity implements ExamInfoAdapter.OnExamClickListener {

    private static final String TAG = "AdminTestingCenterDetail";
    private static final String EXTRA_CENTER_ID = "center_id";

    private int centerId;
    private TestingCenter testingCenter;
    private List<ExamInfo> exams;
    private TestingCenterService service;
    private ExamInfoAdapter adapter;

    private MaterialToolbar toolbar;
    private TextView centerNameTextView;
    private TextView locationTextView;
    private TextView descriptionTextView;
    private RecyclerView examsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private FloatingActionButton fab;

    public static void start(Context context, int centerId) {
        Intent intent = new Intent(context, AdminTestingCenterDetailActivity.class);
        intent.putExtra(EXTRA_CENTER_ID, centerId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_center_detail);

        // Get center ID from intent
        centerId = getIntent().getIntExtra(EXTRA_CENTER_ID, -1);
        if (centerId == -1) {
            Log.e(TAG, "No center ID provided");
            finish();
            return;
        }

        // Initialize service
        service = new TestingCenterService(this);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        centerNameTextView = findViewById(R.id.center_name);
        locationTextView = findViewById(R.id.center_location);
        descriptionTextView = findViewById(R.id.center_description);
        examsRecyclerView = findViewById(R.id.exams_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.empty_state_text);
        fab = findViewById(R.id.fab_add_exam);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Admin: Testing Center Details");

        // Setup RecyclerView
        exams = new ArrayList<>();
        adapter = new ExamInfoAdapter(this, exams, this, true); // true = admin view
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examsRecyclerView.setAdapter(adapter);

        // Setup FAB
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> showAddExamDialog());

        // Load data
        loadTestingCenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (testingCenter != null) {
            loadExams();
        }
    }

    private void loadTestingCenter() {
        progressBar.setVisibility(View.VISIBLE);

        service.getTestingCenterById(centerId, new TestingCenterService.TestingCenterCallback() {
            @Override
            public void onSuccess(TestingCenter center) {
                testingCenter = center;

                // Update UI
                centerNameTextView.setText(center.getCenterName());
                locationTextView.setText(center.getLocation());
                descriptionTextView.setText(center.getCenterDescription());

                // Update toolbar title
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(center.getCenterName());
                }

                // Load exams
                loadExams();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading testing center: " + error);
                emptyStateTextView.setText("Error loading testing center details");
                emptyStateTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadExams() {
        service.getExamsForTestingCenter(centerId, new TestingCenterService.ExamsCallback() {
            @Override
            public void onSuccess(List<ExamInfo> examList) {
                progressBar.setVisibility(View.GONE);

                exams.clear();
                exams.addAll(examList);
                adapter.notifyDataSetChanged();

                if (exams.isEmpty()) {
                    emptyStateTextView.setText("No exams available at this testing center");
                    emptyStateTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyStateTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading exams: " + error);

                if (exams.isEmpty()) {
                    emptyStateTextView.setText("Error loading exams. Please try again.");
                    emptyStateTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showAddExamDialog() {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_exam, null);

        // Get references to dialog views
        EditText examNameEditText = dialogView.findViewById(R.id.exam_name_edit);
        EditText examDescriptionEditText = dialogView.findViewById(R.id.exam_description_edit);

        // Create and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Exam")
                .setView(dialogView)
                .setPositiveButton("Add", null) // Set this to null initially
                .setNegativeButton("Cancel", null)
                .create();

        // Show the dialog
        dialog.show();

        // Override the click listener to prevent automatic dismissal
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            // Get input values
            String examName = examNameEditText.getText().toString().trim();
            String examDescription = examDescriptionEditText.getText().toString().trim();

            // Validate input
            if (TextUtils.isEmpty(examName)) {
                examNameEditText.setError("Exam name is required");
                return;
            }

            // Create exam object
            ExamInfo exam = new ExamInfo();
            exam.setExamName(examName);
            exam.setExamDescription(examDescription);

            // Add exam to testing center
            addExamToTestingCenter(exam);

            // Dismiss the dialog
            dialog.dismiss();
        });
    }

    private void addExamToTestingCenter(ExamInfo exam) {
        progressBar.setVisibility(View.VISIBLE);

        // FIXED: Using ExamCallback instead of OperationCallback
        service.addExamToTestingCenter(centerId, exam, new TestingCenterService.ExamCallback() {
            @Override
            public void onSuccess(ExamInfo examResult) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this,
                        "Exam added successfully", Toast.LENGTH_SHORT).show();

                // Refresh the exam list
                loadExams();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this,
                        "Error adding exam: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(ExamInfo exam) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Exam")
                .setMessage("Are you sure you want to delete this exam?")
                .setPositiveButton("Delete", (dialog, which) -> deleteExam(exam))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteExam(ExamInfo exam) {
        progressBar.setVisibility(View.VISIBLE);

        service.deleteExam(exam.getId(), new TestingCenterService.OperationCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this,
                        "Exam deleted successfully", Toast.LENGTH_SHORT).show();

                // Refresh the list
                loadExams();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this,
                        "Error deleting exam: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onExamClick(ExamInfo exam) {
        // Show exam details or navigate to edit screen
        showEditExamDialog(exam);
    }

    @Override
    public void onEditClick(ExamInfo exam) {
        showEditExamDialog(exam);
    }

    @Override
    public void onDeleteClick(ExamInfo exam) {
        showDeleteConfirmationDialog(exam);
    }

    private void showEditExamDialog(ExamInfo exam) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_exam, null);

        // Get references to dialog views
        EditText examNameEditText = dialogView.findViewById(R.id.exam_name_edit);
        EditText examDescriptionEditText = dialogView.findViewById(R.id.exam_description_edit);

        // Populate with existing data
        examNameEditText.setText(exam.getExamName());
        examDescriptionEditText.setText(exam.getExamDescription());

        // Create and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Exam")
                .setView(dialogView)
                .setPositiveButton("Save", null) // Set this to null initially
                .setNegativeButton("Cancel", null)
                .create();

        // Show the dialog
        dialog.show();

        // Override the click listener to prevent automatic dismissal
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            // Get input values
            String examName = examNameEditText.getText().toString().trim();
            String examDescription = examDescriptionEditText.getText().toString().trim();

            // Validate input
            if (TextUtils.isEmpty(examName)) {
                examNameEditText.setError("Exam name is required");
                return;
            }

            // Update exam object
            exam.setExamName(examName);
            exam.setExamDescription(examDescription);

            // Update exam
            updateExam(exam);

            // Dismiss the dialog
            dialog.dismiss();
        });
    }

    private void updateExam(ExamInfo exam) {
        progressBar.setVisibility(View.VISIBLE);

        service.updateExam(exam.getId(), exam, new TestingCenterService.ExamCallback() {
            @Override
            public void onSuccess(ExamInfo updatedExam) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this,
                        "Exam updated successfully", Toast.LENGTH_SHORT).show();

                // Refresh the list
                loadExams();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this,
                        "Error updating exam: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}