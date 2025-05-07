package com.example.own_example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BusMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "BusMapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final LatLng AMES_CENTER = new LatLng(42.026, -93.648); // Ames, Iowa center coordinates
    private static final float DEFAULT_ZOOM = 15f;
    private static final int BUS_ANIMATION_DURATION = 3000; // Animation duration in milliseconds

    private GoogleMap googleMap;
    private GTFSService gtfsService;
    private ChipGroup routeChipGroup;
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
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private FusedLocationProviderClient fusedLocationClient;
    private boolean showStops = false;
    private BitmapDescriptor regularStopIcon;
    private BitmapDescriptor majorStopIcon;

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
        routeChipGroup = findViewById(R.id.route_chip_group);
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

        // Start real-time updates
        startRealTimeBusUpdates();
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
        if (stopId == null || arrivalInfoCard == null) {
            return;
        }

        // Get arrivals for this stop
        List<Map<String, Object>> arrivals = gtfsService.getUpcomingArrivals(stopId);

        if (arrivals.isEmpty()) {
            Toast.makeText(this, "No upcoming arrivals for this stop", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Create and set an adapter for the RecyclerView to display arrivals
        // For now, just show the card
        arrivalInfoCard.setVisibility(View.VISIBLE);
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
        // Draw all route lines first
        if (googleMap != null) {
            drawAllRoutes();
        }

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
                    }
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    hideLoadingIndicator();
                    Toast.makeText(BusMapActivity.this, error, Toast.LENGTH_SHORT).show();

                    // If error occurs, try to load mock data instead
                    gtfsService.getMockGTFSBuses(new GTFSService.GTFSBusCallback() {
                        @Override
                        public void onSuccess(List<GTFSBus> buses) {
                            if (buses.isEmpty()) {
                                showEmptyState();
                            } else {
                                hideEmptyState();
                                updateBusMarkersWithAnimation(buses);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(BusMapActivity.this,
                                    "Failed to load bus data: " + error, Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }
                    });
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
        // First, update route filters if needed
        updateRouteFilters(buses);

        // Create a set of current bus IDs
        Set<String> currentBusIds = new HashSet<>();

        for (GTFSBus bus : buses) {
            // Skip if not in service
            if (!bus.isInService()) {
                continue;
            }

            // Skip if route is filtered out
            if (!filteredRouteIds.isEmpty() && !filteredRouteIds.contains(bus.getRouteId())) {
                continue;
            }

            String busId = bus.getVehicleId() != null ? bus.getVehicleId() : bus.getTripId();
            // If still null, generate a unique ID
            if (busId == null) {
                busId = "bus-" + bus.getBusNum() + "-" + bus.getRouteId();
            }

            currentBusIds.add(busId);
            LatLng newPosition = new LatLng(bus.getLatitude(), bus.getLongitude());

            if (busMarkers.containsKey(busId)) {
                // Update existing marker
                Marker marker = busMarkers.get(busId);

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

                // Format the snippet with current and next stop information
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
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(newPosition)
                        .title("Bus " + bus.getBusNum() + ": " + bus.getBusName())
                        .snippet("Current Stop: " + (bus.getStopLocation() != null ? bus.getStopLocation() : "Unknown"))
                        .icon(getBusIcon(bus.getRouteId()))
                        .rotation(bus.getBearing())
                        .anchor(0.5f, 0.5f)
                        .flat(true)); // Make marker flat to better represent vehicle direction

                if (marker != null) {
                    busMarkers.put(busId, marker);
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

        // Update empty state visibility
        if (emptyStateText != null) {
            emptyStateText.setVisibility(currentBusIds.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Animate marker movement from previous to new position
     */
    private void animateMarker(final Marker marker, final LatLng start, final LatLng end, final float finalBearing) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long startTime = SystemClock.uptimeMillis();
        final LinearInterpolator interpolator = new LinearInterpolator();
        final float startBearing = marker.getRotation();

        // Adjust bearing calculation
        float bearingDiff = finalBearing - startBearing;
        if (bearingDiff > 180) {
            bearingDiff -= 360;
        } else if (bearingDiff < -180) {
            bearingDiff += 360;
        }

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
     * Update route filters in the chip group
     */
    private void updateRouteFilters(List<GTFSBus> buses) {
        // Only update if we don't have any chips yet and the chip group exists
        if (routeChipGroup == null || routeChipGroup.getChildCount() > 0) {
            return;
        }

        // Collect unique routes
        Set<String> routeIds = new HashSet<>();
        Map<String, String> routeNames = new HashMap<>();

        for (GTFSBus bus : buses) {
            if (bus.getRouteId() != null && bus.getBusName() != null) {
                routeIds.add(bus.getRouteId());
                routeNames.put(bus.getRouteId(), bus.getBusName());
            }
        }

        // Add chips for each route
        for (String routeId : routeIds) {
            String routeName = routeNames.getOrDefault(routeId, "Route " + routeId);

            Chip chip = new Chip(this);
            chip.setText(routeName);
            chip.setCheckable(true);
            chip.setChecked(true); // All routes visible by default

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filteredRouteIds.add(routeId);
                } else {
                    filteredRouteIds.remove(routeId);
                }

                // Update markers based on new filters
                for (Map.Entry<String, Marker> entry : busMarkers.entrySet()) {
                    Marker marker = entry.getValue();
                    String busTitle = marker.getTitle();

                    boolean shouldBeVisible = filteredRouteIds.isEmpty() ||
                            busTitle != null && busTitle.contains(routeName);

                    marker.setVisible(shouldBeVisible);
                }
            });

            routeChipGroup.addView(chip);
            filteredRouteIds.add(routeId); // Add to filtered set initially
        }
    }

    /**
     * Draw route lines on the map based on shapes data from GTFS
     */
    private void drawRouteLine(String routeId, String routeColor) {
        // Get shape points for this route from your GTFSService
        List<LatLng> shapePoints = gtfsService.getRouteShape(routeId);

        if (shapePoints != null && !shapePoints.isEmpty()) {
            // Convert color string to actual color
            int color = Color.parseColor(routeColor);

            // Create polyline options
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(shapePoints)
                    .width(8)  // Line width in pixels
                    .color(color)
                    .geodesic(true)
                    .zIndex(1);  // Draw above other map elements

            // Add the polyline to the map
            googleMap.addPolyline(polylineOptions);
        }
    }

    /**
     * Draw all routes on the map
     */
    private void drawAllRoutes() {
        // Get route data from GTFSService
        Map<String, Map<String, String>> routes = gtfsService.getRouteData();

        // Draw each route
        for (Map.Entry<String, Map<String, String>> entry : routes.entrySet()) {
            String routeId = entry.getKey();
            Map<String, String> routeInfo = entry.getValue();

            // Get route color or use default
            String routeColor = "#FF0000"; // Default red
            if (routeInfo.containsKey("route_color")) {
                routeColor = "#" + routeInfo.get("route_color");
            }

            // Draw this route
            drawRouteLine(routeId, routeColor);
        }
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

                // Determine if it's a major stop based on name or other criteria
                boolean isMajorStop = isMajorStop(stopName, stopInfo);

                // Create marker
                MarkerOptions options = new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .title(stopName)
                        .icon(isMajorStop ? majorStopIcon : regularStopIcon)
                        .anchor(0.5f, 0.5f)
                        .zIndex(0) // Below bus markers
                        .visible(true);

                // Add to map
                Marker marker = googleMap.addMarker(options);
                if (marker != null) {
                    stopMarkers.put(stopId, marker);
                }
            }
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
        int iconResId = R.drawable.ic_bus; // Default bus icon

        // Use different colors for different routes
        if (routeId != null) {
            switch (routeId) {
                case "1":
                    iconResId = R.drawable.ic_bus_red;
                    break;
                case "2":
                    iconResId = R.drawable.ic_bus_blue;
                    break;
                case "3":
                    iconResId = R.drawable.ic_bus_green;
                    break;
                default:
                    iconResId = R.drawable.ic_bus;
                    break;
            }
        }

        return getBitmapFromVector(iconResId);
    }

    /**
     * Convert a vector drawable to a bitmap descriptor for map markers
     */
    private BitmapDescriptor getBitmapFromVector(@DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableResourceId);

        if (vectorDrawable == null) {
            Log.e(TAG, "Resource not found: " + vectorDrawableResourceId);
            return BitmapDescriptorFactory.defaultMarker();
        }

        int width = vectorDrawable.getIntrinsicWidth();
        int height = vectorDrawable.getIntrinsicHeight();

        // Use default marker size if intrinsic dimensions are not available
        if (width <= 0) width = 48;
        if (height <= 0) height = 48;

        vectorDrawable.setBounds(0, 0, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
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