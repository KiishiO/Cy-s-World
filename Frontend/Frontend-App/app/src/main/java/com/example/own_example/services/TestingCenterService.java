package com.example.own_example.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.own_example.models.ExamInfo;
import com.example.own_example.models.TestingCenter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TestingCenterService {
    private static final String TAG = "TestingCenterService";
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/testingcenter";
    private RequestQueue requestQueue;
    private Gson gson;

    public TestingCenterService(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        gson = new Gson();
    }

    // Callback interfaces for async operations
    public interface TestingCentersCallback {
        void onSuccess(List<TestingCenter> testingCenters);
        void onError(String error);
    }

    public interface TestingCenterCallback {
        void onSuccess(TestingCenter testingCenter);
        void onError(String error);
    }

    public interface ExamsCallback {
        void onSuccess(List<ExamInfo> exams);
        void onError(String error);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }

    // Get all testing centers
    public void getAllTestingCenters(final TestingCentersCallback callback) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Type listType = new TypeToken<List<TestingCenter>>(){}.getType();
                            List<TestingCenter> testingCenters = gson.fromJson(response.toString(), listType);
                            callback.onSuccess(testingCenters);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing testing centers: " + e.getMessage());
                            callback.onError("Error parsing data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching testing centers: " + error.getMessage());
                        callback.onError("Error connecting to server");

                        // Provide mock data as fallback
                        provideMockData(callback);
                    }
                }
        );

        requestQueue.add(request);
    }

    // Get testing center by ID
    public void getTestingCenterById(int id, final TestingCenterCallback callback) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                BASE_URL + "/" + id,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            TestingCenter testingCenter = gson.fromJson(response.toString(), TestingCenter.class);
                            callback.onSuccess(testingCenter);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing testing center: " + e.getMessage());
                            callback.onError("Error parsing data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching testing center: " + error.getMessage());
                        callback.onError("Error connecting to server");
                    }
                }
        );

        requestQueue.add(request);
    }

    // Create a new testing center
    public void createTestingCenter(TestingCenter testingCenter, final TestingCenterCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject(gson.toJson(testingCenter));

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    BASE_URL + "/new",
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                TestingCenter createdCenter = gson.fromJson(response.toString(), TestingCenter.class);
                                callback.onSuccess(createdCenter);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing created testing center: " + e.getMessage());
                                callback.onError("Error parsing response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error creating testing center: " + error.getMessage());
                            callback.onError("Error creating testing center");
                        }
                    }
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error preparing create request: " + e.getMessage());
            callback.onError("Error preparing request");
        }
    }

    // Update a testing center
    public void updateTestingCenter(int id, TestingCenter testingCenter, final TestingCenterCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject(gson.toJson(testingCenter));

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    BASE_URL + "/" + id,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                TestingCenter updatedCenter = gson.fromJson(response.toString(), TestingCenter.class);
                                callback.onSuccess(updatedCenter);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing updated testing center: " + e.getMessage());
                                callback.onError("Error parsing response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error updating testing center: " + error.getMessage());
                            callback.onError("Error updating testing center");
                        }
                    }
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error preparing update request: " + e.getMessage());
            callback.onError("Error preparing request");
        }
    }

    // Delete a testing center
    public void deleteTestingCenter(int id, final OperationCallback callback) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                BASE_URL + "/" + id,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error deleting testing center: " + error.getMessage());
                        callback.onError("Error deleting testing center");
                    }
                }
        );

        requestQueue.add(request);
    }

    // Get exams for a testing center
    public void getExamsForTestingCenter(int centerId, final ExamsCallback callback) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL + "/" + centerId + "/exams",
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Type listType = new TypeToken<List<ExamInfo>>(){}.getType();
                            List<ExamInfo> exams = gson.fromJson(response.toString(), listType);
                            callback.onSuccess(exams);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing exams: " + e.getMessage());
                            callback.onError("Error parsing data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching exams: " + error.getMessage());
                        callback.onError("Error connecting to server");
                    }
                }
        );

        requestQueue.add(request);
    }

    // Add an exam to a testing center
    public void addExamToTestingCenter(int centerId, ExamInfo examInfo, final OperationCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject(gson.toJson(examInfo));

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    BASE_URL + "/" + centerId + "/exams",
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            callback.onSuccess();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error adding exam: " + error.getMessage());
                            callback.onError("Error adding exam");
                        }
                    }
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error preparing add exam request: " + e.getMessage());
            callback.onError("Error preparing request");
        }
    }

    // Provide mock data as fallback when server is unavailable
    private void provideMockData(TestingCentersCallback callback) {
        List<TestingCenter> mockCenters = new ArrayList<>();

        TestingCenter center1 = new TestingCenter();
        center1.setId(1);
        center1.setCenterName("Pearson VUE Testing Center");
        center1.setLocation("Parks Library, Room 204");
        center1.setCenterDescription("Official Pearson VUE testing center for certification exams.");

        TestingCenter center2 = new TestingCenter();
        center2.setId(2);
        center2.setCenterName("Carver Hall Testing Room");
        center2.setLocation("Carver Hall, Room 305");
        center2.setCenterDescription("Specialized testing facility for engineering exams.");

        TestingCenter center3 = new TestingCenter();
        center3.setId(3);
        center3.setCenterName("Student Services Testing Center");
        center3.setLocation("Student Services Building, Lower Level");
        center3.setCenterDescription("General testing facility for makeup exams and special accommodations.");

        mockCenters.add(center1);
        mockCenters.add(center2);
        mockCenters.add(center3);

        callback.onSuccess(mockCenters);
    }
}