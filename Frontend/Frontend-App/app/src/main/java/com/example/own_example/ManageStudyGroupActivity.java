package com.example.own_example;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.StudyGroupMemberAdapter;
import com.example.own_example.models.StudyGroup;
import com.example.own_example.models.StudyGroupMember;
import com.example.own_example.services.StudyGroupService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ManageStudyGroupActivity extends AppCompatActivity {

    private static final String TAG = "ManageStudyGroupActivity";

    // UI Components
    private TextInputEditText groupNameInput;
    private TextInputEditText memberEmailInput;
    private MaterialButton addMemberButton;
    private MaterialButton saveGroupButton;
    private MaterialButton deleteButton;
    private MaterialButton cancelButton;
    private RecyclerView membersRecyclerView;

    // Adapters and Services
    private StudyGroupMemberAdapter membersAdapter;
    private StudyGroupService studyGroupService;

    // Data
    private long currentUserId;
    private long studyGroupId;
    private StudyGroup currentStudyGroup;
    private List<StudyGroupMember> groupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_study_group);

        // Initialize services and get current user
        studyGroupService = new StudyGroupService(this);
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUserId = prefs.getLong("user_id", 0);

        // Get study group ID from intent
        studyGroupId = getIntent().getLongExtra("study_group_id", -1);
        if (studyGroupId == -1) {
            Toast.makeText(this, "Invalid Study Group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        initializeViews();

        // Load study group details
        loadStudyGroupDetails();
    }

    private void initializeViews() {
        // Input fields
        groupNameInput = findViewById(R.id.studyGroup_name_input);
        memberEmailInput = findViewById(R.id.groupMember_email_input);

        // Buttons
        addMemberButton = findViewById(R.id.add_groupMember_button);
        saveGroupButton = findViewById(R.id.saveGroup_button);
        deleteButton = findViewById(R.id.delete_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Members RecyclerView
        membersRecyclerView = findViewById(R.id.members_recycler_view);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up button listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Add Member Button
        addMemberButton.setOnClickListener(v -> {
            String memberEmail = memberEmailInput.getText().toString().trim();
            if (!TextUtils.isEmpty(memberEmail)) {
                addMemberToGroup(memberEmail);
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            }
        });

        // Save Group Button
        saveGroupButton.setOnClickListener(v -> {
            String newGroupName = groupNameInput.getText().toString().trim();
            if (!TextUtils.isEmpty(newGroupName)) {
                updateStudyGroup(newGroupName);
            } else {
                Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Group Button
        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        // Cancel Button
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadStudyGroupDetails() {
        studyGroupService.getStudyGroupDetails(studyGroupId, new StudyGroupService.StudyGroupDetailsCallback() {
            @Override
            public void onSuccess(StudyGroup studyGroup, List<StudyGroupMember> members) {
                currentStudyGroup = studyGroup;
                groupMembers = members;

                // Update UI
                runOnUiThread(() -> {
                    groupNameInput.setText(studyGroup.getName());

                    // Set up members recycler view
                    membersAdapter = new StudyGroupMemberAdapter(groupMembers,
                            new StudyGroupMemberAdapter.OnMemberActionListener() {
                                @Override
                                public void onRemoveMember(StudyGroupMember member) {
                                    removeMemberFromGroup(member);
                                }
                            }
                    );
                    membersRecyclerView.setAdapter(membersAdapter);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading study group details: " + error);
                Toast.makeText(ManageStudyGroupActivity.this,
                        "Failed to load study group details",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void addMemberToGroup(String memberEmail) {
        studyGroupService.addMemberToStudyGroup(studyGroupId, memberEmail,
                new StudyGroupService.ActionCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageStudyGroupActivity.this,
                                    message,
                                    Toast.LENGTH_SHORT).show();
                            memberEmailInput.setText(""); // Clear input
                            loadStudyGroupDetails(); // Refresh members list
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageStudyGroupActivity.this,
                                    "Error adding member: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void removeMemberFromGroup(StudyGroupMember member) {
        studyGroupService.removeMemberFromStudyGroup(studyGroupId, member.getId(),
                new StudyGroupService.ActionCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageStudyGroupActivity.this,
                                    message,
                                    Toast.LENGTH_SHORT).show();
                            loadStudyGroupDetails(); // Refresh members list
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageStudyGroupActivity.this,
                                    "Error removing member: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void updateStudyGroup(String newGroupName) {
        studyGroupService.updateStudyGroup(studyGroupId, newGroupName,
                new StudyGroupService.ActionCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageStudyGroupActivity.this,
                                    message,
                                    Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after successful update
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageStudyGroupActivity.this,
                                    "Error updating group: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void showDeleteConfirmationDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Study Group")
                .setMessage("Are you sure you want to delete this study group? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteStudyGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteStudyGroup() {
        studyGroupService.deleteStudyGroup(studyGroupId,
                new StudyGroupService.ActionCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageStudyGroupActivity.this,
                                    message,
                                    Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after successful deletion
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageStudyGroupActivity.this,
                                    "Error deleting group: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }
}