package com.example.own_example.models;

import java.util.Date;

public class CampusEvent {
    private String id;
    private String title;
    private String description;
    private String location;
    private Date startTime;
    private Date endTime;
    private String creator;
    private String category;
    private int attendees;
    private boolean isRsvped;

    // Default constructor
    public CampusEvent() {
    }

    // Constructor with essential fields
    public CampusEvent(String id, String title, String description, String location,
                       Date startTime, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.category = category;
    }

    // Full constructor
    public CampusEvent(String id, String title, String description, String location,
                       Date startTime, Date endTime, String creator, String category,
                       int attendees, boolean isRsvped) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.creator = creator;
        this.category = category;
        this.attendees = attendees;
        this.isRsvped = isRsvped;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getAttendees() {
        return attendees;
    }

    public void setAttendees(int attendees) {
        this.attendees = attendees;
    }

    public boolean isRsvped() {
        return isRsvped;
    }

    public void setRsvped(boolean rsvped) {
        isRsvped = rsvped;
    }

    // Utility method to get a formatted date string for display
    public String getFormattedStartTime() {
        if (startTime == null) {
            return "TBD";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", java.util.Locale.US);
        return sdf.format(startTime);
    }

    public String getFormattedStartDate() {
        if (startTime == null) {
            return "TBD";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        return sdf.format(startTime);
    }

    public String getFormattedEndTime() {
        if (endTime == null) {
            return "TBD";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", java.util.Locale.US);
        return sdf.format(endTime);
    }
}