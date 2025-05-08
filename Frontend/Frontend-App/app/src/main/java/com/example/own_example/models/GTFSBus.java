package com.example.own_example.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model class representing a bus from GTFS data
 */
public class GTFSBus {
    private int busNum;
    private String busName;
    private String stopLocation;
    private double latitude;
    private double longitude;
    private float bearing;
    private float speed;
    private String routeId;
    private String vehicleId;
    private String tripId;
    private boolean inService;
    private String nextStop;
    private long predictedArrivalTime;
    private char busRating;

    // Default constructor
    public GTFSBus() {
        this.inService = true;
        this.bearing = 0f;
        this.busRating = 'C';
    }

    /**
     * Format the predicted arrival time as a readable string
     * @return Formatted time string
     */
    public String getFormattedArrivalTime() {
        if (predictedArrivalTime <= 0) {
            return "Unknown";
        }

        // Convert seconds to milliseconds
        Date date = new Date(predictedArrivalTime * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    // Getters and Setters
    public int getBusNum() {
        return busNum;
    }

    public void setBusNum(int busNum) {
        this.busNum = busNum;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public boolean isInService() {
        return inService;
    }

    public void setInService(boolean inService) {
        this.inService = inService;
    }

    public String getNextStop() {
        return nextStop;
    }

    public void setNextStop(String nextStop) {
        this.nextStop = nextStop;
    }

    public long getPredictedArrivalTime() {
        return predictedArrivalTime;
    }

    public void setPredictedArrivalTime(long predictedArrivalTime) {
        this.predictedArrivalTime = predictedArrivalTime;
    }

    public char getBusRating() {
        return busRating;
    }

    public void setBusRating(char busRating) {
        this.busRating = busRating;
    }

    @Override
    public String toString() {
        return "Bus " + busNum + ": " + busName +
                " (Route " + routeId + ")" +
                " at " + stopLocation;
    }
}