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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.*;
import java.awt.color.ColorSpace;

@RestController
@RequestMapping("/busOpt")
@Tag(name = "Bus System", description = "Bus Management API")
public class BusController {

    @Autowired
    private busRepository busRepository;

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
            bus.setBusRating(ratingRequest.getBusRating());
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
            @ApiResponse(responseCode = "200", description = "Bus successfully deleted or bus not found")
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
            return ResponseEntity.ok("That is not a valid bus!");
        }
    }

    @Operation(summary = "Remove stop location", description = "Removes stop location from a bus route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stop location removed or stop not found")
    })
    @DeleteMapping("removeStop/{stopLocation}")
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
        return ResponseEntity.ok("Bus stop was not found for this bus!");
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
}