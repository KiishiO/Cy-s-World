package com.example.own_example.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.CampusEvent;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<CampusEvent> events;
    private OnEventClickListener eventClickListener;

    public interface OnEventClickListener {
        void onEventClick(CampusEvent event);
    }

    public EventsAdapter(List<CampusEvent> events, OnEventClickListener listener) {
        this.events = events;
        this.eventClickListener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CampusEvent event = events.get(position);

        // Set event details
        holder.titleTextView.setText(event.getTitle());
        holder.locationTextView.setText(event.getLocation());
        holder.attendeesTextView.setText(event.getAttendees() + " attending");
        holder.dateTextView.setText(event.getFormattedStartTime());

        // Set category color
        setCategoryColor(holder.categoryIndicator, event.getCategory());

        // Set item click listener
        holder.itemView.setOnClickListener(v -> {
            if (eventClickListener != null) {
                eventClickListener.onEventClick(event);
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

    public void updateEvents(List<CampusEvent> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    public List<CampusEvent> getEvents() {
        return events;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        View categoryIndicator;
        TextView titleTextView;
        TextView locationTextView;
        TextView attendeesTextView;
        TextView dateTextView;
        CardView cardView;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
            titleTextView = itemView.findViewById(R.id.event_title);
            locationTextView = itemView.findViewById(R.id.event_location);
            attendeesTextView = itemView.findViewById(R.id.event_attendees);
            dateTextView = itemView.findViewById(R.id.event_date);
            cardView = itemView.findViewById(R.id.event_card);
        }
    }
}