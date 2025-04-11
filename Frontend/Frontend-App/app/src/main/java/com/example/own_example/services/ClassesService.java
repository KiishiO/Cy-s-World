package com.example.own_example.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.own_example.models.ClassModel;
import com.example.own_example.models.Student;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for interacting with the Classes API
 */
public class ClassesService {
    private static final String TAG = "ClassesService";
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080";
    private RequestQueue requestQueue;
    private Context context;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClassesService(Context context) {
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
     * Get all classes for the current student
     */
    public void getStudentClasses(ApiCallback<List<ClassModel>> callback) {
        // Get the student ID from shared preferences
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        long studentId = prefs.getLong("user_id", -1);

        if (studentId == -1) {
            callback.onError("User ID not found. Please log in again.");
            return;
        }

        String url = BASE_URL + "/classes/student/" + studentId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
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
                        callback.onError("Error fetching classes: " + error.getMessage());
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Get all classes for the current teacher
     */
    public void getTeacherClasses(ApiCallback<List<ClassModel>> callback) {
        // Get the teacher ID from shared preferences
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int teacherId = prefs.getInt("user_id", -1);

        if (teacherId == -1) {
            callback.onError("User ID not found. Please log in again.");
            return;
        }

        String url = BASE_URL + "/classes/teacher/" + teacherId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
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
                        callback.onError("Error fetching classes: " + error.getMessage());
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Get details for a specific class
     */
    public void getClassDetails(int classId, ApiCallback<ClassModel> callback) {
        String url = BASE_URL + "/classes/" + classId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ClassModel classModel = parseClassModel(response);
                            callback.onSuccess(classModel);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing class details: " + e.getMessage());
                            callback.onError("Error parsing class data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching class details: " + error.toString());
                        callback.onError("Error fetching class details: " + error.getMessage());
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Get students enrolled in a specific class
     */
    public void getStudentsInClass(int classId, ApiCallback<List<Student>> callback) {
        String url = BASE_URL + "/classes/" + classId + "/students";

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

                                Student student = new Student(
                                        studentObj.getInt("id"),
                                        studentObj.getString("name"),
                                        studentObj.optString("email", "")
                                );

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
                        callback.onError("Error fetching students: " + error.getMessage());
                    }
                }
        );

        requestQueue.add(request);
    }

    /**
     * Update a student's grade
     */
    public void updateStudentGrade(int studentId, int classId, String grade, ApiCallback<Boolean> callback) {
        String url = BASE_URL + "/grades/update";

        try {
            JSONObject gradeData = new JSONObject();
            gradeData.put("studentId", studentId);
            gradeData.put("classId", classId);
            gradeData.put("grade", grade);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    gradeData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String message = response.getString("message");
                                callback.onSuccess(message.equals("success"));
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing grade update response: " + e.getMessage());
                                callback.onError("Error processing grade update");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error updating grade: " + error.toString());
                            callback.onError("Error updating grade: " + error.getMessage());
                        }
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating grade update request: " + e.getMessage());
            callback.onError("Error preparing grade update");
        }
    }

    /**
     * Parse a JSON object into a Class
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
}