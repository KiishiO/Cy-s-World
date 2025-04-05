package com.example.own_example.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.own_example.models.CampusEvent;
import com.example.own_example.models.EventChat;

import org.json.JSONArray;
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

public class EventWebSocketClient {
    private static final String TAG = "EventWebSocketClient";
    private static final String WS_BASE_URL = "ws://your-backend-url/events/";
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final int RECONNECT_DELAY = 5000; // 5 seconds

    private WebSocket webSocket;
    private OkHttpClient client;
    private EventsListener eventsListener;
    private String username;
    private Handler mainHandler;
    private boolean reconnectOnClose = true;

    private List<CampusEvent> allEvents = new ArrayList<>();
    private List<EventChat> eventChats = new ArrayList<>();
    private boolean isConnected = false;

    public interface EventsListener {
        void onEventsReceived(List<CampusEvent> events);
        void onNewEventReceived(CampusEvent event);
        void onEventUpdated(CampusEvent event);
        void onRsvpUpdated(String eventId, int attendees, boolean isRsvped);
        void onChatMessageReceived(EventChat chatMessage);
        void onConnectionStateChanged(boolean connected);
        void onError(String errorMessage);
    }

    public EventWebSocketClient(Context context, String username, EventsListener listener) {
        this.username = username;
        this.eventsListener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());

