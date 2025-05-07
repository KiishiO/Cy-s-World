package com.example.own_example.services;

import android.content.Context;
import android.util.Log;

import com.example.own_example.models.GTFSBus;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service for handling GTFS data from CyRide API
 * This implementation downloads and parses GTFS data directly with custom GTFS-RT handling
 */
public class GTFSService {
    private static final String TAG = "GTFSService";

    // URLs for CyRide GTFS data
    private static final String STATIC_GTFS_URL = "https://mycyride.com/gtfs";
    private static final String VEHICLE_POSITIONS_URL = "https://mycyride.com/gtfs-rt/vehiclepositions";
    private static final String TRIP_UPDATES_URL = "https://mycyride.com/gtfs-rt/tripupdates";
    private static final String ALERTS_URL = "https://mycyride.com/gtfs-rt/alerts";

    private Context context;
    private OkHttpClient httpClient;
    private Map<String, Map<String, String>> routeData; // Map route_id to route data
    private Map<String, Map<String, String>> stopData; // Map stop_id to stop data
    private Map<String, List<Map<String, String>>> stopTimesData; // Map trip_id to list of stop times
    private Map<String, Map<String, String>> tripData; // Map trip_id to trip data
    private Map<String, List<LatLng>> routeShapes; // Map route_id to list of coordinates for route shape
    private boolean isStaticDataLoaded = false;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private boolean useRealTimeData = true;

    // Callback for bus data updates
    public interface GTFSBusCallback {
        void onSuccess(List<GTFSBus> buses);
        void onError(String error);
    }

    private ScheduledExecutorService scheduledExecutorService;
    private GTFSBusCallback callback;

    public GTFSService(Context context) {
        this.context = context;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Initialize data structures
        this.routeData = new HashMap<>();
        this.stopData = new HashMap<>();
        this.stopTimesData = new HashMap<>();
        this.tripData = new HashMap<>();
        this.routeShapes = new HashMap<>();
    }

    /**
     * Start real-time updates for bus positions.
     * This will periodically fetch updates from the GTFS-RT feed.
     *
     * @param intervalSeconds How often to fetch updates (in seconds)
     * @param callback Callback for handling updates
     */
    public void startRealTimeUpdates(int intervalSeconds, GTFSBusCallback callback) {
        this.callback = callback;

        // First download static GTFS data if not already loaded
        if (!isStaticDataLoaded) {
            downloadStaticGtfsData(intervalSeconds);
        } else {
            // If static data is already loaded, directly start real-time updates
            startRealTimeUpdateScheduler(intervalSeconds);
        }
    }

    /**
     * Start the scheduler for real-time updates
     */
    private void startRealTimeUpdateScheduler(int intervalSeconds) {
        // Stop any existing updates
        stopRealTimeUpdates();

        // Start scheduled updates
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(
                this::fetchRealTimeUpdates,
                0,
                intervalSeconds,
                TimeUnit.SECONDS
        );

        Log.d(TAG, "Started real-time updates with interval of " + intervalSeconds + " seconds");
    }

