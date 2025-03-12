package com.example.own_example.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.StudyGroup;
import com.example.own_example.models.StudyGroupMember;
import com.example.own_example.services.StudyGroupService;

import java.util.List;

public class StudyGroupAdapter extends RecyclerView.Adapter<StudyGroupAdapter.ViewHolder> {

    private List<StudyGroup> studyGroup;
    private Context context;
    private long currentUserId;

    public StudyGroupAdapter(List<StudyGroup> studyGroup, Context context) {
        this.studyGroup = studyGroup;
        this.context = context;

        // Get current user ID
        this.currentUserId = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                .getLong("user_id", 0);

        if (this.currentUserId == 0) {
            this.currentUserId = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .getLong("user_id", 1);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyGroup group = studyGroup.get(position);

        // Set name based on whether current user is leader
        if (group.getLeaderId() == currentUserId) {
            holder.groupName.setText("Your Study Group");
        } else {
            holder.groupName.setText(group.getLeaderName() + "'s Study Group");
        }

        // Set member count
        holder.memberCount.setText(group.getMemberCountText());

        // Set up manage button click
        holder.manageButton.setOnClickListener(v -> {
            showManageOptions(group, position);
        });
    }

    @Override
    public int getItemCount() {
        return studyGroup != null ? studyGroup.size() : 0;
    }

    private void showManageOptions(StudyGroup group, int position) {
        boolean isLeader = group.getLeaderId() == currentUserId;
        String[] options;

        if (isLeader) {
            // Leader options
            options = new String[]{"View Details", "Delete Group"};
        } else {
            // Member options
            if ("PENDING".equals(group.getStatus())) {
                options = new String[]{"View Details", "Accept Invitation", "Decline Invitation"};
            } else {
                options = new String[]{"View Details", "Leave Group"};
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Manage Study Group")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // View Details
                            showTableDetails(group);
                            break;
                        case 1: // Delete Table or Accept/Leave
                            if (isLeader) {
                                deleteTable(group, position);
                            } else if ("PENDING".equals(group.getStatus())) {
                                respondToInvitation(group, position, true);
                            } else {
                                leaveTable(group, position);
                            }
                            break;
                        case 2: // Decline Invitation (only for pending members)
                            respondToInvitation(group, position, false);
                            break;
                    }
                })
                .show();
    }

    private void showTableDetails(StudyGroup group) {
        StringBuilder membersText = new StringBuilder();
        membersText.append("Leader: ").append(group.getLeaderName()).append("\n\n");
        membersText.append("Members:\n");

        List<StudyGroupMember> members = group.getMembers();
        if (members != null && !members.isEmpty()) {
            for (int i = 0; i < members.size(); i++) {
                StudyGroupMember member = members.get(i);
                membersText.append("- ").append(member.getName());
                if (i < members.size() - 1) {
                    membersText.append("\n");
                }
            }
        } else {
            membersText.append("No members yet");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Study Group Details")
                .setMessage(membersText.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void deleteTable(StudyGroup table, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Study Group")
                .setMessage("Are you sure you want to delete this study group?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    StudyGroupService service = new StudyGroupService(context);
                    service.deleteStudyGroup(table.getId(), new StudyGroupService.ActionCallback() {
                        @Override
                        public void onSuccess(String message) {
                            studyGroup.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, studyGroup.size());
                            Toast.makeText(context, "Study group deleted", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void respondToInvitation(StudyGroup table, int position, boolean accept) {
        StudyGroupService service = new StudyGroupService(context);
        service.respondToStudyGroup(table.getId(), accept, new StudyGroupService.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                if (accept) {
                    table.setStatus("ACCEPTED");
                    notifyItemChanged(position);
                } else {
                    studyGroup.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, studyGroup.size());
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveTable(StudyGroup table, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Leave Study Group")
                .setMessage("Are you sure you want to leave this study group?")
                .setPositiveButton("Leave", (dialog, which) -> {
                    StudyGroupService service = new StudyGroupService(context);
                    service.respondToStudyGroup(table.getId(), false, new StudyGroupService.ActionCallback() {
                        @Override
                        public void onSuccess(String message) {
                            studyGroup.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, studyGroup.size());
                            Toast.makeText(context, "Left study group", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        TextView memberCount;
        ImageButton manageButton;

        ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.study_group_name);
            memberCount = itemView.findViewById(R.id.member_count);
            manageButton = itemView.findViewById(R.id.manage_group_button);
        }
    }
}
