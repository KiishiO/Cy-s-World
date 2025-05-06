package onetoone.BusSystem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bus_system")
public class Bus {
    @Id
    @Column(nullable = false)
    private String busName;

    @Column(nullable = false, unique = true)
    private int busNum;

    @ElementCollection
    @CollectionTable(name = "bus_stop_locations", joinColumns = @JoinColumn(name = "bus_id"))
    @Column(name = "stop_location")
    private List<String> stopLocations = new ArrayList<>();

    @Column
    private String currentStopLocation;

    @Column
    private char busRating;

    @Column
    private LocalDateTime lastReportTime;

    // Real-time location data
    @Column
    private double latitude;

    @Column
    private double longitude;

    @Column
    private double speed;

    @Column
    private double bearing;

    // GTFS Integration fields
    @Column
    private String routeId;

    @Column
    private String vehicleId;

    @Column
    private String tripId;

    @Column
    private String nextStopId;

    @Column
    private LocalDateTime nextStopArrivalTime;

    // Default constructor needed for JPA
    public Bus() {
    }

    @NotBlank
    @Size(min = 0, max = 20)
    public Bus(int busNum, String busName, char busRating) {
        this.busNum = busNum;
        this.busName = busName;
        this.busRating = busRating;
        this.lastReportTime = LocalDateTime.now();
        // Set routeId to match busNum for integration with Iowa-GTFS API
        this.routeId = String.valueOf(busNum);
    }

    public LocalDateTime getLastReportTime() {
        return lastReportTime;
    }

    public void setLastReportTime(LocalDateTime lastReportTime) {
        this.lastReportTime = lastReportTime;
    }

    public void updateStopLocation(String newStopLocation) {
        this.currentStopLocation = newStopLocation;
        this.lastReportTime = LocalDateTime.now(); // Update time when stop location changes
    }

    // Real-time position update method
    public void updatePosition(double latitude, double longitude, double speed, double bearing) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.bearing = bearing;
        this.lastReportTime = LocalDateTime.now();
    }

    public char getBusRating(){
        return this.busRating;
    }

    public void setBusRating(char busRating){
        this.busRating = busRating;
    }

    public String getBusName(){
        return this.busName;
    }

    public void setBusName(String busName){
        this.busName = busName;
    }

    public String getCurrentStopLocation(){
        return this.currentStopLocation;
    }

    public void setStopLocation(String stopLocation){
        this.currentStopLocation = stopLocation;
    }

    public List<String> getStopLocations() {
        return stopLocations;
    }

    public void setStopLocations(List<String> stopLocations) {
        this.stopLocations = stopLocations;
    }

    public void addStopLocation(String stopLocation) {
        this.stopLocations.add(stopLocation);
    }

    public void removeStopLocation(String stopLocation) {
        this.stopLocations.remove(stopLocation);
    }

    public int getBusNum(){
        return this.busNum;
    }

    public void setBusNum(int busNum){
        this.busNum = busNum;
    }

    // Getters and setters for real-time location data
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    // Getters and setters for GTFS integration fields
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

    public String getNextStopId() {
        return nextStopId;
    }

    public void setNextStopId(String nextStopId) {
        this.nextStopId = nextStopId;
    }

    public LocalDateTime getNextStopArrivalTime() {
        return nextStopArrivalTime;
    }

    public void setNextStopArrivalTime(LocalDateTime nextStopArrivalTime) {
        this.nextStopArrivalTime = nextStopArrivalTime;
    }

    @Override
    public String toString() {
        return "Bus{" +
                "busName='" + busName + '\'' +
                ", busNum=" + busNum +
                ", stopLocations=" + stopLocations +
                ", currentStopLocation='" + currentStopLocation + '\'' +
                ", busRating=" + busRating +
                ", lastReportTime=" + lastReportTime +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", speed=" + speed +
                ", bearing=" + bearing +
                ", routeId='" + routeId + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", tripId='" + tripId + '\'' +
                ", nextStopId='" + nextStopId + '\'' +
                ", nextStopArrivalTime=" + nextStopArrivalTime +
                '}';
    }
}