package onetoone.BusSystem;

//Add class for getting the updated stopLocation for the put in BusController//
//Before the put
public class StopLocationRequest {
    private String stopLocation;


    public String getStopLocation() {
        return stopLocation;
    }
    public void setStopLocation(String stopLocation){
        this.stopLocation = stopLocation;
    }
}
