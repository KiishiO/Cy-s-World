package coms309.BusSystem;

import java.time.LocalDateTime;


public class Bus {

    private String busName;

    private int busNum;

    private String stopLocation;

    private char busRating;

    private LocalDateTime lastReportTime; // New field


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

    public String toSpring(){
        return busName + "" +
                stopLocation + "" +
                busNum + "" +
                busRating;
    }











}
