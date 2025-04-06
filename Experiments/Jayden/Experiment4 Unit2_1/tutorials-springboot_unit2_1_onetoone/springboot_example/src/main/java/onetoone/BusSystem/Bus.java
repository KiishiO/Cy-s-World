package onetoone.BusSystem;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bus_system")
public class Bus {
    @Id
    @Column(nullable = false)
    private String busName;

    @Column(nullable = false)
    private int busNum;

    @Column
    private String stopLocation;

    @Column
    private char busRating;

    @Column
    private LocalDateTime lastReportTime;

    // Default constructor needed for JPA
    public Bus() {
    }

    public Bus(int busNum, String busName, String stopLocation, char busRating) {
        this.busNum = busNum;
        this.busName = busName;
        this.stopLocation = stopLocation;
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
        this.stopLocation = newStopLocation;
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

    public String getStopLocation(){
        return this.stopLocation;
    }

    public void setStopLocation(String stopLocation){
        this.stopLocation = stopLocation;
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
                ", stopLocation='" + stopLocation + '\'' +
                ", busRating=" + busRating +
                ", lastReportTime=" + lastReportTime +
                '}';
    }
}