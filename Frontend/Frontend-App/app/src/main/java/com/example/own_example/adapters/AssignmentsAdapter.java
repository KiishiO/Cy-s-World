package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.AssignmentModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for displaying assignments in a RecyclerView
 */
public class AssignmentsAdapter extends RecyclerView.Adapter<AssignmentsAdapter.AssignmentViewHolder> {
    private static final String TAG = "AssignmentsAdapter";
    private List<AssignmentModel> assignments;
    private Context context;
    private boolean isTeacherView;
    private OnAssignmentClickListener listener;

    /**
     * Interface for assignment click events
     */
    public interface OnAssignmentClickListener {
        void onAssignmentClick(AssignmentModel assignment, int position);
    }

    public AssignmentsAdapter(Context context, List<AssignmentModel> assignments,
                              boolean isTeacherView, OnAssignmentClickListener listener) {
        this.context = context;
        this.assignments = assignments;
        this.isTeacherView = isTeacherView;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        AssignmentModel assignment = assignments.get(position);

        // Set assignment details
        holder.assignmentName.setText(assignment.getAssignmentName());

        // Set appropriate info based on view type (teacher or student)
        if (isTeacherView) {
            holder.assignmentDetails.setText(assignment.getStudentName());
        } else {
            // For student view, display the weight
            holder.assignmentDetails.setText("Weight: " + assignment.getFormattedWeight());
        }

        // Set grade information
        holder.assignmentGrade.setText(assignment.getFormattedGrade());

        // Set date information
        if (assignment.isGraded()) {
            holder.assignmentDate.setText("Graded: " + assignment.getFormattedGradedDate());
            holder.assignmentGrade.setTextColor(ContextCompat.getColor(context, R.color.colorGraded));
        } else {
            holder.assignmentDate.setText("Due: " + assignment.getFormattedSubmissionDate());
            holder.assignmentGrade.setTextColor(ContextCompat.getColor(context, R.color.colorPending));
        }

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignmentClick(assignment, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assignments != null ? assignments.size() : 0;
    }

    /**
     * Update the adapter's data
     * @param newAssignments The new list of assignments
     */
    public void updateData(List<AssignmentModel> newAssignments) {
        this.assignments = newAssignments;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for assignment items
     */
    public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView assignmentName;
        TextView assignmentDetails;
        TextView assignmentGrade;
        TextView assignmentDate;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            assignmentName = itemView.findViewById(R.id.assignment_name);
            assignmentDetails = itemView.findViewById(R.id.assignment_details);
            assignmentGrade = itemView.findViewById(R.id.assignment_grade);
            assignmentDate = itemView.findViewById(R.id.assignment_date);
        }
    }