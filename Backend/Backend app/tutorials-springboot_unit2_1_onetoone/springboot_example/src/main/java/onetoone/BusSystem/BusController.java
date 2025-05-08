package onetoone.BusSystem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/busOpt")
@Tag(name = "Bus System", description = "Bus Management API with Real-Time CyRide Integration")
public class BusController {

    @Autowired
    private busRepository busRepository;

    @Autowired
    private IowaGtfsService iowaGtfsService;

    @Operation(summary = "Add a new bus", description = "Creates a new bus with unique bus number and name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bus successfully added"),
            @ApiResponse(responseCode = "400", description = "Bus with specified number or name already exists")
    })
    @PostMapping("/add")
    public ResponseEntity<String> addBus(
            @Parameter(description = "Bus object to be added", required = true)
            @RequestBody Bus bus) {
        // Check if bus with this number already exists
        Optional<Bus> existingBusWithNumber = busRepository.findByBusNum(bus.getBusNum());
        if (existingBusWithNumber.isPresent()) {
            return ResponseEntity.badRequest().body("A bus with number " + bus.getBusNum() + " already exists");
        }

        // Check if bus with this name already exists
        Optional<Bus> existingBusWithName = busRepository.findByBusName(bus.getBusName());
        if (existingBusWithName.isPresent()) {
            return ResponseEntity.badRequest().body("A bus with name " + bus.getBusName() + " already exists");
        }

        bus.setLastReportTime(LocalDateTime.now());
        // Set routeId to match busNum for integration with Iowa-GTFS API
        bus.setRouteId(String.valueOf(bus.getBusNum()));
        busRepository.save(bus);
        return ResponseEntity.ok("A new Bus has been added successfully: " + bus.getBusName());
    }

    @Operation(summary = "Get all buses", description = "Retrieves a list of all buses in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all buses",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Bus.class)))
    @GetMapping("/all")
    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    @Operation(summary = "Get bus by number", description = "Retrieves a specific bus by its unique number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the bus"),
            @ApiResponse(responseCode = "404", description = "Bus with specified number not found")
    })
    @GetMapping("/{busNum}")
    public ResponseEntity<Bus> getBusByNumber(
            @Parameter(description = "Bus number to retrieve", required = true)
            @PathVariable int busNum) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            return ResponseEntity.ok(busOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update bus rating", description = "Updates the rating of a specific bus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bus rating successfully updated"),
            @ApiResponse(responseCode = "404", description = "Bus with specified name not found")
    })
    @PutMapping("/{busName}/rating")
    public ResponseEntity<String> updateBusRating(
            @Parameter(description = "Name of the bus to update", required = true)
            @PathVariable String busName,
            @Parameter(description = "New rating value", required = true)
            @RequestBody RatingRequest ratingRequest) {
        Optional<Bus> busOptional = busRepository.findByBusName(busName);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            // Fix: Convert int to char for bus rating
            bus.setBusRating((char) ('0' + ratingRequest.getBusRating()));
            busRepository.save(bus);
            return ResponseEntity.ok("Bus rating updated successfully to " + ratingRequest.getBusRating());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update bus stop location", description = "Updates the current stop location of a bus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stop location successfully updated"),
            @ApiResponse(responseCode = "404", description = "Bus with specified name not found")
    })
    @PutMapping("/{busName}/updateStop")
    public ResponseEntity<String> updateStopLocation(
            @Parameter(description = "Name of the bus to update", required = true)
            @PathVariable String busName,
            @Parameter(description = "New stop location", required = true)
            @RequestBody StopLocationRequest stopLocationRequest) {
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

    @Operation(summary = "Delete bus by name", description = "Removes a bus from the system by its name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bus successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Bus not found")
    })
    @DeleteMapping("/{busName}")
    public ResponseEntity<String> deleteBus(
            @Parameter(description = "Name of the bus to delete", required = true)
            @PathVariable String busName) {
        Optional<Bus> busOptional = busRepository.findByBusName(busName);
        if (busOptional.isPresent()) {
            busRepository.delete(busOptional.get());
            return ResponseEntity.ok("Bus deleted successfully!");
        } else {
            // Fix: Return 404 status for not found condition
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Remove stop location", description = "Removes stop location from a bus route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stop location removed"),
            @ApiResponse(responseCode = "404", description = "Stop not found")
    })
    @DeleteMapping("/removeStop/{stopLocation}")
    public ResponseEntity<String> deleteLocation(
            @Parameter(description = "Stop location to remove", required = true)
            @PathVariable String stopLocation) {
        List<Bus> buses = busRepository.findByStopLocation(stopLocation);
        if (!buses.isEmpty()) {
            Bus bus = buses.get(0);
            bus.setStopLocation("");
            busRepository.save(bus);
            return ResponseEntity.ok("Bus stop Location has been removed from the bus: " + bus.getBusName());
        }
        // Fix: Return 404 for not found condition
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Add stop to bus route", description = "Adds a new stop location to a specific bus route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stop location successfully added to route"),
            @ApiResponse(responseCode = "400", description = "Stop location already exists in route"),
            @ApiResponse(responseCode = "404", description = "Bus with specified number not found")
    })
    @PostMapping("/{busNum}/addRouteStop")
    public ResponseEntity<String> addRouteStop(
            @Parameter(description = "Bus number", required = true)
            @PathVariable int busNum,
            @Parameter(description = "Stop location to add", required = true)
            @RequestBody StopLocationRequest stopLocationRequest) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            String stopLocation = stopLocationRequest.getStopLocation();

            // Check if stop already exists in route
            if (bus.getStopLocations().contains(stopLocation)) {
                return ResponseEntity.badRequest().body("Stop location already exists in the route");
            }

            bus.addStopLocation(stopLocation);
            busRepository.save(bus);
            return ResponseEntity.ok("Stop location added to route: " + stopLocation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Remove stop from bus route", description = "Removes a stop location from a specific bus route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stop location successfully removed from route"),
            @ApiResponse(responseCode = "400", description = "Stop location not found in route"),
            @ApiResponse(responseCode = "404", description = "Bus with specified number not found")
    })
    @DeleteMapping("/{busNum}/removeRouteStop")
    public ResponseEntity<String> removeRouteStop(
            @Parameter(description = "Bus number", required = true)
            @PathVariable int busNum,
            @Parameter(description = "Stop location to remove", required = true)
            @RequestBody StopLocationRequest stopLocationRequest) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            String stopLocation = stopLocationRequest.getStopLocation();

            if (bus.getStopLocations().contains(stopLocation)) {
                bus.removeStopLocation(stopLocation);
                busRepository.save(bus);
                return ResponseEntity.ok("Stop location removed from route: " + stopLocation);
            } else {
                return ResponseEntity.badRequest().body("Stop location not found in the route");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get all stops for a bus route", description = "Retrieves all stop locations for a specific bus route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all stops for the route"),
            @ApiResponse(responseCode = "404", description = "Bus with specified number not found")
    })
    @GetMapping("/{busNum}/routeStops")
    public ResponseEntity<?> getBusRouteStops(
            @Parameter(description = "Bus number", required = true)
            @PathVariable int busNum) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            return ResponseEntity.ok(bus.getStopLocations());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GTFS Real-time API Endpoints

    @Operation(summary = "Get all real-time vehicle positions",
            description = "Retrieves current positions of all buses from GTFS-RT feed")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle positions",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = VehiclePositionDTO.class)))
    @GetMapping("/realtime/vehiclePositions")
    public ResponseEntity<List<VehiclePositionDTO>> getAllVehiclePositions() {
        try {
            List<VehiclePositionDTO> positions = iowaGtfsService.getVehiclePositions();
            return ResponseEntity.ok(positions);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get all trip updates",
            description = "Retrieves arrival and departure predictions for all buses from GTFS-RT feed")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved trip updates",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TripUpdateDTO.class)))
    @GetMapping("/realtime/tripUpdates")
    public ResponseEntity<List<TripUpdateDTO>> getAllTripUpdates() {
        try {
            List<TripUpdateDTO> tripUpdates = iowaGtfsService.getTripUpdates();
            return ResponseEntity.ok(tripUpdates);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get all service alerts",
            description = "Retrieves service disruptions, detours and other alerts from GTFS-RT feed")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved service alerts",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ServiceAlertDTO.class)))
    @GetMapping("/realtime/alerts")
    public ResponseEntity<List<ServiceAlertDTO>> getAllServiceAlerts() {
        try {
            List<ServiceAlertDTO> alerts = iowaGtfsService.getServiceAlerts();
            return ResponseEntity.ok(alerts);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get real-time data for specific bus",
            description = "Retrieves real-time position, trip updates and alerts for a specific bus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved real-time data"),
            @ApiResponse(responseCode = "404", description = "Bus with specified number not found")
    })
    @GetMapping("/{vehicleId}/realtime")
    public ResponseEntity<Map<String, Object>> getBusRealTimeDataByVehicleId(
            @Parameter(description = "GTFS Vehicle ID", required = true)
            @PathVariable String vehicleId) {

        Map<String, Object> realTimeData = new HashMap<>();

        try {
            // Get all current vehicle positions
            List<VehiclePositionDTO> positions = iowaGtfsService.getVehiclePositions();

            // Find the one matching the requested vehicleId
            Optional<VehiclePositionDTO> match = positions.stream()
                    .filter(pos -> vehicleId.equals(pos.getVehicleId()))
                    .findFirst();

            if (match.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            VehiclePositionDTO vehicle = match.get();
            realTimeData.put("position", vehicle);

            // You can now use vehicle.getRouteId() to fetch other info:
            String routeId = vehicle.getRouteId();

            // Get trip updates for this vehicle's route
            List<TripUpdateDTO> tripUpdates = iowaGtfsService.getTripUpdatesByRouteId(routeId);
            if (!tripUpdates.isEmpty()) {
                realTimeData.put("tripUpdates", tripUpdates);
            }

            // Get service alerts affecting this route
            List<ServiceAlertDTO> alerts = iowaGtfsService.getServiceAlertsByRouteId(routeId);
            if (!alerts.isEmpty()) {
                realTimeData.put("alerts", alerts);
            }

            return ResponseEntity.ok(realTimeData);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Find nearest buses",
            description = "Finds buses nearest to the provided coordinates")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved nearest buses",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Bus.class)))
    @GetMapping("/nearestBuses")
    public ResponseEntity<List<Bus>> getNearestBuses(
            @Parameter(description = "Latitude coordinate", required = true)
            @RequestParam double latitude,
            @Parameter(description = "Longitude coordinate", required = true)
            @RequestParam double longitude,
            @Parameter(description = "Maximum number of buses to return", required = false)
            @RequestParam(required = false, defaultValue = "5") int limit) {
        try {
            List<Bus> nearestBuses = iowaGtfsService.findNearestBuses(latitude, longitude, limit);

            return ResponseEntity.ok(nearestBuses);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get active buses",
            description = "Retrieves all buses that are currently active/in service")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved active buses",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Bus.class)))
    @GetMapping("/activeBuses")
    public ResponseEntity<List<Bus>> getActiveBuses() {
        try {
            List<Bus> activeBuses = iowaGtfsService.getActiveBuses();
            return ResponseEntity.ok(activeBuses);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get predicted arrival times",
            description = "Retrieves predicted arrival times for a specific stop")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved arrival predictions"),
            @ApiResponse(responseCode = "404", description = "Stop not found")
    })
    @GetMapping("/stop/{stopId}/arrivals")
    public ResponseEntity<List<Map<String, Object>>> getPredictedArrivals(
            @Parameter(description = "Stop ID", required = true)
            @PathVariable String stopId) {
        try {
            List<Map<String, Object>> arrivals = iowaGtfsService.getPredictedArrivals(stopId);
            if (arrivals.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(arrivals);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Manually update GTFS-RT data",
            description = "Triggers an immediate refresh of all GTFS real-time data")
    @ApiResponse(responseCode = "200", description = "Successfully updated GTFS-RT data")
    @PostMapping("/realtime/refresh")
    public ResponseEntity<String> refreshRealTimeData() {
        try {
            iowaGtfsService.refreshRealTimeData();
            return ResponseEntity.ok("Real-time data successfully refreshed");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to refresh real-time data: " + e.getMessage());
        }
    }


}