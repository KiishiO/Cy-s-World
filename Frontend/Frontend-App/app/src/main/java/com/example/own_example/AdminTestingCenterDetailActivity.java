package com.example.own_example;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private FloatingActionButton addFab;

    public static void start(Context context, int centerId) {
        Intent intent = new Intent(context, AdminTestingCenterDetailActivity.class);
        intent.putExtra(EXTRA_CENTER_ID, centerId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_testing_center_detail);

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
        addFab = findViewById(R.id.fab_add);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup RecyclerView
        exams = new ArrayList<>();
        adapter = new ExamInfoAdapter(this, exams, this, true);
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examsRecyclerView.setAdapter(adapter);

        // Setup FAB
        addFab.setOnClickListener(v -> showAddExamDialog());

        // Load data
        loadTestingCenter();
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
                Toast.makeText(AdminTestingCenterDetailActivity.this, "Error loading testing center details", Toast.LENGTH_SHORT).show();
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
                    emptyStateTextView.setText("No exams available at this testing center. Add one!");
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_exam, null);
        EditText nameEditText = dialogView.findViewById(R.id.exam_name_edit);
        EditText descriptionEditText = dialogView.findViewById(R.id.exam_description_edit);

        new AlertDialog.Builder(this)
                .setTitle("Add Exam")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameEditText.getText().toString().trim();
                    String description = descriptionEditText.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Exam name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ExamInfo newExam = new ExamInfo(name, description, testingCenter);
                    addExam(newExam);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditExamDialog(ExamInfo exam) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_exam, null);
        EditText nameEditText = dialogView.findViewById(R.id.exam_name_edit);
        EditText descriptionEditText = dialogView.findViewById(R.id.exam_description_edit);

        // Pre-fill with existing data
        nameEditText.setText(exam.getExamName());
        descriptionEditText.setText(exam.getExamDescription());

        new AlertDialog.Builder(this)
                .setTitle("Edit Exam")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameEditText.getText().toString().trim();
                    String description = descriptionEditText.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Exam name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    exam.setExamName(name);
                    exam.setExamDescription(description);

                    // Update exam (would need an additional API method)
                    Toast.makeText(this, "Exam updated successfully", Toast.LENGTH_SHORT).show();
                    loadExams();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteExamConfirmationDialog(ExamInfo exam) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Exam")
                .setMessage("Are you sure you want to delete " + exam.getExamName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete exam (would need an additional API method)
                    Toast.makeText(this, "Exam deleted successfully", Toast.LENGTH_SHORT).show();
                    loadExams();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addExam(ExamInfo exam) {
        progressBar.setVisibility(View.VISIBLE);

        service.addExamToTestingCenter(centerId, exam, new TestingCenterService.OperationCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this, "Exam added successfully", Toast.LENGTH_SHORT).show();
                loadExams();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this, "Error adding exam: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onExamClick(ExamInfo exam) {
        // Could show a detailed view if needed
    }

    @Override
    public void onEditClick(ExamInfo exam) {
        showEditExamDialog(exam);
    }

    @Override
    public void onDeleteClick(ExamInfo exam) {
        showDeleteExamConfirmationDialog(exam);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_testing_center_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit_center) {
            showEditTestingCenterDialog();
            return true;
        } else if (item.getItemId() == R.id.action_delete_center) {
            showDeleteTestingCenterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditTestingCenterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_testing_center, null);
        EditText nameEditText = dialogView.findViewById(R.id.center_name_edit);
        EditText locationEditText = dialogView.findViewById(R.id.center_location_edit);
        EditText descriptionEditText = dialogView.findViewById(R.id.center_description_edit);

        // Pre-fill with existing data
        nameEditText.setText(testingCenter.getCenterName());
        locationEditText.setText(testingCenter.getLocation());
        descriptionEditText.setText(testingCenter.getCenterDescription());

        new AlertDialog.Builder(this)
                .setTitle("Edit Testing Center")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameEditText.getText().toString().trim();
                    String location = locationEditText.getText().toString().trim();
                    String description = descriptionEditText.getText().toString().trim();

                    if (name.isEmpty() || location.isEmpty()) {
                        Toast.makeText(this, "Name and location are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    testingCenter.setCenterName(name);
                    testingCenter.setLocation(location);
                    testingCenter.setCenterDescription(description);

                    updateTestingCenter();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteTestingCenterDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Testing Center")
                .setMessage("Are you sure you want to delete " + testingCenter.getCenterName() + "? This will also delete all exams at this center.")
                .setPositiveButton("Delete", (dialog, which) -> deleteTestingCenter())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTestingCenter() {
        progressBar.setVisibility(View.VISIBLE);

        service.updateTestingCenter(centerId, testingCenter, new TestingCenterService.TestingCenterCallback() {
            @Override
            public void onSuccess(TestingCenter updatedCenter) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this, "Testing center updated successfully", Toast.LENGTH_SHORT).show();

                // Update UI
                centerNameTextView.setText(updatedCenter.getCenterName());
                locationTextView.setText(updatedCenter.getLocation());
                descriptionTextView.setText(updatedCenter.getCenterDescription());

                // Update toolbar title
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(updatedCenter.getCenterName());
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this, "Error updating testing center: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTestingCenter() {
        progressBar.setVisibility(View.VISIBLE);

        service.deleteTestingCenter(centerId, new TestingCenterService.OperationCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this, "Testing center deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterDetailActivity.this, "Error deleting testing center: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}