package onetoone.BusSystem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request object for stop location operations
 */
class StopLocationRequest {
    private String stopLocation;

    public String getStopLocation() {
        return stopLocation;
    }

    public void setStopLocation(String stopLocation) {
        this.stopLocation = stopLocation;
    }
}

/**
 * Request object for bus rating operations
 */
class RatingRequest {
    private int busRating;

    public int getBusRating() {
        return busRating;
    }

    public void setBusRating(int busRating) {
        this.busRating = busRating;
    }
}

/**
 * Model representing a real-time bus position
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class RealTimeBusPosition {
    private String vehicleId;
    private String routeId;
    private String tripId;
    private double latitude;
    private double longitude;
    private String currentStopId;
    private String currentStopName;
    private double bearing;
    private double speed;
    private String timestamp;

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
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

    public String getCurrentStopId() {
        return currentStopId;
    }

    public void setCurrentStopId(String currentStopId) {
        this.currentStopId = currentStopId;
    }

    public String getCurrentStopName() {
        return currentStopName;
    }

    public void setCurrentStopName(String currentStopName) {
        this.currentStopName = currentStopName;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

/**
 * Model representing a bus stop
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class BusStop {
    private String stopId;
    private String stopName;
    private double latitude;
    private double longitude;
    private String stopCode;
    private List<String> routes;

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
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

    public String getStopCode() {
        return stopCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    public List<String> getRoutes() {
        return routes;
    }

    public void setRoutes(List<String> routes) {
        this.routes = routes;
    }
}

/**
 * Model representing arrival prediction for a bus at a stop
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class ArrivalPrediction {
    private String routeId;
    private String stopId;
    private String stopName;
    private String tripId;
    private String vehicleId;
    private String arrivalTime;
    private int minutesAway;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getMinutesAway() {
        return minutesAway;
    }

    public void setMinutesAway(int minutesAway) {
        this.minutesAway = minutesAway;
    }
}

/**
 * Response object for real-time bus information
 */
class RealTimeBusInfo {
    private int busNum;
    private String busName;
    private RealTimeBusPosition position;
    private String nextStopName;
    private int minutesToNextStop;
    private List<String> upcomingStops;

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

    public RealTimeBusPosition getPosition() {
        return position;
    }

    public void setPosition(RealTimeBusPosition position) {
        this.position = position;
    }

    public String getNextStopName() {
        return nextStopName;
    }

    public void setNextStopName(String nextStopName) {
        this.nextStopName = nextStopName;
    }

    public int getMinutesToNextStop() {
        return minutesToNextStop;
    }

    public void setMinutesToNextStop(int minutesToNextStop) {
        this.minutesToNextStop = minutesToNextStop;
    }

    public List<String> getUpcomingStops() {
        return upcomingStops;
    }

    public void setUpcomingStops(List<String> upcomingStops) {
        this.upcomingStops = upcomingStops;
    }
}