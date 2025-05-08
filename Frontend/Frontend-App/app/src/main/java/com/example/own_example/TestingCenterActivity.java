package com.example.own_example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.own_example.adapters.TestingCenterAdapter;
import com.example.own_example.models.ExamInfo;
import com.example.own_example.models.TestingCenter;
import com.example.own_example.services.TestingCenterService;
import com.google.android.material.appbar.MaterialToolbar;

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

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Testing Centers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup RecyclerView
        testingCenters = new ArrayList<>();
        adapter = new TestingCenterAdapter(this, testingCenters, this, false);
        testingCentersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        testingCentersRecyclerView.setAdapter(adapter);

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadTestingCenters);

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

    @Override
    public void onTestingCenterClick(TestingCenter testingCenter) {
        // Navigate to testing center detail activity
        TestingCenterDetailActivity.start(this, testingCenter.getId());
    }

    @Override
    public void onEditClick(TestingCenter testingCenter) {
        // Not used in student view
    }

    @Override
    public void onDeleteClick(TestingCenter testingCenter) {
        // Not used in student view
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}