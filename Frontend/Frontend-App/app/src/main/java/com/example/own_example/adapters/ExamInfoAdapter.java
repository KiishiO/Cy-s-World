package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.ExamInfo;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ExamInfoAdapter extends RecyclerView.Adapter<ExamInfoAdapter.ViewHolder> {

    private List<ExamInfo> exams;
    private Context context;
    private OnExamClickListener listener;
    private boolean isAdminView;

    public interface OnExamClickListener {
        void onExamClick(ExamInfo exam);
        void onEditClick(ExamInfo exam);
        void onDeleteClick(ExamInfo exam);
    }

    public ExamInfoAdapter(Context context, List<ExamInfo> exams, OnExamClickListener listener, boolean isAdminView) {
        this.context = context;
        this.exams = exams;
        this.listener = listener;
        this.isAdminView = isAdminView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exam, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExamInfo exam = exams.get(position);

        holder.examNameTextView.setText(exam.getExamName());
        holder.examDescriptionTextView.setText(exam.getExamDescription());

        // Show/hide admin controls based on isAdminView
        if (isAdminView) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExamClick(exam);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(exam);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(exam);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exams != null ? exams.size() : 0;
    }

    public void updateData(List<ExamInfo> newExams) {
        this.exams = newExams;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView examNameTextView;
        TextView examDescriptionTextView;
        Button editButton;
        Button deleteButton;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            examNameTextView = itemView.findViewById(R.id.exam_name);
            examDescriptionTextView = itemView.findViewById(R.id.exam_description);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            cardView = itemView.findViewById(R.id.exam_card);
        }
    }
}