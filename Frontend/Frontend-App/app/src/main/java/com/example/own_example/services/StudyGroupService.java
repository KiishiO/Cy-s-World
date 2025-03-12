package com.example.own_example.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.own_example.models.StudyGroup;
import com.example.own_example.models.StudyGroupMember;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudyGroupService {
    private static final String TAG = "StudyGroupService";
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/sTables";

    private RequestQueue requestQueue;
    private Context context;
    private long currentUserId;

    public interface StudyTablesCallback {
        void onSuccess(List<StudyGroup> studyGroups);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public StudyGroupService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);

        SharedPreferences prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("user_id", 0);

        if (currentUserId == 0) {
            currentUserId = 1;
        }
    }

    public void getMyStudyGroups(StudyTablesCallback callback) {
        String url = BASE_URL + "/person/" + currentUserId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<StudyGroup> studyGroups = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject tableObj = response.getJSONObject(i);
                            StudyGroup table = parseStudyTableFromJson(tableObj);
                            studyGroups.add(table);
                        }

                        callback.onSuccess(studyGroups);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing study tables: " + e.getMessage());
                        callback.onError("Error parsing data");
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching tables: " + error.toString());
                    callback.onError(getVolleyErrorMessage(error));
                }
        );

        requestQueue.add(request);
    }

    public void getJoinedStudyGroups(StudyTablesCallback callback) {
        String url = BASE_URL + "/friend/" + currentUserId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<StudyGroup> studyGroups = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject tableObj = response.getJSONObject(i);
                            StudyGroup table = parseStudyTableFromJson(tableObj);
                            studyGroups.add(table);
                        }

                        callback.onSuccess(studyGroups);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing study tables: " + e.getMessage());
                        callback.onError("Error parsing data");
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching tables: " + error.toString());
                    callback.onError(getVolleyErrorMessage(error));
                }
        );

        requestQueue.add(request);
    }

    public void createStudyGroup(List<Long> friendIds, ActionCallback callback) {
        String url = BASE_URL + "/create";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("PersonId", currentUserId);

            JSONArray friendIdsArray = new JSONArray();
            for (Long id : friendIds) {
                friendIdsArray.put(id);
            }
            requestBody.put("FriendIds", friendIdsArray);

        } catch (JSONException e) {
            callback.onError("Error creating request: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    callback.onSuccess("Study table created successfully");
                },
                error -> {
                    callback.onError("Error creating study table: " + getVolleyErrorMessage(error));
                }
        );

        requestQueue.add(request);
    }

    public void respondToStudyGroup(long studyTableId, boolean accept, ActionCallback callback) {
        String url = BASE_URL + "/respond";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("studyTableId", studyTableId);
            requestBody.put("status", accept ? "ACCEPTED" : "REJECTED");
        } catch (JSONException e) {
            callback.onError("Error creating response: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                requestBody,
                response -> {
                    String message = accept ? "Study table invitation accepted" : "Study table invitation rejected";
                    callback.onSuccess(message);
                },
                error -> {
                    callback.onError("Error responding to invitation: " + getVolleyErrorMessage(error));
                }
        );

        requestQueue.add(request);
    }

    public void deleteStudyGroup(long studyTableId, ActionCallback callback) {
        String url = BASE_URL + "/delete/" + studyTableId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    callback.onSuccess("Study table deleted successfully");
                },
                error -> {
                    callback.onError("Error deleting study table: " + getVolleyErrorMessage(error));
                }
        );

        requestQueue.add(request);
    }

    private StudyGroup parseStudyTableFromJson(JSONObject json) throws JSONException {
        long id = json.getLong("studyTableId");
        String status = json.getString("status");

        JSONObject leaderObj = json.getJSONObject("TableLeader");
        String leaderName = leaderObj.getString("name");
        long leaderId = leaderObj.getLong("id");

        StudyGroup table = new StudyGroup(id, leaderName, leaderId, status);

        JSONArray membersArray = json.getJSONArray("friendGroup");
        for (int i = 0; i < membersArray.length(); i++) {
            JSONObject memberObj = membersArray.getJSONObject(i);
            long memberId = memberObj.getLong("id");
            String memberName = memberObj.getString("name");

            StudyGroupMember member = new StudyGroupMember(memberId, memberName);
            table.addMember(member);
        }

        return table;
    }

    private String getVolleyErrorMessage(VolleyError error) {
        if (error.networkResponse != null) {
            return "Error code: " + error.networkResponse.statusCode;
        } else {
            return error.getMessage() != null ? error.getMessage() : "Unknown error";
        }
    }
}
