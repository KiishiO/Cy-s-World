package com.example.own_example.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.Student;
import com.example.own_example.services.GradesService;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying students in a RecyclerView with their grades
 */
public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.StudentViewHolder> {
    private static final String TAG = "StudentsAdapter";
    private final List<Student> students;
    private final Context context;
    private final int classId;
    private final OnStudentClickListener listener;
    private final GradesService gradesService;

    /**
     * Interface for handling student click events
     */
    public interface OnStudentClickListener {
        void onStudentClick(Student student, int position);
    }

    public StudentsAdapter(Context context, List<Student> students, int classId, OnStudentClickListener listener) {
        this.context = context;
        this.students = students;
        this.classId = classId;
        this.listener = listener;
        this.gradesService = new GradesService(context);
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        final Student student = students.get(position);

        // Set student name and email
        holder.studentName.setText(student.getName());
        holder.studentEmail.setText(student.getEmail());

        // Load student's overall grade for this class
        loadStudentGrade(student, holder);

        // Set click listener for the entire item
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStudentClick(student, position);
            }
        });
    }

    private void loadStudentGrade(Student student, StudentViewHolder holder) {
        Log.d("StudentsAdapter", "Loading grade for student: " + student.getName() + " with ID: " + student.getId());

        gradesService.getStudentOverallGrade(classId, student.getId(),
                new GradesService.ApiCallback<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        // Add debug log
                        Log.d("StudentsAdapter", "Grade loaded: " + result.toString());
                        // Make sure we're running on the UI thread
                        ((Activity) context).runOnUiThread(() -> {
                            // Check if the result contains an overall grade
                            if (result.containsKey("overallGrade") && result.get("overallGrade") != null) {
                                Double overallGrade = (Double) result.get("overallGrade");
                                // Set the grade text with formatting
                                holder.studentGrade.setText(String.format("%.1f", overallGrade));

                                // Set completed assignments info
                                if (result.containsKey("totalAssignments") && result.containsKey("gradedAssignments")) {
                                    int total = (int) result.get("totalAssignments");
                                    int graded = (int) result.get("gradedAssignments");
                                    holder.completedAssignments.setText(String.format("(%d/%d)", graded, total));
                                    holder.completedAssignments.setVisibility(View.VISIBLE);
                                } else {
                                    holder.completedAssignments.setVisibility(View.GONE);
                                }
                            } else {
                                // No grade available
                                holder.studentGrade.setText("N/A");
                                holder.completedAssignments.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Add debug log
                        Log.e("StudentsAdapter", "Error loading grade: " + errorMessage);
                        // Handle error on UI thread
                        ((Activity) context).runOnUiThread(() -> {
                            holder.studentGrade.setText("N/A");
                            holder.completedAssignments.setVisibility(View.GONE);
                        });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return students != null ? students.size() : 0;
    }

    /**
     * Update the adapter's data
     * @param newStudents The new list of students
     */
    public void updateData(List<Student> newStudents) {
        this.students.clear();
        this.students.addAll(newStudents);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for student items
     */
    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView studentName;
        TextView studentEmail;
        TextView studentGrade;
        TextView completedAssignments;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            studentName = itemView.findViewById(R.id.student_name);
            studentEmail = itemView.findViewById(R.id.student_email);
            studentGrade = itemView.findViewById(R.id.student_overall_grade);
            completedAssignments = itemView.findViewById(R.id.completed_assignments);
        }
    }
}