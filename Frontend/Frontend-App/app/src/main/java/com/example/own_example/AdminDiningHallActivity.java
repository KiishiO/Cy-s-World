package com.example.own_example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.AdminDiningHallAdapter;
import com.example.own_example.models.DiningHall;
import com.example.own_example.services.DiningHallService;
import com.example.own_example.services.UserService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class AdminDiningHallActivity extends AppCompatActivity implements DiningHallService.DiningHallListener {

    private static final String TAG = "AdminDiningHallActivity";

    private RecyclerView diningHallsRecyclerView;
    private AdminDiningHallAdapter diningHallAdapter;
    private FloatingActionButton addDiningHallButton;
    private TextView connectionStatusText;
    private List<DiningHall> diningHalls = new ArrayList<>();

    private DiningHallService diningHallService;
    private String adminUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dining_hall);

//        // Get admin username
//        adminUsername = UserService.getInstance().getCurrentUsername();
//        if (adminUsername == null || adminUsername.isEmpty() || !UserService.getInstance().isAdmin()) {
//            Toast.makeText(this, "Administrator access required", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

        // Get admin username
        adminUsername = UserService.getInstance().getCurrentUsername();

        // Check role using the same method as AdminDashboardActivity
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userRoleStr = prefs.getString("user_role", "STUDENT");

        try {
            UserRoles userRole = UserRoles.valueOf(userRoleStr);
            if (userRole != UserRoles.ADMIN) {
                Toast.makeText(this, "You don't have permission to access this feature", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        Toolbar toolbar = findViewById(R.id.admin_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage Dining Halls");

        diningHallsRecyclerView = findViewById(R.id.admin_dining_halls_recycler_view);
        addDiningHallButton = findViewById(R.id.add_dining_hall_button);
        connectionStatusText = findViewById(R.id.admin_connection_status);

        // Set up RecyclerView
        diningHallsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        diningHallAdapter = new AdminDiningHallAdapter(diningHalls,
                this::onEditDiningHall,
                this::onDeleteDiningHall,
                this::onManageMenu
        );
        diningHallsRecyclerView.setAdapter(diningHallAdapter);

        // Set up add dining hall button
        addDiningHallButton.setOnClickListener(v -> showDiningHallDialog(null));

        // Initialize dining hall service
        diningHallService = DiningHallService.getInstance(this);
        diningHallService.setListener(this);
        diningHallService.setUsername(adminUsername);

        // Set initial connection status
        updateConnectionStatus(false);

        // Load data and initialize service
        diningHallService.initialize();
    }

    private void showDiningHallDialog(DiningHall diningHallToEdit) {
        boolean isEditing = diningHallToEdit != null;

        // Inflate dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_dining_hall, null);

        // Get references to dialog views
        EditText nameInput = dialogView.findViewById(R.id.dining_hall_name_input);
        EditText locationInput = dialogView.findViewById(R.id.dining_hall_location_input);
        EditText hoursInput = dialogView.findViewById(R.id.dining_hall_hours_input);
        EditText popularItemInput = dialogView.findViewById(R.id.dining_hall_popular_item_input);
        SwitchMaterial statusSwitch = dialogView.findViewById(R.id.dining_hall_status_switch);
        Slider busynessSlider = dialogView.findViewById(R.id.dining_hall_busyness_slider);

        // If editing, fill in existing values
        if (isEditing) {
            nameInput.setText(diningHallToEdit.getName());
            locationInput.setText(diningHallToEdit.getLocation());
            hoursInput.setText(diningHallToEdit.getHours());
            popularItemInput.setText(diningHallToEdit.getPopularItem());
            statusSwitch.setChecked(diningHallToEdit.isOpen());
            busynessSlider.setValue(diningHallToEdit.getBusynessLevel());
        }

        // Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(isEditing ? "Edit Dining Hall" : "Add New Dining Hall")
                .setView(dialogView)
                .setPositiveButton(isEditing ? "Update" : "Add", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Show dialog and handle button clicks
        dialog.show();

        // Override positive button to validate input before dismissing
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            // Get input values
            String name = nameInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            String hours = hoursInput.getText().toString().trim();
            String popularItem = popularItemInput.getText().toString().trim();
            boolean isOpen = statusSwitch.isChecked();
            int busynessLevel = (int) busynessSlider.getValue();

            // Validate inputs
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(location) || TextUtils.isEmpty(hours)) {
                Toast.makeText(this, "Name, location, and hours are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditing) {
                // Update existing dining hall
                diningHallService.updateDiningHall(
                        diningHallToEdit.getId(),
                        name,
                        location,
                        isOpen,
                        hours,
                        popularItem,
                        busynessLevel
                );
            } else {
                // Create new dining hall
                diningHallService.createDiningHall(
                        name,
                        location,
                        isOpen,
                        hours,
                        popularItem,
                        busynessLevel
                );
            }

            // Dismiss dialog
            dialog.dismiss();
        });
    }

    private void onEditDiningHall(DiningHall diningHall) {
        showDiningHallDialog(diningHall);
    }

    private void onDeleteDiningHall(DiningHall diningHall) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Dining Hall")
                .setMessage("Are you sure you want to delete this dining hall?\n\n" + diningHall.getName())
                .setPositiveButton("Delete", (dialog, which) -> {
                    diningHallService.deleteDiningHall(diningHall.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onManageMenu(DiningHall diningHall) {
        try {
            int id = diningHall.getId();
            String name = diningHall.getName();

            // Log the values to debug
            Log.d("AdminDiningHall", "Managing menu for ID: " + id + ", Name: " + name);

            Intent intent = new Intent(this, AdminDiningMenuActivity.class);
            intent.putExtra("dining_hall_id", id);
            intent.putExtra("dining_hall_name", name);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("AdminDiningHall", "Error launching menu activity: " + e.getMessage());
            Toast.makeText(this, "Error opening menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateConnectionStatus(boolean connected) {
        runOnUiThread(() -> {
            if (connected) {
                connectionStatusText.setText("Connected");
                connectionStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                connectionStatusText.setText("Disconnected");
                connectionStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Disable/enable add button based on connection
            addDiningHallButton.setEnabled(connected);
        });
    }

    // DiningHallService.DiningHallListener implementation

    @Override
    public void onDiningHallsLoaded(List<DiningHall> loadedDiningHalls) {
        runOnUiThread(() -> {
            diningHalls.clear();
            diningHalls.addAll(loadedDiningHalls);
            diningHallAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDiningHallLoaded(DiningHall diningHall) {
        // Not used in this activity
    }

    @Override
    public void onDiningHallCreated(DiningHall newDiningHall) {
        runOnUiThread(() -> {
            diningHalls.add(newDiningHall);
            diningHallAdapter.notifyItemInserted(diningHalls.size() - 1);
            Toast.makeText(this, "Dining hall created successfully", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDiningHallUpdated(DiningHall updatedDiningHall) {
        runOnUiThread(() -> {
            for (int i = 0; i < diningHalls.size(); i++) {
                if (diningHalls.get(i).getId() == updatedDiningHall.getId()) {
                    diningHalls.set(i, updatedDiningHall);
                    diningHallAdapter.notifyItemChanged(i);
                    break;
                }
            }
            Toast.makeText(this, "Dining hall updated successfully", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDiningHallDeleted(int diningHallId) {
        runOnUiThread(() -> {
            for (int i = 0; i < diningHalls.size(); i++) {
                if (diningHalls.get(i).getId() == diningHallId) {
                    diningHalls.remove(i);
                    diningHallAdapter.notifyItemRemoved(i);
                    break;
                }
            }
            Toast.makeText(this, "Dining hall deleted successfully", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onConnectionStateChanged(boolean connected) {
        updateConnectionStatus(connected);
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        diningHallService.disconnect();
    }

}