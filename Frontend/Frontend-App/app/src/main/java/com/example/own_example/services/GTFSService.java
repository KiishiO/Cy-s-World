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
 * This implementation downloads and parses GTFS data directly
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
                // Fall back to mock data
                getMockGTFSBuses(callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed to download static GTFS data: " + response.code());
                    if (callback != null) {
                        callback.onError("Failed to download route and stop data: HTTP " + response.code());
                    }
                    // Fall back to mock data
                    getMockGTFSBuses(callback);
                    return;
                }

                try {
                    Log.d(TAG, "Static GTFS data downloaded successfully, processing...");
                    // Process the ZIP file from the response
                    processStaticGtfsData(response.body().byteStream());

                    // Mark static data as loaded
                    isStaticDataLoaded = true;

                    // Once static data is loaded, we can start fetching real-time updates
                    startRealTimeUpdateScheduler(intervalSeconds);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing static GTFS data", e);
                    if (callback != null) {
                        callback.onError("Error processing route and stop data: " + e.getMessage());
                    }
                    // Fall back to mock data
                    getMockGTFSBuses(callback);
                }
            }
        });
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
                    ", Trips: " + tripData.size() + ", StopTimes: " + stopTimesData.size());
        } finally {
            zis.close();
        }
    }

    /**
     * Fetch real-time updates
     */
    private void fetchRealTimeUpdates() {
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
     * Note: GTFS-RT uses Protocol Buffers format
     * In a production app, you would use the official GTFS-RT proto files to parse this data
     * For now, we'll use mock data since implementing a full protobuf parser is outside the scope
     */
    private void processVehiclePositions(byte[] data) {
        try {
            Log.d(TAG, "Processing vehicle positions data: " + data.length + " bytes");

            // TODO: In a real implementation, you would use a proper GTFS-RT parser library here
            // For now, we'll use mock data instead

            // This is where you would normally parse the protobuf data using the official library:
            // Example pseudocode:
            // FeedMessage feed = FeedMessage.parseFrom(data);
            // List<GTFSBus> buses = new ArrayList<>();
            // for (FeedEntity entity : feed.getEntityList()) {
            //     if (entity.hasVehicle()) {
            //         VehiclePosition vehicle = entity.getVehicle();
            //         GTFSBus bus = new GTFSBus();
            //         // Convert to your bus model...
            //         buses.add(bus);
            //     }
            // }

            // For now, let's use mock data
            Log.w(TAG, "Using mock data instead of parsing real GTFS-RT data");
            getMockGTFSBuses(callback);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing vehicle positions", e);
            // Fall back to mock data
            getMockGTFSBuses(callback);
        }
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
            return new ArrayList<>(); // No trip found for this route
        }

        // Use the trip to find the shape
        return generateRouteFromStops(tripId);
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
     * This should be called during processStaticGtfsData
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
        while ((line // finish from here, still working on this