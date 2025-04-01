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

import com.example.own_example.CampusEventsActivity;
import com.example.own_example.R;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<CampusEventsActivity.EventItem> events;

    public EventsAdapter(List<CampusEventsActivity.EventItem> events) {
        this.events = events;
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
        CampusEventsActivity.EventItem event = events.get(position);

        // Set event details
        holder.titleTextView.setText(event.getTitle());
        holder.locationTextView.setText(event.getLocation());
        holder.attendeesTextView.setText(event.getAttendees() + " attending");

        // Set category color
        setCategoryColor(holder.categoryIndicator, event.getCategory());

        // Set RSVP button state
        updateRsvpButton(holder.rsvpButton, event.isRsvped());

        // Set RSVP button click listener
        holder.rsvpButton.setOnClickListener(v -> {
            boolean newRsvpState = !event.isRsvped();
            event.setRsvped(newRsvpState);
            updateRsvpButton(holder.rsvpButton, newRsvpState);
            holder.attendeesTextView.setText(event.getAttendees() + " attending");
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

    private void updateRsvpButton(Button button, boolean isRsvped) {
        if (isRsvped) {
            button.setText("Cancel RSVP");
            button.setBackgroundResource(R.drawable.rounded_button_gray);
        } else {
            button.setText("RSVP");
            button.setBackgroundResource(R.drawable.rounded_button_red);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<CampusEventsActivity.EventItem> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        View categoryIndicator;
        TextView titleTextView;
        TextView locationTextView;
        TextView attendeesTextView;
        Button rsvpButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
            titleTextView = itemView.findViewById(R.id.event_title);
            locationTextView = itemView.findViewById(R.id.event_location);
            attendeesTextView = itemView.findViewById(R.id.event_attendees);
            rsvpButton = itemView.findViewById(R.id.rsvp_button);
        }
    }
}