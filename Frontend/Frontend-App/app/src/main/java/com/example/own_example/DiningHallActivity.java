package com.example.own_example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.DiningHallAdapter;
import com.example.own_example.models.DiningHall;
import com.example.own_example.services.DiningHallService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class DiningHallActivity extends AppCompatActivity implements DiningHallService.DiningHallListener {

    private static final String TAG = "DiningHallActivity";

    private RecyclerView diningHallsRecyclerView;
    private LinearLayout emptyStateView;
    private BottomNavigationView bottomNavigationView;

    private DiningHallAdapter diningHallAdapter;
    private List<DiningHall> diningHalls = new ArrayList<>();

    private DiningHallService diningHallService;
    private Button viewOrderHistoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_hall);

        Log.d(TAG, "DiningHallActivity onCreate");

        // Initialize services
        diningHallService = DiningHallService.getInstance(this);
        diningHallService.setListener(this);

        // Initialize views
        diningHallsRecyclerView = findViewById(R.id.dining_halls_recycler);
        emptyStateView = findViewById(R.id.empty_state_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewOrderHistoryButton = findViewById(R.id.view_order_history_button);

        //set up view order history button
        viewOrderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(DiningHallActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        // Set up RecyclerView
        diningHallsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        diningHallAdapter = new DiningHallAdapter(diningHalls, this);
        diningHallsRecyclerView.setAdapter(diningHallAdapter);

        // Set up bottom navigation
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(DiningHallActivity.this, StudentDashboardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_dining) {
                    //Already here
                    return true;
                } else if (itemId == R.id.nav_buses) {
                    Intent intent = new Intent(DiningHallActivity.this, ClassesActivity.class);//needs to be changed to bus activity
                    startActivity(intent);
                    return true;
                }
                return false;
            });
        }

        // Load data
        loadDiningHalls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to activity
        loadDiningHalls();
    }

    private void loadDiningHalls() {
        // First clear existing data
        diningHalls.clear();

        // Show loading state
        showEmptyState(true);

        // Load dining halls
        diningHallService.loadDiningHalls();
    }

    private void updateUI() {
        runOnUiThread(() -> {
            if (diningHalls.isEmpty()) {
                showEmptyState(true);
            } else {
                showEmptyState(false);
                diningHallAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            diningHallsRecyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            diningHallsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    // DiningHallService.DiningHallListener implementation

    @Override
    public void onDiningHallsLoaded(List<DiningHall> loadedDiningHalls) {
        // Clear the list again to be safe, in case it was modified between loadDiningHalls() and here
        diningHalls.clear();
        diningHalls.addAll(loadedDiningHalls);
        updateUI();
    }

    @Override
    public void onDiningHallLoaded(DiningHall diningHall) {
        // Not used in this activity
    }

    @Override
    public void onError(String errorMessage) {
        Log.e(TAG, "Error: " + errorMessage);
        runOnUiThread(() -> {
            showEmptyState(true);
            Toast.makeText(DiningHallActivity.this,
                    "Could not load dining halls: " + errorMessage,
                    Toast.LENGTH_SHORT).show();
        });
    }
}