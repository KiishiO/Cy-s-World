package com.example.own_example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.own_example.adapters.BusAdapter;
import com.example.own_example.models.Bus;
import com.example.own_example.services.BusService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class BusActivity extends AppCompatActivity implements BusAdapter.OnBusClickListener {

    private static final String TAG = "BusActivity";

    private BusService busService;
    private List<Bus> busList;
    private BusAdapter busAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateTextView;
    private FloatingActionButton addBusFab;
    private Button mapViewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);

        // Initialize variables
        busService = new BusService(this);
        busList = new ArrayList<>();

        // Set up views
        recyclerView = findViewById(R.id.bus_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        emptyStateTextView = findViewById(R.id.empty_state_text);
        addBusFab = findViewById(R.id.add_bus_fab);
        mapViewButton = findViewById(R.id.btn_map_view);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        busAdapter = new BusAdapter(this, busList, this);
        recyclerView.setAdapter(busAdapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadBuses);

        // Set up FAB click listener
        addBusFab.setOnClickListener(v -> showAddBusDialog());

        // Set up Map View button
        mapViewButton.setOnClickListener(v -> {
            Intent intent = new Intent(BusActivity.this, BusMapActivity.class);
            startActivity(intent);
        });

        // Load buses
        loadBuses();
    }

    /**
     * Load buses from the server
     */
    private void loadBuses() {
        swipeRefreshLayout.setRefreshing(true);

        busService.getAllBuses(new BusService.BusCallback() {
            @Override
            public void onSuccess(List<Bus> buses) {
                busList.clear();
                busList.addAll(buses);
                busAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

                // Update empty state visibility
                if (buses.isEmpty()) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                // On error, load mock data instead
                Toast.makeText(BusActivity.this, "Error: " + error + "\nUsing mock data instead", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error connecting to server, using mock data: " + error);

                // Load mock data
                busService.getMockBuses(new BusService.BusCallback() {
                    @Override
                    public void onSuccess(List<Bus> buses) {
                        busList.clear();
                        busList.addAll(buses);
                        busAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);

                        // Update views
                        emptyStateTextView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(String mockError) {
                        // This shouldn't happen with mock data
                        Toast.makeText(BusActivity.this, mockError, Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);

                        // Update empty state for error
                        emptyStateTextView.setText("Error loading buses. Pull down to retry.");
                        emptyStateTextView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    /**
     * Show dialog to add a new bus
     */
    private void showAddBusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_bus, null);
        builder.setView(dialogView);

        EditText busNameEditText = dialogView.findViewById(R.id.et_bus_name);
        EditText busNumberEditText = dialogView.findViewById(R.id.et_bus_number);
        EditText stopLocationEditText = dialogView.findViewById(R.id.et_stop_location);
        RadioGroup ratingRadioGroup = dialogView.findViewById(R.id.radio_group_rating);

        builder.setTitle("Add New Bus")
                .setPositiveButton("Add", null) // Set to null, we'll override later
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();

        // Override the positive button click to validate before dismiss
        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(view -> {
                // Validate inputs
                String busName = busNameEditText.getText().toString().trim();
                String busNumberStr = busNumberEditText.getText().toString().trim();
                String stopLocation = stopLocationEditText.getText().toString().trim();

                int selectedRatingId = ratingRadioGroup.getCheckedRadioButtonId();

                if (busName.isEmpty() || busNumberStr.isEmpty()) {
                    Toast.makeText(BusActivity.this, "Bus name and number are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedRatingId == -1) {
                    Toast.makeText(BusActivity.this, "Please select a bus rating", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int busNumber = Integer.parseInt(busNumberStr);
                    RadioButton selectedRating = dialogView.findViewById(selectedRatingId);
                    char rating = selectedRating.getText().toString().charAt(0);

                    // Create new bus
                    Bus newBus = new Bus(busNumber, busName, stopLocation, rating);

                    // Add bus to server
                    addBus(newBus);

                    // Dismiss dialog
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(BusActivity.this, "Invalid bus number", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    /**
     * Add a new bus to the server
     */
    private void addBus(Bus bus) {
        busService.addBus(bus, new BusService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(BusActivity.this, message, Toast.LENGTH_SHORT).show();
                loadBuses(); // Reload buses to show the newly added one
            }

            @Override
            public void onError(String error) {
                Toast.makeText(BusActivity.this, error, Toast.LENGTH_SHORT).show();
                // Still add to local mock data if server fails
                busService.getMockBuses(new BusService.BusCallback() {
                    @Override
                    public void onSuccess(List<Bus> buses) {
                        // Add the new bus to the mock data list
                        buses.add(bus);
                        busList.clear();
                        busList.addAll(buses);
                        busAdapter.notifyDataSetChanged();
                        Toast.makeText(BusActivity.this, "Bus added to local data", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String mockError) {
                        // This shouldn't happen with mock data
                        Toast.makeText(BusActivity.this, mockError, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * Show dialog to update bus location
     */
    private void showUpdateLocationDialog(Bus bus) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_location, null);
        builder.setView(dialogView);

        EditText locationEditText = dialogView.findViewById(R.id.et_new_location);

        // Pre-fill with current location if available
        if (bus.getStopLocation() != null && !bus.getStopLocation().isEmpty()) {
            locationEditText.setText(bus.getStopLocation());
        }

        builder.setTitle("Update Bus Location")
                .setPositiveButton("Update", (dialog, id) -> {
                    String newLocation = locationEditText.getText().toString().trim();
                    if (!newLocation.isEmpty()) {
                        updateBusLocation(bus.getBusName(), newLocation);
                    } else {
                        Toast.makeText(this, "Location cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    /**
     * Update bus location on the server
     */
    private void updateBusLocation(String busName, String newLocation) {
        busService.updateBusLocation(busName, newLocation, new BusService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(BusActivity.this, message, Toast.LENGTH_SHORT).show();
                loadBuses(); // Reload buses to reflect the update
            }

            @Override
            public void onError(String error) {
                Toast.makeText(BusActivity.this, error, Toast.LENGTH_SHORT).show();
                // Update locally if server update fails
                for (Bus bus : busList) {
                    if (bus.getBusName().equals(busName)) {
                        bus.setStopLocation(newLocation);
                        busAdapter.notifyDataSetChanged();
                        Toast.makeText(BusActivity.this, "Location updated locally", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
    }

    /**
     * Show dialog to rate a bus
     */
    private void showRateBusDialog(Bus bus) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate_bus, null);
        builder.setView(dialogView);

        RadioGroup ratingRadioGroup = dialogView.findViewById(R.id.radio_group_rating);

        // Set current rating if available
        if (bus.getBusRating() != 0) {
            switch (bus.getBusRating()) {
                case 'A':
                    ratingRadioGroup.check(R.id.radio_a);
                    break;
                case 'B':
                    ratingRadioGroup.check(R.id.radio_b);
                    break;
                case 'C':
                    ratingRadioGroup.check(R.id.radio_c);
                    break;
                case 'D':
                    ratingRadioGroup.check(R.id.radio_d);
                    break;
                case 'F':
                    ratingRadioGroup.check(R.id.radio_f);
                    break;
            }
        }

        builder.setTitle("Rate Bus: " + bus.getBusName())
                .setPositiveButton("Submit", (dialog, id) -> {
                    int selectedRatingId = ratingRadioGroup.getCheckedRadioButtonId();
                    if (selectedRatingId != -1) {
                        RadioButton selectedRating = dialogView.findViewById(selectedRatingId);
                        char rating = selectedRating.getText().toString().charAt(0);
                        updateBusRating(bus.getBusName(), rating);
                    } else {
                        Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    /**
     * Update bus rating on the server
     */
    private void updateBusRating(String busName, char rating) {
        busService.updateBusRating(busName, rating, new BusService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(BusActivity.this, message, Toast.LENGTH_SHORT).show();
                loadBuses(); // Reload buses to reflect the update
            }

            @Override
            public void onError(String error) {
                Toast.makeText(BusActivity.this, error, Toast.LENGTH_SHORT).show();
                // Update locally if server update fails
                for (Bus bus : busList) {
                    if (bus.getBusName().equals(busName)) {
                        bus.setBusRating(rating);
                        busAdapter.notifyDataSetChanged();
                        Toast.makeText(BusActivity.this, "Rating updated locally", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
    }

    // BusAdapter.OnBusClickListener implementation

    @Override
    public void onBusClick(Bus bus) {
        // Show bus details - could navigate to a details activity
        Toast.makeText(this, "Bus selected: " + bus.getBusName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdateLocationClick(Bus bus) {
        showUpdateLocationDialog(bus);
    }

    @Override
    public void onRateBusClick(Bus bus) {
        showRateBusDialog(bus);
    }
}