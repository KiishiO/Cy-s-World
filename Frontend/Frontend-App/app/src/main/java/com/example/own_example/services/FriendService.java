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
import com.example.own_example.models.Friend;
import com.example.own_example.models.FriendRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FriendService {
    private static final String TAG = "FriendService";
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/Logins";

    private RequestQueue requestQueue;
    private Context context;
    private List<JSONObject> allUsers = new ArrayList<>();
    private long currentUserId;

    public FriendService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);

        // Get current user ID from preferences
        SharedPreferences prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("user_id", 0);

        // Load all users on initialization
        loadAllUsers();
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

    private void loadAllUsers() {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    Log.d(TAG, "Loaded " + response.length() + " users");
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject user = response.getJSONObject(i);
                            allUsers.add(user);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing user: " + e.getMessage());
                        }
                    }
                },
                error -> {
                    Log.e(TAG, "Error loading users: " + error.toString());
                }
        );

        requestQueue.add(request);
    }

    // Get user's friends (simulated with other users from the system)
    public void getFriends(long userId, FriendsCallback callback) {
        // Ensure users are loaded
        if (allUsers.isEmpty()) {
            loadAllUsers();
            // Return a small delay to allow users to load
            new android.os.Handler().postDelayed(() -> {
                generateFriends(userId, callback);
            }, 1000);
        } else {
            generateFriends(userId, callback);
        }
    }

    private void generateFriends(long userId, FriendsCallback callback) {
        List<Friend> friendsList = new ArrayList<>();

        // For demo purposes, simulate 2-3 friends
        int friendCount = Math.min(allUsers.size() - 1, new Random().nextInt(2) + 2);

        for (int i = 0; i < friendCount; i++) {
            try {
                JSONObject user = allUsers.get(i);
                if (user.getLong("id") == userId) {
                    continue; // Skip the current user
                }

                int id = user.getInt("id");
                String name = user.getString("name");

                // Random status for demo
                String[] statuses = {"Online", "Away", "Offline"};
                String status = statuses[new Random().nextInt(statuses.length)];

                Friend friend = new Friend(id, name, status);
                friendsList.add(friend);
            } catch (JSONException e) {
                Log.e(TAG, "Error generating friend: " + e.getMessage());
            }
        }

        callback.onSuccess(friendsList);
    }

    // Get pending friend requests (simulated)
    public void getFriendRequests(long userId, RequestsCallback callback) {
        // Ensure users are loaded
        if (allUsers.isEmpty()) {
            loadAllUsers();
            // Return a small delay to allow users to load
            new android.os.Handler().postDelayed(() -> {
                generateRequests(userId, callback);
            }, 1000);
        } else {
            generateRequests(userId, callback);
        }
    }

    private void generateRequests(long userId, RequestsCallback callback) {
        List<FriendRequest> requestsList = new ArrayList<>();

        // For demo purposes, simulate 1-2 requests
        int requestCount = Math.min(allUsers.size() - 1, new Random().nextInt(2) + 1);

        for (int i = 0; i < requestCount; i++) {
            try {
                // Skip current user and users already used as friends
                int index = allUsers.size() - 1 - i;
                JSONObject user = allUsers.get(index);
                if (user.getLong("id") == userId) {
                    continue; // Skip the current user
                }

                int id = user.getInt("id");
                String name = user.getString("name");

                // Random timestamp for demo
                String[] times = {"Just now", "5 minutes ago", "1 hour ago"};
                String time = times[new Random().nextInt(times.length)];

                FriendRequest request = new FriendRequest(id, name, time);
                requestsList.add(request);
            } catch (JSONException e) {
                Log.e(TAG, "Error generating request: " + e.getMessage());
            }
        }

        callback.onSuccess(requestsList);
    }

    // Send a friend request (simulated)
    public void sendFriendRequest(long senderId, long receiverId, ActionCallback callback) {
        // Simulated success
        new android.os.Handler().postDelayed(() -> {
            callback.onSuccess("Friend request sent successfully");
        }, 800);
    }

    // Accept a friend request (simulated)
    public void respondToRequest(long requestId, boolean accept, ActionCallback callback) {
        // Simulated success
        new android.os.Handler().postDelayed(() -> {
            String message = accept ? "Friend request accepted" : "Friend request rejected";
            callback.onSuccess(message);
        }, 800);
    }

    // Remove a friend (simulated)
    public void removeFriend(long friendId, ActionCallback callback) {
        // Simulated success
        new android.os.Handler().postDelayed(() -> {
            callback.onSuccess("Connection removed successfully");
        }, 800);
    }

    // Search for a user by Net-ID
    public void searchNetId(String netId, ActionCallback callback) {
        // Find matching users in our already loaded list
        boolean found = false;

        for (JSONObject user : allUsers) {
            try {
                String email = user.getString("emailId").toLowerCase();
                String name = user.getString("name").toLowerCase();

                if (email.contains(netId.toLowerCase()) || name.contains(netId.toLowerCase())) {
                    found = true;
                    callback.onSuccess("User found: " + user.getString("name"));
                    break;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error searching for user: " + e.getMessage());
            }
        }

        if (!found) {
            callback.onSuccess("No user found with Net-ID: " + netId);
        }
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