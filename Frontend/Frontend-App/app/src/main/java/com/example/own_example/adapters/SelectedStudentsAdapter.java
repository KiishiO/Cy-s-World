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
import com.example.own_example.models.Student;

import java.util.List;

/**
 * Adapter for displaying selected students in the class edit dialog
 */
public class SelectedStudentsAdapter extends RecyclerView.Adapter<SelectedStudentsAdapter.StudentViewHolder> {
    private static final String TAG = "SelectedStudentsAdapter";
    private List<Student> students;
    private Context context;
    private OnStudentRemovedListener listener;

    /**
     * Interface for handling student removal
     */
    public interface OnStudentRemovedListener {
        void onStudentRemoved(Student student, int position);
    }

    public SelectedStudentsAdapter(Context context, List<Student> students, OnStudentRemovedListener listener) {
        this.context = context;
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);

        holder.studentName.setText(student.getName());

        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStudentRemoved(student, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students != null ? students.size() : 0;
    }

    /**
     * Add a student to the list
     */
    public void addStudent(Student student) {
        // Check if student already exists in the list
        for (Student existingStudent : students) {
            if (existingStudent.getId() == student.getId()) {
                return; // Student already in the list
            }
        }

        students.add(student);
        notifyItemInserted(students.size() - 1);
    }

    /**
     * Remove a student at the specified position
     */
    public void removeStudent(int position) {
        if (position >= 0 && position < students.size()) {
            students.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Get the current list of students
     */
    public List<Student> getStudents() {
        return students;
    }

    /**
     * ViewHolder for student items
     */
    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName;
        ImageButton removeButton;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.student_name);
            removeButton = itemView.findViewById(R.id.remove_student_button);
        }
    }
}