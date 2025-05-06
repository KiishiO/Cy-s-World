package onetoone.BusSystem;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for interacting with the Iowa GTFS API
 * Provides methods to retrieve real-time bus information
 */
@Service
public class IowaGtfsService {

    @Value("${https://iowa-gtfs.com/}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Cache for API responses to reduce external calls
    private final Map<String, Object> vehiclePositionsCache = new ConcurrentHashMap<>();
    private final Map<String, Object> tripUpdatesCache = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> stopsCache = new ArrayList<>();
    private final List<Map<String, Object>> routesCache = new ArrayList<>();

    // Cache timestamps
    private LocalDateTime vehiclePositionsCacheTime;
    private LocalDateTime tripUpdatesCacheTime;
    private LocalDateTime stopsCacheTime;
    private LocalDateTime routesCacheTime;

    // Cache validity period in milliseconds (5 minutes)
    private static final long CACHE_VALIDITY_PERIOD = 300000;

    public IowaGtfsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gets real-time positions of all vehicles
     * @return Map containing vehicle position data
     */
    public Map<String, Object> getVehiclePositions() {
        if (isCacheValid(vehiclePositionsCacheTime) && !vehiclePositionsCache.isEmpty()) {
            return vehiclePositionsCache;
        }

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    apiBaseUrl + "/vehicle-positions", Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                vehiclePositionsCache.putAll(result);
                vehiclePositionsCacheTime = LocalDateTime.now();
                return result;
            } else {
                throw new RuntimeException("Failed to fetch vehicle positions: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching vehicle positions: " + e.getMessage(), e);
        }
    }

