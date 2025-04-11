package com.example.own_example.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.services.ClassesService;
import com.example.own_example.R;
import com.example.own_example.models.Student;

import java.util.List;

/**
 * Adapter for displaying students in a RecyclerView
 */
public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.StudentViewHolder> {
    private static final String TAG = "StudentsAdapter";
    private final List<Student> students;
    private final Context context;
    private final int classId;
    private final OnGradeUpdatedListener listener;

    /**
     * Interface for handling grade update events
     */
    public interface OnGradeUpdatedListener {
        void onGradeUpdated(int studentId, String grade);
    }

    public StudentsAdapter(Context context, List<Student> students, int classId, OnGradeUpdatedListener listener) {
        this.context = context;
        this.students = students;
        this.classId = classId;
        this.listener = listener;
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

        // Set student name
        holder.studentName.setText(student.getName());

        // Set click listener for the grade button
        holder.gradeButton.setOnClickListener(v -> showGradeDialog(student));
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
     * Show the grade entry dialog
     * @param student The student to grade
     */
    private void showGradeDialog(final Student student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_grade_entry, null);
        builder.setView(dialogView);

        // Initialize dialog views
        final TextView studentNameText = dialogView.findViewById(R.id.student_name_text);
        final TextView gradeInput = dialogView.findViewById(R.id.grade_input);
        final TextView numericGradeInput = dialogView.findViewById(R.id.numeric_grade_input);
        final Button saveButton = dialogView.findViewById(R.id.save_grade_button);
        final Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        // Set student name
        studentNameText.setText(student.getName());

        // Get current grade if available
        final String currentGrade = student.getGrade(classId);
        if (!currentGrade.equals("N/A")) {
            gradeInput.setText(currentGrade);
            // If there's a numeric component, set that too
            if (currentGrade.length() > 1 && Character.isDigit(currentGrade.charAt(1))) {
                try {
                    int numericGrade = Integer.parseInt(currentGrade.substring(1));
                    numericGradeInput.setText(String.valueOf(numericGrade));
                } catch (NumberFormatException e) {
                    // Ignore parse errors
                }
            }
        }

        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set button click listeners
        saveButton.setOnClickListener(v -> {
            final String grade = gradeInput.getText().toString().trim().toUpperCase();
            final String numericGrade = numericGradeInput.getText().toString().trim();

            // Validate grade input
            final String finalGrade;
            if (grade.isEmpty()) {
                finalGrade = "N/A";
            } else if (!numericGrade.isEmpty()) {
                // Combine letter and numeric grade if both provided
                finalGrade = grade + numericGrade;
            } else {
                finalGrade = grade;
            }

            // Update the grade
            ClassesService apiService = new ClassesService(context);
            apiService.updateStudentGrade(student.getId(), classId, finalGrade, new ClassesService.ApiCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        // Update the student model
                        student.setGrade(classId, finalGrade);

                        // Notify the listener
                        if (listener != null) {
                            listener.onGradeUpdated(student.getId(), finalGrade);
                        }

                        // Dismiss the dialog
                        dialog.dismiss();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Show error message
                    new AlertDialog.Builder(context)
                            .setTitle("Error")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    /**
     * ViewHolder for student items
     */
    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName;
        Button gradeButton;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.student_name);
            gradeButton = itemView.findViewById(R.id.grade_button);
        }
    }
}