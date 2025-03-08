package com.example.own_example;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.own_example.StudyGroup;
import com.example.own_example.StudyGroupMember;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiService {
    private static final String TAG = "ApiService";
    private static final String BASE_URL = "https://api.studyapp.example.com";
    private static ApiService instance;
    private RequestQueue requestQueue;
    private Context context;

    private ApiService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized ApiService getInstance(Context context) {
        if (instance == null) {
            instance = new ApiService(context.getApplicationContext());
        }
        return instance;
    }

    public interface StudyGroupsCallback {
        void onSuccess(List<StudyGroup> studyGroups);
        void onError(String error);
    }

    public interface StudyGroupCallback {
        void onSuccess(StudyGroup studyGroup);
        void onError(String error);
    }

    public interface SuccessCallback {
        void onSuccess();
        void onError(String error);
    }

    // Get all study groups for the current user
    public void getUserStudyGroups(String userId, final StudyGroupsCallback callback) {
        String url = BASE_URL + "/users/" + userId + "/studygroups";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<StudyGroup> studyGroups = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject groupJson = response.getJSONObject(i);
                                StudyGroup group = parseStudyGroup(groupJson);
                                studyGroups.add(group);
                            }
                            callback.onSuccess(studyGroups);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing data");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Network error: " + error.toString());
                        callback.onError("Network error occurred");
                    }
                }
        );

        requestQueue.add(request);
    }

    // Create a new study group
    public void createStudyGroup(String userId, StudyGroup studyGroup, final StudyGroupCallback callback) {
        String url = BASE_URL + "/studygroups";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("name", studyGroup.getName());
            requestBody.put("userId", userId);

            // Add initial members if any
            JSONArray membersArray = new JSONArray();
            for (StudyGroupMember member : studyGroup.getMembers()) {
                JSONObject memberJson = new JSONObject();
                memberJson.put("email", member.getEmail());
                membersArray.put(memberJson);
            }
            requestBody.put("members", membersArray);

            requestBody.put("maxMembers", studyGroup.getMaxMembers());
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("Error creating request");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            StudyGroup createdGroup = parseStudyGroup(response);
                            callback.onSuccess(createdGroup);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Network error: " + error.toString());
                        callback.onError("Network error occurred");
                    }
                }
        );

        requestQueue.add(request);
    }

    // Update an existing study group
    public void updateStudyGroup(StudyGroup studyGroup, final StudyGroupCallback callback) {
        String url = BASE_URL + "/studygroups/" + studyGroup.getId();

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("name", studyGroup.getName());

            // Add members
            JSONArray membersArray = new JSONArray();
            for (StudyGroupMember member : studyGroup.getMembers()) {
                JSONObject memberJson = new JSONObject();
                memberJson.put("id", member.getId());
                memberJson.put("email", member.getEmail());
                memberJson.put("active", member.isActive());
                membersArray.put(memberJson);
            }
            requestBody.put("members", membersArray);

            requestBody.put("maxMembers", studyGroup.getMaxMembers());
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("Error creating request");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            StudyGroup updatedGroup = parseStudyGroup(response);
                            callback.onSuccess(updatedGroup);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Network error: " + error.toString());
                        callback.onError("Network error occurred");
                    }
                }
        );

        requestQueue.add(request);
    }

    // Delete a study group
    public void deleteStudyGroup(String groupId, final SuccessCallback callback) {
        String url = BASE_URL + "/studygroups/" + groupId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Network error: " + error.toString());
                        callback.onError("Network error occurred");
                    }
                }
        );

        requestQueue.add(request);
    }

    // Add a member to a study group
    public void addStudyGroupMember(String groupId, String email, final StudyGroupCallback callback) {
        String url = BASE_URL + "/studygroups/" + groupId + "/members";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("Error creating request");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            StudyGroup updatedGroup = parseStudyGroup(response);
                            callback.onSuccess(updatedGroup);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Network error: " + error.toString());
                        callback.onError("Network error occurred");
                    }
                }
        );

        requestQueue.add(request);
    }

    // Remove a member from a study group
    public void removeStudyGroupMember(String groupId, String memberId, final StudyGroupCallback callback) {
        String url = BASE_URL + "/studygroups/" + groupId + "/members/" + memberId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            StudyGroup updatedGroup = parseStudyGroup(response);
                            callback.onSuccess(updatedGroup);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Error parsing response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Network error: " + error.toString());
                        callback.onError("Network error occurred");
                    }
                }
        );

        requestQueue.add(request);
    }

    // Helper method to parse a study group from JSON
    private StudyGroup parseStudyGroup(JSONObject json) throws JSONException {
        StudyGroup group = new StudyGroup();
        group.setId(json.getString("id"));
        group.setName(json.getString("name"));

        if (json.has("maxMembers")) {
            group.setMaxMembers(json.getInt("maxMembers"));
        }

        JSONArray membersArray = json.getJSONArray("members");
        List<StudyGroupMember> members = new ArrayList<>();
        for (int i = 0; i < membersArray.length(); i++) {
            JSONObject memberJson = membersArray.getJSONObject(i);
            StudyGroupMember member = new StudyGroupMember();
            member.setId(memberJson.getString("id"));
            member.setName(memberJson.getString("name"));
            member.setEmail(memberJson.getString("email"));
            member.setActive(memberJson.getBoolean("active"));
            members.add(member);
        }
        group.setMembers(members);

        return group;
    }
}