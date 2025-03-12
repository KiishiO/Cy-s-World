package com.example.own_example.adapters;

import android.content.Context;
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

    private Context context;
    private List<StudyGroupMember> members;
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onRemoveMember(StudyGroupMember member, int position);
    }

    public StudyGroupMemberAdapter(Context context, List<StudyGroupMember> members, OnMemberActionListener listener) {
        this.context = context;
        this.members = members;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_study_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        StudyGroupMember member = members.get(position);

        holder.memberName.setText(member.getName());
        holder.memberStatus.setText(member.getStatus());

        holder.removeMemberButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveMember(member, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return members != null ? members.size() : 0;
    }

    public void updateData(List<StudyGroupMember> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView memberName;
        TextView memberStatus;
        ImageButton removeMemberButton;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.member_name);
            memberStatus = itemView.findViewById(R.id.member_status);
            removeMemberButton = itemView.findViewById(R.id.remove_member_button);
        }
    }
}