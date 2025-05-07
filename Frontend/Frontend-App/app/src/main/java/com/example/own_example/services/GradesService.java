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
import com.example.own_example.models.AssignmentModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for interacting with the Grades API
 */
public class GradesService {
    private static final String TAG = "GradesService";
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080";
    private static final String GPA_PATH = "/gpa";

    private RequestQueue requestQueue;
    private Context context;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public GradesService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Interface for API response callbacks
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    /**
     * Get all assignments for a specific class
     */
    public void getClassAssignments(int classId, ApiCallback<List<AssignmentModel>> callback) {
        String url = BASE_URL + GPA_PATH + "/class/" + classId;

        Log.d(TAG, "Getting class assignments from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<AssignmentModel> assignments = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject assignmentObj = response.getJSONObject(i);
                                AssignmentModel assignment = parseAssignmentModel(assignmentObj);
                                assignments.add(assignment);
                            }

                            callback.onSuccess(assignments);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing assignments: " + e.getMessage());
                            callback.onError("Error parsing assignments data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching assignments: " + error.toString());
                        callback.onError("Error fetching assignments: " + getErrorMessage(error));
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Get all assignments for a student
     */
    public void getStudentAssignments(int studentId, ApiCallback<List<AssignmentModel>> callback) {
        String url = BASE_URL + GPA_PATH + "/student/" + studentId;

        Log.d(TAG, "Getting student assignments from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<AssignmentModel> assignments = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject assignmentObj = response.getJSONObject(i);
                                AssignmentModel assignment = parseAssignmentModel(assignmentObj);
                                assignments.add(assignment);
                            }

                            callback.onSuccess(assignments);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing assignments: " + e.getMessage());
                            callback.onError("Error parsing assignments data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching assignments: " + error.toString());
                        callback.onError("Error fetching assignments: " + getErrorMessage(error));
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Get all assignments for a student in a specific class
     */
    public void getStudentClassAssignments(int classId, int studentId, ApiCallback<List<AssignmentModel>> callback) {
        String url = BASE_URL + GPA_PATH + "/class/" + classId + "/student/" + studentId;

        Log.d(TAG, "Getting student class assignments from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<AssignmentModel> assignments = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject assignmentObj = response.getJSONObject(i);
                                AssignmentModel assignment = parseAssignmentModel(assignmentObj);
                                assignments.add(assignment);
                            }

                            callback.onSuccess(assignments);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing assignments: " + e.getMessage());
                            callback.onError("Error parsing assignments data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching assignments: " + error.toString());
                        callback.onError("Error fetching assignments: " + getErrorMessage(error));
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Get student's overall grade for a class
     */
    public void getStudentOverallGrade(int classId, int studentId, ApiCallback<Map<String, Object>> callback) {
        String url = BASE_URL + GPA_PATH + "/class/" + classId + "/student/" + studentId + "/overall";

        Log.d(TAG, "Getting student overall grade from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Map<String, Object> gradeInfo = new HashMap<>();

                            // Extract data from response
                            gradeInfo.put("studentId", response.getInt("studentId"));
                            gradeInfo.put("studentName", response.getString("studentName"));
                            gradeInfo.put("classId", response.getInt("classId"));
                            gradeInfo.put("className", response.getString("className"));

                            // Handle possible null values for grades
                            if (!response.isNull("overallGrade")) {
                                gradeInfo.put("overallGrade", response.getDouble("overallGrade"));
                            } else {
                                gradeInfo.put("overallGrade", null);
                            }

                            gradeInfo.put("totalAssignments", response.getInt("totalAssignments"));
                            gradeInfo.put("gradedAssignments", response.getInt("gradedAssignments"));

                            callback.onSuccess(gradeInfo);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing overall grade: " + e.getMessage());
                            callback.onError("Error parsing overall grade data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching overall grade: " + error.toString());
                        callback.onError("Error fetching overall grade: " + getErrorMessage(error));
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Get class average grade
     */
    public void getClassAverageGrade(int classId, ApiCallback<Map<String, Object>> callback) {
        String url = BASE_URL + GPA_PATH + "/class/" + classId + "/average";

        Log.d(TAG, "Getting class average from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Map<String, Object> averageInfo = new HashMap<>();

                            // Extract data from response
                            averageInfo.put("classId", response.getInt("classId"));
                            averageInfo.put("className", response.getString("className"));

                            // Handle possible null values for average
                            if (!response.isNull("classAverage")) {
                                averageInfo.put("classAverage", response.getDouble("classAverage"));
                            } else {
                                averageInfo.put("classAverage", null);
                            }

                            callback.onSuccess(averageInfo);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing class average: " + e.getMessage());
                            callback.onError("Error parsing class average data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching class average: " + error.toString());
                        callback.onError("Error fetching class average: " + getErrorMessage(error));
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Create a new assignment
     */
    public void createAssignment(AssignmentModel assignment, ApiCallback<AssignmentModel> callback) {
        String url = BASE_URL + GPA_PATH;

        Log.d(TAG, "Creating assignment at: " + url);

        try {
            JSONObject assignmentJson = createAssignmentJson(assignment);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    assignmentJson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                AssignmentModel createdAssignment = parseAssignmentModel(response);
                                callback.onSuccess(createdAssignment);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing created assignment: " + e.getMessage());
                                callback.onError("Error parsing created assignment data");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error creating assignment: " + error.toString());
                            callback.onError("Error creating assignment: " + getErrorMessage(error));
                        }
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating assignment JSON: " + e.getMessage());
            callback.onError("Error preparing assignment data: " + e.getMessage());
        }
    }

    /**
     * Update an assignment
     */
    public void updateAssignment(AssignmentModel assignment, ApiCallback<AssignmentModel> callback) {
        String url = BASE_URL + GPA_PATH + "/" + assignment.getId();

        Log.d(TAG, "Updating assignment at: " + url);

        try {
            JSONObject assignmentJson = createAssignmentJson(assignment);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    assignmentJson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                AssignmentModel updatedAssignment = parseAssignmentModel(response);
                                callback.onSuccess(updatedAssignment);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing updated assignment: " + e.getMessage());
                                callback.onError("Error parsing updated assignment data");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error updating assignment: " + error.toString());
                            callback.onError("Error updating assignment: " + getErrorMessage(error));
                        }
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating assignment JSON: " + e.getMessage());
            callback.onError("Error preparing assignment data: " + e.getMessage());
        }
    }

    /**
     * Grade an assignment
     */
    public void gradeAssignment(int assignmentId, double grade, int gradedById, String comments, ApiCallback<AssignmentModel> callback) {
        String url = BASE_URL + GPA_PATH + "/" + assignmentId + "/grade";

        Log.d(TAG, "Grading assignment at: " + url);

        try {
            JSONObject gradeJson = new JSONObject();
            gradeJson.put("grade", grade);
            gradeJson.put("gradedBy", gradedById);
            if (comments != null && !comments.isEmpty()) {
                gradeJson.put("comments", comments);
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    gradeJson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                AssignmentModel gradedAssignment = parseAssignmentModel(response);
                                callback.onSuccess(gradedAssignment);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing graded assignment: " + e.getMessage());
                                callback.onError("Error parsing graded assignment data");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error grading assignment: " + error.toString());
                            callback.onError("Error grading assignment: " + getErrorMessage(error));
                        }
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating grade JSON: " + e.getMessage());
            callback.onError("Error preparing grade data: " + e.getMessage());
        }
    }

    /**
     * Delete an assignment
     */
    public void deleteAssignment(int assignmentId, ApiCallback<Boolean> callback) {
        String url = BASE_URL + GPA_PATH + "/" + assignmentId;

        Log.d(TAG, "Deleting assignment at: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            callback.onSuccess(message.equals("success"));
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing delete response: " + e.getMessage());
                            callback.onError("Error processing delete response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error deleting assignment: " + error.toString());
                        callback.onError("Error deleting assignment: " + getErrorMessage(error));
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Create JSON object from AssignmentModel for API requests
     */
    private JSONObject createAssignmentJson(AssignmentModel assignment) throws JSONException {
        JSONObject json = new JSONObject();

        // Include ID if it's an update
        if (assignment.getId() > 0) {
            json.put("id", assignment.getId());
        }

        // Add student info
        JSONObject student = new JSONObject();
        student.put("id", assignment.getStudentId());
        json.put("student", student);

        // Add class info
        JSONObject studentClass = new JSONObject();
        studentClass.put("id", assignment.getClassId());
        json.put("studentClass", studentClass);

        // Add assignment details
        json.put("assignmentName", assignment.getAssignmentName());
        if (assignment.getAssignmentDescription() != null) {
            json.put("assignmentDescription", assignment.getAssignmentDescription());
        }
        json.put("weightPercentage", assignment.getWeightPercentage());

        // Add grade if available
        if (assignment.getGrade() != null) {
            json.put("grade", assignment.getGrade());

            // Add graded by if available
            if (assignment.getGradedById() > 0) {
                JSONObject gradedBy = new JSONObject();
                gradedBy.put("id", assignment.getGradedById());
                json.put("gradedBy", gradedBy);
            }

            // Add comments if available
            if (assignment.getComments() != null && !assignment.getComments().isEmpty()) {
                json.put("comments", assignment.getComments());
            }
        }

        return json;
    }

    /**
     * Parse a JSON object into an AssignmentModel
     */
    private AssignmentModel parseAssignmentModel(JSONObject json) throws JSONException {
        AssignmentModel assignment = new AssignmentModel();

        assignment.setId(json.getInt("id"));

        // Parse class data
        if (json.has("studentClass") && !json.isNull("studentClass")) {
            JSONObject classObj = json.getJSONObject("studentClass");
            assignment.setClassId(classObj.getInt("id"));
            assignment.setClassName(classObj.getString("className"));
        }

        // Parse student data
        if (json.has("student") && !json.isNull("student")) {
            JSONObject studentObj = json.getJSONObject("student");
            assignment.setStudentId(studentObj.getInt("id"));
            assignment.setStudentName(studentObj.getString("name"));
        }

        // Parse assignment details
        assignment.setAssignmentName(json.getString("assignmentName"));
        if (json.has("assignmentDescription") && !json.isNull("assignmentDescription")) {
            assignment.setAssignmentDescription(json.getString("assignmentDescription"));
        }

        // Parse grade data if available
        if (json.has("grade") && !json.isNull("grade")) {
            assignment.setGrade(json.getDouble("grade"));
        }

        if (json.has("weightPercentage") && !json.isNull("weightPercentage")) {
            assignment.setWeightPercentage(json.getDouble("weightPercentage"));
        }

        // Parse dates
        if (json.has("submissionDate") && !json.isNull("submissionDate")) {
            try {
                String dateStr = json.getString("submissionDate");
                assignment.setSubmissionDate(LocalDateTime.parse(dateStr, dateTimeFormatter));
            } catch (DateTimeParseException e) {
                Log.e(TAG, "Error parsing submission date: " + e.getMessage());
            }
        }

        if (json.has("gradedDate") && !json.isNull("gradedDate")) {
            try {
                String dateStr = json.getString("gradedDate");
                assignment.setGradedDate(LocalDateTime.parse(dateStr, dateTimeFormatter));
            } catch (DateTimeParseException e) {
                Log.e(TAG, "Error parsing graded date: " + e.getMessage());
            }
        }

        // Parse graded by data if available
        if (json.has("gradedBy") && !json.isNull("gradedBy")) {
            JSONObject gradedByObj = json.getJSONObject("gradedBy");
            assignment.setGradedById(gradedByObj.getInt("id"));
            assignment.setGradedByName(gradedByObj.getString("name"));
        }

        // Parse comments if available
        if (json.has("comments") && !json.isNull("comments")) {
            assignment.setComments(json.getString("comments"));
        }

        return assignment;
    }

    /**
     * Get a more useful error message from Volley error
     */
    private String getErrorMessage(VolleyError error) {
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            String data = new String(error.networkResponse.data);
            return "Status code: " + statusCode + " - " + data;
        } else if (error.getMessage() != null) {
            return error.getMessage();
        } else {
            return "Network error";
        }
    }
}