package com.example.own_example;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.StudyGroupAdapter;
import com.example.own_example.adapters.StudyGroupAdapter;
import com.example.own_example.models.Friend;
import com.example.own_example.models.StudyGroup;
import com.example.own_example.models.StudyGroupMember;
import com.example.own_example.services.FriendService;
import com.example.own_example.services.StudyGroupService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class StudyGroupsActivity extends AppCompatActivity {

    private static final String TAG = "StudyGroupsActivity";

    private RecyclerView studyGroupsRecyclerView;
    private LinearLayout emptyStateView;
    private MaterialButton createGroupButton;

    private ImageButton manageGroupButton;
    //private BottomNavigationView bottomNavigationView;

    private StudyGroupAdapter studyGroupsAdapter;
    private List<StudyGroup> studyGroups = new ArrayList<>();

    private StudyGroupService studyGroupService;
    private FriendService friendService;

    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_group);

        Log.d(TAG, "StudyGroupsActivity onCreate");

        // Initialize services
        studyGroupService = new StudyGroupService(this);
        friendService = new FriendService(this);

        // Get current user ID
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUserId = prefs.getLong("user_id", 0);

        if (currentUserId == 0) {
            prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            currentUserId = prefs.getLong("user_id", 1);
        }

        // Initialize views
        studyGroupsRecyclerView = findViewById(R.id.study_groups_recycler);
        emptyStateView = findViewById(R.id.empty_state_view);
        createGroupButton = findViewById(R.id.create_group_button);
        //bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up RecyclerView
        studyGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studyGroupsAdapter = new StudyGroupAdapter(studyGroups, this);
        studyGroupsRecyclerView.setAdapter(studyGroupsAdapter);

        // Set up create button
        createGroupButton.setOnClickListener(v -> showCreateTableDialog());

        MaterialButton emptyStateCreateButton = findViewById(R.id.empty_state_create_button);
        if (emptyStateCreateButton != null) {
            emptyStateCreateButton.setOnClickListener(v -> showCreateTableDialog());
        }

