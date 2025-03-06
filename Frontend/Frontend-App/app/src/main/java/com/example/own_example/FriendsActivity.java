package com.example.own_example;

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
import com.google.android.material.button.MaterialButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

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

        // Load example data for testing purposes
        loadTestData();

        updateUI();
    }

    private void loadTestData() {
        // Mock friends data - replace with actual API calls later
        friendsList.add(new Friend(1, "jsmith", "Online"));
        friendsList.add(new Friend(2, "rbriggs", "Away"));
        friendsList.add(new Friend(3, "jdoe", "Offline"));

        // Mock friend requests
        requestsList.add(new FriendRequest(4, "ewilson", "Just now"));
        requestsList.add(new FriendRequest(5, "dbrown", "5 minutes ago"));

        // Update counters
        updateCounters();
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

        // Notify adapters to refresh
        friendsAdapter.notifyDataSetChanged();
        requestsAdapter.notifyDataSetChanged();
    }

    private void searchNetId() {
        String netId = searchNetIdEditText.getText().toString().trim();
        if (netId.isEmpty()) {
            Toast.makeText(this, "Please enter a Net-ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show a mock search result
        searchResultLayout.removeAllViews();
        searchResultLayout.setVisibility(View.VISIBLE);

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
    }

    private void sendFriendRequest(String netId) {
        // For now, just show a toast
        Toast.makeText(this, "Connection request sent to " + netId, Toast.LENGTH_SHORT).show();
    }

    // Implement FriendsAdapter.OnFriendActionListener
    @Override
    public void onRemoveFriend(int friendId, int position) {
        // For now, just update the UI
        friendsList.remove(position);
        friendsAdapter.notifyItemRemoved(position);
        updateCounters();
        updateUI();

        Toast.makeText(this, "Friend removed", Toast.LENGTH_SHORT).show();
    }

    // Implement RequestsAdapter.OnRequestActionListener
    @Override
    public void onAcceptRequest(int requestId, int position) {
        // For now, just update the UI
        FriendRequest request = requestsList.get(position);
        friendsList.add(new Friend(request.getId(), request.getName(), "Online"));
        requestsList.remove(position);

        friendsAdapter.notifyDataSetChanged();
        requestsAdapter.notifyItemRemoved(position);
        updateCounters();
        updateUI();

        Toast.makeText(this, "Connection request accepted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRejectRequest(int requestId, int position) {
        // For now, just update the UI
        requestsList.remove(position);
        requestsAdapter.notifyItemRemoved(position);
        updateCounters();
        updateUI();

        Toast.makeText(this, "Connection request declined", Toast.LENGTH_SHORT).show();
    }
}