package com.example.own_example.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.ClassDetailActivity;
import com.example.own_example.R;
import com.example.own_example.models.ClassModel;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for displaying classes in a RecyclerView
 */
public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.ClassViewHolder> {
    private static final String TAG = "ClassesAdapter";
    private List<ClassModel> classModels;
    private Context context;

    public ClassesAdapter(Context context, List<ClassModel> classModels) {
        this.context = context;
        this.classModels = classModels;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
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

        // Set click listener for the card
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ClassDetailActivity.class);
                intent.putExtra("CLASS_ID", classModelItem.getId());
                context.startActivity(intent);
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
     * ViewHolder for class items
     */
    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView className;
        TextView classTeacher;
        TextView classSchedule;
        TextView classLocation;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            className = itemView.findViewById(R.id.class_name);
            classTeacher = itemView.findViewById(R.id.class_teacher);
            classSchedule = itemView.findViewById(R.id.class_schedule);
            classLocation = itemView.findViewById(R.id.class_location);
        }
    }
}