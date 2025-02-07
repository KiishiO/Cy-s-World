package coms309.BusSystem;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.awt.color.ColorSpace;

@RestController
@RequestMapping("/busOpt") //Must initial after the port number to use this class//
public class BusController {

    //Initializes busList which holds information of buses optional//
    private final List<Bus> busList = new ArrayList<>();

    // Create a new bus
    @PostMapping("/add")
    public String addBus(@RequestBody Bus bus) {
        busList.add(bus);
        return "A new Bus has been added successfully: " + bus.getBusName();
    }


    // Get all buses
    @GetMapping("/all")
    public List<Bus> getAllBuses() {
        return busList;
    }

    // Get a bus by number
    @GetMapping("/{busNum}")
    public Bus getBusByNumber(@PathVariable int busNum) {
        return busList.stream()
                .filter(bus -> bus.getBusNum() == busNum)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bus not found"));
    }

    // Update the bus rating
    @PutMapping("/{busName}/rating")
    public String updateBusRating(@PathVariable String busName, @RequestBody RatingRequest ratingRequest) {
        char busRating = ratingRequest.getBusRating();
        for (Bus bus : busList) {
            if (bus.getBusName().equalsIgnoreCase(busName)) {
                bus.setBusRating(busRating);
                return "Bus rating updated successfully to " + busRating;
            }
        }
        throw new RuntimeException("Bus not found");
    }

    @PutMapping("/{busName}/updateStop")
    public String updateStopLocation(@PathVariable String busName, @RequestBody StopLocationRequest stopLocationRequest) {
        String newStopLocation = stopLocationRequest.getStopLocation();
        for (Bus bus : busList) {
            if (bus.getBusName().equalsIgnoreCase(busName)) {
                bus.updateStopLocation(newStopLocation);
                return "Stop location updated to: " + newStopLocation + " at " + bus.getLastReportTime();
            }
        }
        throw new RuntimeException("Bus not found");
    }

    // Delete a bus by Name
    @DeleteMapping("/{busName}")
    public String deleteBus(@PathVariable String busName) {
        boolean removed = busList.removeIf(bus -> bus.getBusName().equalsIgnoreCase(busName));

        if (removed) {
            return "Bus deleted successfully!";
        } else {
            return "That is not a valid bus!";
        }
    }

    @DeleteMapping("removeStop/{stopLocation}")
    public String deleteLocation(@PathVariable String stopLocation){
        for(Bus bus : busList){
            if(bus.getStopLocation().equalsIgnoreCase(stopLocation)){
                bus.setStopLocation("");
                return "Bus stop Location has been removed from the bus: " + bus.getBusName();
            }
        }
    return "Bus stop was not found for this bus!";
    }


}