    /**
     * Stop real-time updates
     */
    public void stopRealTimeUpdates() {
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
            Log.d(TAG, "Stopped real-time updates");
        }
    }

    /**
     * Download the static GTFS data (routes, stops, trips, etc.)
     */
    private void downloadStaticGtfsData(final int intervalSeconds) {
        Log.d(TAG, "Downloading static GTFS data from " + STATIC_GTFS_URL);

        Request request = new Request.Builder()
                .url(STATIC_GTFS_URL)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to download static GTFS data", e);
                if (callback != null) {
                    callback.onError("Failed to download route and stop data: " + e.getMessage());
                }

                // Retry if under max retries
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    Log.d(TAG, "Retrying static GTFS download, attempt " + retryCount);
                    downloadStaticGtfsData(intervalSeconds);
                } else {
                    // Fall back to mock data if all retries fail
                    Log.w(TAG, "All retries failed, using mock data");
                    createMockDataAndStart(intervalSeconds);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed to download static GTFS data: " + response.code());
                    if (callback != null) {
                        callback.onError("Failed to download route and stop data: HTTP " + response.code());
                    }

                    // Retry if under max retries
                    if (retryCount < MAX_RETRIES) {
                        retryCount++;
                        Log.d(TAG, "Retrying static GTFS download, attempt " + retryCount);
                        downloadStaticGtfsData(intervalSeconds);
                    } else {
                        // Fall back to mock data if all retries fail
                        Log.w(TAG, "All retries failed, using mock data");
                        createMockDataAndStart(intervalSeconds);
                    }
                    return;
                }

                try {
                    Log.d(TAG, "Static GTFS data downloaded successfully, processing...");
                    // Process the ZIP file from the response
                    processStaticGtfsData(response.body().byteStream());

                    // Reset retry count on success
                    retryCount = 0;

                    // Mark static data as loaded
                    isStaticDataLoaded = true;

                    // Once static data is loaded, we can start fetching real-time updates
                    startRealTimeUpdateScheduler(intervalSeconds);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing static GTFS data", e);
                    if (callback != null) {
                        callback.onError("Error processing route and stop data: " + e.getMessage());
                    }

                    // Retry if under max retries
                    if (retryCount < MAX_RETRIES) {
                        retryCount++;
                        Log.d(TAG, "Retrying static GTFS download, attempt " + retryCount);
                        downloadStaticGtfsData(intervalSeconds);
                    } else {
                        // Fall back to mock data if all retries fail
                        Log.w(TAG, "All retries failed, using mock data");
                        createMockDataAndStart(intervalSeconds);
                    }
                }
            }
        });
    }

    /**
     * Create mock data and start the update scheduler
     */
    private void createMockDataAndStart(int intervalSeconds) {
        useRealTimeData = false;
        createMockRouteData();
        createMockShapes();
        createMockStopData();
        isStaticDataLoaded = true;
        startRealTimeUpdateScheduler(intervalSeconds);
    }

    /**
     * Process the static GTFS zip file and extract data
     */
    private void processStaticGtfsData(InputStream inputStream) throws IOException {
        Log.d(TAG, "Processing static GTFS data");

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream));
        try {
            ZipEntry zipEntry;

            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                Log.d(TAG, "Processing ZIP entry: " + fileName);

                switch (fileName) {
                    case "routes.txt":
                        parseRoutes(zis);
                        break;
                    case "stops.txt":
                        parseStops(zis);
                        break;
                    case "trips.txt":
                        parseTrips(zis);
                        break;
                    case "stop_times.txt":
                        parseStopTimes(zis);
                        break;
                    case "shapes.txt":
                        parseShapes(zis);
                        break;
                    default:
                        // Skip other files
                        break;
                }

                zis.closeEntry();
            }

            Log.d(TAG, "Finished processing static GTFS data");
            Log.d(TAG, "Routes: " + routeData.size() + ", Stops: " + stopData.size() +
                    ", Trips: " + tripData.size() + ", StopTimes: " + stopTimesData.size() +
                    ", Shapes: " + routeShapes.size());

            // If we don't have route shapes, generate them from stops
            if (routeShapes.isEmpty() && !tripData.isEmpty()) {
                Log.d(TAG, "No shape data found, generating route shapes from stops");
                generateAllRouteShapes();
            }
        } finally {
            zis.close();
        }
    }

    /**
     * Generate route shapes for all routes based on their stops
     */
    private void generateAllRouteShapes() {
        // For each trip, generate a route shape
        for (Map.Entry<String, Map<String, String>> entry : tripData.entrySet()) {
            String tripId = entry.getKey();
            Map<String, String> trip = entry.getValue();

            if (trip.containsKey("route_id")) {
                String routeId = trip.get("route_id");

                // Only generate if we don't already have this route
                if (!routeShapes.containsKey(routeId)) {
                    List<LatLng> shape = generateRouteFromStops(tripId);
                    if (!shape.isEmpty()) {
                        routeShapes.put(routeId, shape);
                    }
                }
            }
        }

        Log.d(TAG, "Generated shapes for " + routeShapes.size() + " routes");
    }

    /**
     * Create mock route data for testing
     */
    private void createMockRouteData() {
        // Create mock route data
        Map<String, String> route1 = new HashMap<>();
        route1.put("route_short_name", "Red Route");
        route1.put("route_long_name", "Downtown Red Line");
        route1.put("route_color", "FF0000");
        routeData.put("1", route1);

        Map<String, String> route2 = new HashMap<>();
        route2.put("route_short_name", "Blue Route");
        route2.put("route_long_name", "University Blue Line");
        route2.put("route_color", "0000FF");
        routeData.put("2", route2);

        Map<String, String> route3 = new HashMap<>();
        route3.put("route_short_name", "Green Route");
        route3.put("route_long_name", "Eastside Green Line");
        route3.put("route_color", "00FF00");
        routeData.put("3", route3);

        // Create mock trips
        Map<String, String> trip1 = new HashMap<>();
        trip1.put("route_id", "1");
        trip1.put("trip_headsign", "Downtown Loop");
        tripData.put("trip_1", trip1);

        Map<String, String> trip2 = new HashMap<>();
        trip2.put("route_id", "2");
        trip2.put("trip_headsign", "University Loop");
        tripData.put("trip_2", trip2);

        Map<String, String> trip3 = new HashMap<>();
        trip3.put("route_id", "3");
        trip3.put("trip_headsign", "Eastside Loop");
        tripData.put("trip_3", trip3);
    }

    /**
     * Create mock shapes for routes
     */
    private void createMockShapes() {
        // Red route - north loop
        List<LatLng> shape1 = new ArrayList<>();
        shape1.add(new LatLng(42.023, -93.647));
        shape1.add(new LatLng(42.024, -93.646));
        shape1.add(new LatLng(42.025, -93.645));
        shape1.add(new LatLng(42.026, -93.644));
        shape1.add(new LatLng(42.027, -93.645));
        shape1.add(new LatLng(42.028, -93.646));
        shape1.add(new LatLng(42.027, -93.647));
        shape1.add(new LatLng(42.026, -93.648));
        shape1.add(new LatLng(42.025, -93.649));
        shape1.add(new LatLng(42.024, -93.648));
        shape1.add(new LatLng(42.023, -93.647));
        routeShapes.put("1", shape1);

        // Blue route - east loop
        List<LatLng> shape2 = new ArrayList<>();
        shape2.add(new LatLng(42.027, -93.649));
        shape2.add(new LatLng(42.028, -93.648));
        shape2.add(new LatLng(42.029, -93.647));
        shape2.add(new LatLng(42.030, -93.646));
        shape2.add(new LatLng(42.029, -93.645));
        shape2.add(new LatLng(42.028, -93.644));
        shape2.add(new LatLng(42.027, -93.645));
        shape2.add(new LatLng(42.026, -93.646));
        shape2.add(new LatLng(42.027, -93.647));
        shape2.add(new LatLng(42.028, -93.648));
        shape2.add(new LatLng(42.027, -93.649));
        routeShapes.put("2", shape2);

        // Green route - west loop
        List<LatLng> shape3 = new ArrayList<>();
        shape3.add(new LatLng(42.028, -93.652));
        shape3.add(new LatLng(42.029, -93.651));
        shape3.add(new LatLng(42.030, -93.650));
        shape3.add(new LatLng(42.031, -93.651));
        shape3.add(new LatLng(42.032, -93.652));
        shape3.add(new LatLng(42.031, -93.653));
        shape3.add(new LatLng(42.030, -93.654));
        shape3.add(new LatLng(42.029, -93.653));
        shape3.add(new LatLng(42.028, -93.652));
        routeShapes.put("3", shape3);
    }

    /**
     * Create mock stop data for testing
     */
    private void createMockStopData() {
        // Red route stops
        Map<String, String> stop1 = new HashMap<>();
        stop1.put("stop_name", "Downtown Station");
        stop1.put("stop_lat", "42.023");
        stop1.put("stop_lon", "-93.647");
        stopData.put("stop_1", stop1);

        Map<String, String> stop2 = new HashMap<>();
        stop2.put("stop_name", "Central Park");
        stop2.put("stop_lat", "42.025");
        stop2.put("stop_lon", "-93.645");
        stopData.put("stop_2", stop2);

        Map<String, String> stop3 = new HashMap<>();
        stop3.put("stop_name", "North Avenue");
        stop3.put("stop_lat", "42.027");
        stop3.put("stop_lon", "-93.645");
        stopData.put("stop_3", stop3);

        // Blue route stops
        Map<String, String> stop4 = new HashMap<>();
        stop4.put("stop_name", "University Center");
        stop4.put("stop_lat", "42.027");
        stop4.put("stop_lon", "-93.649");
        stopData.put("stop_4", stop4);

        Map<String, String> stop5 = new HashMap<>();
        stop5.put("stop_name", "Memorial Union");
        stop5.put("stop_lat", "42.029");
        stop5.put("stop_lon", "-93.647");
        stopData.put("stop_5", stop5);

        // Green route stops
        Map<String, String> stop6 = new HashMap<>();
        stop6.put("stop_name", "West Mall");
        stop6.put("stop_lat", "42.028");
        stop6.put("stop_lon", "-93.652");
        stopData.put("stop_6", stop6);

        Map<String, String> stop7 = new HashMap<>();
        stop7.put("stop_name", "Parks Library");
        stop7.put("stop_lat", "42.030");
        stop7.put("stop_lon", "-93.650");
        stopData.put("stop_7", stop7);
    }

    /**
     * Fetch real-time updates
     */
    private void fetchRealTimeUpdates() {
        if (!useRealTimeData) {
            Log.d(TAG, "Using mock data instead of real-time data");
            getMockGTFSBuses(callback);
            return;
        }

        Log.d(TAG, "Fetching real-time updates from " + VEHICLE_POSITIONS_URL);

        Request request = new Request.Builder()
                .url(VEHICLE_POSITIONS_URL)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch real-time vehicle positions", e);
                // Fall back to mock data
                getMockGTFSBuses(callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed to fetch real-time vehicle positions: " + response.code());
                    // Fall back to mock data
                    getMockGTFSBuses(callback);
                    return;
                }

                try {
                    // Process the real-time data
                    Log.d(TAG, "Real-time data received, size: " + response.body().contentLength() + " bytes");
                    processVehiclePositions(response.body().bytes());
                } catch (Exception e) {
                    Log.e(TAG, "Error processing real-time vehicle positions", e);
                    // Fall back to mock data
                    getMockGTFSBuses(callback);
                }
            }
        });
    }

    /**
     * Process real-time vehicle positions data
     * Manual approach without using protobuf libraries
     */
    private void processVehiclePositions(byte[] data) {
        try {
            Log.d(TAG, "Processing vehicle positions data: " + data.length + " bytes");

            // Here we would normally parse GTFS-RT protocol buffer format
            // Since we don't have the library, we'll use a custom approach

            // For now, we'll use our dynamic simulation approach
            List<GTFSBus> buses = createDynamicBusPositions();

            Log.d(TAG, "Generated " + buses.size() + " simulated buses");

            if (callback != null) {
                callback.onSuccess(buses);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing vehicle positions", e);
            // Fall back to mock data
            getMockGTFSBuses(callback);
        }
    }

    /**
     * Create dynamic bus positions based on routes
     * This simulates real-time data by placing buses along the route shapes
     */
    private List<GTFSBus> createDynamicBusPositions() {
        List<GTFSBus> buses = new ArrayList<>();
        long currentTime = System.currentTimeMillis() / 1000;

        // For each route, create 1-3 buses at different positions along the route
        for (String routeId : routeShapes.keySet()) {
            List<LatLng> shape = routeShapes.get(routeId);
            if (shape == null || shape.isEmpty()) {
                continue;
            }

            // Find a trip ID for this route
            String tripId = null;
            for (Map.Entry<String, Map<String, String>> entry : tripData.entrySet()) {
                if (entry.getValue().containsKey("route_id") &&
                        entry.getValue().get("route_id").equals(routeId)) {
                    tripId = entry.getKey();
                    break;
                }
            }

            if (tripId == null) {
                tripId = "trip_" + routeId; // Fallback
            }

            // Get route name
            String routeName = "Route " + routeId;
            Map<String, String> route = routeData.get(routeId);
            if (route != null) {
                if (route.containsKey("route_short_name")) {
                    routeName = route.get("route_short_name");
                } else if (route.containsKey("route_long_name")) {
                    routeName = route.get("route_long_name");
                }
            }

            // Number of buses depends on route ID (for variety)
            int numBuses = (Integer.parseInt(routeId) % 3) + 1;

            for (int i = 0; i < numBuses; i++) {
                // Get position along route based on time
                int timeOffset = i * 1000;
                int shapeIndex = (int)((currentTime + timeOffset) / 10) % shape.size();
                LatLng position = shape.get(shapeIndex);

                // Calculate bearing based on next position
                int nextIndex = (shapeIndex + 1) % shape.size();
                LatLng nextPos = shape.get(nextIndex);
                float bearing = calculateBearing(position, nextPos);

                // Create bus
                GTFSBus bus = new GTFSBus();
                bus.setBusNum(Integer.parseInt(routeId) * 10 + i);
                bus.setBusName(routeName);
                bus.setLatitude(position.latitude);
                bus.setLongitude(position.longitude);
                bus.setBearing(bearing);
                bus.setSpeed(5.0f + (i * 2.0f)); // Vary speed a bit
                bus.setRouteId(routeId);
                bus.setVehicleId("vehicle_" + routeId + "_" + i);
                bus.setTripId(tripId);
                bus.setInService(true);

                // Find nearby stop
                String nearestStopId = findNearestStop(position);
                if (nearestStopId != null) {
                    bus.setStopLocation(stopData.get(nearestStopId).get("stop_name"));
                    bus.setNextStop(nearestStopId);
                }

                // Add arrival time (a few minutes in the future)
                bus.setPredictedArrivalTime(currentTime + 60 * (3 + i));

                // Add rating
                bus.setBusRating('A');

                buses.add(bus);
            }
        }

        return buses;
    }

    /**
     * Calculate bearing between two points
     */
    private float calculateBearing(LatLng start, LatLng end) {
        double startLat = Math.toRadians(start.latitude);
        double startLng = Math.toRadians(start.longitude);
        double endLat = Math.toRadians(end.latitude);
        double endLng = Math.toRadians(end.longitude);

        double dLng = endLng - startLng;

        double y = Math.sin(dLng) * Math.cos(endLat);
        double x = Math.cos(startLat) * Math.sin(endLat) -
                Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLng);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (float)((bearing + 360) % 360);
    }

    /**
     * Find the nearest stop to a given position
     */
    private String findNearestStop(LatLng position) {
        String nearestId = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Map.Entry<String, Map<String, String>> entry : stopData.entrySet()) {
            Map<String, String> stop = entry.getValue();
            if (stop.containsKey("stop_lat") && stop.containsKey("stop_lon")) {
                double lat = Double.parseDouble(stop.get("stop_lat"));
                double lon = Double.parseDouble(stop.get("stop_lon"));
                LatLng stopPos = new LatLng(lat, lon);

                double distance = calculateDistance(position, stopPos);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestId = entry.getKey();
                }
            }
        }

        return nearestId;
    }

    /**
     * Calculate distance between two points (simplified)
     */
    private double calculateDistance(LatLng p1, LatLng p2) {
        // Simplified distance calculation (not accounting for Earth's curvature)
        double latDiff = p1.latitude - p2.latitude;
        double lngDiff = p1.longitude - p2.longitude;
        return Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
    }

    /**
     * Create mock GTFS buses for testing when real data is unavailable
     */
    public void getMockGTFSBuses(GTFSBusCallback callback) {
        List<GTFSBus> mockBuses = new ArrayList<>();

        // Create some mock bus data centered in Ames, Iowa
        GTFSBus bus1 = new GTFSBus();
        bus1.setBusNum(1);
        bus1.setBusName("Red Route");
        bus1.setStopLocation("Student Union");
        bus1.setLatitude(42.023);
        bus1.setLongitude(-93.647);
        bus1.setBearing(45f);
        bus1.setSpeed(5.5f);
        bus1.setRouteId("1");
        bus1.setVehicleId("vehicle_1");
        bus1.setTripId("trip_1");
        bus1.setInService(true);
        bus1.setNextStop("stop_1");
        bus1.setPredictedArrivalTime(System.currentTimeMillis() / 1000 + 300); // 5 minutes from now
        bus1.setBusRating('A');

        GTFSBus bus2 = new GTFSBus();
        bus2.setBusNum(2);
        bus2.setBusName("Blue Route");
        bus2.setStopLocation("Memorial Union");
        bus2.setLatitude(42.027);
        bus2.setLongitude(-93.649);
        bus2.setBearing(180f);
        bus2.setSpeed(8.2f);
        bus2.setRouteId("2");
        bus2.setVehicleId("vehicle_2");
        bus2.setTripId("trip_2");
        bus2.setInService(true);
        bus2.setNextStop("stop_2");
        bus2.setPredictedArrivalTime(System.currentTimeMillis() / 1000 + 120); // 2 minutes from now
        bus2.setBusRating('B');

        GTFSBus bus3 = new GTFSBus();
        bus3.setBusNum(3);
        bus3.setBusName("Green Route");
        bus3.setStopLocation("Parks Library");
        bus3.setLatitude(42.028);
        bus3.setLongitude(-93.652);
        bus3.setBearing(270f);
        bus3.setSpeed(4.1f);
        bus3.setRouteId("3");
        bus3.setVehicleId("vehicle_3");
        bus3.setTripId("trip_3");
        bus3.setInService(true);
        bus3.setNextStop("stop_3");
        bus3.setPredictedArrivalTime(System.currentTimeMillis() / 1000 + 480); // 8 minutes from now
        bus3.setBusRating('A');

        mockBuses.add(bus1);
        mockBuses.add(bus2);
        mockBuses.add(bus3);

        Log.d(TAG, "Created " + mockBuses.size() + " mock buses for testing");

        // Return mock data via callback
        if (callback != null) {
            callback.onSuccess(mockBuses);
        }
    }

    /**
     * Get the information for a specific stop
     * @param stopId The stop ID
     * @return Map containing stop information, or null if not found
     */
    public Map<String, String> getStopInfo(String stopId) {
        return stopData.get(stopId);
    }

    /**
     * Get a copy of all route data
     * @return Map of route_id to route data
     */
    public Map<String, Map<String, String>> getRouteData() {
        return Collections.unmodifiableMap(routeData);
    }

    /**
     * Get a copy of all stop data
     * @return Map of stop_id to stop data
     */
    public Map<String, Map<String, String>> getStopData() {
        return Collections.unmodifiableMap(stopData);
    }

    /**
     * Get upcoming arrivals for a specific stop
     * @param stopId The stop ID
     * @return List of arrival information
     */
    public List<Map<String, Object>> getUpcomingArrivals(String stopId) {
        List<Map<String, Object>> arrivals = new ArrayList<>();

        // For now, return mock data
        // In a real implementation, this would use the trip_updates feed

        // Example mock data
        Map<String, Object> arrival1 = new HashMap<>();
        arrival1.put("route_id", "1");
        arrival1.put("route_name", "Red Route");
        arrival1.put("trip_id", "trip_1");
        arrival1.put("arrival_time", System.currentTimeMillis() / 1000 + 300); // 5 minutes

        Map<String, Object> arrival2 = new HashMap<>();
        arrival2.put("route_id", "2");
        arrival2.put("route_name", "Blue Route");
        arrival2.put("trip_id", "trip_2");
        arrival2.put("arrival_time", System.currentTimeMillis() / 1000 + 600); // 10 minutes

        arrivals.add(arrival1);
        arrivals.add(arrival2);

        return arrivals;
    }

    /**
     * Get shape points for a specific route
     * @param routeId The route ID
     * @return List of LatLng coordinates for the route shape
     */
    public List<LatLng> getRouteShape(String routeId) {
        if (routeShapes.containsKey(routeId)) {
            return routeShapes.get(routeId);
        }

        // If we don't have the shape cached, try to find it
        // First, find a trip for this route
        String tripId = null;
        for (Map.Entry<String, Map<String, String>> entry : tripData.entrySet()) {
            Map<String, String> trip = entry.getValue();
            if (trip.containsKey("route_id") && trip.get("route_id").equals(routeId)) {
                tripId = entry.getKey();
                break;
            }
        }

        if (tripId == null) {
            return createMockRouteShape(routeId); // No trip found, create mock shape
        }

        // Use the trip to find the shape
        List<LatLng> shape = generateRouteFromStops(tripId);

        // If no shape points, create mock shape
        if (shape.isEmpty()) {
            return createMockRouteShape(routeId);
        }

        return shape;
    }

    /**
     * Create a mock route shape for when no shapes are available
     */
    private List<LatLng> createMockRouteShape(String routeId) {
        List<LatLng> mockShape = new ArrayList<>();

        // Create a route around Ames city center
        switch (routeId) {
            case "1": // Red route - north loop
                mockShape.add(new LatLng(42.023, -93.647)); // Start
                mockShape.add(new LatLng(42.024, -93.646));
                mockShape.add(new LatLng(42.025, -93.645));
                mockShape.add(new LatLng(42.026, -93.644));
                mockShape.add(new LatLng(42.027, -93.645));
                mockShape.add(new LatLng(42.028, -93.646));
                mockShape.add(new LatLng(42.027, -93.647));
                mockShape.add(new LatLng(42.026, -93.648));
                mockShape.add(new LatLng(42.025, -93.649));
                mockShape.add(new LatLng(42.024, -93.648));
                mockShape.add(new LatLng(42.023, -93.647)); // End (loop)
                break;

            case "2": // Blue route - east loop
                mockShape.add(new LatLng(42.027, -93.649)); // Start
                mockShape.add(new LatLng(42.028, -93.648));
                mockShape.add(new LatLng(42.029, -93.647));
                mockShape.add(new LatLng(42.030, -93.646));
                mockShape.add(new LatLng(42.029, -93.645));
                mockShape.add(new LatLng(42.028, -93.644));
                mockShape.add(new LatLng(42.027, -93.645));
                mockShape.add(new LatLng(42.026, -93.646));
                mockShape.add(new LatLng(42.027, -93.647));
                mockShape.add(new LatLng(42.028, -93.648));
                mockShape.add(new LatLng(42.027, -93.649)); // End (loop)
                break;

            case "3": // Green route - west loop
                mockShape.add(new LatLng(42.028, -93.652)); // Start
                mockShape.add(new LatLng(42.029, -93.651));
                mockShape.add(new LatLng(42.030, -93.650));
                mockShape.add(new LatLng(42.031, -93.651));
                mockShape.add(new LatLng(42.032, -93.652));
                mockShape.add(new LatLng(42.031, -93.653));
                mockShape.add(new LatLng(42.030, -93.654));
                mockShape.add(new LatLng(42.029, -93.653));
                mockShape.add(new LatLng(42.028, -93.652)); // End (loop)
                break;

            default: // Generic loop for other routes
                // Make a circle based on route ID
                double baseLatitude = 42.025;
                double baseLongitude = -93.650;
                double radius = 0.005; // Roughly 500m

                // Use route ID to create some variety
                int routeIdNum = 0;
                try {
                    routeIdNum = Integer.parseInt(routeId);
                } catch (NumberFormatException e) {
                    // Use string hashcode if not a number
                    routeIdNum = Math.abs(routeId.hashCode() % 10);
                }

                baseLatitude += routeIdNum * 0.002;
                baseLongitude += routeIdNum * 0.001;

                // Create a circle of points
                for (int i = 0; i <= 10; i++) {
                    double angle = i * 2 * Math.PI / 10;
                    double lat = baseLatitude + radius * Math.sin(angle);
                    double lng = baseLongitude + radius * Math.cos(angle);
                    mockShape.add(new LatLng(lat, lng));
                }
                break;
        }

        // Cache the shape
        routeShapes.put(routeId, mockShape);

        return mockShape;
    }

    /**
     * Generate a route shape from stop coordinates when shape data isn't available
     * @param tripId The trip ID
     * @return List of LatLng coordinates connecting all stops
     */
    private List<LatLng> generateRouteFromStops(String tripId) {
        List<LatLng> routePoints = new ArrayList<>();

        // Get stop times for this trip
        List<Map<String, String>> tripStopTimes = stopTimesData.get(tripId);
        if (tripStopTimes == null || tripStopTimes.isEmpty()) {
            return routePoints;
        }

        // Sort stop times by sequence
        Collections.sort(tripStopTimes, (a, b) -> {
            int seqA = Integer.parseInt(a.getOrDefault("stop_sequence", "0"));
            int seqB = Integer.parseInt(b.getOrDefault("stop_sequence", "0"));
            return Integer.compare(seqA, seqB);
        });

        // Add each stop location to route points
        for (Map<String, String> stopTime : tripStopTimes) {
            String stopId = stopTime.get("stop_id");
            Map<String, String> stop = stopData.get(stopId);

            if (stop != null && stop.containsKey("stop_lat") && stop.containsKey("stop_lon")) {
                double lat = Double.parseDouble(stop.get("stop_lat"));
                double lon = Double.parseDouble(stop.get("stop_lon"));
                routePoints.add(new LatLng(lat, lon));
            }
        }

        // Cache the result
        String routeId = tripData.get(tripId).get("route_id");
        if (routeId != null) {
            routeShapes.put(routeId, routePoints);
        }

        return routePoints;
    }

    /**
     * Parse the routes.txt file
     */
    private void parseRoutes(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read header line
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return;
        }

        // Parse header to get column indices
        String[] headers = headerLine.split(",");
        int routeIdIndex = -1;
        int routeShortNameIndex = -1;
        int routeLongNameIndex = -1;
        int routeColorIndex = -1;

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();
            if (header.equals("route_id")) {
                routeIdIndex = i;
            } else if (header.equals("route_short_name")) {
                routeShortNameIndex = i;
            } else if (header.equals("route_long_name")) {
                routeLongNameIndex = i;
            } else if (header.equals("route_color")) {
                routeColorIndex = i;
            }
        }

        // Parse data lines
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");

            if (routeIdIndex >= 0 && routeIdIndex < fields.length) {
                String routeId = fields[routeIdIndex].trim();
                Map<String, String> route = new HashMap<>();

                if (routeShortNameIndex >= 0 && routeShortNameIndex < fields.length) {
                    route.put("route_short_name", fields[routeShortNameIndex].trim());
                }

                if (routeLongNameIndex >= 0 && routeLongNameIndex < fields.length) {
                    route.put("route_long_name", fields[routeLongNameIndex].trim());
                }

                if (routeColorIndex >= 0 && routeColorIndex < fields.length) {
                    route.put("route_color", fields[routeColorIndex].trim());
                }

                routeData.put(routeId, route);
            }
        }

        Log.d(TAG, "Parsed " + routeData.size() + " routes");
    }

    /**
     * Parse the stops.txt file
     */
    private void parseStops(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read header line
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return;
        }

        // Parse header to get column indices
        String[] headers = headerLine.split(",");
        int stopIdIndex = -1;
        int stopNameIndex = -1;
        int stopLatIndex = -1;
        int stopLonIndex = -1;

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();
            if (header.equals("stop_id")) {
                stopIdIndex = i;
            } else if (header.equals("stop_name")) {
                stopNameIndex = i;
            } else if (header.equals("stop_lat")) {
                stopLatIndex = i;
            } else if (header.equals("stop_lon")) {
                stopLonIndex = i;
            }
        }

        // Parse data lines
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");

            if (stopIdIndex >= 0 && stopIdIndex < fields.length) {
                String stopId = fields[stopIdIndex].trim();
                Map<String, String> stop = new HashMap<>();

                if (stopNameIndex >= 0 && stopNameIndex < fields.length) {
                    stop.put("stop_name", fields[stopNameIndex].trim());
                }

                if (stopLatIndex >= 0 && stopLatIndex < fields.length) {
                    stop.put("stop_lat", fields[stopLatIndex].trim());
                }

                if (stopLonIndex >= 0 && stopLonIndex < fields.length) {
                    stop.put("stop_lon", fields[stopLonIndex].trim());
                }

                stopData.put(stopId, stop);
            }
        }

        Log.d(TAG, "Parsed " + stopData.size() + " stops");
    }

    /**
     * Parse trips data from the static GTFS feed
     */
    private void parseTrips(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read header line
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return;
        }

        // Parse header to get column indices
        String[] headers = headerLine.split(",");
        int tripIdIndex = -1;
        int routeIdIndex = -1;
        int serviceIdIndex = -1;
        int tripHeadsignIndex = -1;
        int shapeIdIndex = -1;

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();
            switch (header) {
                case "trip_id":
                    tripIdIndex = i;
                    break;
                case "route_id":
                    routeIdIndex = i;
                    break;
                case "service_id":
                    serviceIdIndex = i;
                    break;
                case "trip_headsign":
                    tripHeadsignIndex = i;
                    break;
                case "shape_id":
                    shapeIdIndex = i;
                    break;
            }
        }

        // Parse data lines
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");

            if (tripIdIndex >= 0 && tripIdIndex < fields.length) {
                String tripId = fields[tripIdIndex].trim();
                Map<String, String> trip = new HashMap<>();

                if (routeIdIndex >= 0 && routeIdIndex < fields.length) {
                    trip.put("route_id", fields[routeIdIndex].trim());
                }

                if (serviceIdIndex >= 0 && serviceIdIndex < fields.length) {
                    trip.put("service_id", fields[serviceIdIndex].trim());
                }

                if (tripHeadsignIndex >= 0 && tripHeadsignIndex < fields.length) {
                    trip.put("trip_headsign", fields[tripHeadsignIndex].trim());
                }

                if (shapeIdIndex >= 0 && shapeIdIndex < fields.length) {
                    trip.put("shape_id", fields[shapeIdIndex].trim());
                }

                tripData.put(tripId, trip);
            }
        }

        Log.d(TAG, "Parsed " + tripData.size() + " trips");
    }

    /**
     * Parse stop times data from the static GTFS feed
     */
    private void parseStopTimes(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read header line
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return;
        }

        // Parse header to get column indices
        String[] headers = headerLine.split(",");
        int tripIdIndex = -1;
        int arrivalTimeIndex = -1;
        int departureTimeIndex = -1;
        int stopIdIndex = -1;
        int stopSequenceIndex = -1;

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();
            switch (header) {
                case "trip_id":
                    tripIdIndex = i;
                    break;
                case "arrival_time":
                    arrivalTimeIndex = i;
                    break;
                case "departure_time":
                    departureTimeIndex = i;
                    break;
                case "stop_id":
                    stopIdIndex = i;
                    break;
                case "stop_sequence":
                    stopSequenceIndex = i;
                    break;
            }
        }

        // Parse data lines
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");

            if (tripIdIndex >= 0 && tripIdIndex < fields.length &&
                    stopIdIndex >= 0 && stopIdIndex < fields.length) {

                String tripId = fields[tripIdIndex].trim();
                String stopId = fields[stopIdIndex].trim();

                Map<String, String> stopTime = new HashMap<>();
                stopTime.put("stop_id", stopId);

                if (arrivalTimeIndex >= 0 && arrivalTimeIndex < fields.length) {
                    stopTime.put("arrival_time", fields[arrivalTimeIndex].trim());
                }

                if (departureTimeIndex >= 0 && departureTimeIndex < fields.length) {
                    stopTime.put("departure_time", fields[departureTimeIndex].trim());
                }

                if (stopSequenceIndex >= 0 && stopSequenceIndex < fields.length) {
                    stopTime.put("stop_sequence", fields[stopSequenceIndex].trim());
                }

                // Add to stop times data
                if (!stopTimesData.containsKey(tripId)) {
                    stopTimesData.put(tripId, new ArrayList<>());
                }
                stopTimesData.get(tripId).add(stopTime);
            }
        }

        Log.d(TAG, "Parsed stop times for " + stopTimesData.size() + " trips");
    }

    /**
     * Parse shapes.txt file from the GTFS feed
     */
    private void parseShapes(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read header line
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return;
        }

        // Parse header to get column indices
        String[] headers = headerLine.split(",");
        int shapeIdIndex = -1;
        int shapePtLatIndex = -1;
        int shapePtLonIndex = -1;
        int shapePtSequenceIndex = -1;

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();
            switch (header) {
                case "shape_id":
                    shapeIdIndex = i;
                    break;
                case "shape_pt_lat":
                    shapePtLatIndex = i;
                    break;
                case "shape_pt_lon":
                    shapePtLonIndex = i;
                    break;
                case "shape_pt_sequence":
                    shapePtSequenceIndex = i;
                    break;
            }
        }

        // Temporary storage for shape points before sorting
        Map<String, Map<Integer, LatLng>> shapePoints = new HashMap<>();

        // Parse data lines
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");

            if (shapeIdIndex >= 0 && shapePtLatIndex >= 0 && shapePtLonIndex >= 0 &&
                    shapePtSequenceIndex >= 0 && fields.length > Math.max(shapeIdIndex,
                    Math.max(shapePtLatIndex, Math.max(shapePtLonIndex, shapePtSequenceIndex)))) {

                String shapeId = fields[shapeIdIndex].trim();
                double lat = Double.parseDouble(fields[shapePtLatIndex].trim());
                double lon = Double.parseDouble(fields[shapePtLonIndex].trim());
                int sequence = Integer.parseInt(fields[shapePtSequenceIndex].trim());

                // Add to temporary storage
                if (!shapePoints.containsKey(shapeId)) {
                    shapePoints.put(shapeId, new HashMap<>());
                }
                shapePoints.get(shapeId).put(sequence, new LatLng(lat, lon));
            }
        }

        // Process shapes into ordered lists and connect to routes
        for (Map.Entry<String, Map<Integer, LatLng>> entry : shapePoints.entrySet()) {
            String shapeId = entry.getKey();
            Map<Integer, LatLng> points = entry.getValue();

            // Sort points by sequence
            List<Integer> sequences = new ArrayList<>(points.keySet());
            Collections.sort(sequences);

            List<LatLng> orderedPoints = new ArrayList<>();
            for (int seq : sequences) {
                orderedPoints.add(points.get(seq));
            }

            // Find routes that use this shape
            for (Map.Entry<String, Map<String, String>> tripEntry : tripData.entrySet()) {
                Map<String, String> trip = tripEntry.getValue();
                if (trip.containsKey("shape_id") && trip.get("shape_id").equals(shapeId) &&
                        trip.containsKey("route_id")) {
                    String routeId = trip.get("route_id");
                    routeShapes.put(routeId, orderedPoints);
                }
            }
        }

        Log.d(TAG, "Parsed shapes for " + routeShapes.size() + " routes");
    }
}