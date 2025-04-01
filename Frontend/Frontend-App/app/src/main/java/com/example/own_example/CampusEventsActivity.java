package com.example.own_example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.EventsAdapter;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CampusEventsActivity extends AppCompatActivity {

    private static final String TAG = "CampusEventsActivity";

    private CalendarView calendarView;
    private RecyclerView eventsRecyclerView;
    private SearchView searchView;
    private Spinner categorySpinner;
    private TextView selectedDateText;

    private EventsAdapter eventsAdapter;
    private List<EventItem> allEvents;
    private List<EventItem> filteredEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_campus_events);
            Log.d(TAG, "CampusEventsActivity onCreate");

            // Initialize views
            calendarView = findViewById(R.id.calendar_view);
            eventsRecyclerView = findViewById(R.id.events_recycler_view);
            searchView = findViewById(R.id.search_view);
            categorySpinner = findViewById(R.id.category_spinner);
            selectedDateText = findViewById(R.id.selected_date_text);

            // Set up RecyclerView
            eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Initialize event lists
            allEvents = generateSampleEvents(); // Replace with your actual data fetching method
            filteredEvents = new ArrayList<>(allEvents);

            // Set up adapter
            eventsAdapter = new EventsAdapter(filteredEvents);
            eventsRecyclerView.setAdapter(eventsAdapter);

            // Set up category filter spinner
            setupCategorySpinner();

            // Set up calendar date change listener
            setupCalendarListener();

            // Set up search functionality
            setupSearchView();

            // Set initial date text
            Calendar calendar = Calendar.getInstance();
            updateSelectedDateText(calendar.getTime());

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }

    private void setupCalendarListener() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                Date selectedDate = calendar.getTime();

                // Update the date display
                updateSelectedDateText(selectedDate);

                // Filter events by selected date
                filterEventsByDate(selectedDate);

            } catch (Exception e) {
                Log.e(TAG, "Error in calendar date change: " + e.getMessage());
            }
        });
    }

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

    private String getSelectedCategory() {
        return categorySpinner.getSelectedItemPosition() == 0 ?
                "" : categorySpinner.getSelectedItem().toString();
    }

    private void updateSelectedDateText(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US);
        selectedDateText.setText(dateFormat.format(date));
    }

    private void filterEventsByDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateString = dateFormat.format(date);

        List<EventItem> dateFilteredEvents = new ArrayList<>();
        for (EventItem event : allEvents) {
            if (event.getDate().equals(dateString)) {
                dateFilteredEvents.add(event);
            }
        }

        filteredEvents.clear();
        filteredEvents.addAll(dateFilteredEvents);
        eventsAdapter.notifyDataSetChanged();

        // Apply any existing search or category filters
        filterEvents(searchView.getQuery().toString(), getSelectedCategory());
    }

    private void filterEvents(String query, String category) {
        List<EventItem> tempFilteredList = new ArrayList<>();

        for (EventItem event : filteredEvents) {
            boolean matchesQuery = query.isEmpty() ||
                    event.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    event.getLocation().toLowerCase().contains(query.toLowerCase());

            boolean matchesCategory = category.isEmpty() ||
                    event.getCategory().equalsIgnoreCase(category);

            if (matchesQuery && matchesCategory) {
                tempFilteredList.add(event);
            }
        }

        eventsAdapter.updateEvents(tempFilteredList);
    }

    // This would be replaced by actual API call to fetch events data
    private List<EventItem> generateSampleEvents() {
        List<EventItem> events = new ArrayList<>();

        // Get current date for sample events
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String currentDate = dateFormat.format(calendar.getTime());

        // Add some events for current date
        events.add(new EventItem("Career Fair", "Memorial Union", currentDate, "Career", 42));
        events.add(new EventItem("Engineering Seminar", "Howe Hall", currentDate, "Academic", 18));
        events.add(new EventItem("Concert in the Park", "Central Campus", currentDate, "Entertainment", 87));

        // Add events for tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String tomorrowDate = dateFormat.format(calendar.getTime());
        events.add(new EventItem("Resume Workshop", "Careers Building", tomorrowDate, "Career", 15));
        events.add(new EventItem("Study Group", "Parks Library", tomorrowDate, "Academic", 8));
        events.add(new EventItem("Cyclones Game", "Hilton Coliseum", tomorrowDate, "Entertainment", 120));
        events.add(new EventItem("Club Social", "Memorial Union", tomorrowDate, "Social", 35));

        return events;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // Event item model class
    public static class EventItem {
        private String title;
        private String location;
        private String date;
        private String category;
        private int attendees;
        private boolean isRsvped = false;

        public EventItem(String title, String location, String date, String category, int attendees) {
            this.title = title;
            this.location = location;
            this.date = date;
            this.category = category;
            this.attendees = attendees;
        }

        public String getTitle() {
            return title;
        }

        public String getLocation() {
            return location;
        }

        public String getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }

        public int getAttendees() {
            return attendees;
        }

        public boolean isRsvped() {
            return isRsvped;
        }

        public void setRsvped(boolean rsvped) {
            isRsvped = rsvped;
            if (rsvped) {
                attendees++;
            } else {
                attendees--;
            }
        }
    }
}