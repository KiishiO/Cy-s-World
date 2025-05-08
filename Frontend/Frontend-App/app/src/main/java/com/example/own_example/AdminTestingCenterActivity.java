package com.example.own_example;

import android.app.AlertDialog;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.own_example.adapters.TestingCenterAdapter;
import com.example.own_example.models.TestingCenter;
import com.example.own_example.services.TestingCenterService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminTestingCenterActivity extends AppCompatActivity implements TestingCenterAdapter.OnTestingCenterClickListener {

    private static final String TAG = "AdminTestingCenter";

    private RecyclerView testingCentersRecyclerView;
    private TestingCenterAdapter adapter;
    private List<TestingCenter> testingCenters;
    private TestingCenterService service;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;
    private FloatingActionButton addFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_testing_center);

        // Initialize service
        service = new TestingCenterService(this);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.empty_state_text);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        testingCentersRecyclerView = findViewById(R.id.testing_centers_recycler_view);
        addFab = findViewById(R.id.fab_add);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage Testing Centers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup RecyclerView
        testingCenters = new ArrayList<>();
        adapter = new TestingCenterAdapter(this, testingCenters, this, true);
        testingCentersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        testingCentersRecyclerView.setAdapter(adapter);

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadTestingCenters);

        // Setup FAB
        addFab.setOnClickListener(v -> showAddTestingCenterDialog());

        // Load data
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
                    emptyStateTextView.setText("No testing centers available. Add one!");
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

    private void showAddTestingCenterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_testing_center, null);
        EditText nameEditText = dialogView.findViewById(R.id.center_name_edit);
        EditText locationEditText = dialogView.findViewById(R.id.center_location_edit);
        EditText descriptionEditText = dialogView.findViewById(R.id.center_description_edit);

        new AlertDialog.Builder(this)
                .setTitle("Add Testing Center")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameEditText.getText().toString().trim();
                    String location = locationEditText.getText().toString().trim();
                    String description = descriptionEditText.getText().toString().trim();

                    if (name.isEmpty() || location.isEmpty()) {
                        Toast.makeText(this, "Name and location are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TestingCenter newCenter = new TestingCenter(name, location, description);
                    addTestingCenter(newCenter);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditTestingCenterDialog(TestingCenter testingCenter) {
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

                    updateTestingCenter(testingCenter);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(TestingCenter testingCenter) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Testing Center")
                .setMessage("Are you sure you want to delete " + testingCenter.getCenterName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTestingCenter(testingCenter))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addTestingCenter(TestingCenter testingCenter) {
        progressBar.setVisibility(View.VISIBLE);

        service.createTestingCenter(testingCenter, new TestingCenterService.TestingCenterCallback() {
            @Override
            public void onSuccess(TestingCenter createdCenter) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterActivity.this, "Testing center added successfully", Toast.LENGTH_SHORT).show();
                loadTestingCenters();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterActivity.this, "Error adding testing center: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTestingCenter(TestingCenter testingCenter) {
        progressBar.setVisibility(View.VISIBLE);

        service.updateTestingCenter(testingCenter.getId(), testingCenter, new TestingCenterService.TestingCenterCallback() {
            @Override
            public void onSuccess(TestingCenter updatedCenter) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterActivity.this, "Testing center updated successfully", Toast.LENGTH_SHORT).show();
                loadTestingCenters();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterActivity.this, "Error updating testing center: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTestingCenter(TestingCenter testingCenter) {
        progressBar.setVisibility(View.VISIBLE);

        service.deleteTestingCenter(testingCenter.getId(), new TestingCenterService.OperationCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterActivity.this, "Testing center deleted successfully", Toast.LENGTH_SHORT).show();
                loadTestingCenters();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminTestingCenterActivity.this, "Error deleting testing center: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTestingCenterClick(TestingCenter testingCenter) {
        // Navigate to testing center detail activity for admin
        AdminTestingCenterDetailActivity.start(this, testingCenter.getId());
    }

    @Override
    public void onEditClick(TestingCenter testingCenter) {
        showEditTestingCenterDialog(testingCenter);
    }

    @Override
    public void onDeleteClick(TestingCenter testingCenter) {
        showDeleteConfirmationDialog(testingCenter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_testing_center, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadTestingCenters();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}