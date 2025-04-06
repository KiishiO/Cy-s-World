package com.example.own_example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.EventChatAdapter;
import com.example.own_example.models.CampusEvent;
import com.example.own_example.models.EventChat;
import com.example.own_example.services.EventWebSocketClient;
import com.example.own_example.services.UserService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity implements EventWebSocketClient.EventsListener {

    private static final String TAG = "EventDetailActivity";

    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView locationTextView;
    private TextView dateTimeTextView;
    private TextView creatorTextView;
    private TextView categoryTextView;
    private TextView attendeesTextView;
    private Button rsvpButton;
    private RecyclerView chatRecyclerView;
    private EditText chatInputEditText;
    private Button sendChatButton;
    private View chatInputLayout;

    private CampusEvent event;
    private List<EventChat> chatMessages = new ArrayList<>();
    private EventChatAdapter chatAdapter;
    private EventWebSocketClient webSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize views
        titleTextView = findViewById(R.id.event_title);
        descriptionTextView = findViewById(R.id.event_description);
        locationTextView = findViewById(R.id.event_location);
        dateTimeTextView = findViewById(R.id.event_datetime);
        creatorTextView = findViewById(R.id.event_creator);
        categoryTextView = findViewById(R.id.event_category);
        attendeesTextView = findViewById(R.id.event_attendees);
        rsvpButton = findViewById(R.id.rsvp_button);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatInputEditText = findViewById(R.id.chat_input);
        sendChatButton = findViewById(R.id.send_chat_button);
        chatInputLayout = findViewById(R.id.chat_input_layout);

        // Get event data from intent
        event = getEventFromIntent();

        if (event == null) {
            Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup chat recycler view
        chatAdapter = new EventChatAdapter(chatMessages, UserService.getInstance().getCurrentUsername());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Display event details
        displayEventDetails();

        // Set up WebSocket connection
        setupWebSocket();

        // Set up RSVP button
        rsvpButton.setOnClickListener(v -> toggleRsvp());

        // Show chat input only for admins
        boolean isAdmin = UserService.getInstance().isAdmin();
        chatInputLayout.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        // Set up send chat button (for admins only)
        if (isAdmin) {
            sendChatButton.setOnClickListener(v -> sendChatMessage());
        }
    }

    private CampusEvent getEventFromIntent() {
        // Extract event from intent extras
        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) {
            return null;
        }

        // Create event object from intent extras
        CampusEvent event = new CampusEvent();
        event.setId(eventId);
        event.setTitle(getIntent().getStringExtra("event_title"));
        event.setDescription(getIntent().getStringExtra("event_description"));
        event.setLocation(getIntent().getStringExtra("event_location"));
        event.setCategory(getIntent().getStringExtra("event_category"));
        event.setCreator(getIntent().getStringExtra("event_creator"));
        event.setAttendees(getIntent().getIntExtra("event_attendees", 0));
        event.setRsvped(getIntent().getBooleanExtra("event_rsvped", false));

        // Parse dates if they exist
        long startTimeMillis = getIntent().getLongExtra("event_start_time", -1);
        if (startTimeMillis != -1) {
            event.setStartTime(new Date(startTimeMillis));
        }

        long endTimeMillis = getIntent().getLongExtra("event_end_time", -1);
        if (endTimeMillis != -1) {
            event.setEndTime(new Date(endTimeMillis));
        }

        return event;
    }

    private void displayEventDetails() {
        titleTextView.setText(event.getTitle());
        descriptionTextView.setText(event.getDescription());
        locationTextView.setText(event.getLocation());

        // Format date and time
        String dateTimeText = event.getFormattedStartTime();
        if (event.getEndTime() != null) {
            dateTimeText += " - " + event.getFormattedEndTime();
        }
        dateTimeTextView.setText(dateTimeText);
        creatorTextView.setText("Posted by: " + event.getCreator());
        categoryTextView.setText("Category: " + event.getCategory());
        attendeesTextView.setText(event.getAttendees() + " attending");

        // Set RSVP button state
        updateRsvpButtonState();
    }

    private void setupWebSocket() {
        String username = UserService.getInstance().getCurrentUsername();
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Cannot set up WebSocket: username is null or empty");
            Toast.makeText(this, "Error: User not properly logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        webSocketClient = new EventWebSocketClient(this, username, this);
    }

    private void toggleRsvp() {
        if (webSocketClient != null && webSocketClient.isConnected()) {
            // Toggle RSVP state locally for immediate UI feedback
            event.setRsvped(!event.isRsvped());
            updateRsvpButtonState();

            // Send RSVP update to server
            webSocketClient.toggleRsvp(event.getId(), event.isRsvped());
        } else {
            Toast.makeText(this, "Cannot update RSVP: Not connected to server",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendChatMessage() {
        String message = chatInputEditText.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        if (webSocketClient != null && webSocketClient.isConnected()) {
            try {
                // Format the message as JSON to identify it as a chat message for an event
                org.json.JSONObject chatJson = new org.json.JSONObject();
                chatJson.put("type", "chat");
                chatJson.put("eventId", event.getId());
                chatJson.put("message", message);

                // Send the message through WebSocket
                webSocketClient.sendChatMessage(event.getId(), chatJson.toString());

                // Clear input field
                chatInputEditText.setText("");
            } catch (org.json.JSONException e) {
                Log.e(TAG, "Error creating chat JSON", e);
                Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Cannot send message: Not connected to server",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRsvpButtonState() {
        if (event.isRsvped()) {
            rsvpButton.setText("Cancel RSVP");
            rsvpButton.setBackgroundResource(R.drawable.rounded_button_gray);
        } else {
            rsvpButton.setText("RSVP");
            rsvpButton.setBackgroundResource(R.drawable.rounded_button_red);
        }
    }

    // EventWebSocketClient.EventsListener implementation

    @Override
    public void onEventsReceived(List<CampusEvent> events) {
        // This callback is more relevant for the main events list
        // For this activity, we're focused on a single event
        for (CampusEvent updatedEvent : events) {
            if (updatedEvent.getId().equals(event.getId())) {
                updateEventDetails(updatedEvent);
                break;
            }
        }
    }

    @Override
    public void onNewEventReceived(CampusEvent newEvent) {
        // Not relevant for the detail view
    }

    @Override
    public void onEventUpdated(CampusEvent updatedEvent) {
        if (updatedEvent.getId().equals(event.getId())) {
            updateEventDetails(updatedEvent);
        }
    }

    @Override
    public void onRsvpUpdated(String eventId, int attendees, boolean isRsvped) {
        if (event.getId().equals(eventId)) {
            runOnUiThread(() -> {
                event.setAttendees(attendees);
                event.setRsvped(isRsvped);
                attendeesTextView.setText(attendees + " attending");
                updateRsvpButtonState();
            });
        }
    }

    @Override
    public void onChatMessageReceived(EventChat chatMessage) {
        // Check if this chat message is related to this event
        if (event.getId().equals(chatMessage.getEventId()) || chatMessage.getEventId() == null) {
            runOnUiThread(() -> {
                // Add the message to our list
                chatMessages.add(chatMessage);
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                // Scroll to the bottom to show new message
                chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            });
        }
    }

    @Override
    public void onConnectionStateChanged(boolean connected) {
        runOnUiThread(() -> {
            if (connected) {
                Toast.makeText(this, "Connected to event server", Toast.LENGTH_SHORT).show();
                rsvpButton.setEnabled(true);
                if (UserService.getInstance().isAdmin()) {
                    sendChatButton.setEnabled(true);
                }
            } else {
                Toast.makeText(this, "Disconnected from event server", Toast.LENGTH_SHORT).show();
                rsvpButton.setEnabled(false);
                if (UserService.getInstance().isAdmin()) {
                    sendChatButton.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateEventDetails(CampusEvent updatedEvent) {
        runOnUiThread(() -> {
            // Update our local event copy
            event.setTitle(updatedEvent.getTitle());
            event.setDescription(updatedEvent.getDescription());
            event.setLocation(updatedEvent.getLocation());
            event.setStartTime(updatedEvent.getStartTime());
            event.setEndTime(updatedEvent.getEndTime());
            event.setCategory(updatedEvent.getCategory());
            event.setAttendees(updatedEvent.getAttendees());

            // Refresh the UI
            displayEventDetails();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }
}