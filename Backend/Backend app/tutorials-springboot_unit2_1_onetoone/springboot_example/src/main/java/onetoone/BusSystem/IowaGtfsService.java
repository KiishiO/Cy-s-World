package onetoone.BusSystem;

import com.google.transit.realtime.GtfsRealtime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IowaGtfsService {

    private static final String VEHICLE_POSITIONS_URL = "https://mycyride.com/gtfs-rt/vehiclepositions";
    private static final String TRIP_UPDATES_URL = "https://mycyride.com/gtfs-rt/tripupdates";
    private static final String ALERTS_URL = "https://mycyride.com/gtfs-rt/alerts";

    @Autowired
    private busRepository busRepository;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Fetches vehicle position data from GTFS-RT feed and updates the database
     */
    public List<VehiclePositionDTO> getVehiclePositions() throws IOException {
        List<VehiclePositionDTO> positions = new ArrayList<>();

        try {
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(new URL(VEHICLE_POSITIONS_URL).openStream());

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasVehicle()) {
                    GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();

                    VehiclePositionDTO position = new VehiclePositionDTO();
                    position.setVehicleId(vehicle.getVehicle().getId());
                    position.setRouteId(vehicle.getTrip().getRouteId());
                    position.setTripId(vehicle.getTrip().getTripId());
                    position.setLatitude(vehicle.getPosition().getLatitude());
                    position.setLongitude(vehicle.getPosition().getLongitude());
                    position.setBearing(vehicle.getPosition().getBearing());
                    position.setSpeed(vehicle.getPosition().getSpeed());

                    if (vehicle.hasCurrentStopSequence()) {
                        position.setCurrentStopSequence(vehicle.getCurrentStopSequence());
                    }

                    if (vehicle.hasCurrentStatus()) {
                        position.setCurrentStatus(vehicle.getCurrentStatus().toString());
                    }

                    if (vehicle.hasTimestamp()) {
                        position.setTimestamp(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(vehicle.getTimestamp()),
                                ZoneId.systemDefault()));
                    }

                    positions.add(position);

                    // Try to update the corresponding bus in our database
                    updateBusWithVehiclePosition(position);
                }
            }
        } catch (Exception e) {
            throw new IOException("Error fetching vehicle positions: " + e.getMessage(), e);
        }

        return positions;
    }

    /**
     * Updates our bus database with vehicle position information
     */
    private void updateBusWithVehiclePosition(VehiclePositionDTO position) {
        // First try to find by vehicleId
        Optional<Bus> busOptional = busRepository.findByVehicleId(position.getVehicleId());

        // If not found, try by routeId (which should match our bus number)
        if (busOptional.isEmpty()) {
            List<Bus> buses = busRepository.findByRouteId(position.getRouteId());
            if (!buses.isEmpty()) {
                busOptional = Optional.of(buses.get(0));
            }
        }

        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            bus.setVehicleId(position.getVehicleId());
            bus.setTripId(position.getTripId());
            bus.updatePosition(
                    position.getLatitude(),
                    position.getLongitude(),
                    position.getSpeed(),
                    position.getBearing()
            );
            busRepository.save(bus);
        }
    }

    /**
     * Fetches trip updates from GTFS-RT feed
     */
    public List<TripUpdateDTO> getTripUpdates() throws IOException {
        List<TripUpdateDTO> updates = new ArrayList<>();

        try {
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(new URL(TRIP_UPDATES_URL).openStream());

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasTripUpdate()) {
                    GtfsRealtime.TripUpdate tripUpdate = entity.getTripUpdate();

                    TripUpdateDTO update = new TripUpdateDTO();
                    update.setTripId(tripUpdate.getTrip().getTripId());
                    update.setRouteId(tripUpdate.getTrip().getRouteId());
                    update.setVehicleId(tripUpdate.getVehicle().getId());

                    List<StopTimeUpdateDTO> stopTimeUpdates = new ArrayList<>();
                    for (GtfsRealtime.TripUpdate.StopTimeUpdate stopUpdate : tripUpdate.getStopTimeUpdateList()) {
                        StopTimeUpdateDTO stopTimeUpdateDTO = new StopTimeUpdateDTO();
                        stopTimeUpdateDTO.setStopId(stopUpdate.getStopId());
                        stopTimeUpdateDTO.setStopSequence(stopUpdate.getStopSequence());

                        if (stopUpdate.hasArrival()) {
                            LocalDateTime arrivalTime = LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(stopUpdate.getArrival().getTime()),
                                    ZoneId.systemDefault());
                            stopTimeUpdateDTO.setArrivalTime(arrivalTime);
                        }

                        if (stopUpdate.hasDeparture()) {
                            LocalDateTime departureTime = LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(stopUpdate.getDeparture().getTime()),
                                    ZoneId.systemDefault());
                            stopTimeUpdateDTO.setDepartureTime(departureTime);
                        }

                        stopTimeUpdates.add(stopTimeUpdateDTO);
                    }

                    update.setStopTimeUpdates(stopTimeUpdates);
                    updates.add(update);

                    // Update the next stop information in our database
                    updateBusWithTripUpdate(update);
                }
            }
        } catch (Exception e) {
            throw new IOException("Error fetching trip updates: " + e.getMessage(), e);
        }

        return updates;
    }

    /**
     * Updates our bus database with trip update information
     */
    private void updateBusWithTripUpdate(TripUpdateDTO update) {
        // Try to find by tripId
        Optional<Bus> busOptional = busRepository.findByTripId(update.getTripId());

        // If not found, try by vehicleId
        if (busOptional.isEmpty()) {
            busOptional = busRepository.findByVehicleId(update.getVehicleId());
        }

        // If still not found, try by routeId
        if (busOptional.isEmpty()) {
            List<Bus> buses = busRepository.findByRouteId(update.getRouteId());
            if (!buses.isEmpty()) {
                busOptional = Optional.of(buses.get(0));
            }
        }

        if (busOptional.isPresent() && !update.getStopTimeUpdates().isEmpty()) {
            Bus bus = busOptional.get();

            // Get the next stop (first in the list)
            StopTimeUpdateDTO nextStop = update.getStopTimeUpdates().get(0);
            bus.setNextStopId(nextStop.getStopId());

            if (nextStop.getArrivalTime() != null) {
                bus.setNextStopArrivalTime(nextStop.getArrivalTime());
            }

            busRepository.save(bus);
        }
    }

    /**
     * Fetches service alerts from GTFS-RT feed
     */
    public List<ServiceAlertDTO> getServiceAlerts() throws IOException {
        List<ServiceAlertDTO> alerts = new ArrayList<>();

        try {
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(new URL(ALERTS_URL).openStream());

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasAlert()) {
                    GtfsRealtime.Alert alert = entity.getAlert();

                    ServiceAlertDTO alertDTO = new ServiceAlertDTO();
                    alertDTO.setId(entity.getId());

                    // Process affected entities
                    List<AffectedEntityDTO> affectedEntities = new ArrayList<>();
                    for (GtfsRealtime.EntitySelector informed : alert.getInformedEntityList()) {
                        AffectedEntityDTO entityDTO = new AffectedEntityDTO();

                        if (informed.hasRouteId()) {
                            entityDTO.setRouteId(informed.getRouteId());
                        }

                        if (informed.hasStopId()) {
                            entityDTO.setStopId(informed.getStopId());
                        }

                        if (informed.hasTrip()) {
                            entityDTO.setTripId(informed.getTrip().getTripId());
                        }

                        affectedEntities.add(entityDTO);
                    }
                    alertDTO.setAffectedEntities(affectedEntities);

                    // Process header text
                    if (alert.hasHeaderText()) {
                        for (GtfsRealtime.TranslatedString.Translation translation : alert.getHeaderText().getTranslationList()) {
                            if (translation.getLanguage().isEmpty() || translation.getLanguage().equals("en")) {
                                alertDTO.setHeaderText(translation.getText());
                                break;
                            }
                        }
                    }

                    // Process description text
                    if (alert.hasDescriptionText()) {
                        for (GtfsRealtime.TranslatedString.Translation translation : alert.getDescriptionText().getTranslationList()) {
                            if (translation.getLanguage().isEmpty() || translation.getLanguage().equals("en")) {
                                alertDTO.setDescriptionText(translation.getText());
                                break;
                            }
                        }
                    }

                    // Process cause and effect
                    if (alert.hasCause()) {
                        alertDTO.setCause(alert.getCause().toString());
                    }

                    if (alert.hasEffect()) {
                        alertDTO.setEffect(alert.getEffect().toString());
                    }

                    alerts.add(alertDTO);
                }
            }
        } catch (Exception e) {
            throw new IOException("Error fetching service alerts: " + e.getMessage(), e);
        }

        return alerts;
    }

    /**
     * Get vehicle position for a specific route ID
     */
    public Optional<VehiclePositionDTO> getVehiclePositionByRouteId(String routeId) throws IOException {
        return getVehiclePositions().stream()
                .filter(position -> position.getRouteId().equals(routeId))
                .findFirst();
    }

    /**
     * Get trip updates for a specific route ID
     */
    public List<TripUpdateDTO> getTripUpdatesByRouteId(String routeId) throws IOException {
        return getTripUpdates().stream()
                .filter(update -> update.getRouteId().equals(routeId))
                .collect(Collectors.toList());
    }

    /**
     * Get service alerts for a specific route ID
     */
    public List<ServiceAlertDTO> getServiceAlertsByRouteId(String routeId) throws IOException {
        return getServiceAlerts().stream()
                .filter(alert -> alert.getAffectedEntities().stream()
                        .anyMatch(entity -> routeId.equals(entity.getRouteId())))
                .collect(Collectors.toList());
    }

    /**
     * Find buses nearest to the provided coordinates
     */
    public List<Bus> findNearestBuses(double latitude, double longitude, int limit) throws IOException {
        // Get the latest vehicle positions
        List<VehiclePositionDTO> positions = getVehiclePositions();

        positions.stream()
                .filter(p -> p.getRouteId() == null || p.getRouteId().isBlank())
                .forEach(p -> System.out.println("Vehicle with missing routeId: " + p.getVehicleId()));

        // Calculate distance for each bus and sort
        Map<String, Double> vehicleDistances = new HashMap<>();

        for (VehiclePositionDTO position : positions) {
            if (position.getVehicleId() == null || position.getVehicleId().isBlank()) continue;

            double distance = calculateDistance(latitude, longitude, position.getLatitude(), position.getLongitude());
            vehicleDistances.put(position.getVehicleId(), distance);
        }

        // Get buses from our database using route IDs
        List<Bus> allBuses = busRepository.findAll();

        // Sort buses by distance
        return allBuses.stream()
                .filter(bus -> vehicleDistances.containsKey(bus.getVehicleId()))
                .sorted(Comparator.comparing(bus -> vehicleDistances.get(bus.getVehicleId())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    /**
     * Get buses that are currently active (have recent position updates)
     */
    public List<Bus> getActiveBuses() throws IOException {
        // Get the latest vehicle positions
        List<VehiclePositionDTO> positions = getVehiclePositions();

        // Create a set of active route IDs
        Set<String> activeRouteIds = positions.stream()
                .map(VehiclePositionDTO::getRouteId)
                .collect(Collectors.toSet());

        // Get all buses
        List<Bus> allBuses = busRepository.findAll();

        // Filter for active buses
        return allBuses.stream()
                .filter(bus -> activeRouteIds.contains(bus.getRouteId()) ||
                        (bus.getLastReportTime() != null &&
                                bus.getLastReportTime().isAfter(LocalDateTime.now().minusMinutes(15))))
                .collect(Collectors.toList());
    }

    /**
     * Get predicted arrival times for a specific stop
     */
    public List<Map<String, Object>> getPredictedArrivals(String stopId) throws IOException {
        // Get trip updates
        List<TripUpdateDTO> tripUpdates = getTripUpdates();

        List<Map<String, Object>> arrivals = new ArrayList<>();

        for (TripUpdateDTO tripUpdate : tripUpdates) {
            for (StopTimeUpdateDTO stopTimeUpdate : tripUpdate.getStopTimeUpdates()) {
                if (stopTimeUpdate.getStopId().equals(stopId) && stopTimeUpdate.getArrivalTime() != null) {
                    Map<String, Object> arrival = new HashMap<>();
                    arrival.put("routeId", tripUpdate.getRouteId());
                    arrival.put("tripId", tripUpdate.getTripId());
                    arrival.put("arrivalTime", stopTimeUpdate.getArrivalTime());

                    // Try to get the bus name
                    List<Bus> buses = busRepository.findByRouteId(tripUpdate.getRouteId());
                    if (!buses.isEmpty()) {
                        arrival.put("busName", buses.get(0).getBusName());
                    }

                    arrivals.add(arrival);
                }
            }
        }

        // Sort by arrival time
        arrivals.sort(Comparator.comparing(a -> (LocalDateTime) a.get("arrivalTime")));

        return arrivals;
    }

    /**
     * Manually refresh real-time data
     */
    public void refreshRealTimeData() throws IOException {
        getVehiclePositions();
        getTripUpdates();
        getServiceAlerts();
        LocalDateTime lastUpdateTime = LocalDateTime.now();
    }

    /**
     * Scheduled task to update all buses with real-time data
     */
    @Scheduled(fixedRate = 30000) // Update every 30 seconds
    public void updateRealTimeData() {
        try {
            refreshRealTimeData();
        } catch (Exception e) {
            // Log error but don't crash the service
            System.err.println("Error updating real-time data: " + e.getMessage());
        }
    }
}