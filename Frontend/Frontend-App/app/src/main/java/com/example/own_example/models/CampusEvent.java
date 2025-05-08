package com.example.own_example.models;

import java.util.Date;

/**
 * Model class representing a campus event.
 * Contains all information about an event including its details, timing, and attendance.
 */
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

    /**
     * Default constructor for CampusEvent.
     * Initializes an empty event object.
     */
    public CampusEvent() {
    }

    /**
     * Constructor with essential fields for creating a basic event.
     *
     * @param id The unique identifier for the event
     * @param title The title of the event
     * @param description The description of the event
     * @param location The location where the event will be held
     * @param startTime The start date and time of the event
     * @param category The category of the event (e.g., Career, Academic, Entertainment, Social)
     */
    public CampusEvent(String id, String title, String description, String location,
                       Date startTime, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.category = category;
    }

    /**
     * Full constructor for creating a complete event with all details.
     *
     * @param id The unique identifier for the event
     * @param title The title of the event
     * @param description The description of the event
     * @param location The location where the event will be held
     * @param startTime The start date and time of the event
     * @param endTime The end date and time of the event
     * @param creator The username of the event creator
     * @param category The category of the event (e.g., Career, Academic, Entertainment, Social)
     * @param attendees The number of attendees who have RSVP'd for the event
     * @param isRsvped Whether the current user has RSVP'd for this event
     */
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

    /**
     * Gets the unique identifier of the event.
     *
     * @return The event ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the event.
     *
     * @param id The event ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the title of the event.
     *
     * @return The event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the event.
     *
     * @param title The event title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the description of the event.
     *
     * @return The event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     *
     * @param description The event description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the location of the event.
     *
     * @return The event location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the event.
     *
     * @param location The event location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the start date and time of the event.
     *
     * @return The start date and time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the start date and time of the event.
     *
     * @param startTime The start date and time to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end date and time of the event.
     *
     * @return The end date and time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the end date and time of the event.
     *
     * @param endTime The end date and time to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the username of the event creator.
     *
     * @return The creator's username
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the username of the event creator.
     *
     * @param creator The creator's username to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Gets the category of the event.
     *
     * @return The event category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the event.
     *
     * @param category The event category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the number of attendees who have RSVP'd for the event.
     *
     * @return The number of attendees
     */
    public int getAttendees() {
        return attendees;
    }

    /**
     * Sets the number of attendees who have RSVP'd for the event.
     *
     * @param attendees The number of attendees to set
     */
    public void setAttendees(int attendees) {
        this.attendees = attendees;
    }

    /**
     * Checks if the current user has RSVP'd for this event.
     *
     * @return true if the current user has RSVP'd, false otherwise
     */
    public boolean isRsvped() {
        return isRsvped;
    }

    /**
     * Sets whether the current user has RSVP'd for this event.
     *
     * @param rsvped true if the current user has RSVP'd, false otherwise
     */
    public void setRsvped(boolean rsvped) {
        isRsvped = rsvped;
    }

    /**
     * Gets a formatted string representation of the start time for display.
     * Format: "Day, Month Date, Year at Hour:Minute AM/PM"
     *
     * @return The formatted start time string, or "TBD" if start time is null
     */
    public String getFormattedStartTime() {
        if (startTime == null) {
            return "TBD";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", java.util.Locale.US);
        return sdf.format(startTime);
    }

    /**
     * Gets a formatted date string of the start time in yyyy-MM-dd format.
     * Used for date filtering and comparison.
     *
     * @return The formatted start date string, or "TBD" if start time is null
     */
    public String getFormattedStartDate() {
        if (startTime == null) {
            return "TBD";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        return sdf.format(startTime);
    }

    /**
     * Gets a formatted string representation of the end time for display.
     * Format: "Day, Month Date, Year at Hour:Minute AM/PM"
     *
     * @return The formatted end time string, or "TBD" if end time is null
     */
    public String getFormattedEndTime() {
        if (endTime == null) {
            return "TBD";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", java.util.Locale.US);
        return sdf.format(endTime);
    }
}