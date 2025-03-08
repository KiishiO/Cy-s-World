package com.example.own_example;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.StudyGroupAdapter;
import com.example.own_example.ApiService;
import com.example.own_example.StudyGroup;
import com.example.own_example.utils.UserSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class StudyGroupsActivity extends AppCompatActivity implements StudyGroupAdapter.OnStudyGroupClickListener {

    private RecyclerView studyGroupsRecycler;
    private LinearLayout emptyStateView;
    private MaterialButton createGroupButton;
    private BottomNavigationView bottomNavigation;

    private StudyGroupAdapter adapter;
    private List<StudyGroup> studyGroups;
    private ApiService apiService;
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_group);

        initViews();
        setupRecyclerView();
        setupListeners();

        apiService = ApiService.getInstance(this);
        userSession = UserSession.getInstance(this);

        // Load study groups when activity is created
        loadStudyGroups();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when coming back to this activity
        loadStudyGroups();
    }

    private void initViews() {
        studyGroupsRecycler = findViewById(R.id.study_groups_recycler);
        emptyStateView = findViewById(R.id.empty_state_view);
        createGroupButton = findViewById(R.id.create_group_button);
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupRecyclerView() {
        studyGroups = new ArrayList<>();
        adapter = new StudyGroupAdapter(this, studyGroups, this);
        studyGroupsRecycler.setLayoutManager(new LinearLayoutManager(this));
        studyGroupsRecycler.setAdapter(adapter);
    }

    private void setupListeners() {
        createGroupButton.setOnClickListener(v -> showCreateStudyGroupDialog());

        // Setup bottom navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            // Handle navigation menu item clicks
            // For example:
            // if (item.getItemId() == R.id.nav_home) {
            //     startActivity(new Intent(this, HomeActivity.class));
            //     return true;
            // }
            return true;
        });
    }

    private void loadStudyGroups() {
        String userId = userSession.getCurrentUserId();
        if (userId == null) {
            // User not logged in
            Toast.makeText(this, "Please log in to view your study groups", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getUserStudyGroups(userId, new ApiService.StudyGroupsCallback() {
            @Override
            public void onSuccess(List<StudyGroup> fetchedStudyGroups) {
                studyGroups.clear();
                studyGroups.addAll(fetchedStudyGroups);
                adapter.notifyDataSetChanged();

                // Show/hide empty state
                updateEmptyState();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(StudyGroupsActivity.this,
                        "Error loading study groups: " + error, Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (studyGroups.isEmpty()) {
            studyGroupsRecycler.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            studyGroupsRecycler.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void showCreateStudyGroupDialog() {
        StudyGroupDialogFragment dialogFragment = StudyGroupDialogFragment.newInstance(null);
        dialogFragment.setStudyGroupListener(new StudyGroupDialogFragment.StudyGroupDialogListener() {
            @Override
            public void onStudyGroupCreated(StudyGroup studyGroup) {
                // Add the new study group to our list
                studyGroups.add(studyGroup);
                adapter.notifyItemInserted(studyGroups.size() - 1);
                updateEmptyState();
                Toast.makeText(StudyGroupsActivity.this,
                        "Study group created successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStudyGroupUpdated(StudyGroup studyGroup, int position) {
                // Not used for create operation
            }
        });
        dialogFragment.show(getSupportFragmentManager(), "CreateStudyGroup");
    }

    @Override
    public void onManageGroupClick(StudyGroup studyGroup, int position) {
        StudyGroupDialogFragment dialogFragment = StudyGroupDialogFragment.newInstance(studyGroup);
        dialogFragment.setStudyGroupListener(new StudyGroupDialogFragment.StudyGroupDialogListener() {
            @Override
            public void onStudyGroupCreated(StudyGroup studyGroup) {
                // Not used for manage operation
            }

            @Override
            public void onStudyGroupUpdated(StudyGroup updatedGroup, int position) {
                // Update the study group in our list
                studyGroups.set(position, updatedGroup);
                adapter.notifyItemChanged(position);
                Toast.makeText(StudyGroupsActivity.this,
                        "Study group updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
        dialogFragment.setPosition(position);
        dialogFragment.show(getSupportFragmentManager(), "ManageStudyGroup");
    }
}