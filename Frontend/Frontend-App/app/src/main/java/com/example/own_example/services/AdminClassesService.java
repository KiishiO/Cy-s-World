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
import com.example.own_example.services.AuthService;
import com.example.own_example.UserRoles;
import com.example.own_example.models.ClassModel;
import com.example.own_example.models.Student;
import com.example.own_example.models.Teacher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for admin class management operations
 */
public class AdminClassesService {
    private static final String TAG = "AdminClassesService";
    // Base URL for the server
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080";

    // Path constants based on the backend code
    private static final String CLASSES_PATH = "/classes";
    private static final String PERSONS_PATH = "/persons";

    private RequestQueue requestQueue;
    private Context context;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AdminClassesService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);

        // Log the role of the current user for debugging
        UserRoles role = AuthService.getUserRole(context);
        Log.d(TAG, "Initialized AdminClassesService with user role: " + role);
    }

    /**
     * Interface for API callback with list data
     */
    public interface ListCallback<T> {
        void onSuccess(List<T> result);
        void onError(String errorMessage);
    }

    /**
     * Interface for API callback with single item data
     */
    public interface ItemCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    /**
     * Interface for API callback with simple status
     */
    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    /**
     * Get all classes for admin view
     */
    public void getAllClasses(ListCallback<ClassModel> callback) {
        // Use the correct path based on backend controller mapping
        String url = AuthService.getAdminAuthUrl(BASE_URL + CLASSES_PATH);

        Log.d(TAG, "Fetching all classes from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "Received classes response: " + response.toString());
                        try {
                            List<ClassModel> classModels = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject classObj = response.getJSONObject(i);
                                ClassModel classModel = parseClassModel(classObj);
                                classModels.add(classModel);
                            }

                            callback.onSuccess(classModels);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing classes: " + e.getMessage());
                            callback.onError("Error parsing classes data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching classes: " + error.toString());
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String data = new String(error.networkResponse.data);
                            Log.e(TAG, "Network error code: " + statusCode);
                            Log.e(TAG, "Error data: " + data);
                        }
                        callback.onError("Error fetching classes: " + getErrorMessage(error));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add Admin-Id header if needed
                headers.put("Admin-Id", String.valueOf(AuthService.getUserId(context)));
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Get all teachers for class assignment
     */
    public void getAllTeachers(ListCallback<Teacher> callback) {
        // Updated URL structure based on PersonController
        String url = AuthService.getAdminAuthUrl(BASE_URL + PERSONS_PATH + "/by-role/TEACHER");

        Log.d(TAG, "Fetching all teachers from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Teacher> teachers = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject teacherObj = response.getJSONObject(i);

                                int id = teacherObj.getInt("id");
                                String name = teacherObj.getString("name");
                                String email = teacherObj.optString("phoneNumber", ""); // Using phoneNumber as email
                                String department = ""; // Department not available in Person model

                                Teacher teacher = new Teacher(id, name, email, department);
                                teachers.add(teacher);
                            }

                            callback.onSuccess(teachers);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing teachers: " + e.getMessage());
                            callback.onError("Error parsing teachers data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching teachers: " + error.toString());
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String data = new String(error.networkResponse.data);
                            Log.e(TAG, "Network error code: " + statusCode);
                            Log.e(TAG, "Error data: " + data);
                        }
                        callback.onError("Error fetching teachers: " + getErrorMessage(error));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add Admin-Id header as required by PersonController
                headers.put("Admin-Id", String.valueOf(AuthService.getUserId(context)));
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Get all students for class assignment
     */
    public void getAllStudents(ListCallback<Student> callback) {
        // Updated URL structure based on PersonController
        String url = AuthService.getAdminAuthUrl(BASE_URL + PERSONS_PATH + "/by-role/STUDENT");

        Log.d(TAG, "Fetching all students from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Student> students = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject studentObj = response.getJSONObject(i);

                                int id = studentObj.getInt("id");
                                String name = studentObj.getString("name");
                                String email = studentObj.optString("phoneNumber", ""); // Using phoneNumber as email

                                Student student = new Student(id, name, email);
                                students.add(student);
                            }

                            callback.onSuccess(students);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing students: " + e.getMessage());
                            callback.onError("Error parsing students data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching students: " + error.toString());
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String data = new String(error.networkResponse.data);
                            Log.e(TAG, "Network error code: " + statusCode);
                            Log.e(TAG, "Error data: " + data);
                        }
                        callback.onError("Error fetching students: " + getErrorMessage(error));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add Admin-Id header as required by PersonController
                headers.put("Admin-Id", String.valueOf(AuthService.getUserId(context)));
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Search for a student by Net-ID or name
     */
    public void searchStudent(String query, ItemCallback<Student> callback) {
        // Based on the backend, there might not be a direct search endpoint
        // We'll fetch all students and filter client-side as a workaround
        getAllStudents(new ListCallback<Student>() {
            @Override
            public void onSuccess(List<Student> result) {
                for (Student student : result) {
                    if (student.getName().toLowerCase().contains(query.toLowerCase()) ||
                            student.getEmail().toLowerCase().contains(query.toLowerCase())) {
                        callback.onSuccess(student);
                        return;
                    }
                }
                callback.onError("No student found matching: " + query);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError("Error searching for student: " + errorMessage);
            }
        });
    }

    /**
     * Create a new class
     */
    public void createClass(ClassModel classModel, ActionCallback callback) {
        String url = AuthService.getAdminAuthUrl(BASE_URL + CLASSES_PATH);

        Log.d(TAG, "Creating class at URL: " + url);

        try {
            JSONObject classJson = createClassJson(classModel);
            Log.d(TAG, "Class JSON: " + classJson.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    classJson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "Create class response: " + response.toString());
                                if (response.has("id")) {
                                    callback.onSuccess("Class created successfully");
                                } else if (response.has("message") && response.getString("message").equals("success")) {
                                    callback.onSuccess("Class created successfully");
                                } else {
                                    callback.onError("Class creation failed");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing class creation response: " + e.getMessage());
                                callback.onError("Error processing class creation");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error creating class: " + error.toString());
                            if (error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;
                                String data = new String(error.networkResponse.data);
                                Log.e(TAG, "Network error code: " + statusCode);
                                Log.e(TAG, "Error data: " + data);
                            }
                            callback.onError("Error creating class: " + getErrorMessage(error));
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    // Add Admin-Id header if needed
                    headers.put("Admin-Id", String.valueOf(AuthService.getUserId(context)));
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating class JSON: " + e.getMessage());
            callback.onError("Error preparing class data: " + e.getMessage());
        }
    }

    /**
     * Update an existing class
     */
    public void updateClass(ClassModel classModel, ActionCallback callback) {
        String url = AuthService.getAdminAuthUrl(BASE_URL + CLASSES_PATH + "/" + classModel.getId());

        Log.d(TAG, "Updating class at URL: " + url);

        try {
            JSONObject classJson = createClassJson(classModel);
            Log.d(TAG, "Class JSON: " + classJson.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    classJson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "Update class response: " + response.toString());
                                if (response.has("id")) {
                                    callback.onSuccess("Class updated successfully");
                                } else if (response.has("message") && response.getString("message").equals("success")) {
                                    callback.onSuccess("Class updated successfully");
                                } else {
                                    callback.onError("Class update failed");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing class update response: " + e.getMessage());
                                callback.onError("Error processing class update");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error updating class: " + error.toString());
                            if (error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;
                                String data = new String(error.networkResponse.data);
                                Log.e(TAG, "Network error code: " + statusCode);
                                Log.e(TAG, "Error data: " + data);
                            }
                            callback.onError("Error updating class: " + getErrorMessage(error));
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    // Add Admin-Id header if needed
                    headers.put("Admin-Id", String.valueOf(AuthService.getUserId(context)));
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating class JSON: " + e.getMessage());
            callback.onError("Error preparing class data: " + e.getMessage());
        }
    }

    /**
     * Delete a class
     */
    public void deleteClass(int classId, ActionCallback callback) {
        String url = AuthService.getAdminAuthUrl(BASE_URL + CLASSES_PATH + "/" + classId);

        Log.d(TAG, "Deleting class at URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Delete class response: " + response.toString());
                            if (response.has("message") && response.getString("message").equals("success")) {
                                callback.onSuccess("Class deleted successfully");
                            } else {
                                callback.onError("Failed to delete class");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing delete response: " + e.getMessage());
                            callback.onError("Error processing delete response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error deleting class: " + error.toString());
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String data = new String(error.networkResponse.data);
                            Log.e(TAG, "Network error code: " + statusCode);
                            Log.e(TAG, "Error data: " + data);
                        }
                        callback.onError("Error deleting class: " + getErrorMessage(error));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add Admin-Id header if needed
                headers.put("Admin-Id", String.valueOf(AuthService.getUserId(context)));
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Add a student to a class
     */
    public void addStudentToClass(int classId, int studentId, ActionCallback callback) {
        // Based on backend code, the URL would be something like:
        String url = AuthService.getAdminAuthUrl(BASE_URL + CLASSES_PATH + "/" + classId + "/student/" + studentId);

        Log.d(TAG, "Adding student to class at URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Add student response: " + response.toString());
                            if (response.has("message") && response.getString("message").equals("success")) {
                                callback.onSuccess("Student added to class successfully");
                            } else {
                                callback.onError("Failed to add student to class");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing add student response: " + e.getMessage());
                            callback.onError("Error processing add student response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error adding student: " + error.toString());
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String data = new String(error.networkResponse.data);
                            Log.e(TAG, "Network error code: " + statusCode);
                            Log.e(TAG, "Error data: " + data);
                        }
                        callback.onError("Error adding student: " + getErrorMessage(error));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add Admin-Id header if needed
                headers.put("Admin-Id", String.valueOf(AuthService.getUserId(context)));
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Remove a student from a class
     */
    public void removeStudentFromClass(int classId, int studentId, ActionCallback callback) {
        // Based on backend code, the URL would be something like:
        String url = AuthService.getAdminAuthUrl(BASE_URL + CLASSES_PATH + "/" + classId + "/student/" + studentId);

        Log.d(TAG, "Removing student from class at URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Remove student response: " + response.toString());
                            if (response.has("message") && response.getString("message").equals("success")) {
                                callback.onSuccess("Student removed from class successfully");
                            } else {
                                callback.onError("Failed to remove student from class");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing remove student response: " + e.getMessage());
                            callback.onError("Error processing remove student response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error removing student: " + error.toString());
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String data = new String(error.networkResponse.data);
                            Log.e(TAG, "Network error code: " + statusCode);
                            Log.e(TAG, "Error data: " + data);
                        }
                        callback.onError("Error removing student: " + getErrorMessage(error));
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                // Add Admin-Id header if needed
                headers.put("Admin-Id", String.valueOf(AuthService.getUserId(context)));
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Create JSON object from ClassModel for API requests
     */
    private JSONObject createClassJson(ClassModel classModel) throws JSONException {
        JSONObject jsonClass = new JSONObject();

        // Add basic class info
        jsonClass.put("className", classModel.getClassName());
        jsonClass.put("location", classModel.getLocation());

        // Add teacher info
        if (classModel.getTeacherId() > 0) {
            JSONObject teacher = new JSONObject();
            teacher.put("id", classModel.getTeacherId());
            jsonClass.put("teacher", teacher);
        }

        // Add schedules
        if (classModel.getSchedules() != null && !classModel.getSchedules().isEmpty()) {
            JSONArray schedulesArray = new JSONArray();
            for (ClassModel.ScheduleItem schedule : classModel.getSchedules()) {
                JSONObject scheduleObj = new JSONObject();
                scheduleObj.put("dayOfWeek", schedule.getDayOfWeek().name());
                scheduleObj.put("startTime", schedule.getStartTime().format(timeFormatter));
                scheduleObj.put("endTime", schedule.getEndTime().format(timeFormatter));
                schedulesArray.put(scheduleObj);
            }
            jsonClass.put("schedules", schedulesArray);
        }

        // If updating existing class, add ID
        if (classModel.getId() > 0) {
            jsonClass.put("id", classModel.getId());
        }

        return jsonClass;
    }

    /**
     * Parse a JSON object into a ClassModel
     */
    private ClassModel parseClassModel(JSONObject classObj) throws JSONException {
        int id = classObj.getInt("id");
        String className = classObj.getString("className");
        String location = classObj.optString("location", "");

        ClassModel classModel = new ClassModel(id, className, 0, "", location);

        // Parse teacher data if available
        if (classObj.has("teacher") && !classObj.isNull("teacher")) {
            JSONObject teacherObj = classObj.getJSONObject("teacher");
            int teacherId = teacherObj.getInt("id");
            String teacherName = teacherObj.getString("name");
            classModel.setTeacherId(teacherId);
            classModel.setTeacherName(teacherName);
        }

        // Parse schedules if available
        if (classObj.has("schedules") && !classObj.isNull("schedules")) {
            JSONArray schedulesArray = classObj.getJSONArray("schedules");

            for (int i = 0; i < schedulesArray.length(); i++) {
                JSONObject scheduleObj = schedulesArray.getJSONObject(i);

                DayOfWeek dayOfWeek = DayOfWeek.valueOf(scheduleObj.getString("dayOfWeek"));
                LocalTime startTime = LocalTime.parse(scheduleObj.getString("startTime"), timeFormatter);
                LocalTime endTime = LocalTime.parse(scheduleObj.getString("endTime"), timeFormatter);

                classModel.addSchedule(new ClassModel.ScheduleItem(dayOfWeek, startTime, endTime));
            }
        }

        // Parse student IDs if available
        if (classObj.has("students") && !classObj.isNull("students")) {
            JSONArray studentsArray = classObj.getJSONArray("students");

            for (int i = 0; i < studentsArray.length(); i++) {
                JSONObject studentObj = studentsArray.getJSONObject(i);
                classModel.addStudentId(studentObj.getInt("id"));
            }
        }

        return classModel;
    }

    /**
     * Get a more useful error message from Volley error
     */
    private String getErrorMessage(VolleyError error) {
        StringBuilder errorMsg = new StringBuilder();

        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            errorMsg.append("Error code: ").append(statusCode);

            // Add special handling for auth errors
            if (statusCode == 401 || statusCode == 403) {
                errorMsg.append(" - Authentication failed. Please check your credentials and permissions.");
            } else if (statusCode == 400) {
                errorMsg.append(" - Bad request. Check the API endpoint and parameters.");
            } else if (statusCode == 404) {
                errorMsg.append(" - Resource not found. Check the API endpoint.");
            } else if (statusCode == 500) {
                errorMsg.append(" - Server error. Please try again later.");
            }

            // Try to extract response body if available
            try {
                String responseBody = new String(error.networkResponse.data);
                if (responseBody != null && !responseBody.isEmpty()) {
                    errorMsg.append(" Response: ").append(responseBody);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing error response", e);
            }
        } else if (error.getMessage() != null) {
            errorMsg.append(error.getMessage());
        } else {
            errorMsg.append("Network error");
        }

        return errorMsg.toString();
    }
}