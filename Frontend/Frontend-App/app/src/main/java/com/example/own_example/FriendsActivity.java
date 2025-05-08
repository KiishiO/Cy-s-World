/**
 * Activity for managing friend connections in the CyWorld application.
 * This activity allows users to view their friends, manage friend requests,
 * search for other users, and initiate chats with friends.
 *
 * @author Jawad Ali
 */
package com.example.own_example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.FriendsAdapter;
import com.example.own_example.adapters.RequestsAdapter;
import com.example.own_example.models.Friend;
import com.example.own_example.models.FriendRequest;
import com.example.own_example.services.FriendService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements
        FriendsAdapter.OnFriendActionListener,
        RequestsAdapter.OnRequestActionListener {

    /** Tag for logging purposes */
    private static final String TAG = "FriendsActivity";

    /** RecyclerView for displaying the friends list */
    private RecyclerView friendsRecyclerView;

    /** RecyclerView for displaying friend requests */
    private RecyclerView requestsRecyclerView;

    /** Adapter for the friends list */
    private FriendsAdapter friendsAdapter;

    /** Adapter for the friend requests list */
    private RequestsAdapter requestsAdapter;

    /** Text field for searching users by Net-ID */
    private TextInputEditText searchNetIdEditText;

    /** Button to trigger the search */
    private MaterialButton searchButton;

    /** Layout to display search results */
    private LinearLayout searchResultLayout;

    /** Text displayed when the user has no friends */
    private TextView noFriendsTextView;

    /** Text displayed when the user has no friend requests */
    private TextView noRequestsTextView;

    /** Counter displaying the number of friends */
    private TextView friendsCountTextView;

    /** Counter displaying the number of friend requests */
    private TextView requestsCountTextView;

    /** List of friends */
    private List<Friend> friendsList = new ArrayList<>();

    /** List of friend requests */
    private List<FriendRequest> requestsList = new ArrayList<>();

    /** Service for friend-related API calls */
    private FriendService friendService;

    /** ID of the current user */
    private long currentUserId = 1; // Default to 1 for testing

    /**
     * Initializes the activity, sets up UI components, and loads initial data.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down, this contains the data it most recently
     *     supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Log.d(TAG, "FriendsActivity onCreate");

        // Initialize service
        friendService = new FriendService(this);

        // Try to get user ID from shared preferences
        try {
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            currentUserId = prefs.getLong("user_id", 1); // Default to 1 if not found
            Log.d(TAG, "Loaded user ID: " + currentUserId);
        } catch (Exception e) {
            Log.e(TAG, "Error loading user ID: " + e.getMessage());
        }

        // Initialize views
        friendsRecyclerView = findViewById(R.id.friends_recycler_view);
        requestsRecyclerView = findViewById(R.id.requests_recycler_view);
        searchNetIdEditText = findViewById(R.id.search_netid_edit_text);
        searchButton = findViewById(R.id.search_button);
        searchResultLayout = findViewById(R.id.search_result_layout);
        noFriendsTextView = findViewById(R.id.no_friends_text);
        noRequestsTextView = findViewById(R.id.no_requests_text);
        friendsCountTextView = findViewById(R.id.friends_count);
        requestsCountTextView = findViewById(R.id.requests_count);

        // Set up RecyclerViews
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapters with listeners
        friendsAdapter = new FriendsAdapter(friendsList, this);
        requestsAdapter = new RequestsAdapter(requestsList, this);

        friendsRecyclerView.setAdapter(friendsAdapter);
        requestsRecyclerView.setAdapter(requestsAdapter);

        // Set up search button
        searchButton.setOnClickListener(v -> searchNetId());

        // Load actual data from the server
        loadFriends();
        loadFriendRequests();
    }

    /**
     * Called when the activity becomes visible to the user.
     * Refreshes friend and request data.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        loadFriends();
        loadFriendRequests();
    }

    /**
     * Loads the friends list from the server.
     */
    private void loadFriends() {
        friendService.getFriends(currentUserId, new FriendService.FriendsCallback() {
            /**
             * Called when friends are successfully loaded.
             *
             * @param friends The list of friends
             */
            @Override
            public void onSuccess(List<Friend> friends) {
                friendsList.clear();
                friendsList.addAll(friends);
                updateUI();
            }

            /**
             * Called when there's an error loading friends.
             *
             * @param error The error message
             */
            @Override
            public void onError(String error) {
                showSnackbar("Error loading Cyclones: " + error);
                // For demo purposes, load test data if the API fails
                loadTestFriends();
            }
        });
    }

    /**
     * Loads the friend requests from the server.
     */
    private void loadFriendRequests() {
        friendService.getFriendRequests(currentUserId, new FriendService.RequestsCallback() {
            /**
             * Called when friend requests are successfully loaded.
             *
             * @param requests The list of friend requests
             */
            @Override
            public void onSuccess(List<FriendRequest> requests) {
                requestsList.clear();
                requestsList.addAll(requests);
                updateUI();
            }

            /**
             * Called when there's an error loading friend requests.
             *
             * @param error The error message
             */
            @Override
            public void onError(String error) {
                showSnackbar("Error loading connection requests: " + error);
                // For demo purposes, load test data if the API fails
                loadTestRequests();
            }
        });
    }

    /**
     * Loads mock friends data for testing or when the API fails.
     */
    private void loadTestFriends() {
        // Mock friends data for testing
        friendsList.clear();
        friendsList.add(new Friend(1, "jsmith", "Online"));
        friendsList.add(new Friend(2, "rbriggs", "Away"));
        friendsList.add(new Friend(3, "jdoe", "Offline"));
        updateUI();
    }

    /**
     * Loads mock friend requests for testing or when the API fails.
     */
    private void loadTestRequests() {
        // Mock friend requests for testing
        requestsList.clear();
        requestsList.add(new FriendRequest(4, "ewilson", "Just now"));
        requestsList.add(new FriendRequest(5, "dbrown", "5 minutes ago"));
        updateUI();
    }

    /**
     * Updates the friend and request count displays.
     */
    private void updateCounters() {
        try {
            if (friendsCountTextView != null) {
                friendsCountTextView.setText(String.valueOf(friendsList.size()));
            }
            if (requestsCountTextView != null) {
                requestsCountTextView.setText(String.valueOf(requestsList.size()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating counters: " + e.getMessage());
        }
    }

    /**
     * Updates the UI based on the current state of friends and requests lists.
     */
    private void updateUI() {
        try {
            runOnUiThread(() -> {
                // Update friends list visibility
                if (friendsList.isEmpty()) {
                    friendsRecyclerView.setVisibility(View.GONE);
                    noFriendsTextView.setVisibility(View.VISIBLE);
                } else {
                    friendsRecyclerView.setVisibility(View.VISIBLE);
                    noFriendsTextView.setVisibility(View.GONE);
                }

                // Update requests list visibility
                if (requestsList.isEmpty()) {
                    requestsRecyclerView.setVisibility(View.GONE);
                    noRequestsTextView.setVisibility(View.VISIBLE);
                } else {
                    requestsRecyclerView.setVisibility(View.VISIBLE);
                    noRequestsTextView.setVisibility(View.GONE);
                }

                // Update counters
                updateCounters();

                // Notify adapters to refresh
                if (friendsAdapter != null) {
                    friendsAdapter.notifyDataSetChanged();
                }
                if (requestsAdapter != null) {
                    requestsAdapter.notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage());
        }
    }

    /**
     * Searches for a user by Net-ID.
     * Triggered when the search button is clicked.
     */
    private void searchNetId() {
        String netId = searchNetIdEditText.getText().toString().trim();
        if (netId.isEmpty()) {
            showSnackbar("Please enter a Net-ID");
            return;
        }

        // Clear previous search results
        searchResultLayout.removeAllViews();

        // First search for the user
        friendService.searchNetId(netId, new FriendService.ActionCallback() {
            /**
             * Called when the search is successful.
             *
             * @param message The response message
             */
            @Override
            public void onSuccess(String message) {
                // Show search result if "User found" is in the message
                if (message.contains("User found")) {
                    showSearchResult(netId);
                } else {
                    showSnackbar(message);
                }
            }

            /**
             * Called when there's an error searching.
             *
             * @param error The error message
             */
            @Override
            public void onError(String error) {
                showSnackbar("Error searching: " + error);
            }
        });
    }

    /**
     * Displays the search result for a found user.
     *
     * @param netId The Net-ID of the found user
     */
    private void showSearchResult(String netId) {
        // Show mock search result for now
        try {
            View searchResultView = getLayoutInflater().inflate(R.layout.item_search_result, searchResultLayout, false);
            TextView usernameTextView = searchResultView.findViewById(R.id.user_name);
            MaterialButton addFriendButton = searchResultView.findViewById(R.id.add_friend_button);

            usernameTextView.setText(netId);
            addFriendButton.setOnClickListener(v -> {
                sendFriendRequest(netId);
                searchResultLayout.setVisibility(View.GONE);
                searchNetIdEditText.setText("");
            });

            searchResultLayout.addView(searchResultView);
            searchResultLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "Error showing search result: " + e.getMessage());
            showSnackbar("Error showing search result");
        }
    }

    /**
     * Sends a friend request to another user.
     *
     * @param netId The Net-ID of the recipient
     */
    private void sendFriendRequest(String netId) {
        // For a real implementation, you would need to get the user ID for the netId
        // For now, use a dummy receiver ID of 2
        long receiverId = 2;

        friendService.sendFriendRequest(currentUserId, receiverId, new FriendService.ActionCallback() {
            /**
             * Called when the friend request is successfully sent.
             *
             * @param message The success message
             */
            @Override
            public void onSuccess(String message) {
                showSnackbar("Connection request sent to " + netId);
            }

            /**
             * Called when there's an error sending the friend request.
             *
             * @param error The error message
             */
            @Override
            public void onError(String error) {
                showSnackbar("Error sending request: " + error);
            }
        });
    }

    /**
     * Called when a user removes a friend.
     * Implements FriendsAdapter.OnFriendActionListener.
     *
     * @param friendId The ID of the friend to remove
     * @param position The position of the friend in the list
     */
    @Override
    public void onRemoveFriend(int friendId, int position) {
        if (position < 0 || position >= friendsList.size()) {
            showSnackbar("Invalid friend position");
            return;
        }

        friendService.removeFriend(friendId, new FriendService.ActionCallback() {
            /**
             * Called when the friend is successfully removed.
             *
             * @param message The success message
             */
            @Override
            public void onSuccess(String message) {
                try {
                    friendsList.remove(position);
                    runOnUiThread(() -> {
                        friendsAdapter.notifyItemRemoved(position);
                        updateCounters();
                        updateUI();
                    });
                    showSnackbar("Cyclone connection removed");
                } catch (Exception e) {
                    Log.e(TAG, "Error removing friend: " + e.getMessage());
                    showSnackbar("Error removing friend");
                }
            }

            /**
             * Called when there's an error removing the friend.
             *
             * @param error The error message
             */
            @Override
            public void onError(String error) {
                showSnackbar("Error removing connection: " + error);
            }
        });
    }

    /**
     * Initiates a chat with a friend.
     * Implements FriendsAdapter.OnFriendActionListener.
     *
     * @param friend The friend to chat with
     */
    @Override
    public void onChatWithFriend(Friend friend) {
        try {
            // Create intent to launch chat activity
            Intent chatIntent = new Intent(this, ChatActivity.class);

            // Pass friend details to the chat activity
            chatIntent.putExtra("friendId", friend.getId());
            chatIntent.putExtra("friendName", friend.getName());
            chatIntent.putExtra("friendStatus", friend.getStatus());

            // Get username from SharedPreferences for the sender
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String username = prefs.getString("username", "user");
            chatIntent.putExtra("username", username);

            // Start the chat activity
            startActivity(chatIntent);

            Log.d(TAG, "Starting chat with: " + friend.getName());
        } catch (Exception e) {
            Log.e(TAG, "Error starting chat: " + e.getMessage());
            showSnackbar("Could not start chat. Please try again.");
        }
    }

    /**
     * Called when a user accepts a friend request.
     * Implements RequestsAdapter.OnRequestActionListener.
     *
     * @param requestId The ID of the request to accept
     * @param position The position of the request in the list
     */
    @Override
    public void onAcceptRequest(int requestId, int position) {
        if (position < 0 || position >= requestsList.size()) {
            showSnackbar("Invalid request position");
            return;
        }

        final FriendRequest request = requestsList.get(position);

        friendService.respondToRequest(requestId, true, new FriendService.ActionCallback() {
            /**
             * Called when the request is successfully accepted.
             *
             * @param message The success message
             */
            @Override
            public void onSuccess(String message) {
                try {
                    // Make a copy of the request for safety
                    final String requestName = request.getName();
                    final int requestId = request.getId();

                    // Add to friends first, then remove from requests list
                    friendsList.add(new Friend(requestId, requestName, "Online"));

                    // Then safely remove from requests
                    requestsList.remove(position);

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        // Notify adapters
                        friendsAdapter.notifyDataSetChanged();
                        requestsAdapter.notifyItemRemoved(position);
                        updateCounters();
                        updateUI();
                    });

                    showSnackbar("Connection request accepted");
                } catch (Exception e) {
                    Log.e(TAG, "Error accepting request: " + e.getMessage());
                    // Reload both lists to recover
                    loadFriends();
                    loadFriendRequests();
                }
            }

            /**
             * Called when there's an error accepting the request.
             *
             * @param error The error message
             */
            @Override
            public void onError(String error) {
                showSnackbar("Error accepting request: " + error);
            }
        });
    }

    /**
     * Called when a user rejects a friend request.
     * Implements RequestsAdapter.OnRequestActionListener.
     *
     * @param requestId The ID of the request to reject
     * @param position The position of the request in the list
     */
    @Override
    public void onRejectRequest(int requestId, int position) {
        if (position < 0 || position >= requestsList.size()) {
            showSnackbar("Invalid request position");
            return;
        }

        friendService.respondToRequest(requestId, false, new FriendService.ActionCallback() {
            /**
             * Called when the request is successfully rejected.
             *
             * @param message The success message
             */
            @Override
            public void onSuccess(String message) {
                try {
                    requestsList.remove(position);
                    runOnUiThread(() -> {
                        requestsAdapter.notifyItemRemoved(position);
                        updateCounters();
                        updateUI();
                    });
                    showSnackbar("Connection request declined");
                } catch (Exception e) {
                    Log.e(TAG, "Error rejecting request: " + e.getMessage());
                    // Reload to recover
                    loadFriendRequests();
                }
            }

            /**
             * Called when there's an error rejecting the request.
             *
             * @param error The error message
             */
            @Override
            public void onError(String error) {
                showSnackbar("Error declining request: " + error);
            }
        });
    }

    /**
     * Displays a snackbar message at the bottom of the screen.
     * Falls back to a toast message if the snackbar fails.
     *
     * @param message The message to display
     */
    private void showSnackbar(String message) {
        try {
            runOnUiThread(() -> {
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error showing snackbar: " + e.getMessage());
            // Fallback to toast
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}