    /**
     * Gets real-time position of vehicles on a specific route
     * @param routeId the route ID
     * @return Map containing vehicle position data for the route
     */
    public Map<String, Object> getVehiclePositionByRoute(String routeId) {
        Map<String, Object> allPositions = getVehiclePositions();

        try {
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> filteredEntities = new ArrayList<>();

            if (allPositions.containsKey("entity") && allPositions.get("entity") instanceof List) {
                List<?> entities = (List<?>) allPositions.get("entity");

                for (Object entityObj : entities) {
                    if (entityObj instanceof Map) {
                        Map<?, ?> entity = (Map<?, ?>) entityObj;
                        if (entity.containsKey("vehicle") && entity.get("vehicle") instanceof Map) {
                            Map<?, ?> vehicle = (Map<?, ?>) entity.get("vehicle");

                            if (vehicle.containsKey("trip") && vehicle.get("trip") instanceof Map) {
                                Map<?, ?> trip = (Map<?, ?>) vehicle.get("trip");

                                if (trip.containsKey("route_id") &&
                                        routeId.equals(String.valueOf(trip.get("route_id")))) {
                                    filteredEntities.add((Map<String, Object>) entity);
                                }
                            }
                        }
                    }
                }
            }

            if (!filteredEntities.isEmpty()) {
                result.put("header", allPositions.getOrDefault("header", new HashMap<>()));
                result.put("entity", filteredEntities);
                return result;
            } else {
                throw new RuntimeException("No vehicle found for route: " + routeId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing vehicle position for route " + routeId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Gets all bus stops
     * @return List of stops
     */
    public List<Map<String, Object>> getAllStops() {
        if (isCacheValid(stopsCacheTime) && !stopsCache.isEmpty()) {
            return new ArrayList<>(stopsCache);
        }

        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    apiBaseUrl + "/stops", List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> stops = response.getBody();
                stopsCache.clear();
                stopsCache.addAll(stops);
                stopsCacheTime = LocalDateTime.now();
                return stops;
            } else {
                throw new RuntimeException("Failed to fetch stops: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching stops: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a specific bus stop by ID
     * @param stopId the stop ID
     * @return Map containing stop data
     */
    public Map<String, Object> getStopById(String stopId) {
        List<Map<String, Object>> allStops = getAllStops();

        return allStops.stream()
                .filter(stop -> stopId.equals(stop.get("stop_id")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Stop not found with ID: " + stopId));
    }

    /**
     * Gets real-time trip updates (arrivals/departures)
     * @return Map containing trip update data
     */
    public Map<String, Object> getTripUpdates() {
        if (isCacheValid(tripUpdatesCacheTime) && !tripUpdatesCache.isEmpty()) {
            return tripUpdatesCache;
        }

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    apiBaseUrl + "/trip-updates", Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = response.getBody();
                tripUpdatesCache.putAll(result);
                tripUpdatesCacheTime = LocalDateTime.now();
                return result;
            } else {
                throw new RuntimeException("Failed to fetch trip updates: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching trip updates: " + e.getMessage(), e);
        }
    }

    /**
     * Gets real-time trip updates for a specific route
     * @param routeId the route ID
     * @return Map containing trip update data for the route
     */
    public Map<String, Object> getTripUpdatesByRoute(String routeId) {
        Map<String, Object> allUpdates = getTripUpdates();

        try {
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> filteredEntities = new ArrayList<>();

            if (allUpdates.containsKey("entity") && allUpdates.get("entity") instanceof List) {
                List<?> entities = (List<?>) allUpdates.get("entity");

                for (Object entityObj : entities) {
                    if (entityObj instanceof Map) {
                        Map<?, ?> entity = (Map<?, ?>) entityObj;
                        if (entity.containsKey("trip_update") && entity.get("trip_update") instanceof Map) {
                            Map<?, ?> tripUpdate = (Map<?, ?>) entity.get("trip_update");

                            if (tripUpdate.containsKey("trip") && tripUpdate.get("trip") instanceof Map) {
                                Map<?, ?> trip = (Map<?, ?>) tripUpdate.get("trip");

                                if (trip.containsKey("route_id") &&
                                        routeId.equals(String.valueOf(trip.get("route_id")))) {
                                    filteredEntities.add((Map<String, Object>) entity);
                                }
                            }
                        }
                    }
                }
            }

            if (!filteredEntities.isEmpty()) {
                result.put("header", allUpdates.getOrDefault("header", new HashMap<>()));
                result.put("entity", filteredEntities);
                return result;
            } else {
                throw new RuntimeException("No trip updates found for route: " + routeId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing trip updates for route " + routeId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Gets all routes
     * @return List of routes
     */
    public List<Map<String, Object>> getAllRoutes() {
        if (isCacheValid(routesCacheTime) && !routesCache.isEmpty()) {
            return new ArrayList<>(routesCache);
        }

        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    apiBaseUrl + "/routes", List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> routes = response.getBody();
                routesCache.clear();
                routesCache.addAll(routes);
                routesCacheTime = LocalDateTime.now();
                return routes;
            } else {
                throw new RuntimeException("Failed to fetch routes: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching routes: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a specific route by ID
     * @param routeId the route ID
     * @return Map containing route data
     */
    public Map<String, Object> getRouteById(String routeId) {
        List<Map<String, Object>> allRoutes = getAllRoutes();

        return allRoutes.stream()
                .filter(route -> routeId.equals(route.get("route_id")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + routeId));
    }

    /**
     * Gets the estimated time of arrival to the next stop for a specific bus
     * @param busNum the bus number
     * @return Map containing ETA information
     */
    public Map<String, Object> getNextStopETA(int busNum) {
        String routeId = String.valueOf(busNum);

        try {
            // Get vehicle position to find the current trip
            Map<String, Object> vehiclePosition = getVehiclePositionByRoute(routeId);
            String tripId = null;
            String vehicleId = null;

            if (vehiclePosition.containsKey("entity") && vehiclePosition.get("entity") instanceof List) {
                List<?> entities = (List<?>) vehiclePosition.get("entity");

                if (!entities.isEmpty() && entities.get(0) instanceof Map) {
                    Map<?, ?> entity = (Map<?, ?>) entities.get(0);

                    if (entity.containsKey("vehicle") && entity.get("vehicle") instanceof Map) {
                        Map<?, ?> vehicle = (Map<?, ?>) entity.get("vehicle");

                        if (vehicle.containsKey("trip") && vehicle.get("trip") instanceof Map) {
                            Map<?, ?> trip = (Map<?, ?>) vehicle.get("trip");

                            if (trip.containsKey("trip_id")) {
                                tripId = String.valueOf(trip.get("trip_id"));
                            }
                        }

                        if (vehicle.containsKey("vehicle") && vehicle.get("vehicle") instanceof Map) {
                            Map<?, ?> vehicleInfo = (Map<?, ?>) vehicle.get("vehicle");

                            if (vehicleInfo.containsKey("id")) {
                                vehicleId = String.valueOf(vehicleInfo.get("id"));
                            }
                        }
                    }
                }
            }

            if (tripId == null) {
                throw new RuntimeException("No active trip found for bus: " + busNum);
            }

            // Get trip updates to find the next stop and ETA
            Map<String, Object> tripUpdates = getTripUpdates();
            Map<String, Object> etaInfo = new HashMap<>();

            if (tripUpdates.containsKey("entity") && tripUpdates.get("entity") instanceof List) {
                List<?> entities = (List<?>) tripUpdates.get("entity");

                for (Object entityObj : entities) {
                    if (entityObj instanceof Map) {
                        Map<?, ?> entity = (Map<?, ?>) entityObj;

                        if (entity.containsKey("trip_update") && entity.get("trip_update") instanceof Map) {
                            Map<?, ?> tripUpdate = (Map<?, ?>) entity.get("trip_update");

                            if (tripUpdate.containsKey("trip") && tripUpdate.get("trip") instanceof Map) {
                                Map<?, ?> trip = (Map<?, ?>) tripUpdate.get("trip");

                                if (trip.containsKey("trip_id") && tripId.equals(String.valueOf(trip.get("trip_id")))) {
                                    if (tripUpdate.containsKey("stop_time_update") &&
                                            tripUpdate.get("stop_time_update") instanceof List) {

                                        List<?> stopTimeUpdates = (List<?>) tripUpdate.get("stop_time_update");

                                        if (!stopTimeUpdates.isEmpty() && stopTimeUpdates.get(0) instanceof Map) {
                                            Map<?, ?> nextStopUpdate = (Map<?, ?>) stopTimeUpdates.get(0);

                                            String stopId = nextStopUpdate.containsKey("stop_id") ?
                                                    String.valueOf(nextStopUpdate.get("stop_id")) : null;

                                            if (stopId != null) {
                                                // Get stop name
                                                Map<String, Object> stopInfo = getStopById(stopId);
                                                String stopName = stopInfo.containsKey("stop_name") ?
                                                        String.valueOf(stopInfo.get("stop_name")) : "Unknown Stop";

                                                // Calculate minutes until arrival
                                                int minutesUntilArrival = 0;

                                                if (nextStopUpdate.containsKey("arrival") &&
                                                        nextStopUpdate.get("arrival") instanceof Map) {

                                                    Map<?, ?> arrival = (Map<?, ?>) nextStopUpdate.get("arrival");

                                                    if (arrival.containsKey("time") && arrival.get("time") instanceof Number) {
                                                        long arrivalTimestamp = ((Number) arrival.get("time")).longValue();
                                                        long currentTimestamp = System.currentTimeMillis() / 1000;
                                                        long secondsUntilArrival = arrivalTimestamp - currentTimestamp;
                                                        minutesUntilArrival = (int) (secondsUntilArrival / 60);
                                                    }
                                                }

                                                // Format arrival time
                                                LocalDateTime arrivalTime = LocalDateTime.now().plusMinutes(minutesUntilArrival);
                                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
                                                String formattedArrivalTime = arrivalTime.format(formatter);

                                                // Build result
                                                etaInfo.put("busNum", busNum);
                                                etaInfo.put("routeId", routeId);
                                                etaInfo.put("tripId", tripId);
                                                etaInfo.put("vehicleId", vehicleId);
                                                etaInfo.put("nextStopId", stopId);
                                                etaInfo.put("nextStopName", stopName);
                                                etaInfo.put("minutesUntilArrival", minutesUntilArrival);
                                                etaInfo.put("arrivalTime", formattedArrivalTime);

                                                // Add upcoming stops if available
                                                if (stopTimeUpdates.size() > 1) {
                                                    List<String> upcomingStopIds = new ArrayList<>();

                                                    for (int i = 1; i < Math.min(stopTimeUpdates.size(), 5); i++) {
                                                        if (stopTimeUpdates.get(i) instanceof Map) {
                                                            Map<?, ?> upcomingStop = (Map<?, ?>) stopTimeUpdates.get(i);

                                                            if (upcomingStop.containsKey("stop_id")) {
                                                                upcomingStopIds.add(String.valueOf(upcomingStop.get("stop_id")));
                                                            }
                                                        }
                                                    }

                                                    if (!upcomingStopIds.isEmpty()) {
                                                        List<String> upcomingStopNames = new ArrayList<>();

                                                        for (String upcomingStopId : upcomingStopIds) {
                                                            try {
                                                                Map<String, Object> upcomingStopInfo = getStopById(upcomingStopId);
                                                                upcomingStopNames.add(String.valueOf(upcomingStopInfo.get("stop_name")));
                                                            } catch (Exception e) {
                                                                upcomingStopNames.add("Unknown Stop");
                                                            }
                                                        }

                                                        etaInfo.put("upcomingStops", upcomingStopNames);
                                                    }
                                                }

                                                return etaInfo;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            throw new RuntimeException("No trip updates found for bus: " + busNum);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating next stop ETA: " + e.getMessage(), e);
        }
    }

    /**
     * Scheduled task to periodically refresh the caches
     */
    @Scheduled(fixedRate = CACHE_VALIDITY_PERIOD)
    public void refreshCaches() {
        try {
            getVehiclePositions();
            getTripUpdates();
            getAllStops();
            getAllRoutes();
        } catch (Exception e) {
            // Log error but don't rethrow to prevent scheduled task from being disabled
            System.err.println("Error refreshing GTFS caches: " + e.getMessage());
        }
    }

    /**
     * Checks if a cache is still valid
     * @param cacheTime the timestamp when the cache was last updated
     * @return true if the cache is valid, false otherwise
     */
    private boolean isCacheValid(LocalDateTime cacheTime) {
        if (cacheTime == null) {
            return false;
        }

        long millisSinceUpdate = java.time.Duration.between(cacheTime, LocalDateTime.now()).toMillis();
        return millisSinceUpdate < CACHE_VALIDITY_PERIOD;
    }
}