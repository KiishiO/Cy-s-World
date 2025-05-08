package onetoone.BusSystem;

import java.time.LocalDateTime;
import java.util.List;

// DTO for vehicle position data from GTFS-RT
class VehiclePositionDTO {
    private String vehicleId;
    private String routeId;
    private String tripId;
    private double latitude;
    private double longitude;
    private double bearing;
    private double speed;
    private int currentStopSequence;
    private String currentStatus;
    private LocalDateTime timestamp;

    // Getters and setters
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

    public int getCurrentStopSequence() {
        return currentStopSequence;
    }

    public void setCurrentStopSequence(int currentStopSequence) {
        this.currentStopSequence = currentStopSequence;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

// DTO for stop time update in trip updates
class StopTimeUpdateDTO {
    private String stopId;
    private int stopSequence;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;

    // Getters and setters
    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
}

// DTO for trip updates from GTFS-RT
class TripUpdateDTO {
    private String tripId;
    private String routeId;
    private String vehicleId;
    private List<StopTimeUpdateDTO> stopTimeUpdates;

    // Getters and setters
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
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

    public List<StopTimeUpdateDTO> getStopTimeUpdates() {
        return stopTimeUpdates;
    }

    public void setStopTimeUpdates(List<StopTimeUpdateDTO> stopTimeUpdates) {
        this.stopTimeUpdates = stopTimeUpdates;
    }
}

// DTO for affected entities in service alerts
class AffectedEntityDTO {
    private String routeId;
    private String stopId;
    private String tripId;

    // Getters and setters
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

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}

// DTO for service alerts from GTFS-RT
class ServiceAlertDTO {
    private String id;
    private List<AffectedEntityDTO> affectedEntities;
    private String headerText;
    private String descriptionText;
    private String cause;
    private String effect;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<AffectedEntityDTO> getAffectedEntities() {
        return affectedEntities;
    }

    public void setAffectedEntities(List<AffectedEntityDTO> affectedEntities) {
        this.affectedEntities = affectedEntities;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }
}

// Request models used by existing API
class RatingRequest {
    private int busRating;

    public int getBusRating() {
        return busRating;
    }

    public void setBusRating(int busRating) {
        this.busRating = busRating;
    }
}

class StopLocationRequest {
    private String stopLocation;

    public String getStopLocation() {
        return stopLocation;
    }

    public void setStopLocation(String stopLocation) {
        this.stopLocation = stopLocation;
    }
}