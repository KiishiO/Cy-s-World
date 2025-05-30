package onetoone.BusSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private busRepository busRepository;

    // Create a new bus
    @PostMapping("/add")
    public String addBus(@RequestBody Bus bus) {
        busRepository.save(bus);
        return "A new Bus has been added successfully: " + bus.getBusName();
    }


    // Get all buses
    @GetMapping("/all")
    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    // Get a bus by number
    @GetMapping("/{busNum}")
    public ResponseEntity<Bus> getBusByNumber(@PathVariable int busNum) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            return ResponseEntity.ok(busOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update the bus rating
    @PutMapping("/{busName}/rating")
    public ResponseEntity<String> updateBusRating(@PathVariable String busName, @RequestBody RatingRequest ratingRequest) {
        Optional<Bus> busOptional = busRepository.findByBusName(busName);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            bus.setBusRating(ratingRequest.getBusRating());
            busRepository.save(bus);
            return ResponseEntity.ok("Bus rating updated successfully to " + ratingRequest.getBusRating());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{busName}/updateStop")
    public ResponseEntity<String> updateStopLocation(@PathVariable String busName, @RequestBody StopLocationRequest stopLocationRequest) {
        Optional<Bus> busOptional = busRepository.findByBusName(busName);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            bus.updateStopLocation(stopLocationRequest.getStopLocation());
            busRepository.save(bus);
            return ResponseEntity.ok("Stop location updated to: " + stopLocationRequest.getStopLocation() + " at " + bus.getLastReportTime());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a bus by Name
    @DeleteMapping("/{busName}")
    public ResponseEntity<String> deleteBus(@PathVariable String busName) {
        Optional<Bus> busOptional = busRepository.findByBusName(busName);
        if (busOptional.isPresent()) {
            busRepository.delete(busOptional.get());
            return ResponseEntity.ok("Bus deleted successfully!");
        } else {
            return ResponseEntity.ok("That is not a valid bus!");
        }
    }

    @DeleteMapping("removeStop/{stopLocation}")
    public ResponseEntity<String> deleteLocation(@PathVariable String stopLocation) {
        List<Bus> buses = busRepository.findByStopLocation(stopLocation);
        if (!buses.isEmpty()) {
            Bus bus = buses.get(0);
            bus.setStopLocation("");
            busRepository.save(bus);
            return ResponseEntity.ok("Bus stop Location has been removed from the bus: " + bus.getBusName());
        }
        return ResponseEntity.ok("Bus stop was not found for this bus!");
    }
}
