package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.models.ClassModel;
import com.example.own_example.R;

import java.util.List;

/**
 * Adapter for displaying classes in the admin view
 */
public class AdminClassesAdapter extends RecyclerView.Adapter<AdminClassesAdapter.ClassViewHolder> {
    private static final String TAG = "AdminClassesAdapter";
    private List<ClassModel> classModels;
    private Context context;
    private OnClassActionListener listener;

    /**
     * Interface for handling class actions
     */
    public interface OnClassActionListener {
        void onEditClass(ClassModel classModel, int position);
        void onDeleteClass(int classId, int position);
    }

    public AdminClassesAdapter(Context context, List<ClassModel> classModels, OnClassActionListener listener) {
        this.context = context;
        this.classModels = classModels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModel classModelItem = classModels.get(position);

        // Set class information
        holder.className.setText(classModelItem.getClassName());
        holder.classTeacher.setText("Teacher: " + classModelItem.getTeacherName());
        holder.classSchedule.setText("Schedule: " + classModelItem.getFormattedSchedule());
        holder.classLocation.setText("Location: " + classModelItem.getLocation());

        // Set student count
        int studentCount = classModelItem.getStudentIds() != null ? classModelItem.getStudentIds().size() : 0;
        holder.studentsCount.setText("Students: " + studentCount);

        // Set click listeners for action buttons
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClass(classModelItem, position);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClass(classModelItem.getId(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return classModels != null ? classModels.size() : 0;
    }

    /**
     * Update the adapter's data
     * @param newClassModels The new list of classes
     */
    public void updateData(List<ClassModel> newClassModels) {
        this.classModels = newClassModels;
        notifyDataSetChanged();
    }

    /**
     * Remove a class at the specified position
     */
    public void removeItem(int position) {
        if (position >= 0 && position < classModels.size()) {
            classModels.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * ViewHolder for class items
     */
    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView className;
        TextView classTeacher;
        TextView classSchedule;
        TextView classLocation;
        TextView studentsCount;
        ImageButton editButton;
        ImageButton deleteButton;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            className = itemView.findViewById(R.id.class_name);
            classTeacher = itemView.findViewById(R.id.class_teacher);
            classSchedule = itemView.findViewById(R.id.class_schedule);
            classLocation = itemView.findViewById(R.id.class_location);
            studentsCount = itemView.findViewById(R.id.students_count);
            editButton = itemView.findViewById(R.id.edit_class_button);
            deleteButton = itemView.findViewById(R.id.delete_class_button);
        }
    }
}