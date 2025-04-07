package com.example.own_example.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.own_example.models.Bus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusService {

    private static final String TAG = "BusService";
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/busOpt"; // Updated to match your server

    private Context context;
    private RequestQueue requestQueue;

    public BusService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Interface for bus data callbacks
     */
    public interface BusCallback {
        void onSuccess(List<Bus> buses);
        void onError(String error);
    }

    /**
     * Interface for simple operation callbacks
     */
    public interface OperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Get all buses from the server
     */
    public void getAllBuses(BusCallback callback) {
        String url = BASE_URL + "/all";
        Log.d(TAG, "Making request to: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Log.d(TAG, "Received response: " + response.toString());
                            List<Bus> busList = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject busJson = response.getJSONObject(i);
                                Bus bus = new Bus();

                                bus.setBusName(busJson.getString("busName"));
                                bus.setBusNum(busJson.getInt("busNum"));

                                if (!busJson.isNull("stopLocation")) {
                                    bus.setStopLocation(busJson.getString("stopLocation"));
                                } else {
                                    bus.setStopLocation("");
                                }

                                if (!busJson.isNull("busRating")) {
                                    String rating = busJson.getString("busRating");
                                    if (!rating.isEmpty()) {
                                        bus.setBusRating(rating.charAt(0));
                                    }
                                }

                                if (!busJson.isNull("lastReportTime")) {
                                    bus.setLastReportTime(busJson.getString("lastReportTime"));
                                }

                                busList.add(bus);
                            }

                            callback.onSuccess(busList);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing data from server");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = "Unknown error";
                        if (error.networkResponse != null) {
                            errorMsg = "Status Code: " + error.networkResponse.statusCode;
                        } else if (error.getMessage() != null) {
                            errorMsg = error.getMessage();
                        }
                        Log.e(TAG, "Error fetching buses: " + errorMsg);
                        Log.e(TAG, "Error details: " + error.toString());
                        callback.onError("Error connecting to server: " + errorMsg);
                    }
                }
        );

        // Add retry policy
        request.setShouldRetryServerErrors(true);

        requestQueue.add(request);
    }

    /**
     * Get a bus by its number
     */
    public void getBusByNumber(int busNum, BusCallback callback) {
        String url = BASE_URL + "/" + busNum;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Bus bus = new Bus();
                            bus.setBusName(response.getString("busName"));
                            bus.setBusNum(response.getInt("busNum"));

                            if (!response.isNull("stopLocation")) {
                                bus.setStopLocation(response.getString("stopLocation"));
                            } else {
                                bus.setStopLocation("");
                            }

                            if (!response.isNull("busRating")) {
                                String rating = response.getString("busRating");
                                if (!rating.isEmpty()) {
                                    bus.setBusRating(rating.charAt(0));
                                }
                            }

                            if (!response.isNull("lastReportTime")) {
                                bus.setLastReportTime(response.getString("lastReportTime"));
                            }

                            List<Bus> busList = new ArrayList<>();
                            busList.add(bus);
                            callback.onSuccess(busList);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing data from server");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = "Unknown error";
                        if (error.networkResponse != null) {
                            errorMsg = "Status Code: " + error.networkResponse.statusCode;
                        } else if (error.getMessage() != null) {
                            errorMsg = error.getMessage();
                        }
                        Log.e(TAG, "Error fetching bus: " + errorMsg);
                        callback.onError("Error connecting to server: " + errorMsg);
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Update a bus stop location
     */
    public void updateBusLocation(String busName, String newLocation, OperationCallback callback) {
        String url = BASE_URL + "/" + busName + "/updateStop";

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("stopLocation", newLocation);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                callback.onSuccess("Stop location updated successfully");
                            } catch (Exception e) {
                                Log.e(TAG, "Response parsing error: " + e.getMessage());
                                callback.onError("Error processing response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String errorMsg = "Unknown error";
                            if (error.networkResponse != null) {
                                errorMsg = "Status Code: " + error.networkResponse.statusCode;
                            } else if (error.getMessage() != null) {
                                errorMsg = error.getMessage();
                            }
                            Log.e(TAG, "Error updating bus location: " + errorMsg);
                            callback.onError("Error updating bus location: " + errorMsg);
                        }
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("Error creating request");
        }
    }

    /**
     * Update a bus rating
     */
    public void updateBusRating(String busName, char rating, OperationCallback callback) {
        String url = BASE_URL + "/" + busName + "/rating";

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("busRating", String.valueOf(rating));

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                callback.onSuccess("Bus rating updated successfully");
                            } catch (Exception e) {
                                Log.e(TAG, "Response parsing error: " + e.getMessage());
                                callback.onError("Error processing response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String errorMsg = "Unknown error";
                            if (error.networkResponse != null) {
                                errorMsg = "Status Code: " + error.networkResponse.statusCode;
                            } else if (error.getMessage() != null) {
                                errorMsg = error.getMessage();
                            }
                            Log.e(TAG, "Error updating bus rating: " + errorMsg);
                            callback.onError("Error updating bus rating: " + errorMsg);
                        }
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("Error creating request");
        }
    }

    /**
     * Add a new bus
     */
    public void addBus(Bus bus, OperationCallback callback) {
        String url = BASE_URL + "/add";

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("busName", bus.getBusName());
            requestBody.put("busNum", bus.getBusNum());
            requestBody.put("stopLocation", bus.getStopLocation());
            requestBody.put("busRating", String.valueOf(bus.getBusRating()));

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            callback.onSuccess("Bus added successfully");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String errorMsg = "Unknown error";
                            if (error.networkResponse != null) {
                                errorMsg = "Status Code: " + error.networkResponse.statusCode;
                            } else if (error.getMessage() != null) {
                                errorMsg = error.getMessage();
                            }
                            Log.e(TAG, "Error adding bus: " + errorMsg);
                            callback.onError("Error adding bus: " + errorMsg);
                        }
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("Error creating request");
        }
    }

    /**
     * Delete a bus
     */
    public void deleteBus(String busName, OperationCallback callback) {
        String url = BASE_URL + "/" + busName;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess("Bus deleted successfully");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = "Unknown error";
                        if (error.networkResponse != null) {
                            errorMsg = "Status Code: " + error.networkResponse.statusCode;
                        } else if (error.getMessage() != null) {
                            errorMsg = error.getMessage();
                        }
                        Log.e(TAG, "Error deleting bus: " + errorMsg);
                        callback.onError("Error deleting bus: " + errorMsg);
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Provide mock data for testing when server is unavailable
     */
    public void getMockBuses(BusCallback callback) {
        List<Bus> mockBuses = new ArrayList<>();

        // Create some mock bus data
        Bus bus1 = new Bus(1, "Red Route", "Student Union", 'A');
        Bus bus2 = new Bus(2, "Blue Route", "Memorial Union", 'B');
        Bus bus3 = new Bus(3, "Green Route", "Parks Library", 'A');
        Bus bus4 = new Bus(4, "Gold Route", "College of Engineering", 'C');

        mockBuses.add(bus1);
        mockBuses.add(bus2);
        mockBuses.add(bus3);
        mockBuses.add(bus4);

        // Return mock data
        callback.onSuccess(mockBuses);
    }
}