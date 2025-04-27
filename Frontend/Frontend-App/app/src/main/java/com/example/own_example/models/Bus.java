/**
 * Represents a campus bus entity in the CyWorld application.
 * This class contains information about bus routes, locations, ratings, and reporting times.
 *
 * @author Jawad Ali
 */
package com.example.own_example.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bus {
    /** Name of the bus route (e.g., "Red West", "Gold South") */
    private String busName;

    /** Unique identifier number for the bus */
    private int busNum;

    /** Current stop location of the bus */
    private String stopLocation;

    /** Rating of the bus service quality (A-F scale) */
    private char busRating;

    /** Timestamp of when this bus information was last updated */
    private String lastReportTime;

    /**
     * Default constructor that creates an empty Bus object.
     */
    public Bus() {
    }

    /**
     * Creates a new Bus instance with the specified details.
     * Automatically sets the current time as the last report time.
     *
     * @param busNum The unique identifier for the bus
     * @param busName The name of the bus route
     * @param stopLocation The current stop location of the bus
     * @param busRating The quality rating of the bus (A-F scale)
     */
    public Bus(int busNum, String busName, String stopLocation, char busRating) {
        this.busNum = busNum;
        this.busName = busName;
        this.stopLocation = stopLocation;
        this.busRating = busRating;

        // Set current time as last report time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.lastReportTime = now.format(formatter);
    }

    /**
     * Gets the timestamp when this bus information was last updated.
     *
     * @return The last report time as a formatted string
     */
    public String getLastReportTime() {
        return lastReportTime;
    }

    /**
     * Sets the timestamp when this bus information was last updated.
     *
     * @param lastReportTime The formatted timestamp string
     */
    public void setLastReportTime(String lastReportTime) {
        this.lastReportTime = lastReportTime;
    }

    /**
     * Gets the quality rating of the bus.
     *
     * @return The bus rating as a character (A-F scale)
     */
    public char getBusRating() {
        return busRating;
    }

    /**
     * Sets the quality rating of the bus.
     *
     * @param busRating The bus rating as a character (A-F scale)
     */
    public void setBusRating(char busRating) {
        this.busRating = busRating;
    }

    /**
     * Gets the name of the bus route.
     *
     * @return The bus route name
     */
    public String getBusName() {
        return busName;
    }

    /**
     * Sets the name of the bus route.
     *
     * @param busName The bus route name
     */
    public void setBusName(String busName) {
        this.busName = busName;
    }

    /**
     * Gets the current stop location of the bus.
     *
     * @return The stop location name
     */
    public String getStopLocation() {
        return stopLocation;
    }

    /**
     * Sets the current stop location of the bus.
     *
     * @param stopLocation The stop location name
     */
    public void setStopLocation(String stopLocation) {
        this.stopLocation = stopLocation;
    }

    /**
     * Gets the unique identifier number of the bus.
     *
     * @return The bus identifier number
     */
    public int getBusNum() {
        return busNum;
    }

    /**
     * Sets the unique identifier number of the bus.
     *
     * @param busNum The bus identifier number
     */
    public void setBusNum(int busNum) {
        this.busNum = busNum;
    }

    /**
     * Converts the bus rating character to a human-readable description.
     *
     * @return A string representation of the bus rating quality
     */
    public String getFormattedRating() {
        switch (busRating) {
            case 'A':
                return "Excellent";
            case 'B':
                return "Good";
            case 'C':
                return "Average";
            case 'D':
                return "Poor";
            case 'F':
                return "Critical";
            default:
                return "Unrated";
        }
    }

    /**
     * Returns a string representation of the Bus object.
     *
     * @return A string containing the bus number, name, and current stop
     */
    @Override
    public String toString() {
        return "Bus " + busNum + ": " + busName +
                " - Current Stop: " + (stopLocation.isEmpty() ? "None" : stopLocation);
    }
}