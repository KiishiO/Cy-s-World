package com.example.own_example.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.own_example.models.CampusEvent;
import com.example.own_example.models.EventChat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class AdminEventService {
    private static final String TAG = "AdminEventService";
    private static final String WS_BASE_URL = "ws://coms-3090-017.class.las.iastate.edu:8080/events/";
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final int RECONNECT_DELAY = 5000; // 5 seconds

    private WebSocket webSocket;
    private OkHttpClient client;
    private AdminEventsListener eventsListener;
    private String adminUsername;
    private Handler mainHandler;
    private boolean reconnectOnClose = true;
    private boolean isConnected = false;
    private List<CampusEvent> cachedEvents = new ArrayList<>();

    public interface AdminEventsListener {
        void onEventsUpdated(List<CampusEvent> events);
        void onEventCreated(CampusEvent newEvent);
        void onEventUpdated(CampusEvent updatedEvent);
        void onEventDeleted(String eventId);
        void onChatMessageSent(EventChat chatMessage);
        void onConnectionStateChanged(boolean connected);
        void onError(String errorMessage);
    }

    public AdminEventService(Context context, String adminUsername, AdminEventsListener listener) {
        this.adminUsername = adminUsername;
        this.eventsListener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());

        // Check username
        if (adminUsername == null || adminUsername.isEmpty()) {
            Log.e(TAG, "Cannot initialize AdminEventService: username is null or empty");
            notifyError("Username error: Admin username is missing");
            return;
        }

        // Create OkHttp client with reasonable timeouts
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        connectWebSocket();
    }

    private void connectWebSocket() {
        String url = WS_BASE_URL + adminUsername;
        Request request = new Request.Builder().url(url).build();

        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "Admin WebSocket connection opened");
                isConnected = true;
                notifyConnectionStateChanged(true);
            }

            @Override
            public void onMessage(WebSocket webSocket, String message) {
                Log.d(TAG, "Admin received: " + message);
                processMessage(message);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Not expected to be used in this implementation
                Log.d(TAG, "Binary message received: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Admin WebSocket closing: " + reason);
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Admin WebSocket closed: " + reason);
                isConnected = false;
                notifyConnectionStateChanged(false);

                if (reconnectOnClose) {
                    mainHandler.postDelayed(() -> connectWebSocket(), RECONNECT_DELAY);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                String errorMessage = t.getMessage();
                Log.e(TAG, "Admin WebSocket error: " + errorMessage, t);
                isConnected = false;
                notifyConnectionStateChanged(false);

                if (eventsListener != null) {
                    String message = "Connection error: " +
                            (errorMessage != null ? errorMessage : "Unknown error");
                    notifyError(message);
                }

                if (reconnectOnClose) {
                    mainHandler.postDelayed(() -> connectWebSocket(), RECONNECT_DELAY);
                }
            }
        };

        webSocket = client.newWebSocket(request, webSocketListener);
    }

    private void notifyConnectionStateChanged(boolean connected) {
        mainHandler.post(() -> {
            if (eventsListener != null) {
                eventsListener.onConnectionStateChanged(connected);
            }
        });
    }

    private void notifyError(String errorMessage) {
        mainHandler.post(() -> {
            if (eventsListener != null) {
                eventsListener.onError(errorMessage);
            }
        });
    }

    private void processMessage(String message) {
        try {
            if (message.startsWith("Welcome to")) {
                // Welcome message, no processing needed
                Log.d(TAG, "Welcome message received: " + message);
            }
            else if (message.startsWith("=== CAMPUS EVENTS & CHAT HISTORY ===")) {
                // Process events history from the server
                processEventsHistory(message);
            }
            else if (message.startsWith("NEW_EVENT: ")) {
                // Process new event notification
                String eventJson = message.substring(11); // Remove "NEW_EVENT: " prefix
                try {
                    JSONObject jsonObject = new JSONObject(eventJson);
                    CampusEvent newEvent = parseEventFromJson(jsonObject);
                    cachedEvents.add(newEvent);

                    mainHandler.post(() -> {
                        if (eventsListener != null) {
                            eventsListener.onEventCreated(newEvent);
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing new event JSON", e);
                }
            }
            else if (message.startsWith("EVENT_UPDATED: ")) {
                // Handle event updated notification
                String eventJson = message.substring(15); // Remove "EVENT_UPDATED: " prefix
                try {
                    JSONObject jsonObject = new JSONObject(eventJson);
                    CampusEvent updatedEvent = parseEventFromJson(jsonObject);

                    // Update in cached list
                    for (int i = 0; i < cachedEvents.size(); i++) {
                        if (cachedEvents.get(i).getId().equals(updatedEvent.getId())) {
                            cachedEvents.set(i, updatedEvent);
                            break;
                        }
                    }

                    mainHandler.post(() -> {
                        if (eventsListener != null) {
                            eventsListener.onEventUpdated(updatedEvent);
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing updated event JSON", e);
                }
            }
            else if (message.startsWith("EVENT_DELETED: ")) {
                // Handle event deleted notification
                String eventId = message.substring(15); // Remove "EVENT_DELETED: " prefix

                // Remove from cached list
                cachedEvents.removeIf(event -> event.getId().equals(eventId));

                mainHandler.post(() -> {
                    if (eventsListener != null) {
                        eventsListener.onEventDeleted(eventId);
                    }
                });
            }
            else if (message.contains("successfully sent")) {
                // Chat message confirmation
                Log.d(TAG, "Message sent confirmation: " + message);
            }
            else {
                Log.d(TAG, "Unprocessed admin message: " + message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing admin WebSocket message", e);
        }
    }

    private void processEventsHistory(String historyText) {
        // Parse the formatted text history into events
        List<CampusEvent> events = new ArrayList<>();

        // Split by double newline to separate events
        String[] entries = historyText.split("\n\n");

        for (String entry : entries) {
            if (entry.startsWith("📢 NEW EVENT NOTIFICATION")) {
                CampusEvent event = parseEventFromText(entry);
                if (event != null) {
                    events.add(event);
                }
            }
        }

        // Update cached events
        cachedEvents.clear();
        cachedEvents.addAll(events);

        // Notify listener
        mainHandler.post(() -> {
            if (eventsListener != null) {
                eventsListener.onEventsUpdated(events);
            }
        });
    }

    private CampusEvent parseEventFromText(String eventText) {
        try {
            // Extract ID
            String idLine = eventText.substring(eventText.indexOf("#") + 1,
                    eventText.indexOf("\n━━━"));
            Long id = Long.parseLong(idLine.trim());

            // Extract title
            String titleLine = eventText.substring(eventText.indexOf("📌 Title: ") + 9);
            String title = titleLine.substring(0, titleLine.indexOf("\n")).trim();

            // Extract description
            String descLine = eventText.substring(eventText.indexOf("📝 Description: ") + 15);
            String description = descLine.substring(0, descLine.indexOf("\n")).trim();

            // Extract location
            String locLine = eventText.substring(eventText.indexOf("📍 Location: ") + 12);
            String location = locLine.substring(0, locLine.indexOf("\n")).trim();

            // Extract start time
            String startLine = eventText.substring(eventText.indexOf("🕒 Starts: ") + 10);
            String startTimeStr = startLine.substring(0, startLine.indexOf("\n")).trim();

            // Extract end time
            String endLine = eventText.substring(eventText.indexOf("🕓 Ends: ") + 9);
            String endTimeStr = endLine.substring(0, endLine.indexOf("\n")).trim();

            // Extract creator
            String creatorLine = eventText.substring(eventText.indexOf("👤 Posted by: ") + 13);
            String creator = creatorLine.substring(0, creatorLine.indexOf("\n")).trim();

            // Extract category
            String catLine = eventText.substring(eventText.indexOf("🏷️ Category: ") + 13);
            String category = catLine.substring(0, catLine.indexOf("\n")).trim();

            // Create event object
            CampusEvent event = new CampusEvent();
            event.setId(id.toString());
            event.setTitle(title);
            event.setDescription(description);
            event.setLocation(location);

            // Parse dates
            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.US);
            try {
                if (!startTimeStr.equals("N/A")) {
                    event.setStartTime(formatter.parse(startTimeStr));
                }
                if (!endTimeStr.equals("N/A")) {
                    event.setEndTime(formatter.parse(endTimeStr));
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + e.getMessage());
            }

            event.setCreator(creator);
            event.setCategory(category);

            return event;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing event text: " + e.getMessage());
            return null;
        }
    }

    private CampusEvent parseEventFromJson(JSONObject jsonObject) throws JSONException {
        CampusEvent event = new CampusEvent();

        event.setId(jsonObject.getString("id"));
        event.setTitle(jsonObject.getString("title"));
        event.setDescription(jsonObject.getString("description"));
        event.setLocation(jsonObject.getString("location"));
        event.setCreator(jsonObject.getString("creator"));
        event.setCategory(jsonObject.getString("category"));

        // Parse dates from ISO format
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        try {
            String startTimeStr = jsonObject.getString("startTime");
            event.setStartTime(iso8601Format.parse(startTimeStr));

            if (jsonObject.has("endTime") && !jsonObject.isNull("endTime")) {
                String endTimeStr = jsonObject.getString("endTime");
                event.setEndTime(iso8601Format.parse(endTimeStr));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing JSON date: " + e.getMessage());
        }

        return event;
    }

    public void createEvent(String title, String description, String location,
                            Date startTime, Date endTime, String category) {
        if (webSocket != null && isConnected) {
            try {
                // Create event JSON object according to backend expectations
                JSONObject eventJson = new JSONObject();
                eventJson.put("title", title);
                eventJson.put("description", description);
                eventJson.put("location", location);

                // Format dates to ISO format
                SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

                if (startTime != null) {
                    eventJson.put("startTime", iso8601Format.format(startTime));
                }

                if (endTime != null) {
                    eventJson.put("endTime", iso8601Format.format(endTime));
                }

                eventJson.put("category", category);
                // Creator will be set by the server based on the connected user

                // Send the event creation message
                webSocket.send(eventJson.toString());

            } catch (JSONException e) {
                Log.e(TAG, "Error creating event JSON", e);
                notifyError("Error creating event: " + e.getMessage());
            }
        } else {
            notifyError("Cannot create event: Not connected to server");
        }
    }

    public void updateEvent(String eventId, String title, String description, String location,
                            Date startTime, Date endTime, String category) {
        if (webSocket != null && isConnected) {
            try {
                // Create command to update an event
                JSONObject updateCommand = new JSONObject();
                updateCommand.put("action", "update_event");
                updateCommand.put("eventId", eventId);

                // Create event details
                JSONObject eventDetails = new JSONObject();
                eventDetails.put("title", title);
                eventDetails.put("description", description);
                eventDetails.put("location", location);

                // Format dates to ISO format
                SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

                if (startTime != null) {
                    eventDetails.put("startTime", iso8601Format.format(startTime));
                }

                if (endTime != null) {
                    eventDetails.put("endTime", iso8601Format.format(endTime));
                }

                eventDetails.put("category", category);

                updateCommand.put("eventDetails", eventDetails);

                // Send the update command
                webSocket.send(updateCommand.toString());

            } catch (JSONException e) {
                Log.e(TAG, "Error creating update event JSON", e);
                notifyError("Error updating event: " + e.getMessage());
            }
        } else {
            notifyError("Cannot update event: Not connected to server");
        }
    }

    public void deleteEvent(String eventId) {
        if (webSocket != null && isConnected) {
            try {
                // Create command to delete an event
                JSONObject deleteCommand = new JSONObject();
                deleteCommand.put("action", "delete_event");
                deleteCommand.put("eventId", eventId);

                // Send the delete command
                webSocket.send(deleteCommand.toString());

            } catch (JSONException e) {
                Log.e(TAG, "Error creating delete event JSON", e);
                notifyError("Error deleting event: " + e.getMessage());
            }
        } else {
            notifyError("Cannot delete event: Not connected to server");
        }
    }

    public void sendEventUpdate(String eventId, String message) {
        if (webSocket != null && isConnected) {
            try {
                // Create chat message for an event
                JSONObject chatCommand = new JSONObject();
                chatCommand.put("type", "chat");
                chatCommand.put("eventId", eventId);
                chatCommand.put("message", message);

                // Send the chat message
                webSocket.send(chatCommand.toString());

                // Create local event chat object for the listener
                EventChat chatMessage = new EventChat(eventId, adminUsername, message);
                chatMessage.setAdminMessage(true);

                // Notify listener
                mainHandler.post(() -> {
                    if (eventsListener != null) {
                        eventsListener.onChatMessageSent(chatMessage);
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "Error creating chat message JSON", e);
                notifyError("Error sending event update: " + e.getMessage());
            }
        } else {
            notifyError("Cannot send event update: Not connected to server");
        }
    }

    public void disconnect() {
        reconnectOnClose = false;
        if (webSocket != null) {
            webSocket.close(NORMAL_CLOSURE_STATUS, "Closing connection");
            webSocket = null;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public List<CampusEvent> getCachedEvents() {
        return new ArrayList<>(cachedEvents);
    }
}