package com.example.own_example;

import android.content.SharedPreferences;
import android.os.Bundle;
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

    private RecyclerView friendsRecyclerView;
    private RecyclerView requestsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private RequestsAdapter requestsAdapter;
    private TextInputEditText searchNetIdEditText;
    private MaterialButton searchButton;
    private LinearLayout searchResultLayout;
    private TextView noFriendsTextView;
    private TextView noRequestsTextView;
    private TextView friendsCountTextView;
    private TextView requestsCountTextView;

    // Data lists
    private List<Friend> friendsList = new ArrayList<>();
    private List<FriendRequest> requestsList = new ArrayList<>();

    // Service
    private FriendService friendService;

    // User ID (would normally be obtained from login session)
    private long currentUserId = 1; // Default to 1 for testing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // Initialize service
        friendService = new FriendService(this);

        // Try to get user ID from shared preferences
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUserId = prefs.getLong("user_id", 1); // Default to 1 if not found

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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        loadFriends();
        loadFriendRequests();
    }

    private void loadFriends() {
        friendService.getFriends(currentUserId, new FriendService.FriendsCallback() {
            @Override
            public void onSuccess(List<Friend> friends) {
                friendsList.clear();
                friendsList.addAll(friends);
                updateUI();
            }

            @Override
            public void onError(String error) {
                showSnackbar("Error loading Cyclones: " + error);
                // For demo purposes, load test data if the API fails
                loadTestFriends();
            }
        });
    }

    private void loadFriendRequests() {
        friendService.getFriendRequests(currentUserId, new FriendService.RequestsCallback() {
            @Override
            public void onSuccess(List<FriendRequest> requests) {
                requestsList.clear();
                requestsList.addAll(requests);
                updateUI();
            }

            @Override
            public void onError(String error) {
                showSnackbar("Error loading connection requests: " + error);
                // For demo purposes, load test data if the API fails
                loadTestRequests();
            }
        });
    }

    private void loadTestFriends() {
        // Mock friends data for testing
        friendsList.clear();
        friendsList.add(new Friend(1, "jsmith", "Online"));
        friendsList.add(new Friend(2, "rbriggs", "Away"));
        friendsList.add(new Friend(3, "jdoe", "Offline"));
        updateUI();
    }

    private void loadTestRequests() {
        // Mock friend requests for testing
        requestsList.clear();
        requestsList.add(new FriendRequest(4, "ewilson", "Just now"));
        requestsList.add(new FriendRequest(5, "dbrown", "5 minutes ago"));
        updateUI();
    }

    private void updateCounters() {
        if (friendsCountTextView != null) {
            friendsCountTextView.setText(String.valueOf(friendsList.size()));
        }
        if (requestsCountTextView != null) {
            requestsCountTextView.setText(String.valueOf(requestsList.size()));
        }
    }

    private void updateUI() {
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
        friendsAdapter.notifyDataSetChanged();
        requestsAdapter.notifyDataSetChanged();
    }

    private void searchNetId() {
        String netId = searchNetIdEditText.getText().toString().trim();
        if (netId.isEmpty()) {
            showSnackbar("Please enter a Net-ID");
            return;
        }

        // Clear previous search results
        searchResultLayout.removeAllViews();

        // Show mock search result for now
        // In a real implementation, you would call the API to search for users
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
    }

    private void sendFriendRequest(String netId) {
        // For a real implementation, you would need to get the user ID for the netId
        // For now, use a dummy receiver ID of 2
        long receiverId = 2;

        friendService.sendFriendRequest(currentUserId, receiverId, new FriendService.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                showSnackbar("Connection request sent to " + netId);
            }

            @Override
            public void onError(String error) {
                showSnackbar("Error sending request: " + error);
            }
        });
    }

    // Implement FriendsAdapter.OnFriendActionListener
    @Override
    public void onRemoveFriend(int friendId, int position) {
        friendService.removeFriend(friendId, new FriendService.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                friendsList.remove(position);
                friendsAdapter.notifyItemRemoved(position);
                updateCounters();
                updateUI();
                showSnackbar("Cyclone connection removed");
            }

            @Override
            public void onError(String error) {
                showSnackbar("Error removing connection: " + error);
            }
        });
    }

    // Implement RequestsAdapter.OnRequestActionListener
    @Override
    public void onAcceptRequest(int requestId, int position) {
        friendService.respondToRequest(requestId, true, new FriendService.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                FriendRequest request = requestsList.get(position);
                friendsList.add(new Friend(request.getId(), request.getName(), "Online"));
                requestsList.remove(position);

                friendsAdapter.notifyDataSetChanged();
                requestsAdapter.notifyItemRemoved(position);
                updateCounters();
                updateUI();
                showSnackbar("Connection request accepted");
            }

            @Override
            public void onError(String error) {
                showSnackbar("Error accepting request: " + error);
            }
        });
    }

    @Override
    public void onRejectRequest(int requestId, int position) {
        friendService.respondToRequest(requestId, false, new FriendService.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                requestsList.remove(position);
                requestsAdapter.notifyItemRemoved(position);
                updateCounters();
                updateUI();
                showSnackbar("Connection request declined");
            }

            @Override
            public void onError(String error) {
                showSnackbar("Error declining request: " + error);
            }
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}