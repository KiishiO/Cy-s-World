package com.example.own_example.models;

import java.util.Date;

public class EventChat {
    private String id;
    private String eventId;
    private String username;
    private String message;
    private Date timestamp;
    private boolean isAdminMessage;

    // Default constructor
    public EventChat() {
        this.timestamp = new Date();
    }

    // Constructor with essential fields
    public EventChat(String eventId, String username, String message) {
        this.eventId = eventId;
        this.username = username;
        this.message = message;
        this.timestamp = new Date();
    }

    // Full constructor
    public EventChat(String id, String eventId, String username, String message,
                     Date timestamp, boolean isAdminMessage) {
        this.id = id;
        this.eventId = eventId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.isAdminMessage = isAdminMessage;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAdminMessage() {
        return isAdminMessage;
    }

    public void setAdminMessage(boolean adminMessage) {
        isAdminMessage = adminMessage;
    }

    // Utility method to get formatted time for display
    public String getFormattedTime() {
        if (timestamp == null) {
            return "";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.US);
        return sdf.format(timestamp);
    }
}