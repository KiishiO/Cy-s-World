package coms309.BusSystem;

import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.awt.color.ColorSpace;
import java.util.Calendar;
import java.util.TimeZone;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/busOpt")
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
    public String updateStopLocation(@PathVariable String busName, @RequestBody String newStopLocation) {
        for (Bus bus : busList) {
            if (bus.getBusName().equalsIgnoreCase(busName)) {  // Allow case-insensitive comparison
                bus.updateStopLocation(newStopLocation);
                return "Bus stop location updated to " + newStopLocation + " at " + bus.getLastReportTime();
            }
        }
        throw new RuntimeException("Bus not found");
    }

    // Delete a bus by Name
    @DeleteMapping("/{busName}")
    public String deleteBus(@PathVariable String busName) {
        busList.removeIf(bus -> bus.getBusName().equals(busName));
        return "Bus deleted successfully!";
    }

}