        // Create OkHttp client with reasonable timeouts
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        connectWebSocket();
    }

    private void connectWebSocket() {
        String url = WS_BASE_URL + username;
        Request request = new Request.Builder().url(url).build();

        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket connection opened for user: " + username);
                isConnected = true;
                notifyConnectionStateChanged(true);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Message received: " + text);
                processMessage(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Not expected to be used in this implementation
                Log.d(TAG, "Binary message received: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closing: " + reason);
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + reason);
                isConnected = false;
                notifyConnectionStateChanged(false);

                if (reconnectOnClose) {
                    mainHandler.postDelayed(() -> connectWebSocket(), RECONNECT_DELAY);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                String errorMessage = t.getMessage();
                Log.e(TAG, "WebSocket error: " + errorMessage, t);
                isConnected = false;
                notifyConnectionStateChanged(false);

                if (eventsListener != null) {
                    String message = "Connection error: " +
                            (errorMessage != null ? errorMessage : "Unknown error");
                    eventsListener.onError(message);
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

    private void processMessage(String message) {
        try {
            if (message.startsWith("Welcome to")) {
                // Welcome message, no processing needed
                Log.d(TAG, "Welcome message received: " + message);
            }
            else if (message.startsWith("=== CAMPUS EVENTS & CHAT HISTORY ===")) {
                // Process events history
                processEventsHistory(message);
            }
            else if (message.startsWith("NEW_EVENT: ")) {
                // Process new event notification
                String eventJson = message.substring(11); // Remove "NEW_EVENT: " prefix
                processNewEvent(eventJson);
            }
            else if (message.startsWith("💬 ")) {
                // Process chat message
                processChatMessage(message);
            }
            else if (message.startsWith("RSVP_UPDATE: ")) {
                // Process RSVP update (you'll need to implement this on the backend)
                String rsvpJson = message.substring(12); // Remove "RSVP_UPDATE: " prefix
                processRsvpUpdate(rsvpJson);
            }
            else {
                Log.d(TAG, "Unprocessed message: " + message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing WebSocket message", e);
        }
    }

    private void processEventsHistory(String historyText) {
        // Parse the formatted text history into events and chat messages
        List<CampusEvent> events = new ArrayList<>();
        List<EventChat> chats = new ArrayList<>();

        // Split by double newline to separate events
        String[] entries = historyText.split("\n\n");

        for (String entry : entries) {
            if (entry.startsWith("📢 NEW EVENT NOTIFICATION")) {
                CampusEvent event = parseEventFromText(entry);
                if (event != null) {
                    events.add(event);
                }
            } else if (entry.startsWith("💬 ")) {
                EventChat chat = parseChatFromText(entry);
                if (chat != null) {
                    chats.add(chat);
                }
            }
        }

        // Update local cache
        allEvents.clear();
        allEvents.addAll(events);

        eventChats.clear();
        eventChats.addAll(chats);

        // Notify listeners on main thread
        mainHandler.post(() -> {
            if (eventsListener != null) {
                eventsListener.onEventsReceived(events);

                // Notify about chat messages
                for (EventChat chat : chats) {
                    eventsListener.onChatMessageReceived(chat);
                }
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

    private EventChat parseChatFromText(String chatText) {
        try {
            // Format is: "💬 username: message [time]"
            String content = chatText.substring(chatText.indexOf(" ") + 1);

            // Extract username
            String username = content.substring(0, content.indexOf(":"));

            // Extract message
            String message = content.substring(content.indexOf(":") + 2, content.lastIndexOf("[") - 1);

            // Extract time
            String timeStr = content.substring(content.lastIndexOf("[") + 1, content.lastIndexOf("]"));

            EventChat chat = new EventChat();
            chat.setUsername(username);
            chat.setMessage(message);

            // Parse time
            SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.US);
            try {
                chat.setTimestamp(formatter.parse(timeStr));
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing chat time: " + e.getMessage());
                chat.setTimestamp(new Date());
            }

            return chat;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing chat text: " + e.getMessage());
            return null;
        }
    }

    private void processNewEvent(String eventJson) {
        try {
            JSONObject jsonObject = new JSONObject(eventJson);

            CampusEvent event = new CampusEvent();
            event.setId(jsonObject.getString("id"));
            event.setTitle(jsonObject.getString("title"));
            event.setDescription(jsonObject.getString("description"));
            event.setLocation(jsonObject.getString("location"));

            // Parse dates from ISO format
            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            String startTimeStr = jsonObject.getString("startTime");
            String endTimeStr = jsonObject.optString("endTime");

            try {
                event.setStartTime(iso8601Format.parse(startTimeStr));
                if (endTimeStr != null && !endTimeStr.isEmpty()) {
                    event.setEndTime(iso8601Format.parse(endTimeStr));
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing event date: " + e.getMessage());
            }

            event.setCreator(jsonObject.getString("creator"));
            event.setCategory(jsonObject.getString("category"));

            // Add to local cache
            allEvents.add(event);

            // Notify listener on main thread
            mainHandler.post(() -> {
                if (eventsListener != null) {
                    eventsListener.onNewEventReceived(event);
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing event JSON: " + e.getMessage());
        }
    }

    private void processChatMessage(String chatMessage) {
        EventChat chat = parseChatFromText(chatMessage);
        if (chat != null) {
            eventChats.add(chat);

            // Notify listener on main thread
            mainHandler.post(() -> {
                if (eventsListener != null) {
                    eventsListener.onChatMessageReceived(chat);
                }
            });
        }
    }

    private void processRsvpUpdate(String rsvpJson) {
        try {
            JSONObject jsonObject = new JSONObject(rsvpJson);

            String eventId = jsonObject.getString("eventId");
            int attendees = jsonObject.getInt("attendees");
            boolean isRsvped = jsonObject.getBoolean("isRsvped");

            // Notify listener on main thread
            mainHandler.post(() -> {
                if (eventsListener != null) {
                    eventsListener.onRsvpUpdated(eventId, attendees, isRsvped);
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing RSVP update JSON: " + e.getMessage());
        }
    }

    public void sendChatMessage(String eventId, String message) {
        if (webSocket != null && isConnected) {
            // Format depends on your backend implementation
            webSocket.send(message);
        } else {
            Log.e(TAG, "Cannot send message - WebSocket not connected");
            if (eventsListener != null) {
                mainHandler.post(() -> {
                    eventsListener.onError("Cannot send message - not connected to server");
                });
            }
        }
    }

    public void toggleRsvp(String eventId, boolean isRsvping) {
        if (webSocket != null && isConnected) {
            try {
                JSONObject request = new JSONObject();
                request.put("action", "toggle_rsvp");
                request.put("eventId", eventId);
                request.put("isRsvping", isRsvping);
                webSocket.send(request.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error creating RSVP request", e);
            }
        } else {
            Log.e(TAG, "Cannot toggle RSVP - WebSocket not connected");
            if (eventsListener != null) {
                mainHandler.post(() -> {
                    eventsListener.onError("Cannot update RSVP - not connected to server");
                });
            }
        }
    }

    public void createEvent(CampusEvent event) {
        if (webSocket != null && isConnected) {
            try {
                JSONObject jsonEvent = new JSONObject();
                jsonEvent.put("title", event.getTitle());
                jsonEvent.put("description", event.getDescription());
                jsonEvent.put("location", event.getLocation());

                // Format dates to ISO format
                SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                if (event.getStartTime() != null) {
                    jsonEvent.put("startTime", iso8601Format.format(event.getStartTime()));
                }
                if (event.getEndTime() != null) {
                    jsonEvent.put("endTime", iso8601Format.format(event.getEndTime()));
                }

                jsonEvent.put("category", event.getCategory());
                // Creator will be set by the server based on the connected user

                webSocket.send(jsonEvent.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error creating event JSON", e);
            }
        } else {
            Log.e(TAG, "Cannot create event - WebSocket not connected");
            if (eventsListener != null) {
                mainHandler.post(() -> {
                    eventsListener.onError("Cannot create event - not connected to server");
                });
            }
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
        return new ArrayList<>(allEvents);
    }

    public List<EventChat> getCachedChats() {
        return new ArrayList<>(eventChats);
    }
}