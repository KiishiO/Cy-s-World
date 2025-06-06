package com.example.own_example;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
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

import com.example.own_example.adapters.AdminEventsAdapter;
import com.example.own_example.models.CampusEvent;
import com.example.own_example.models.EventChat;
import com.example.own_example.services.AdminEventService;
import com.example.own_example.services.UserService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for managing campus events by administrators.
 * Allows admins to create, edit, delete and send updates about campus events.
 * Implements AdminEventService.AdminEventsListener to handle events from the service.
 */
public class AdminEventsActivity extends AppCompatActivity implements AdminEventService.AdminEventsListener {

    private static final String TAG = "AdminEventsActivity";

    private RecyclerView eventsRecyclerView;
    private AdminEventsAdapter eventsAdapter;
    private FloatingActionButton addEventButton;
    private TextView connectionStatusText;
    private List<CampusEvent> events = new ArrayList<>();

    private AdminEventService eventService;
    private String adminUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_events);

        // Get admin username directly from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        adminUsername = prefs.getString("username", "");
        String role = prefs.getString("user_role", "");

        // Log values for debugging
        Log.d("AdminEvents", "Username: " + adminUsername + ", Role: " + role);

        // Check role using the same method as AdminDashboardActivity
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
        getSupportActionBar().setTitle("Manage Campus Events");

        eventsRecyclerView = findViewById(R.id.admin_events_recycler_view);
        addEventButton = findViewById(R.id.add_event_button);
        connectionStatusText = findViewById(R.id.admin_connection_status);

        // Set up RecyclerView
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsAdapter = new AdminEventsAdapter(events,
                this::onEditEvent,
                this::onDeleteEvent,
                this::onSendEventUpdate
        );
        eventsRecyclerView.setAdapter(eventsAdapter);

        // Set up add event button
        addEventButton.setOnClickListener(v -> showEventDialog(null));

        // Initialize event service
        eventService = new AdminEventService(this, adminUsername, this);

        // Set initial connection status
        updateConnectionStatus(false);
    }

    /**
     * Displays a dialog for adding a new event or editing an existing one.
     *
     * @param eventToEdit The event to edit, or null if adding a new event
     */
    private void showEventDialog(CampusEvent eventToEdit) {
        boolean isEditing = eventToEdit != null;

        // Inflate dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_event, null);

        // Get references to dialog views
        EditText titleInput = dialogView.findViewById(R.id.event_title_input);
        EditText descriptionInput = dialogView.findViewById(R.id.event_description_input);
        EditText locationInput = dialogView.findViewById(R.id.event_location_input);
        EditText startDateInput = dialogView.findViewById(R.id.event_start_date_input);
        EditText startTimeInput = dialogView.findViewById(R.id.event_start_time_input);
        EditText endDateInput = dialogView.findViewById(R.id.event_end_date_input);
        EditText endTimeInput = dialogView.findViewById(R.id.event_end_time_input);
        Spinner categorySpinner = dialogView.findViewById(R.id.event_category_spinner);

        // Set up category spinner
        String[] categories = {"Career", "Academic", "Entertainment", "Social"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Initialize date and time values
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.US);

        // Set current date/time as default
        startDateInput.setText(dateFormatter.format(calendar.getTime()));
        startTimeInput.setText(timeFormatter.format(calendar.getTime()));

        // Add one hour for default end time
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        endDateInput.setText(dateFormatter.format(calendar.getTime()));
        endTimeInput.setText(timeFormatter.format(calendar.getTime()));

        // Set up date pickers
        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));

        // Set up time pickers
        startTimeInput.setOnClickListener(v -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePicker(endTimeInput));

        // If editing, fill in existing values
        if (isEditing) {
            titleInput.setText(eventToEdit.getTitle());
            descriptionInput.setText(eventToEdit.getDescription());
            locationInput.setText(eventToEdit.getLocation());

            if (eventToEdit.getStartTime() != null) {
                startDateInput.setText(dateFormatter.format(eventToEdit.getStartTime()));
                startTimeInput.setText(timeFormatter.format(eventToEdit.getStartTime()));
            }

            if (eventToEdit.getEndTime() != null) {
                endDateInput.setText(dateFormatter.format(eventToEdit.getEndTime()));
                endTimeInput.setText(timeFormatter.format(eventToEdit.getEndTime()));
            }

            // Set spinner selection based on category
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(eventToEdit.getCategory())) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }

        // Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(isEditing ? "Edit Event" : "Add New Event")
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
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String startTime = startTimeInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();
            String endTime = endTimeInput.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();

            // Validate inputs
            if (title.isEmpty() || description.isEmpty() || location.isEmpty() ||
                    startDate.isEmpty() || startTime.isEmpty()) {
                Toast.makeText(this, "Required fields missing", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Parse start date/time
                Date startDateTime = parseDateTime(startDate, startTime);

                // Parse end date/time if provided
                Date endDateTime = null;
                if (!endDate.isEmpty() && !endTime.isEmpty()) {
                    endDateTime = parseDateTime(endDate, endTime);

                    // Validate end time is after start time
                    if (endDateTime.before(startDateTime)) {
                        Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (isEditing) {
                    // Update existing event
                    eventService.updateEvent(
                            eventToEdit.getId(),
                            title,
                            description,
                            location,
                            startDateTime,
                            endDateTime,
                            category
                    );
                } else {
                    // Create new event
                    eventService.createEvent(
                            title,
                            description,
                            location,
                            startDateTime,
                            endDateTime,
                            category
                    );
                }

                // Dismiss dialog after submitting
                dialog.dismiss();

            } catch (Exception e) {
                Toast.makeText(this, "Error with date/time format", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Date parsing error", e);
            }
        });
    }

    /**
     * Parse a date and time string into a Date object.
     *
     * @param date The date string in format yyyy-MM-dd
     * @param time The time string in format HH:mm
     * @return A Date object representing the combined date and time
     * @throws Exception If the date/time cannot be parsed
     */
    private Date parseDateTime(String date, String time) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        return formatter.parse(date + " " + time);
    }

    /**
     * Shows a date picker dialog for the given EditText.
     *
     * @param dateInput The EditText to update with the selected date
     */
    private void showDatePicker(final EditText dateInput) {
        final Calendar calendar = Calendar.getInstance();

        // Try to parse existing date if any
        try {
            String currentDate = dateInput.getText().toString();
            if (!currentDate.isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = dateFormat.parse(currentDate);
                calendar.setTime(date);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date for picker", e);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    dateInput.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Shows a time picker dialog for the given EditText.
     *
     * @param timeInput The EditText to update with the selected time
     */
    private void showTimePicker(final EditText timeInput) {
        final Calendar calendar = Calendar.getInstance();

        // Try to parse existing time if any
        try {
            String currentTime = timeInput.getText().toString();
            if (!currentTime.isEmpty()) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
                Date time = timeFormat.parse(currentTime);
                calendar.setTime(time);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing time for picker", e);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
                    timeInput.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)
        );
        timePickerDialog.show();
    }

    /**
     * Handles the action to edit an event.
     *
     * @param event The event to edit
     */
    private void onEditEvent(CampusEvent event) {
        showEventDialog(event);
    }

    /**
     * Handles the action to delete an event.
     *
     * @param event The event to delete
     */
    private void onDeleteEvent(CampusEvent event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?\n\n" + event.getTitle())
                .setPositiveButton("Delete", (dialog, which) -> {
                    eventService.deleteEvent(event.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Handles the action to send an update about an event.
     *
     * @param event The event to send an update about
     */
    private void onSendEventUpdate(CampusEvent event) {
        // Show dialog to send an update message about this event
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_send_event_update, null);

        EditText messageInput = dialogView.findViewById(R.id.event_update_input);
        TextView eventTitleText = dialogView.findViewById(R.id.event_title_display);

        eventTitleText.setText(event.getTitle());

        new AlertDialog.Builder(this)
                .setTitle("Send Event Update")
                .setView(dialogView)
                .setPositiveButton("Send", (dialog, which) -> {
                    String message = messageInput.getText().toString().trim();
                    if (!message.isEmpty()) {
                        eventService.sendEventUpdate(event.getId(), message);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Updates the connection status UI based on the connection state.
     *
     * @param connected Whether the app is connected to the server
     */
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
            addEventButton.setEnabled(connected);
        });
    }

    // AdminEventService.AdminEventsListener implementation

    /**
     * Called when the list of events is updated.
     *
     * @param updatedEvents The new list of events
     */
    @Override
    public void onEventsUpdated(List<CampusEvent> updatedEvents) {
        runOnUiThread(() -> {
            events.clear();
            events.addAll(updatedEvents);
            eventsAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Called when a new event is created.
     *
     * @param newEvent The newly created event
     */
    @Override
    public void onEventCreated(CampusEvent newEvent) {
        runOnUiThread(() -> {
            events.add(newEvent);
            eventsAdapter.notifyItemInserted(events.size() - 1);
            Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Called when an event is updated.
     *
     * @param updatedEvent The updated event
     */
    @Override
    public void onEventUpdated(CampusEvent updatedEvent) {
        runOnUiThread(() -> {
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getId().equals(updatedEvent.getId())) {
                    events.set(i, updatedEvent);
                    eventsAdapter.notifyItemChanged(i);
                    break;
                }
            }
            Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Called when an event is deleted.
     *
     * @param eventId The ID of the deleted event
     */
    @Override
    public void onEventDeleted(String eventId) {
        runOnUiThread(() -> {
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getId().equals(eventId)) {
                    events.remove(i);
                    eventsAdapter.notifyItemRemoved(i);
                    break;
                }
            }
            Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Called when a chat message is sent for an event.
     *
     * @param chatMessage The sent chat message
     */
    @Override
    public void onChatMessageSent(EventChat chatMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Event update sent", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Called when the connection state changes.
     *
     * @param connected Whether the app is connected to the server
     */
    @Override
    public void onConnectionStateChanged(boolean connected) {
        updateConnectionStatus(connected);
    }

    /**
     * Called when an error occurs in the event service.
     *
     * @param errorMessage The error message
     */
    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventService != null) {
            eventService.disconnect();
        }
    }
}