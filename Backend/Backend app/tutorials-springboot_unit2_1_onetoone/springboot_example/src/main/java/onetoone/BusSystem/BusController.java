package onetoone.BusSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/busOpt")
public class BusController {

    @Autowired
    private busRepository busRepository;

    // Create a new bus
    @PostMapping("/add")
    public ResponseEntity<String> addBus(@RequestBody Bus bus) {
        Optional<Bus> existingBusWithNumber = busRepository.findByBusNum(bus.getBusNum());
        if (existingBusWithNumber.isPresent()) {
            return ResponseEntity.badRequest().body("A bus with number " + bus.getBusNum() + " already exists");
        }

        Optional<Bus> existingBusWithName = busRepository.findByBusName(bus.getBusName());
        if (existingBusWithName.isPresent()) {
            return ResponseEntity.badRequest().body("A bus with name " + bus.getBusName() + " already exists");
        }

        bus.setLastReportTime(LocalDateTime.now());
        busRepository.save(bus);
        return ResponseEntity.ok("A new Bus has been added successfully: " + bus.getBusName() +
                " (Time: " + bus.getFormattedLastReportTime() + ")");
    }

    // Get all buses
    @GetMapping("/all")
    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    // Get a bus by number - with formatted time
    @GetMapping("/{busNum}")
    public ResponseEntity<?> getBusByNumber(@PathVariable int busNum) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("busName", bus.getBusName());
            response.put("busNum", bus.getBusNum());
            response.put("stopLocations", bus.getStopLocations());
            response.put("currentStopLocation", bus.getCurrentStopLocation());
            response.put("busRating", bus.getBusRating());
            response.put("lastReportTime", bus.getLastReportTime());
            response.put("formattedLastReportTime", bus.getFormattedLastReportTime());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update the bus rating
    @PutMapping("/{busNum}/rating")
    public ResponseEntity<String> updateBusRating(@PathVariable int busNum, @RequestBody RatingRequest ratingRequest) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            bus.setBusRating(ratingRequest.getBusRating());
            bus.setLastReportTime(LocalDateTime.now());
            busRepository.save(bus);
            return ResponseEntity.ok("Bus rating updated successfully to " + ratingRequest.getBusRating() +
                    " at " + bus.getFormattedLastReportTime());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update stop location with formatted time
    @PutMapping("/{busNum}/updateStop")
    public ResponseEntity<String> updateStopLocation(@PathVariable int busNum, @RequestBody StopLocationRequest stopLocationRequest) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            bus.updateStopLocation(stopLocationRequest.getStopLocation());
            busRepository.save(bus);
            return ResponseEntity.ok("Stop location updated to: " + stopLocationRequest.getStopLocation() +
                    " at " + bus.getFormattedLastReportTime());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a bus by Number
    @DeleteMapping("/{busNum}")
    public ResponseEntity<String> deleteBus(@PathVariable int busNum) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            busRepository.delete(busOptional.get());
            return ResponseEntity.ok("Bus deleted successfully!");
        } else {
            return ResponseEntity.ok("That is not a valid bus!");
        }
    }

    @DeleteMapping("removeStop/{stopLocation}")
    public ResponseEntity<String> deleteLocation(@PathVariable String stopLocation) {
        List<Bus> buses = busRepository.findByCurrentStopLocation(stopLocation);
        if (!buses.isEmpty()) {
            Bus bus = buses.get(0);
            bus.setStopLocation("");
            bus.setLastReportTime(LocalDateTime.now());
            busRepository.save(bus);
            return ResponseEntity.ok("Bus stop Location has been removed from the bus: " + bus.getBusName() +
                    " at " + bus.getFormattedLastReportTime());
        }
        return ResponseEntity.ok("Bus stop was not found for this bus!");
    }

    // Add a new stop location to a bus route - with formatted time
    @PostMapping("/{busNum}/addRouteStop")
    public ResponseEntity<String> addRouteStop(@PathVariable int busNum, @RequestBody StopLocationRequest stopLocationRequest) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            String stopLocation = stopLocationRequest.getStopLocation();

            if (bus.getStopLocations().contains(stopLocation)) {
                return ResponseEntity.badRequest().body("Stop location already exists in the route");
            }

            bus.addStopLocation(stopLocation);
            bus.setLastReportTime(LocalDateTime.now());
            busRepository.save(bus);
            return ResponseEntity.ok("Stop location added to route: " + stopLocation +
                    " at " + bus.getFormattedLastReportTime());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Remove a stop location from a bus route - with formatted time
    @DeleteMapping("/{busNum}/removeRouteStop")
    public ResponseEntity<String> removeRouteStop(@PathVariable int busNum, @RequestBody StopLocationRequest stopLocationRequest) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            String stopLocation = stopLocationRequest.getStopLocation();

            if (bus.getStopLocations().contains(stopLocation)) {
                bus.removeStopLocation(stopLocation);
                bus.setLastReportTime(LocalDateTime.now());
                busRepository.save(bus);
                return ResponseEntity.ok("Stop location removed from route: " + stopLocation +
                        " at " + bus.getFormattedLastReportTime());
            } else {
                return ResponseEntity.badRequest().body("Stop location not found in the route");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get all stops for a specific bus route
    @GetMapping("/{busNum}/routeStops")
    public ResponseEntity<?> getBusRouteStops(@PathVariable int busNum) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("busName", bus.getBusName());
            response.put("busNum", bus.getBusNum());
            response.put("stopLocations", bus.getStopLocations());
            response.put("lastUpdated", bus.getFormattedLastReportTime());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Dashboard with formatted time
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardSummary() {
        List<Bus> buses = busRepository.findAll();
        List<Map<String, Object>> formattedBuses = new ArrayList<>();

        for(Bus bus : buses) {
            Map<String, Object> formattedBus = new HashMap<>();
            formattedBus.put("busName", bus.getBusName());
            formattedBus.put("busNum", bus.getBusNum());
            formattedBus.put("currentLocation", bus.getCurrentStopLocation());
            formattedBus.put("busRating", bus.getBusRating());
            formattedBus.put("stopCount", bus.getStopLocations().size());
            formattedBus.put("lastUpdated", bus.getFormattedLastReportTime());

            formattedBuses.add(formattedBus);
        }

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalBuses", buses.size());
        dashboard.put("buses", formattedBuses);
        dashboard.put("generatedAt", LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm:ss")));

        return ResponseEntity.ok(dashboard);
    }
}