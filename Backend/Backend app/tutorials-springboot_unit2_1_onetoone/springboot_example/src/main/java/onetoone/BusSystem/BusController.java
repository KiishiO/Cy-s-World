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

    // ============= REAL-TIME API ENDPOINTS =============

    @Operation(summary = "Get real-time bus positions", description = "Retrieves real-time locations of all buses from the Iowa-GTFS API")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved real-time bus positions")
    @GetMapping("/realtime/positions")
    public ResponseEntity<Map<String, Object>> getRealTimeBusPositions() {
        try {
            Map<String, Object> vehiclePositions = iowaGtfsService.getVehiclePositions();
            return ResponseEntity.ok(vehiclePositions);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch real-time bus positions");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Get real-time bus position by route", description = "Retrieves real-time location of buses on a specific route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved real-time bus position"),
            @ApiResponse(responseCode = "404", description = "Route not found or no active buses on route")
    })
    @GetMapping("/realtime/positions/{routeId}")
    public ResponseEntity<Map<String, Object>> getRealTimeBusPositionByRoute(
            @Parameter(description = "Route ID", required = true)
            @PathVariable String routeId) {
        try {
            Map<String, Object> vehiclePosition = iowaGtfsService.getVehiclePositionByRoute(routeId);
            return ResponseEntity.ok(vehiclePosition);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch real-time bus position for route: " + routeId);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Get all bus stops", description = "Retrieves all bus stops from the Iowa-GTFS API")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all bus stops")
    @GetMapping("/realtime/stops")
    public ResponseEntity<List<Map<String, Object>>> getAllBusStops() {
        try {
            List<Map<String, Object>> stops = iowaGtfsService.getAllStops();
            return ResponseEntity.ok(stops);
        } catch (Exception e) {
            // Fix: Add error information instead of empty list
            List<Map<String, Object>> errorList = new ArrayList<>();
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", "Failed to fetch bus stops");
            errorInfo.put("message", e.getMessage());
            errorList.add(errorInfo);
            return ResponseEntity.status(500).body(errorList);
        }
    }

    @Operation(summary = "Get bus stop by ID", description = "Retrieves details of a specific bus stop by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bus stop details"),
            @ApiResponse(responseCode = "404", description = "Bus stop not found")
    })
    @GetMapping("/realtime/stops/{stopId}")
    public ResponseEntity<Map<String, Object>> getBusStopById(
            @Parameter(description = "Stop ID", required = true)
            @PathVariable String stopId) {
        try {
            Map<String, Object> stop = iowaGtfsService.getStopById(stopId);
            if (stop == null || stop.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(stop);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch bus stop with ID: " + stopId);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Get trip updates", description = "Retrieves real-time trip updates (arrival predictions) for all routes")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved trip updates")
    @GetMapping("/realtime/tripUpdates")
    public ResponseEntity<Map<String, Object>> getTripUpdates() {
        try {
            Map<String, Object> tripUpdates = iowaGtfsService.getTripUpdates();
            return ResponseEntity.ok(tripUpdates);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch trip updates");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Get trip updates by route", description = "Retrieves real-time trip updates for a specific route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved trip updates for route"),
            @ApiResponse(responseCode = "404", description = "Route not found or no active trips")
    })
    @GetMapping("/realtime/tripUpdates/{routeId}")
    public ResponseEntity<Map<String, Object>> getTripUpdatesByRoute(
            @Parameter(description = "Route ID", required = true)
            @PathVariable String routeId) {
        try {
            Map<String, Object> tripUpdates = iowaGtfsService.getTripUpdatesByRoute(routeId);
            if (tripUpdates == null || tripUpdates.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(tripUpdates);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch trip updates for route: " + routeId);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Get all routes", description = "Retrieves all bus routes from the Iowa-GTFS API")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all routes")
    @GetMapping("/realtime/routes")
    public ResponseEntity<List<Map<String, Object>>> getAllRoutes() {
        try {
            List<Map<String, Object>> routes = iowaGtfsService.getAllRoutes();
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            // Fix: Add error information instead of empty list
            List<Map<String, Object>> errorList = new ArrayList<>();
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", "Failed to fetch routes");
            errorInfo.put("message", e.getMessage());
            errorList.add(errorInfo);
            return ResponseEntity.status(500).body(errorList);
        }
    }

    @Operation(summary = "Get route by ID", description = "Retrieves details of a specific bus route by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved route details"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @GetMapping("/realtime/routes/{routeId}")
    public ResponseEntity<Map<String, Object>> getRouteById(
            @Parameter(description = "Route ID", required = true)
            @PathVariable String routeId) {
        try {
            Map<String, Object> route = iowaGtfsService.getRouteById(routeId);
            if (route == null || route.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(route);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch route with ID: " + routeId);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Get next stop ETA", description = "Gets the estimated time of arrival to the next stop for a specific bus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ETA information"),
            @ApiResponse(responseCode = "404", description = "Bus with specified number not found")
    })
    @GetMapping("/{busNum}/nextStopETA")
    public ResponseEntity<Map<String, Object>> getNextStopETA(
            @Parameter(description = "Bus number", required = true)
            @PathVariable int busNum) {
        try {
            Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
            if (busOptional.isPresent()) {
                Map<String, Object> etaInfo = iowaGtfsService.getNextStopETA(busNum);
                return ResponseEntity.ok(etaInfo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch next stop ETA for bus: " + busNum);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Sync bus with real-time data", description = "Manually triggers synchronization of a specific bus with real-time data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bus successfully synchronized with real-time data"),
            @ApiResponse(responseCode = "404", description = "Bus with specified number not found")
    })
    @PostMapping("/{busNum}/syncRealTime")
    public ResponseEntity<String> syncBusWithRealTimeData(
            @Parameter(description = "Bus number", required = true)
            @PathVariable int busNum) {
        Optional<Bus> busOptional = busRepository.findByBusNum(busNum);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            String routeId = String.valueOf(bus.getBusNum());

            try {
                // Get vehicle position data
                Map<String, Object> vehiclePosition = iowaGtfsService.getVehiclePositionByRoute(routeId);

                if (vehiclePosition != null && vehiclePosition.containsKey("entity")) {
                    // Fix: Use safe casting with type checking
                    Object entityObj = vehiclePosition.get("entity");
                    if (entityObj instanceof List<?>) {
                        List<?> entities = (List<?>) entityObj;

                        if (!entities.isEmpty() && entities.get(0) instanceof Map) {
                            Map<?, ?> entity = (Map<?, ?>) entities.get(0);
                            Object vehicleObj = entity.get("vehicle");

                            if (vehicleObj instanceof Map) {
                                Map<?, ?> vehicle = (Map<?, ?>) vehicleObj;
                                Object positionObj = vehicle.get("position");

                                if (positionObj instanceof Map) {
                                    Map<?, ?> position = (Map<?, ?>) positionObj;

                                    // Extract and update position data with safe conversions
                                    if (position.get("latitude") instanceof Number &&
                                            position.get("longitude") instanceof Number) {

                                        double latitude = ((Number) position.get("latitude")).doubleValue();
                                        double longitude = ((Number) position.get("longitude")).doubleValue();
                                        double bearing = position.containsKey("bearing") && position.get("bearing") instanceof Number ?
                                                ((Number) position.get("bearing")).doubleValue() : 0.0;
                                        double speed = position.containsKey("speed") && position.get("speed") instanceof Number ?
                                                ((Number) position.get("speed")).doubleValue() : 0.0;

                                        bus.updatePosition(latitude, longitude, speed, bearing);

                                        // Update vehicle and trip IDs
                                        if (vehicle.containsKey("vehicle") && vehicle.get("vehicle") instanceof Map) {
                                            Map<?, ?> vehicleInfo = (Map<?, ?>) vehicle.get("vehicle");
                                            if (vehicleInfo.containsKey("id") && vehicleInfo.get("id") instanceof String) {
                                                bus.setVehicleId((String) vehicleInfo.get("id"));
                                            }
                                        }

                                        if (vehicle.containsKey("trip") && vehicle.get("trip") instanceof Map) {
                                            Map<?, ?> trip = (Map<?, ?>) vehicle.get("trip");
                                            if (trip.containsKey("trip_id") && trip.get("trip_id") instanceof String) {
                                                bus.setTripId((String) trip.get("trip_id"));
                                            }
                                        }

                                        busRepository.save(bus);
                                        return ResponseEntity.ok("Bus synchronized with real-time data successfully");
                                    }
                                }
                            }
                        }
                    }
                }

                return ResponseEntity.ok("No real-time data available for this bus at the moment");
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Failed to sync bus with real-time data: " + e.getMessage());
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}