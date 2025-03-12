package com.example.own_example;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.StudyGroupMemberAdapter;
import com.example.own_example.models.StudyGroup;
import com.example.own_example.models.StudyGroupMember;
import com.example.own_example.services.StudyGroupService;
import com.example.own_example.utils.UserSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class StudyGroupDialogFragment extends DialogFragment implements StudyGroupMemberAdapter.OnMemberActionListener {

    private static final String ARG_STUDY_GROUP = "study_group";

    private StudyGroup studyGroup;
    private int position = -1;
    private boolean isEditMode = false;

    private TextInputLayout groupNameLayout;
    private TextInputEditText groupNameInput;
    private TextInputLayout memberEmailLayout;
    private TextInputEditText memberEmailInput;
    private RecyclerView membersRecyclerView;
    private View currentMembersTitle;
    private MaterialButton addMemberButton;
    private MaterialButton deleteButton;
    private MaterialButton cancelButton;
    private MaterialButton saveButton;

    private StudyGroupService studyGroupService;
    private UserSession userSession;
    private StudyGroupMemberAdapter memberAdapter;
    private StudyGroupDialogListener listener;

    public interface StudyGroupDialogListener {
        void onStudyGroupCreated(StudyGroup studyGroup);
        void onStudyGroupUpdated(StudyGroup studyGroup, int position);
    }

    public static StudyGroupDialogFragment newInstance(StudyGroup studyGroup) {
        StudyGroupDialogFragment fragment = new StudyGroupDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STUDY_GROUP, studyGroup);
        fragment.setArguments(args);
        return fragment;
    }

    public void setStudyGroupListener(StudyGroupDialogListener listener) {
        this.listener = listener;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);

        if (getArguments() != null) {
            studyGroup = (StudyGroup) getArguments().getSerializable(ARG_STUDY_GROUP);
            isEditMode = studyGroup != null;
        }

        if (studyGroup == null) {
            studyGroup = new StudyGroup();
        }

        studyGroupService = StudyGroupService.getInstance(requireContext());
        userSession = UserSession.getInstance(requireContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_study_group, container, false);
        initViews(view);
        setupRecyclerView();
        setupListeners();

        if (isEditMode) {
            populateFields();
        } else {
            // New group - hide delete button and member management initially
            deleteButton.setVisibility(View.GONE);
            currentMembersTitle.setVisibility(View.GONE);
            membersRecyclerView.setVisibility(View.GONE);
        }

        return view;
    }

    private void initViews(View view) {
        groupNameLayout = view.findViewById(R.id.studyGroup_name_layout);
        groupNameInput = view.findViewById(R.id.studyGroup_name_input);
        memberEmailLayout = view.findViewById(R.id.groupMember_email_layout);
        memberEmailInput = view.findViewById(R.id.groupMember_email_input);
        membersRecyclerView = view.findViewById(R.id.members_recycler_view);
        currentMembersTitle = view.findViewById(R.id.current_groupMembers_title);
        addMemberButton = view.findViewById(R.id.add_groupMember_button);
        deleteButton = view.findViewById(R.id.delete_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        saveButton = view.findViewById(R.id.saveGroup_button);
    }

    private void setupRecyclerView() {
        if (studyGroup.getMembers() == null) {
            studyGroup.setMembers(new ArrayList<>());
        }

        memberAdapter = new StudyGroupMemberAdapter(requireContext(), studyGroup.getMembers(), this);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        membersRecyclerView.setAdapter(memberAdapter);
    }

    private void setupListeners() {
        // Add member button
        addMemberButton.setOnClickListener(v -> {
            String email = memberEmailInput.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                memberEmailLayout.setError("Email is required");
                return;
            }

            if (!isValidEmail(email)) {
                memberEmailLayout.setError("Please enter a valid email");
                return;
            }

            if (isEmailAlreadyAdded(email)) {
                memberEmailLayout.setError("This email is already added to the group");
                return;
            }

            memberEmailLayout.setError(null);

            if (isEditMode) {
                // In edit mode, add via API
                addMemberToExistingGroup(email);
            } else {
                // In create mode, add to local list
                addMemberLocally(email);
            }

            // Clear the input field
            memberEmailInput.setText("");
        });

        // Save button
        saveButton.setOnClickListener(v -> {
            if (validateFields()) {
                saveStudyGroup();
            }
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> dismiss());

        // Delete button
        deleteButton.setOnClickListener(v -> confirmDeleteStudyGroup());
    }

    private void populateFields() {
        // Set the title to indicate edit mode
        groupNameInput.setText(studyGroup.getName());

        // Show the members section
        if (studyGroup.getMembers() != null && !studyGroup.getMembers().isEmpty()) {
            currentMembersTitle.setVisibility(View.VISIBLE);
            membersRecyclerView.setVisibility(View.VISIBLE);
        } else {
            currentMembersTitle.setVisibility(View.GONE);
            membersRecyclerView.setVisibility(View.GONE);
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validate group name
        String groupName = groupNameInput.getText().toString().trim();
        if (TextUtils.isEmpty(groupName)) {
            groupNameLayout.setError("Group name is required");
            isValid = false;
        } else {
            groupNameLayout.setError(null);
        }

        return isValid;
    }

    private void saveStudyGroup() {
        // Update study group with values from form
        studyGroup.setName(groupNameInput.getText().toString().trim());

        if (isEditMode) {
            // Update existing group
            studyGroupService.updateStudyGroup(studyGroup, new StudyGroupService.StudyGroupCallback() {
                @Override
                public void onSuccess(StudyGroup updatedGroup) {
                    if (listener != null) {
                        listener.onStudyGroupUpdated(updatedGroup, position);
                    }
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error updating study group: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new group
            String userId = userSession.getCurrentUserId();
            if (userId == null) {
                Toast.makeText(getContext(), "You must be logged in to create a study group",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            studyGroupService.createStudyGroup(userId, studyGroup, new StudyGroupService.StudyGroupCallback() {
                @Override
                public void onSuccess(StudyGroup createdGroup) {
                    if (listener != null) {
                        listener.onStudyGroupCreated(createdGroup);
                    }
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error creating study group: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void confirmDeleteStudyGroup() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Study Group")
                .setMessage("Are you sure you want to delete this study group? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteStudyGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteStudyGroup() {
        studyGroupService.deleteStudyGroup(studyGroup.getId(), new StudyGroupService.SuccessCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Study group deleted successfully",
                        Toast.LENGTH_SHORT).show();
                dismiss();
                // Refresh the activity to update the list
                if (getActivity() instanceof StudyGroupsActivity) {
                    ((StudyGroupsActivity) getActivity()).onResume();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error deleting study group: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMemberToExistingGroup(String email) {
        studyGroupService.addStudyGroupMember(studyGroup.getId(), email, new StudyGroupService.StudyGroupCallback() {
            @Override
            public void onSuccess(StudyGroup updatedGroup) {
                studyGroup = updatedGroup;
                memberAdapter.updateData(studyGroup.getMembers());

                // Show the members section if it was hidden
                if (currentMembersTitle.getVisibility() == View.GONE) {
                    currentMembersTitle.setVisibility(View.VISIBLE);
                    membersRecyclerView.setVisibility(View.VISIBLE);
                }

                Toast.makeText(getContext(), "Member invited successfully",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error adding member: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMemberLocally(String email) {
        // Create a temporary member (the actual ID will be assigned by the server)
        String tempId = "temp_" + System.currentTimeMillis();
        StudyGroupMember member = new StudyGroupMember(tempId, email, email);
        member.setActive(false);  // Set as pending until the user accepts the invitation

        studyGroup.addMember(member);
        memberAdapter.notifyDataSetChanged();

        // Show the members section if it was hidden
        if (currentMembersTitle.getVisibility() == View.GONE) {
            currentMembersTitle.setVisibility(View.VISIBLE);
            membersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRemoveMember(StudyGroupMember member, int position) {
        if (isEditMode) {
            // Remove via API
            studyGroupService.removeStudyGroupMember(studyGroup.getId(), member.getId(),
                    new StudyGroupService.StudyGroupCallback() {
                        @Override
                        public void onSuccess(StudyGroup updatedGroup) {
                            studyGroup = updatedGroup;
                            memberAdapter.updateData(studyGroup.getMembers());

                            // Hide the members section if empty
                            if (studyGroup.getMembers().isEmpty()) {
                                currentMembersTitle.setVisibility(View.GONE);
                                membersRecyclerView.setVisibility(View.GONE);
                            }

                            Toast.makeText(getContext(), "Member removed successfully",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Error removing member: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Remove locally
            studyGroup.removeMember(member.getId());
            memberAdapter.notifyDataSetChanged();

            // Hide the members section if empty
            if (studyGroup.getMembers().isEmpty()) {
                currentMembersTitle.setVisibility(View.GONE);
                membersRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    private boolean isValidEmail(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isEmailAlreadyAdded(String email) {
        for (StudyGroupMember member : studyGroup.getMembers()) {
            if (member.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Make the dialog wider
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}