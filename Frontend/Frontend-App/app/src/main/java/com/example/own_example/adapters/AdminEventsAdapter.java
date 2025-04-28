package com.example.own_example.adapters;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.example.own_example.R;
import com.example.own_example.models.CampusEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminEventsAdapter extends RecyclerView.Adapter<AdminEventsAdapter.EventViewHolder> {

    private List<CampusEvent> events;
    private OnEventEditListener editListener;
    private OnEventDeleteListener deleteListener;
    private OnEventUpdateListener updateListener;

    public interface OnEventEditListener {
        void onEventEdit(CampusEvent event);
    }

    public interface OnEventDeleteListener {
        void onEventDelete(CampusEvent event);
    }

    public interface OnEventUpdateListener {
        void onEventUpdate(CampusEvent event);
    }

    public AdminEventsAdapter(List<CampusEvent> events,
                              OnEventEditListener editListener,
                              OnEventDeleteListener deleteListener,
                              OnEventUpdateListener updateListener) {
        this.events = events;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.updateListener = updateListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CampusEvent event = events.get(position);

        // Set event details
        holder.titleTextView.setText(event.getTitle());
        holder.locationTextView.setText(event.getLocation());

        // Format and display dates
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.US);

        if (event.getStartTime() != null) {
            holder.dateTextView.setText(fullDateFormat.format(event.getStartTime()));

            // Show status (past/upcoming) based on current time
            Date now = new Date();
            if (event.getEndTime() != null && event.getEndTime().before(now)) {
                holder.statusTextView.setText("PAST");
                holder.statusTextView.setTextColor(Color.GRAY);
            } else if (event.getStartTime().before(now)) {
                holder.statusTextView.setText("ACTIVE");
                holder.statusTextView.setTextColor(Color.rgb(0, 128, 0)); // Green
            } else {
                holder.statusTextView.setText("UPCOMING");
                holder.statusTextView.setTextColor(Color.rgb(0, 0, 128)); // Navy
            }
        } else {
            holder.dateTextView.setText("Date not set");
            holder.statusTextView.setText("DRAFT");
            holder.statusTextView.setTextColor(Color.GRAY);
        }

        holder.categoryTextView.setText(event.getCategory());

        // Set category color
        setCategoryColor(holder.categoryIndicator, event.getCategory());

        // Set click listeners for buttons
        holder.editButton.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEventEdit(event);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onEventDelete(event);
            }
        });

        holder.updateButton.setOnClickListener(v -> {
            if (updateListener != null) {
                updateListener.onEventUpdate(event);
            }
        });

        // Make item itself clickable for edit
        holder.itemView.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEventEdit(event);
            }
        });
    }

    private void setCategoryColor(View categoryIndicator, String category) {
        int color;
        switch (category.toLowerCase()) {
            case "career":
                color = Color.rgb(74, 144, 226); // Blue
                break;
            case "academic":
                color = Color.rgb(80, 200, 120); // Green
                break;
            case "entertainment":
                color = Color.rgb(233, 30, 99); // Pink
                break;
            case "social":
                color = Color.rgb(255, 152, 0); // Orange
                break;
            default:
                color = Color.GRAY;
        }
        categoryIndicator.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        View categoryIndicator;
        TextView titleTextView;
        TextView locationTextView;
        TextView dateTextView;
        TextView categoryTextView;
        TextView statusTextView;
        ImageButton editButton;
        ImageButton deleteButton;
        Button updateButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
            titleTextView = itemView.findViewById(R.id.event_title);
            locationTextView = itemView.findViewById(R.id.event_location);
            dateTextView = itemView.findViewById(R.id.event_date);
            categoryTextView = itemView.findViewById(R.id.event_category);
            statusTextView = itemView.findViewById(R.id.event_status);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            updateButton = itemView.findViewById(R.id.update_button);
        }
    }
}