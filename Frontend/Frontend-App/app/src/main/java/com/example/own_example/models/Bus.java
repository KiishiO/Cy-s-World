package com.example.own_example.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bus {
    private String busName;
    private int busNum;
    private String stopLocation;
    private char busRating;
    private String lastReportTime;

    // Default constructor
    public Bus() {
    }

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

    public String getLastReportTime() {
        return lastReportTime;
    }

    public void setLastReportTime(String lastReportTime) {
        this.lastReportTime = lastReportTime;
    }

    public char getBusRating() {
        return busRating;
    }

    public void setBusRating(char busRating) {
        this.busRating = busRating;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public String getStopLocation() {
        return stopLocation;
    }

    public void setStopLocation(String stopLocation) {
        this.stopLocation = stopLocation;
    }

    public int getBusNum() {
        return busNum;
    }

    public void setBusNum(int busNum) {
        this.busNum = busNum;
    }

    // Helper method to get the formatted bus rating as a string
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

    @Override
    public String toString() {
        return "Bus " + busNum + ": " + busName +
                " - Current Stop: " + (stopLocation.isEmpty() ? "None" : stopLocation);
    }
}