package com.example.own_example;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.models.GTFSBus;
import com.example.own_example.services.GTFSService;
import com.example.own_example.adapters.ArrivalInfoAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.view.LayoutInflater;
import android.graphics.drawable.GradientDrawable;

public class BusMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "BusMapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final LatLng AMES_CENTER = new LatLng(42.026, -93.648); // Ames, Iowa center coordinates
    private static final float DEFAULT_ZOOM = 15f;
    private static final int BUS_ANIMATION_DURATION = 3000; // Animation duration in milliseconds

    private GoogleMap googleMap;
    private GTFSService gtfsService;
    private Spinner routeFilterSpinner;
    private FloatingActionButton listViewFab;
    private FloatingActionButton myLocationFab;
    private FloatingActionButton toggleStopsFab;
    private TextView emptyStateText;
    private ProgressBar loadingIndicator;
    private MaterialCardView arrivalInfoCard;
    private RecyclerView arrivalRecyclerView;
    private TextView disclaimerText;

    private final Map<String, Marker> busMarkers = new HashMap<>();
    private final Map<String, Marker> stopMarkers = new HashMap<>();
    private final Map<String, LatLng> previousBusPositions = new HashMap<>();
    private final Set<String> filteredRouteIds = new HashSet<>();
    private Map<String, Polyline> routePolylines = new HashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private FusedLocationProviderClient fusedLocationClient;
    private boolean showStops = false;
    private BitmapDescriptor regularStopIcon;
    private BitmapDescriptor majorStopIcon;
    private Map<String, Integer> routeColors = new HashMap<>(); // Store route colors for reuse
    private boolean routesDrawn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_map);

        // Initialize views
        initializeViews();

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize GTFS service
        gtfsService = new GTFSService(this);

        // Set up click listeners
        setUpClickListeners();
    }

    /**
     * Initialize views and handle null checks safely
     */
    private void initializeViews() {
        routeFilterSpinner = findViewById(R.id.route_filter_spinner);
        listViewFab = findViewById(R.id.list_view_fab);
        myLocationFab = findViewById(R.id.my_location_fab);
        toggleStopsFab = findViewById(R.id.toggle_stops_fab);
        emptyStateText = findViewById(R.id.empty_state_text);
        loadingIndicator = findViewById(R.id.loading_indicator);
        arrivalInfoCard = findViewById(R.id.arrival_info_card);
        arrivalRecyclerView = findViewById(R.id.arrival_recycler_view);
        disclaimerText = findViewById(R.id.disclaimer_text);

        // Show loading indicator if it exists
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        // Hide empty state text if it exists
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
        }

        // Hide arrival info card initially
        if (arrivalInfoCard != null) {
            arrivalInfoCard.setVisibility(View.GONE);
        }

        // Setup RecyclerView
        if (arrivalRecyclerView != null) {
            arrivalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        // Set disclaimer text
        if (disclaimerText != null) {
            disclaimerText.setText(R.string.disclaimer_text);
        }
    }

    /**
     * Set up click listeners for buttons
     */
    private void setUpClickListeners() {
        if (listViewFab != null) {
            listViewFab.setOnClickListener(v -> finish()); // Return to list view
        }

        if (myLocationFab != null) {
            myLocationFab.setOnClickListener(v -> moveToCurrentLocation());
        }

        if (toggleStopsFab != null) {
            toggleStopsFab.setOnClickListener(v -> {
                showStops = !showStops;
                if (showStops) {
                    showBusStops();
                } else {
                    hideBusStops();
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Set up map settings
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Move camera to Ames center
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(AMES_CENTER, DEFAULT_ZOOM));

        // Initialize stop icons
        initStopIcons();

        // Check for and request location permissions
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set up marker click listener
        googleMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            if (stopMarkers.containsValue(marker)) {
                // Show arrival info for this stop
                showArrivalInfo(getStopIdForMarker(marker));
            }
            return true;
        });

        // Draw all route lines first (if data is available)
        drawAllRoutes();

        // Start real-time updates
        startRealTimeBusUpdates();
    }

    /**
     * Set up the route filter spinner with data
     */
    private void setupRouteFilterSpinner(Map<String, Map<String, String>> routes) {
        List<RouteItem> routeItems = new ArrayList<>();

        // Add "All Routes" option
        routeItems.add(new RouteItem("all", "All Routes"));

        // Sort route IDs for better organization
        List<String> sortedRouteIds = new ArrayList<>(routes.keySet());
        Collections.sort(sortedRouteIds, (a, b) -> {
            try {
                // Try to sort numerically
                return Integer.parseInt(a) - Integer.parseInt(b);
            } catch (NumberFormatException e) {
                // Fall back to string comparison
                return a.compareTo(b);
            }
        });

        // Add all routes without limits
        for (String routeId : sortedRouteIds) {
            // Get formatted route name from GTFSService
            String displayName = gtfsService.getRouteName(routeId);
            routeItems.add(new RouteItem(routeId, displayName));
        }

        // Create and set the adapter
        RouteSpinnerAdapter adapter = new RouteSpinnerAdapter(this, routeItems);
        routeFilterSpinner.setAdapter(adapter);

        // Set selection listener
        routeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RouteItem selectedItem = (RouteItem) parent.getItemAtPosition(position);

                // Clear existing filters
                filteredRouteIds.clear();

                // Add selected route to filter (unless "All Routes" is selected)
                if (!selectedItem.getId().equals("all")) {
                    filteredRouteIds.add(selectedItem.getId());
                }

                // Update visibility of buses and route lines
                updateVisibleBuses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Clear filters
                filteredRouteIds.clear();
                updateVisibleBuses();
            }
        });
    }


    /**
     * Helper class for route items
     */
    private static class RouteItem {
        private final String id;
        private final String name;

        public RouteItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Custom adapter for route spinner
     */
    private class RouteSpinnerAdapter extends ArrayAdapter<RouteItem> {
        public RouteSpinnerAdapter(Context context, List<RouteItem> items) {
            super(context, android.R.layout.simple_spinner_item, items);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view;

            RouteItem item = getItem(position);
            if (item != null) {
                textView.setText(item.getName());
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(16);
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView textView = (TextView) view;

            RouteItem item = getItem(position);
            if (item != null) {
                textView.setText(item.getName());
                textView.setPadding(24, 16, 24, 16);

                // Add color indicator for routes (except "All Routes")
                if (!item.getId().equals("all")) {
                    int routeColor = getRouteColor(item.getId());

                    // Create a colored background with transparency for readability
                    int r = Color.red(routeColor);
                    int g = Color.green(routeColor);
                    int b = Color.blue(routeColor);

                    // Use a very light tint of the color
                    int bgColor = Color.argb(40, r, g, b);

                    // Set left border with route color
                    GradientDrawable gd = new GradientDrawable();
                    gd.setColor(bgColor);
                    gd.setStroke(8, routeColor);
                    textView.setBackground(gd);
                } else {
                    textView.setBackgroundColor(Color.WHITE);
                }
            }

            return view;
        }
    }


    /**
     * Find stop ID for a given marker
     */
    private String getStopIdForMarker(Marker marker) {
        for (Map.Entry<String, Marker> entry : stopMarkers.entrySet()) {
            if (entry.getValue().equals(marker)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Show arrival information for a stop
     */
    private void showArrivalInfo(String stopId) {
        if (stopId == null || arrivalInfoCard == null || arrivalRecyclerView == null) {
            return;
        }

        // Get the stop name
        Map<String, String> stopInfo = gtfsService.getStopInfo(stopId);
        String stopName = stopInfo != null && stopInfo.containsKey("stop_name")
                ? stopInfo.get("stop_name") : "Unknown Stop";

        // Set the title of the card to show the stop name
        TextView cardTitle = arrivalInfoCard.findViewById(R.id.stop_name_title);
        if (cardTitle != null) {
            cardTitle.setText(stopName);
        }

        // Get arrivals for this stop
        List<Map<String, Object>> arrivals = gtfsService.getUpcomingArrivals(stopId);

        if (arrivals.isEmpty()) {
            // Show message if no arrivals
            TextView noArrivalsText = arrivalInfoCard.findViewById(R.id.no_arrivals_text);
            if (noArrivalsText != null) {
                noArrivalsText.setVisibility(View.VISIBLE);
                arrivalRecyclerView.setVisibility(View.GONE);
            }
        } else {
            // Sort arrivals by time
            Collections.sort(arrivals, (a, b) -> {
                long timeA = (long) a.get("arrival_time");
                long timeB = (long) b.get("arrival_time");
                return Long.compare(timeA, timeB);
            });

            // Hide no arrivals message if it exists
            TextView noArrivalsText = arrivalInfoCard.findViewById(R.id.no_arrivals_text);
            if (noArrivalsText != null) {
                noArrivalsText.setVisibility(View.GONE);
            }

            // Set up the RecyclerView
            arrivalRecyclerView.setVisibility(View.VISIBLE);
            ArrivalInfoAdapter adapter = new ArrivalInfoAdapter(this, arrivals);
            arrivalRecyclerView.setAdapter(adapter);
        }

        // Show the card
        arrivalInfoCard.setVisibility(View.VISIBLE);

        // Add close button functionality
        View closeButton = arrivalInfoCard.findViewById(R.id.close_button);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> arrivalInfoCard.setVisibility(View.GONE));
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            moveToCurrentLocation();
        }
    }

    private void moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
                } else {
                    // If location is null, use Ames center
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(AMES_CENTER, DEFAULT_ZOOM));
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    /**
     * Start real-time bus updates
     */
    private void startRealTimeBusUpdates() {
        // Start updates every 5 seconds for more real-time feel
        gtfsService.startRealTimeUpdates(5, new GTFSService.GTFSBusCallback() {
            @Override
            public void onSuccess(List<GTFSBus> buses) {
                // Update on the main thread
                mainHandler.post(() -> {
                    hideLoadingIndicator();
                    if (buses.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        updateBusMarkersWithAnimation(buses);

                        // If we haven't drawn routes yet or they were empty initially, try again
                        if (!routesDrawn && googleMap != null) {
                            drawAllRoutes();
                        }

                        // Setup route filter spinner if not done yet
                        if (routeFilterSpinner.getAdapter() == null) {
                            setupRouteFilterSpinner(gtfsService.getRouteData());
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    hideLoadingIndicator();
                    // Just show toast - the service will automatically fall back to mock data
                    Toast.makeText(BusMapActivity.this, "Using simulation mode: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Show empty state message
     */
    private void showEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide empty state message
     */
    private void hideEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    /**
     * Hide loading indicator
     */
    private void hideLoadingIndicator() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    /**
     * Update bus markers on the map with smooth animation
     */
    private void updateBusMarkersWithAnimation(List<GTFSBus> buses) {
        // Create a set of current bus IDs
        Set<String> currentBusIds = new HashSet<>();

        Log.d(TAG, "Updating " + buses.size() + " buses on map");

        for (GTFSBus bus : buses) {
            // Skip if not in service
            if (!bus.isInService()) {
                continue;
            }

            String busId = bus.getVehicleId() != null ? bus.getVehicleId() : bus.getTripId();
            // If still null, generate a unique ID
            if (busId == null) {
                busId = "bus-" + bus.getBusNum() + "-" + bus.getRouteId();
            }

            currentBusIds.add(busId);
            LatLng newPosition = new LatLng(bus.getLatitude(), bus.getLongitude());

            // Check if this bus should be visible based on filters
            boolean shouldBeVisible = filteredRouteIds.isEmpty() || filteredRouteIds.contains(bus.getRouteId());

            if (busMarkers.containsKey(busId)) {
                // Update existing marker
                Marker marker = busMarkers.get(busId);

                // Update visibility based on current filters
                marker.setVisible(shouldBeVisible);

                // If we have a previous position, animate the movement
                if (previousBusPositions.containsKey(busId)) {
                    LatLng prevPosition = previousBusPositions.get(busId);
                    animateMarker(marker, prevPosition, newPosition, bus.getBearing());
                } else {
                    // Just set position if no previous position
                    marker.setPosition(newPosition);
                    marker.setRotation(bus.getBearing());
                }

                // Update marker title and snippet
                String title = "Bus " + bus.getBusNum() + ": " + bus.getBusName();
                String snippet = "Current Stop: " + (bus.getStopLocation() != null ? bus.getStopLocation() : "Unknown");

                if (bus.getNextStop() != null) {
                    Map<String, String> stopInfo = gtfsService.getStopInfo(bus.getNextStop());
                    if (stopInfo != null && stopInfo.containsKey("stop_name")) {
                        snippet += "\nNext Stop: " + stopInfo.get("stop_name");
                    }
                }

                if (bus.getPredictedArrivalTime() > 0) {
                    snippet += "\nNext Arrival: " + bus.getFormattedArrivalTime();
                }

                marker.setTitle(title);
                marker.setSnippet(snippet);
            } else {
                // Create new marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(newPosition)
                        .title("Bus " + bus.getBusNum() + ": " + bus.getBusName())
                        .snippet("Current Stop: " + (bus.getStopLocation() != null ? bus.getStopLocation() : "Unknown"))
                        .icon(getBusIcon(bus.getRouteId()))
                        .rotation(bus.getBearing())
                        .anchor(0.5f, 0.5f)
                        .flat(true)
                        .visible(shouldBeVisible); // Set initial visibility based on filters

                Marker marker = googleMap.addMarker(markerOptions);

                if (marker != null) {
                    Log.d(TAG, "Created new marker for bus " + busId + " at " + newPosition.latitude + "," + newPosition.longitude);
                    busMarkers.put(busId, marker);
                } else {
                    Log.e(TAG, "Failed to create marker for bus " + busId);
                }
            }

            // Store current position for next update
            previousBusPositions.put(busId, newPosition);
        }

        // Remove markers for buses that are no longer present
        List<String> markersToRemove = new ArrayList<>();
        for (String busId : busMarkers.keySet()) {
            if (!currentBusIds.contains(busId)) {
                busMarkers.get(busId).remove();
                markersToRemove.add(busId);
                previousBusPositions.remove(busId); // Also remove from previous positions
            }
        }

        for (String busId : markersToRemove) {
            busMarkers.remove(busId);
        }

        // Update empty state visibility based on visible buses
        updateEmptyStateVisibility();
    }

    /**
     * Update empty state visibility based on currently visible buses
     */
    private void updateEmptyStateVisibility() {
        if (emptyStateText == null) return;

        // Check if any buses are visible
        boolean anyVisibleBuses = false;
        for (Marker marker : busMarkers.values()) {
            if (marker.isVisible()) {
                anyVisibleBuses = true;
                break;
            }
        }

        // Show empty state if no buses are visible
        emptyStateText.setVisibility(anyVisibleBuses ? View.GONE : View.VISIBLE);
    }

    /**
     * Animate marker movement from previous to new position
     */
    private void animateMarker(final Marker marker, final LatLng start, final LatLng end, final float finalBearing) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long startTime = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();
        final float startBearing = marker.getRotation();

        // Adjust bearing calculation
        final float bearingDiff = calculateBearingDifference(startBearing, finalBearing);

        handler.post(new Runnable() {
            @Override
            public void run() {
                // Calculate progress
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / BUS_ANIMATION_DURATION);

                // Calculate intermediate position
                double lat = start.latitude + ((end.latitude - start.latitude) * t);
                double lng = start.longitude + ((end.longitude - start.longitude) * t);

                // Calculate intermediate bearing
                float bearing = startBearing + (bearingDiff * t);

                // Update marker
                marker.setPosition(new LatLng(lat, lng));
                marker.setRotation(bearing);

                // Continue animation if not done
                if (t < 1.0) {
                    handler.postDelayed(this, 16); // Approx 60fps
                }
            }
        });
    }

    /**
     * Calculate the shortest rotation between two bearings
     */
    private float calculateBearingDifference(float startBearing, float endBearing) {
        float diff = endBearing - startBearing;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }
        return diff;
    }

    /**
     * Update bus visibility based on selected route filters
     */
    private void updateVisibleBuses() {
        // Get the set of active filters
        Set<String> activeFilters = new HashSet<>(filteredRouteIds);

        // Update all bus markers
        for (Map.Entry<String, Marker> entry : busMarkers.entrySet()) {
            Marker marker = entry.getValue();
            String busId = entry.getKey();

            // Find the routeId for this bus
            String routeId = getBusRouteId(busId);

            // Show if this bus's route is in the filtered routes
            boolean shouldBeVisible = activeFilters.isEmpty() || activeFilters.contains(routeId);
            marker.setVisible(shouldBeVisible);
        }

        // Update route polylines visibility
        for (Map.Entry<String, Polyline> entry : routePolylines.entrySet()) {
            String routeId = entry.getKey();
            Polyline polyline = entry.getValue();

            // Show the route line if it's in the filtered routes, or if no filters are active
            boolean shouldBeVisible = activeFilters.isEmpty() || activeFilters.contains(routeId);
            polyline.setVisible(shouldBeVisible);
        }

        // Update stop markers if they're showing
        if (showStops) {
            updateStopMarkerVisibility(activeFilters);
        }

        // Update empty state visibility
        updateEmptyStateVisibility();
    }

    /**
     * Update stop marker visibility based on active route filters
     */
    private void updateStopMarkerVisibility(Set<String> activeFilters) {
        // If no filters active, show all stops
        if (activeFilters.isEmpty()) {
            for (Marker marker : stopMarkers.values()) {
                marker.setVisible(true);
            }
            return;
        }

        // Otherwise, only show stops that are served by the selected routes
        for (Marker marker : stopMarkers.values()) {
            Object tag = marker.getTag();
            if (tag instanceof Map) {
                @SuppressWarnings("unchecked")
                Set<String> stopRoutes = (Set<String>) ((Map<String, Object>) tag).get("routes");

                // If any of the stop's routes are in the active filters, show the stop
                boolean shouldBeVisible = false;
                if (stopRoutes != null) {
                    for (String routeId : stopRoutes) {
                        if (activeFilters.contains(routeId)) {
                            shouldBeVisible = true;
                            break;
                        }
                    }
                }

                marker.setVisible(shouldBeVisible);
            } else {
                // Default to visible if tag is missing
                marker.setVisible(true);
            }
        }
    }

    /**
     * Helper method to get the route ID for a bus marker
     */
    private String getBusRouteId(String busId) {
        // Extract routeId from busId if possible
        if (busId.contains("vehicle_")) {
            String[] parts = busId.split("_");
            if (parts.length >= 2) {
                // Get the second part as route ID
                return parts[1];
            }
        }

        // If we can't determine, return empty
        return "";
    }

    /**
     * Draw all routes on the map
     */
    private void drawAllRoutes() {
        Log.d(TAG, "Drawing all routes on map");

        // Get route data from GTFSService
        Map<String, Map<String, String>> routes = gtfsService.getRouteData();

        if (routes.isEmpty()) {
            Log.w(TAG, "No route data available to draw yet - will try again later");
            return;
        }

        Log.d(TAG, "Found " + routes.size() + " routes to draw");

        // Draw each route
        for (Map.Entry<String, Map<String, String>> entry : routes.entrySet()) {
            String routeId = entry.getKey();
            Map<String, String> routeInfo = entry.getValue();

            Log.d(TAG, "Drawing route: " + routeId);

            // Get route color or use default
            String routeColor = "#FF0000"; // Default red
            if (routeInfo.containsKey("route_color")) {
                routeColor = routeInfo.get("route_color");

                // Make sure routeColor has # prefix
                if (!routeColor.startsWith("#")) {
                    routeColor = "#" + routeColor;
                }

                Log.d(TAG, "Route " + routeId + " color: " + routeColor);
            }

            // Draw this route
            drawRouteLine(routeId, routeColor);
        }

        routesDrawn = true;
    }

    /**
     * Draw route line on the map based on shapes data from GTFS
     */
    private void drawRouteLine(String routeId, String routeColor) {
        // Get shape points for this route from your GTFSService
        List<LatLng> shapePoints = gtfsService.getRouteShape(routeId);

        if (shapePoints == null || shapePoints.isEmpty()) {
            Log.w(TAG, "No shape points for route " + routeId);
            return;
        }

        Log.d(TAG, "Drawing route " + routeId + " with " + shapePoints.size() + " points");

        // Convert color string to actual color with error handling
        int color;
        try {
            // Make sure routeColor has # prefix
            if (!routeColor.startsWith("#")) {
                routeColor = "#" + routeColor;
            }
            color = Color.parseColor(routeColor);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid route color: " + routeColor + " for route " + routeId);
            color = Color.RED; // Default to red if parsing fails
        }

        // Save color for reuse
        routeColors.put(routeId, color);

        // Create polyline options
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(shapePoints)
                .width(8)  // Line width in pixels
                .color(color)
                .geodesic(true)
                .zIndex(1);  // Draw above other map elements

        // Add the polyline to the map
        Polyline polyline = googleMap.addPolyline(polylineOptions);

        // Store the polyline reference
        routePolylines.put(routeId, polyline);

        Log.d(TAG, "Route " + routeId + " drawn successfully");
    }

    /**
     * Initialize bus stop icons
     */
    private void initStopIcons() {
        // Create icons for stops
        int regularStopColor = Color.WHITE;
        int majorStopColor = Color.YELLOW;

        // Create circular icons with borders
        regularStopIcon = createStopIcon(regularStopColor, Color.BLACK, 12);
        majorStopIcon = createStopIcon(majorStopColor, Color.BLACK, 16);
    }

    /**
     * Create a circular icon for bus stops
     */
    private BitmapDescriptor createStopIcon(int fillColor, int strokeColor, int sizeDp) {
        // Convert dp to pixels
        float density = getResources().getDisplayMetrics().density;
        int sizePx = Math.round(sizeDp * density);
        int strokeWidthPx = Math.round(2 * density);

        // Create bitmap
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Create paint for fill
        Paint fillPaint = new Paint();
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        // Create paint for stroke
        Paint strokePaint = new Paint();
        strokePaint.setColor(strokeColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidthPx);
        strokePaint.setAntiAlias(true);

        // Draw circle
        int radius = (sizePx - strokeWidthPx) / 2;
        int center = sizePx / 2;
        canvas.drawCircle(center, center, radius, fillPaint);
        canvas.drawCircle(center, center, radius, strokePaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Show all bus stops on the map
     */
    private void showBusStops() {
        // Get all stops from GTFSService
        Map<String, Map<String, String>> stops = gtfsService.getStopData();

        // Show progress while loading stops
        int totalStops = stops.size();
        int addedStops = 0;

        for (Map.Entry<String, Map<String, String>> entry : stops.entrySet()) {
            String stopId = entry.getKey();
            Map<String, String> stopInfo = entry.getValue();

            // Skip if already on map
            if (stopMarkers.containsKey(stopId)) {
                continue;
            }

            // Get stop coordinates
            if (stopInfo.containsKey("stop_lat") && stopInfo.containsKey("stop_lon")) {
                double lat = Double.parseDouble(stopInfo.get("stop_lat"));
                double lon = Double.parseDouble(stopInfo.get("stop_lon"));
                String stopName = stopInfo.getOrDefault("stop_name", "Unknown Stop");

                // Create marker with the stop position and name
                LatLng position = new LatLng(lat, lon);
                createStopMarker(stopId, position, stopName);
                addedStops++;

                // To avoid ANRs, use batch processing for very large datasets
                if (addedStops % 50 == 0) {
                    final int progress = addedStops;
                    // Optional: show a toast with progress
                    mainHandler.post(() ->
                            Toast.makeText(BusMapActivity.this,
                                    "Loaded " + progress + " of " + totalStops + " stops",
                                    Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }

        // Show final count
        final int finalCount = addedStops;
        mainHandler.post(() ->
                Toast.makeText(BusMapActivity.this,
                        "Showing " + finalCount + " bus stops",
                        Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Create a marker for a bus stop
     */
    private void createStopMarker(String stopId, LatLng position, String stopName) {
        // Get the routes that serve this stop
        Set<String> stopRoutes = getRoutesForStop(stopId);

        // Store the routes with the marker in the tag
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(stopName)
                .icon(getBusStopIcon(stopRoutes))
                .anchor(0.5f, 0.5f);

        Marker marker = googleMap.addMarker(markerOptions);
        if (marker != null) {
            marker.setTag(new HashMap<String, Object>() {{
                put("stopId", stopId);
                put("routes", stopRoutes);
            }});
            stopMarkers.put(stopId, marker);
        }
    }

    /**
     * Hide all bus stops on the map
     */
    private void hideBusStops() {
        for (Marker marker : stopMarkers.values()) {
            marker.remove();
        }
        stopMarkers.clear();
    }

    /**
     * Determine if a stop is a major stop based on name or properties
     */
    private boolean isMajorStop(String stopName, Map<String, String> stopInfo) {
        // Example criteria - you can customize based on your GTFS data
        return stopName.contains("Station") ||
                stopName.contains("Terminal") ||
                stopName.contains("Center") ||
                stopName.contains("Mall") ||
                (stopInfo.containsKey("location_type") && stopInfo.get("location_type").equals("1")) ||
                (stopInfo.containsKey("parent_station") && !stopInfo.get("parent_station").isEmpty());
    }

    /**
     * Get a custom icon for a bus based on its route
     */
    private BitmapDescriptor getBusIcon(String routeId) {
        // Get color based on route ID
        int color;
        if (routeColors.containsKey(routeId)) {
            color = routeColors.get(routeId);
        } else {
            // For real CyRide routes, generate consistent colors based on route ID
            int hash = routeId.hashCode();
            int r = (hash & 0xFF0000) >> 16;
            int g = (hash & 0x00FF00) >> 8;
            int b = hash & 0x0000FF;

            // Ensure colors aren't too dark
            r = Math.max(r, 100);
            g = Math.max(g, 100);
            b = Math.max(b, 100);

            color = Color.rgb(r, g, b);
            routeColors.put(routeId, color);
        }

        // Create a custom bitmap for the bus marker
        float density = getResources().getDisplayMetrics().density;
        int sizeDp = 28; // Size in dp
        int sizePx = Math.round(sizeDp * density);

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Create outer circle paint (colored by route)
        Paint circlePaint = new Paint();
        circlePaint.setColor(color);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAntiAlias(true);

        // Create inner circle paint (black dot)
        Paint innerCirclePaint = new Paint();
        innerCirclePaint.setColor(Color.BLACK);
        innerCirclePaint.setStyle(Paint.Style.FILL);
        innerCirclePaint.setAntiAlias(true);

        // Create border paint
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.round(2 * density));
        borderPaint.setAntiAlias(true);

        // Draw the outer colored circle
        int centerX = sizePx / 2;
        int centerY = sizePx / 2;
        int outerRadius = (sizePx / 2) - Math.round(2 * density);
        canvas.drawCircle(centerX, centerY, outerRadius, circlePaint);

        // Draw white border
        canvas.drawCircle(centerX, centerY, outerRadius, borderPaint);

        // Draw the inner black circle (dot)
        int innerRadius = Math.round(6 * density);
        canvas.drawCircle(centerX, centerY, innerRadius, innerCirclePaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Get routes that serve a specific stop
     *
     * @param stopId The ID of the stop
     * @return Set of route IDs that serve this stop
     */
    private Set<String> getRoutesForStop(String stopId) {
        // This should come from your GTFS service
        Set<String> routes = gtfsService.getRoutesForStop(stopId);

        // If no routes found, log an error and return an empty set
        if (routes.isEmpty()) {
            Log.w(TAG, "No routes found for stop: " + stopId);
        } else {
            Log.d(TAG, "Routes for stop " + stopId + ": " + routes);
        }

        return routes;
    }

    /**
     * Get a custom icon for a bus stop based on routes that serve it
     */
    private BitmapDescriptor getBusStopIcon(Set<String> routes) {
        float density = getResources().getDisplayMetrics().density;
        int sizeDp = 24;
        int sizePx = Math.round(sizeDp * density);

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);

        // Border
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.round(1.5f * density));
        borderPaint.setAntiAlias(true);

        // Draw the base stop icon (white circle with black border)
        int centerX = sizePx / 2;
        int centerY = sizePx / 2;
        int radius = (sizePx / 2) - Math.round(2 * density);
        canvas.drawCircle(centerX, centerY, radius, bgPaint);

        // If there are routes, draw color indicators
        if (!routes.isEmpty()) {
            // Determine how to display the route indicators
            if (routes.size() == 1) {
                // Single route - fill most of the circle
                String routeId = routes.iterator().next();
                Paint routePaint = new Paint();
                routePaint.setColor(getRouteColor(routeId));
                routePaint.setStyle(Paint.Style.FILL);
                routePaint.setAntiAlias(true);

                // Draw a slightly smaller circle inside
                int innerRadius = Math.round(radius * 0.85f);
                canvas.drawCircle(centerX, centerY, innerRadius, routePaint);
            } else {
                // Multiple routes - divide the circle into sections
                int routeCount = routes.size();
                float sweepAngle = 360f / routeCount;

                int i = 0;
                for (String routeId : routes) {
                    Paint routePaint = new Paint();
                    routePaint.setColor(getRouteColor(routeId));
                    routePaint.setStyle(Paint.Style.FILL);
                    routePaint.setAntiAlias(true);

                    // Calculate arc angles
                    float startAngle = i * sweepAngle;

                    // Draw the arc
                    RectF rect = new RectF(centerX - radius + Math.round(3 * density),
                            centerY - radius + Math.round(3 * density),
                            centerX + radius - Math.round(3 * density),
                            centerY + radius - Math.round(3 * density));
                    canvas.drawArc(rect, startAngle, sweepAngle, true, routePaint);

                    i++;
                }

                // Draw a small white circle in the middle to make it look nicer
                Paint centerPaint = new Paint();
                centerPaint.setColor(Color.WHITE);
                centerPaint.setStyle(Paint.Style.FILL);
                centerPaint.setAntiAlias(true);
                canvas.drawCircle(centerX, centerY, radius * 0.3f, centerPaint);
            }
        }

        // Draw the border last so it's on top
        canvas.drawCircle(centerX, centerY, radius, borderPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Get color for a route
     */
    private int getRouteColor(String routeId) {
        // Get color from your cached route colors if available
        if (routeColors.containsKey(routeId)) {
            return routeColors.get(routeId);
        }

        // For real CyRide routes, generate consistent colors based on route ID
        int hash = routeId.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;

        // Ensure colors aren't too dark
        r = Math.max(r, 100);
        g = Math.max(g, 100);
        b = Math.max(b, 100);

        int color = Color.rgb(r, g, b);
        routeColors.put(routeId, color);
        return color;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop real-time updates
        if (gtfsService != null) {
            gtfsService.stopRealTimeUpdates();
        }
    }
}