//        // Set up bottom navigation
//        if (bottomNavigationView != null) {
//            bottomNavigationView.inflateMenu(R.menu.student_bottom_navigation_menu);
//            bottomNavigationView.setOnItemSelectedListener(item -> {
//                int itemId = item.getItemId();
//
//                if (itemId == R.id.nav_home) {
//                    Intent intent = new Intent(StudyGroupsActivity.this, StudentDashboardActivity.class);
//                    startActivity(intent);
//                    return true;
//                } else if (itemId == R.id.nav_dining) {
//                    Intent intent = new Intent(StudyGroupsActivity.this, DiningHallActivity.class);
//                    startActivity(intent);
//                    return true;
//                } else if (itemId == R.id.nav_buses) {
//                    Intent intent = new Intent(StudyGroupsActivity.this, BusActivity.class);
//                    startActivity(intent);
//                    return true;
//                }
//                return false;
//            });
 //       }

        // Load data
        loadStudyTables();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to activity
        loadStudyTables();
    }

    private void loadStudyTables() {
        // Don't clear existing data until we have new data
        List<StudyGroup> newStudyGroups = new ArrayList<>();

        // Show loading state
        ProgressBar loadingIndicator = findViewById(R.id.loading_indicator);
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        // First get my tables
        studyGroupService.getMyStudyGroups(new StudyGroupService.StudyTablesCallback() {
            @Override
            public void onSuccess(List<StudyGroup> myTables) {
                Log.d(TAG, "My tables loaded: " + myTables.size());
                newStudyGroups.addAll(myTables);

                // Then get joined tables
                studyGroupService.getJoinedStudyGroups(new StudyGroupService.StudyTablesCallback() {
                    @Override
                    public void onSuccess(List<StudyGroup> joinedTables) {
                        Log.d(TAG, "Joined tables loaded: " + joinedTables.size());
                        newStudyGroups.addAll(joinedTables);

                        // Now update our main list all at once
                        studyGroups.clear();
                        studyGroups.addAll(newStudyGroups);

                        // Hide loading indicator
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisibility(View.GONE);
                        }
                        updateUI();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error getting joined tables: " + error);

                        // Still update with what we have
                        studyGroups.clear();
                        studyGroups.addAll(newStudyGroups);

                        // Hide loading indicator
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisibility(View.GONE);
                        }
                        updateUI();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error getting my tables: " + error);

                // Try to get at least joined tables
                studyGroupService.getJoinedStudyGroups(new StudyGroupService.StudyTablesCallback() {
                    @Override
                    public void onSuccess(List<StudyGroup> joinedTables) {
                        Log.d(TAG, "Only joined tables loaded: " + joinedTables.size());
                        studyGroups.clear();
                        studyGroups.addAll(joinedTables);

                        // Hide loading indicator
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisibility(View.GONE);
                        }
                        updateUI();
                    }

                    @Override
                    public void onError(String joinedError) {
                        Log.e(TAG, "Error getting joined tables: " + joinedError);

                        // Hide loading indicator
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisibility(View.GONE);
                        }
                        showEmptyState(true);
                        Toast.makeText(StudyGroupsActivity.this,
                                "Could not load study groups",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateUI() {
        runOnUiThread(() -> {
            if (studyGroups.isEmpty()) {
                showEmptyState(true);
            } else {
                showEmptyState(false);
                studyGroupsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            studyGroupsRecyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            studyGroupsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void showCreateTableDialog() {
        // Get friends to invite
        friendService.getFriends(currentUserId, new FriendService.FriendsCallback() {
            @Override
            public void onSuccess(List<Friend> friends) {
                if (friends.isEmpty()) {
                    Toast.makeText(StudyGroupsActivity.this,
                            "You need to have friends to create a study group",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create dialog with friend selection
                String[] friendNames = new String[friends.size()];
                boolean[] checkedItems = new boolean[friends.size()];
                long[] friendIds = new long[friends.size()];

                for (int i = 0; i < friends.size(); i++) {
                    Friend friend = friends.get(i);
                    friendNames[i] = friend.getName();
                    checkedItems[i] = false;
                    friendIds[i] = friend.getId();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(StudyGroupsActivity.this);
                builder.setTitle("Select Friends for Study Group (Max 3)")
                        .setMultiChoiceItems(friendNames, checkedItems, (dialog, which, isChecked) -> {
                            checkedItems[which] = isChecked;

                            // Check if more than 3 are selected
                            int selectedCount = 0;
                            for (boolean checked : checkedItems) {
                                if (checked) selectedCount++;
                            }

                            if (selectedCount > 3) {
                                checkedItems[which] = false;
                                ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                                Toast.makeText(StudyGroupsActivity.this,
                                        "You can only invite up to 3 friends",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton("Create Group", (dialog, which) -> {
                            List<Long> selectedFriendIds = new ArrayList<>();
                            for (int i = 0; i < checkedItems.length; i++) {
                                if (checkedItems[i]) {
                                    selectedFriendIds.add(friendIds[i]);
                                }
                            }

                            if (selectedFriendIds.isEmpty()) {
                                Toast.makeText(StudyGroupsActivity.this,
                                        "Please select at least one friend",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            createStudyTable(selectedFriendIds);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(StudyGroupsActivity.this,
                        "Error loading friends: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createStudyTable(List<Long> friendIds) {
        studyGroupService.createStudyGroup(friendIds, new StudyGroupService.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(StudyGroupsActivity.this,
                        message,
                        Toast.LENGTH_SHORT).show();
                // Add a delay to give server time to process
                new Handler().postDelayed(() -> {
                    Log.d(TAG, "Refreshing study groups after creation");
                    loadStudyTables(); // Refresh the list
                }, 1000); // 1 second delay
            }

            @Override
            public void onError(String error) {
                Toast.makeText(StudyGroupsActivity.this,
                        "Error creating study group: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}