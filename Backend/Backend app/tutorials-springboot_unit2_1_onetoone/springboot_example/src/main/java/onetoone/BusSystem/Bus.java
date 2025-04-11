package onetoone.BusSystem;
import jakarta.persistence.*;
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

    // Default constructor needed for JPA
    public Bus() {
    }

    public Bus(int busNum, String busName, char busRating) {
        this.busNum = busNum;
        this.busName = busName;
        this.busRating = busRating;
        this.lastReportTime = LocalDateTime.now();
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

    @Override
    public String toString() {
        return "Bus{" +
                "busName='" + busName + '\'' +
                ", busNum=" + busNum +
                ", stopLocations=" + stopLocations +
                ", currentStopLocation='" + currentStopLocation + '\'' +
                ", busRating=" + busRating +
                ", lastReportTime=" + lastReportTime +
                '}';
    }
}