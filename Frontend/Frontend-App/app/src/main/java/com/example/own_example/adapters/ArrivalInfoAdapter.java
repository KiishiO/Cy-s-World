package com.example.own_example.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ArrivalInfoAdapter extends RecyclerView.Adapter<ArrivalInfoAdapter.ViewHolder> {

    private List<Map<String, Object>> arrivalsList;
    private Context context;
    private final SimpleDateFormat timeFormat;

    public ArrivalInfoAdapter(Context context, List<Map<String, Object>> arrivalsList) {
        this.context = context;
        this.arrivalsList = arrivalsList;

        // Create time formatter with device's time zone
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        this.timeFormat.setTimeZone(TimeZone.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_arrival, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> arrival = arrivalsList.get(position);

        // Set route name
        String routeName = (String) arrival.get("route_name");
        holder.routeNameText.setText(routeName);

        // Set arrival time - simpler approach
        long arrivalTimeSeconds = (long) arrival.get("arrival_time");
        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        // Calculate minutes until arrival
        long minutesUntilArrival = (arrivalTimeSeconds - currentTimeSeconds) / 60;

        // Just show minutes for all cases to keep it simple
        if (minutesUntilArrival <= 0) {
            holder.arrivalTimeText.setText("Now");
        } else if (minutesUntilArrival == 1) {
            holder.arrivalTimeText.setText("1 min");
        } else {
            holder.arrivalTimeText.setText(minutesUntilArrival + " min");
        }

        // Set route color indicator
        String routeId = (String) arrival.get("route_id");
        GradientDrawable circle = (GradientDrawable) holder.routeIndicator.getBackground();

        int color = Color.GRAY; // Default color
        switch (routeId) {
            case "1":
                color = Color.RED;
                break;
            case "2":
                color = Color.BLUE;
                break;
            case "3":
                color = Color.GREEN;
                break;
            case "4":
                color = Color.YELLOW;
                break;
            case "5":
                color = Color.MAGENTA;
                break;
            case "6":
                color = Color.CYAN;
                break;
        }

        circle.setColor(color);
    }

    @Override
    public int getItemCount() {
        return arrivalsList != null ? arrivalsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeNameText;
        TextView arrivalTimeText;
        View routeIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            routeNameText = itemView.findViewById(R.id.route_name_text);
            arrivalTimeText = itemView.findViewById(R.id.arrival_time_text);
            routeIndicator = itemView.findViewById(R.id.route_indicator);
        }
    }
}