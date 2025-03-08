package com.example.own_example;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.StudyGroup;

import java.util.List;

public class StudyGroupAdapter extends RecyclerView.Adapter<StudyGroupAdapter.StudyGroupViewHolder> {

    private Context context;
    private List<StudyGroup> studyGroups;
    private OnStudyGroupClickListener listener;

    public interface OnStudyGroupClickListener {
        void onManageGroupClick(StudyGroup studyGroup, int position);
    }

    public StudyGroupAdapter(Context context, List<StudyGroup> studyGroups, OnStudyGroupClickListener listener) {
        this.context = context;
        this.studyGroups = studyGroups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudyGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_study_group, parent, false);
        return new StudyGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudyGroupViewHolder holder, int position) {
        StudyGroup studyGroup = studyGroups.get(position);

        holder.studyGroupName.setText(studyGroup.getName());

        String memberCountText = studyGroup.getMemberCount() + "/" +
                studyGroup.getMaxMembers() + " Members";
        holder.memberCount.setText(memberCountText);

        holder.manageGroupButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onManageGroupClick(studyGroup, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return studyGroups != null ? studyGroups.size() : 0;
    }

    public void updateData(List<StudyGroup> newStudyGroups) {
        this.studyGroups = newStudyGroups;
        notifyDataSetChanged();
    }

    public class StudyGroupViewHolder extends RecyclerView.ViewHolder {
        ImageView studyGroupIcon;
        TextView studyGroupName;
        TextView memberCount;
        ImageButton manageGroupButton;

        public StudyGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            studyGroupIcon = itemView.findViewById(R.id.study_group_icon);
            studyGroupName = itemView.findViewById(R.id.study_group_name);
            memberCount = itemView.findViewById(R.id.member_count);
            manageGroupButton = itemView.findViewById(R.id.manage_group_button);
        }
    }
}