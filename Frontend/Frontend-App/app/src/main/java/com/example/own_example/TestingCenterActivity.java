package com.example.own_example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.own_example.adapters.TestingCenterAdapter;
import com.example.own_example.models.TestingCenter;
import com.example.own_example.services.TestingCenterService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TestingCenterActivity extends AppCompatActivity implements TestingCenterAdapter.OnTestingCenterClickListener {

    private static final String TAG = "TestingCenterActivity";

    private RecyclerView testingCentersRecyclerView;
    private TestingCenterAdapter adapter;
    private List<TestingCenter> testingCenters;
    private TestingCenterService service;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;
    private FloatingActionButton fab;
    // Making all users behave like admins for testing
    private boolean isAdmin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_center);

        // Initialize service
        service = new TestingCenterService(this);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.empty_state_text);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        testingCentersRecyclerView = findViewById(R.id.testing_centers_recycler_view);
        fab = findViewById(R.id.fab_add_testing_center);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Testing Centers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup RecyclerView
        testingCenters = new ArrayList<>();
        adapter = new TestingCenterAdapter(this, testingCenters, this, isAdmin);
        testingCentersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        testingCentersRecyclerView.setAdapter(adapter);

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadTestingCenters);

        // Setup FAB for all users
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> openAddTestingCenterDialog());

        // Load data
        loadTestingCenters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadTestingCenters();
    }

    private void loadTestingCenters() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);

        service.getAllTestingCenters(new TestingCenterService.TestingCentersCallback() {
            @Override
            public void onSuccess(List<TestingCenter> testingCenterList) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                testingCenters.clear();
                testingCenters.addAll(testingCenterList);
                adapter.notifyDataSetChanged();

                if (testingCenters.isEmpty()) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyStateTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Error loading testing centers: " + error);

                if (testingCenters.isEmpty()) {
                    emptyStateTextView.setText("Error loading testing centers. Pull to refresh.");
                    emptyStateTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void openAddTestingCenterDialog() {
        // In a real app, this would show a dialog with input fields
        // For this simple example, we'll create a testing center with fixed data
        TestingCenter newCenter = new TestingCenter();
        newCenter.setCenterName("New Testing Center");
        newCenter.setLocation("Campus Location");
        newCenter.setCenterDescription("This is a new testing center");

        createTestingCenter(newCenter);
    }

    private void createTestingCenter(TestingCenter center) {
        progressBar.setVisibility(View.VISIBLE);

        service.createTestingCenter(center, new TestingCenterService.TestingCenterCallback() {
            @Override
            public void onSuccess(TestingCenter testingCenter) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TestingCenterActivity.this,
                        "Testing center created successfully", Toast.LENGTH_SHORT).show();

                // Refresh the list
                loadTestingCenters();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TestingCenterActivity.this,
                        "Error creating testing center: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTestingCenterClick(TestingCenter testingCenter) {
        // Navigate to testing center detail activity
        TestingCenterDetailActivity.start(this, testingCenter.getId());
    }

    @Override
    public void onEditClick(TestingCenter testingCenter) {
        // Allow editing for all users in testing mode
        Toast.makeText(this, "Edit testing center: " + testingCenter.getCenterName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(TestingCenter testingCenter) {
        // Allow deleting for all users in testing mode
        new AlertDialog.Builder(this)
                .setTitle("Delete Testing Center")
                .setMessage("Are you sure you want to delete this testing center?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTestingCenter(testingCenter))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTestingCenter(TestingCenter testingCenter) {
        progressBar.setVisibility(View.VISIBLE);

        service.deleteTestingCenter(testingCenter.getId(), new TestingCenterService.OperationCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TestingCenterActivity.this,
                        "Testing center deleted successfully", Toast.LENGTH_SHORT).show();

                // Refresh the list
                loadTestingCenters();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TestingCenterActivity.this,
                        "Error deleting testing center: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}