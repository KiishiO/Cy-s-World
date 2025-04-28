package com.example.own_example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.StudyGroupMember;

import java.util.List;

public class StudyGroupMemberAdapter extends RecyclerView.Adapter<StudyGroupMemberAdapter.MemberViewHolder> {

    private List<StudyGroupMember> members;
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onRemoveMember(StudyGroupMember member);
    }

    public StudyGroupMemberAdapter(List<StudyGroupMember> members, OnMemberActionListener listener) {
        this.members = members;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        StudyGroupMember member = members.get(position);

        // Set member name and email/status
        holder.memberNameText.setText(member.getName());
        holder.memberEmailText.setText(member.getEmail());

        // Remove button functionality
        holder.removeMemberButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveMember(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView memberNameText;
        TextView memberEmailText;
        ImageButton removeMemberButton;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);

            memberNameText = itemView.findViewById(R.id.member_name);
            memberEmailText = itemView.findViewById(R.id.member_email);
            removeMemberButton = itemView.findViewById(R.id.remove_member_button);
        }
    }
}