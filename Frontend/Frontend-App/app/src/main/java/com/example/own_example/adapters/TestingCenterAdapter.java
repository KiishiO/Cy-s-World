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
import com.example.own_example.models.TestingCenter;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TestingCenterAdapter extends RecyclerView.Adapter<TestingCenterAdapter.ViewHolder> {

    private List<TestingCenter> testingCenters;
    private Context context;
    private OnTestingCenterClickListener listener;
    private boolean isAdminView;

    public interface OnTestingCenterClickListener {
        void onTestingCenterClick(TestingCenter testingCenter);
        void onEditClick(TestingCenter testingCenter);
        void onDeleteClick(TestingCenter testingCenter);
    }

    public TestingCenterAdapter(Context context, List<TestingCenter> testingCenters, OnTestingCenterClickListener listener, boolean isAdminView) {
        this.context = context;
        this.testingCenters = testingCenters;
        this.listener = listener;
        this.isAdminView = isAdminView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_testing_center, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestingCenter testingCenter = testingCenters.get(position);

        holder.centerNameTextView.setText(testingCenter.getCenterName());
        holder.locationTextView.setText(testingCenter.getLocation());
        holder.descriptionTextView.setText(testingCenter.getCenterDescription());

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
                listener.onTestingCenterClick(testingCenter);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(testingCenter);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(testingCenter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return testingCenters != null ? testingCenters.size() : 0;
    }

    public void updateData(List<TestingCenter> newTestingCenters) {
        this.testingCenters = newTestingCenters;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView centerNameTextView;
        TextView locationTextView;
        TextView descriptionTextView;
        Button editButton;
        Button deleteButton;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            centerNameTextView = itemView.findViewById(R.id.center_name);
            locationTextView = itemView.findViewById(R.id.center_location);
            descriptionTextView = itemView.findViewById(R.id.center_description);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            cardView = itemView.findViewById(R.id.testing_center_card);
        }
    }
}