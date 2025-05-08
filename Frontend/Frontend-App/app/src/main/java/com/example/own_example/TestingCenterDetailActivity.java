package com.example.own_example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.ExamInfoAdapter;
import com.example.own_example.models.ExamInfo;
import com.example.own_example.models.TestingCenter;
import com.example.own_example.services.TestingCenterService;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class TestingCenterDetailActivity extends AppCompatActivity implements ExamInfoAdapter.OnExamClickListener {

    private static final String TAG = "TestingCenterDetail";
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

    public static void start(Context context, int centerId) {
        Intent intent = new Intent(context, TestingCenterDetailActivity.class);
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

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup RecyclerView
        exams = new ArrayList<>();
        adapter = new ExamInfoAdapter(this, exams, this, false);
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examsRecyclerView.setAdapter(adapter);

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

    @Override
    public void onExamClick(ExamInfo exam) {
        // For student view, this could potentially show more details
        // or allow registration for the exam
    }

    @Override
    public void onEditClick(ExamInfo exam) {
        // Not used in student view
    }

    @Override
    public void onDeleteClick(ExamInfo exam) {
        // Not used in student view
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}