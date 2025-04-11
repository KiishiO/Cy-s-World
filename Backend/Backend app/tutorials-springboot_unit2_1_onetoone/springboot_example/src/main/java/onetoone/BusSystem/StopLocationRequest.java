package onetoone.BusSystem;

//Add class for getting the updated stopLocation for the put in BusController//
//Before the put
public class StopLocationRequest {
    private String currentLocation;


    public String getStopLocation() {
        return currentLocation;
    }
    public void setStopLocation(String currentLocation){
        this.currentLocation = currentLocation;
    }
}
