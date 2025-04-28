package com.example.own_example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.EventsAdapter;
import com.example.own_example.models.CampusEvent;
import com.example.own_example.models.EventChat;
import com.example.own_example.services.EventWebSocketClient;
import com.example.own_example.services.UserService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity that displays campus events to regular users.
 * Provides functionality to browse, search, and filter events by date and category.
 * Implements EventWebSocketClient.EventsListener for real-time event updates
 * and EventsAdapter.OnEventClickListener for handling event clicks.
 */
public class CampusEventsActivity extends AppCompatActivity implements
        EventWebSocketClient.EventsListener,
        EventsAdapter.OnEventClickListener {

    private static final String TAG = "CampusEventsActivity";

    private CalendarView calendarView;
    private RecyclerView eventsRecyclerView;
    private SearchView searchView;
    private Spinner categorySpinner;
    private TextView selectedDateText;
    private TextView noEventsText;
    private TextView connectionStatusText;

    private EventsAdapter eventsAdapter;
    private List<CampusEvent> allEvents = new ArrayList<>();
    private List<CampusEvent> filteredEvents = new ArrayList<>();

    private EventWebSocketClient webSocketClient;
    private String selectedDate;
    private String currentUsername;

    private static final boolean USE_DUMMY_DATA = false; // Set to false when using real backend

    /**
     * Initializes the activity, sets up the UI components, and establishes connection
     * to the WebSocket server for real-time event updates.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_campus_events);
            Log.d(TAG, "CampusEventsActivity onCreate");

            // Initialize user service and get current username
            currentUsername = UserService.getInstance().getCurrentUsername();

            // Initialize views
            calendarView = findViewById(R.id.calendar_view);
            eventsRecyclerView = findViewById(R.id.events_recycler_view);
            searchView = findViewById(R.id.search_view);
            categorySpinner = findViewById(R.id.category_spinner);
            selectedDateText = findViewById(R.id.selected_date_text);
            noEventsText = findViewById(R.id.no_events_text);
            connectionStatusText = findViewById(R.id.connection_status);

            // Set up RecyclerView
            eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Set up adapter
            eventsAdapter = new EventsAdapter(filteredEvents, this);
            eventsRecyclerView.setAdapter(eventsAdapter);

            if (USE_DUMMY_DATA) {
                // Add dummy data and bypass login check
                currentUsername = "test_user";
                loadSimpleDummyEvents();
            } else {
                // Initialize WebSocket client
                webSocketClient = new EventWebSocketClient(this, currentUsername, this);
            }

            // Set up category filter spinner
            setupCategorySpinner();

            // Set up calendar date change listener
            setupCalendarListener();

            // Set up search functionality
            setupSearchView();

            // Set initial date text and selected date
            Calendar calendar = Calendar.getInstance();
            Date today = calendar.getTime();
            updateSelectedDateText(today);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            selectedDate = dateFormat.format(today);

            // Initialize WebSocket client
            try {
                webSocketClient = new EventWebSocketClient(this, currentUsername, this);
            } catch (Exception e) {
                Log.e(TAG, "Error connecting to WebSocket: " + e.getMessage(), e);
                updateConnectionStatus(false);
                // Don't crash the app, just show a message
                Toast.makeText(this, "Couldn't connect to server", Toast.LENGTH_LONG).show();
            }

            // Set initial connection status
            updateConnectionStatus(false);

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }

    /**
     * Loads dummy event data for testing purposes when not connected to a backend.
     * This method creates sample events with realistic data for UI testing.
     */
    private void loadSimpleDummyEvents() {
        List<CampusEvent> dummyEvents = new ArrayList<>();

        // Create first dummy event
        CampusEvent event1 = new CampusEvent();
        event1.setId("event-001");
        event1.setTitle("Career Fair 2025");
        event1.setDescription("Join us for the largest career fair on campus! Meet representatives from over 200 companies.");
        event1.setLocation("Memorial Union");
        event1.setCategory("Career");
        event1.setCreator("admin_user");
        event1.setAttendees(87);

        // Set dates for first event (tomorrow)
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 10);
        event1.setStartTime(tomorrow.getTime());

        tomorrow.add(Calendar.HOUR_OF_DAY, 3);
        event1.setEndTime(tomorrow.getTime());

        // Create second dummy event
        CampusEvent event2 = new CampusEvent();
        event2.setId("event-002");
        event2.setTitle("Campus Movie Night");
        event2.setDescription("Free outdoor movie screening on central campus. Bring blankets and chairs!");
        event2.setLocation("Central Campus");
        event2.setCategory("Entertainment");
        event2.setCreator("events_coordinator");
        event2.setAttendees(42);

        // Set dates for second event (this weekend)
        Calendar weekend = Calendar.getInstance();
        weekend.add(Calendar.DAY_OF_MONTH, 3); // 3 days from now
        weekend.set(Calendar.HOUR_OF_DAY, 19); // 7 PM
        event2.setStartTime(weekend.getTime());

        weekend.add(Calendar.HOUR_OF_DAY, 2); // 2 hour movie
        event2.setEndTime(weekend.getTime());

        // Add events to list
        dummyEvents.add(event1);
        dummyEvents.add(event2);

        // Update the adapter
        allEvents.clear();
        allEvents.addAll(dummyEvents);
        filteredEvents.clear();
        filteredEvents.addAll(dummyEvents);
        eventsAdapter.notifyDataSetChanged();
    }

    /**
     * Sets up the calendar view with a date change listener to filter events by date.
     */
    private void setupCalendarListener() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                Date selectedDate = calendar.getTime();

                // Update the date display
                updateSelectedDateText(selectedDate);

                // Update selected date string
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                this.selectedDate = dateFormat.format(selectedDate);

                // Filter events by selected date
                filterEventsByDate(this.selectedDate);

            } catch (Exception e) {
                Log.e(TAG, "Error in calendar date change: " + e.getMessage());
            }
        });
    }

    /**
     * Sets up the search view for filtering events by title, location, or description.
     */
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query, getSelectedCategory());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText, getSelectedCategory());
                return true;
            }
        });
    }

    /**
     * Sets up the category spinner for filtering events by category.
     */
    private void setupCategorySpinner() {
        String[] categories = {"All Categories", "Career", "Academic", "Entertainment", "Social"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterEvents(searchView.getQuery().toString(), getSelectedCategory());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    /**
     * Gets the currently selected category from the spinner.
     *
     * @return The selected category, or an empty string if "All Categories" is selected
     */
    private String getSelectedCategory() {
        return categorySpinner.getSelectedItemPosition() == 0 ?
                "" : categorySpinner.getSelectedItem().toString();
    }

    /**
     * Updates the displayed selected date text.
     *
     * @param date The date to display
     */
    private void updateSelectedDateText(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US);
        selectedDateText.setText(dateFormat.format(date));
    }

    /**
     * Filters events by the selected date.
     *
     * @param dateString The date string in format yyyy-MM-dd to filter by
     */
    private void filterEventsByDate(String dateString) {
        List<CampusEvent> dateFilteredEvents = new ArrayList<>();
        for (CampusEvent event : allEvents) {
            if (event.getFormattedStartDate().equals(dateString)) {
                dateFilteredEvents.add(event);
            }
        }

        filteredEvents.clear();
        filteredEvents.addAll(dateFilteredEvents);

        // Update UI based on whether there are events
        if (dateFilteredEvents.isEmpty()) {
            noEventsText.setVisibility(View.VISIBLE);
            eventsRecyclerView.setVisibility(View.GONE);
        } else {
            noEventsText.setVisibility(View.GONE);
            eventsRecyclerView.setVisibility(View.VISIBLE);
        }

        eventsAdapter.notifyDataSetChanged();

        // Apply any existing search or category filters
        filterEvents(searchView.getQuery().toString(), getSelectedCategory());
    }

    /**
     * Filters events by search query and category.
     *
     * @param query The search query to filter by
     * @param category The category to filter by
     */
    private void filterEvents(String query, String category) {
        List<CampusEvent> tempFilteredList = new ArrayList<>();

        for (CampusEvent event : filteredEvents) {
            boolean matchesQuery = query.isEmpty() ||
                    event.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    event.getLocation().toLowerCase().contains(query.toLowerCase()) ||
                    event.getDescription().toLowerCase().contains(query.toLowerCase());

            boolean matchesCategory = category.isEmpty() ||
                    event.getCategory().equalsIgnoreCase(category);

            if (matchesQuery && matchesCategory) {
                tempFilteredList.add(event);
            }
        }

        if (tempFilteredList.isEmpty() && !filteredEvents.isEmpty()) {
            noEventsText.setVisibility(View.VISIBLE);
            noEventsText.setText("No events match your search criteria");
            eventsRecyclerView.setVisibility(View.GONE);
        } else if (!tempFilteredList.isEmpty()) {
            noEventsText.setVisibility(View.GONE);
            eventsRecyclerView.setVisibility(View.VISIBLE);
        }

        eventsAdapter.updateEvents(tempFilteredList);
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
        });
    }

    // EventWebSocketClient.EventsListener implementation

    /**
     * Called when a list of events is received from the server.
     *
     * @param events The list of events received
     */
    @Override
    public void onEventsReceived(List<CampusEvent> events) {
        runOnUiThread(() -> {
            allEvents.clear();
            allEvents.addAll(events);
            filterEventsByDate(selectedDate);
        });
    }

    /**
     * Called when a new event is received from the server.
     *
     * @param event The new event received
     */
    @Override
    public void onNewEventReceived(CampusEvent event) {
        runOnUiThread(() -> {
            // Add to all events
            allEvents.add(event);

            // If the event is for the selected date, update filtered list
            if (event.getFormattedStartDate().equals(selectedDate)) {
                filteredEvents.add(event);
                eventsAdapter.notifyItemInserted(filteredEvents.size() - 1);

                // Update visibility of no events text
                if (filteredEvents.size() > 0) {
                    noEventsText.setVisibility(View.GONE);
                    eventsRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            // Show a toast to notify about the new event
            Toast.makeText(this, "New event added: " + event.getTitle(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Called when an event is updated on the server.
     *
     * @param updatedEvent The updated event
     */
    @Override
    public void onEventUpdated(CampusEvent updatedEvent) {
        runOnUiThread(() -> {
            // Update in all events list
            for (int i = 0; i < allEvents.size(); i++) {
                if (allEvents.get(i).getId().equals(updatedEvent.getId())) {
                    allEvents.set(i, updatedEvent);
                    break;
                }
            }

            // Update in filtered list if present
            for (int i = 0; i < filteredEvents.size(); i++) {
                if (filteredEvents.get(i).getId().equals(updatedEvent.getId())) {
                    filteredEvents.set(i, updatedEvent);
                    eventsAdapter.notifyItemChanged(i);
                    break;
                }
            }
        });
    }

    /**
     * Called when an event's RSVP count is updated.
     *
     * @param eventId The ID of the event
     * @param attendees The new number of attendees
     * @param isRsvped Whether the current user has RSVP'd
     */
    @Override
    public void onRsvpUpdated(String eventId, int attendees, boolean isRsvped) {
        runOnUiThread(() -> {
            // Update in all events list
            for (CampusEvent event : allEvents) {
                if (event.getId().equals(eventId)) {
                    event.setAttendees(attendees);
                    event.setRsvped(isRsvped);
                    break;
                }
            }

            // Update in filtered list if present
            for (int i = 0; i < filteredEvents.size(); i++) {
                CampusEvent event = filteredEvents.get(i);
                if (event.getId().equals(eventId)) {
                    event.setAttendees(attendees);
                    event.setRsvped(isRsvped);
                    eventsAdapter.notifyItemChanged(i);
                    break;
                }
            }
        });
    }

    /**
     * Called when a chat message is received for an event.
     * Not handled in this activity, only in EventDetailActivity.
     *
     * @param chatMessage The chat message received
     */
    @Override
    public void onChatMessageReceived(EventChat chatMessage) {
        // Not handled in the main activity - only in EventDetailActivity
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

    // EventsAdapter.OnEventClickListener implementation

    /**
     * Called when an event is clicked in the RecyclerView.
     * Opens the EventDetailActivity with the clicked event's details.
     *
     * @param event The event that was clicked
     */
    @Override
    public void onEventClick(CampusEvent event) {
        // Open event detail activity
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("event_description", event.getDescription());
        intent.putExtra("event_location", event.getLocation());
        intent.putExtra("event_category", event.getCategory());
        intent.putExtra("event_creator", event.getCreator());
        intent.putExtra("event_attendees", event.getAttendees());
        intent.putExtra("event_rsvped", event.isRsvped());

        if (event.getStartTime() != null) {
            intent.putExtra("event_start_time", event.getStartTime().getTime());
        }
        if (event.getEndTime() != null) {
            intent.putExtra("event_end_time", event.getEndTime().getTime());
        }

        startActivity(intent);
    }

    /**
     * Cleans up resources when the activity is destroyed.
     * Disconnects from the WebSocket server.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }
}