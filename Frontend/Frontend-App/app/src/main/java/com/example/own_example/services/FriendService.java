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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.own_example.models.Friend;
import com.example.own_example.models.FriendRequest;

public class FriendService {
    private static final String TAG = "FriendService";
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/Persons";

    private RequestQueue requestQueue;
    private Context context;

    public FriendService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public interface FriendsCallback {
        void onSuccess(List<Friend> friends);
        void onError(String error);
    }

    public interface RequestsCallback {
        void onSuccess(List<FriendRequest> requests);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Get user's friends
    public void getFriends(long userId, FriendsCallback callback) {
        String url = BASE_URL + "/FriendRequests/friends/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Friend> friendsList = new ArrayList<>();
                        JSONArray friendsArray = response.getJSONArray("friends");

                        for (int i = 0; i < friendsArray.length(); i++) {
                            JSONObject friendObj = friendsArray.getJSONObject(i);
                            int id = friendObj.getInt("id");
                            String name = friendObj.getString("name");
                            // Default status as we don't have actual status from API
                            Friend friend = new Friend(id, name, "Online");
                            friendsList.add(friend);
                        }

                        callback.onSuccess(friendsList);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing friends: " + e.getMessage());
                        callback.onError("Error parsing friend data");
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching friends: " + error.toString());
                    callback.onError("Error fetching friends: " + getVolleyErrorMessage(error));
                });

        requestQueue.add(request);
    }

    // Get pending friend requests
    public void getFriendRequests(long userId, RequestsCallback callback) {
        String url = BASE_URL + "/FriendRequests/received/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<FriendRequest> requestsList = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject requestObj = response.getJSONObject(i);
                            JSONObject senderObj = requestObj.getJSONObject("sender");

                            int id = requestObj.getInt("requestId");
                            String name = senderObj.getString("name");
                            // We don't have a timestamp in the API response, so using a placeholder
                            FriendRequest friendRequest = new FriendRequest(id, name, "Just now");
                            requestsList.add(friendRequest);
                        }

                        callback.onSuccess(requestsList);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing requests: " + e.getMessage());
                        callback.onError("Error parsing request data");
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching requests: " + error.toString());
                    callback.onError("Error fetching requests: " + getVolleyErrorMessage(error));
                });

        requestQueue.add(request);
    }

    // Send a friend request
    public void sendFriendRequest(long senderId, long receiverId, ActionCallback callback) {
        String url = BASE_URL;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("senderId", senderId);
            requestBody.put("receiverId", receiverId);
        } catch (JSONException e) {
            callback.onError("Error creating request: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        callback.onSuccess("Friend request sent successfully");
                    } catch (Exception e) {
                        callback.onError("Error processing response: " + e.getMessage());
                    }
                },
                error -> {
                    callback.onError("Error sending request: " + getVolleyErrorMessage(error));
                });

        requestQueue.add(request);
    }

    // Accept a friend request
    public void respondToRequest(long requestId, boolean accept, ActionCallback callback) {
        String url = BASE_URL;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("requestId", requestId);
            requestBody.put("status", accept ? "ACCEPTED" : "REJECTED");
        } catch (JSONException e) {
            callback.onError("Error creating response: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        String message = accept ? "Friend request accepted" : "Friend request rejected";
                        callback.onSuccess(message);
                    } catch (Exception e) {
                        callback.onError("Error processing response: " + e.getMessage());
                    }
                },
                error -> {
                    callback.onError("Error responding to request: " + getVolleyErrorMessage(error));
                });

        requestQueue.add(request);
    }

    // Remove a friend (cancel a connection)
    public void removeFriend(long requestId, ActionCallback callback) {
        String url = BASE_URL + requestId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    try {
                        callback.onSuccess("Connection removed successfully");
                    } catch (Exception e) {
                        callback.onError("Error processing response: " + e.getMessage());
                    }
                },
                error -> {
                    callback.onError("Error removing connection: " + getVolleyErrorMessage(error));
                });

        requestQueue.add(request);
    }

    // Search for a user by Net-ID
    public void searchNetId(String netId, ActionCallback callback) {
        // This is a placeholder method as the specific endpoint for searching users wasn't provided
        // Normally you would have an API endpoint like /users/search?netId=...

        // For now, just simulate a successful search
        callback.onSuccess("User found: " + netId);
    }

    // Helper method to get a more useful error message from Volley
    private String getVolleyErrorMessage(VolleyError error) {
        if (error.networkResponse != null) {
            return "Status code: " + error.networkResponse.statusCode;
        } else {
            return error.getMessage() != null ? error.getMessage() : "Unknown error";
        }
    }
}