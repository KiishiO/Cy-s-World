package com.example.own_example.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.DiningHallDetailActivity;
import com.example.own_example.R;
import com.example.own_example.models.DiningHall;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DiningHallAdapter extends RecyclerView.Adapter<DiningHallAdapter.DiningHallViewHolder> {

    private List<DiningHall> diningHalls;
    private Context context;

    public DiningHallAdapter(List<DiningHall> diningHalls, Context context) {
        this.diningHalls = diningHalls;
        this.context = context;
    }

    @NonNull
    @Override
    public DiningHallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dining_hall, parent, false);
        return new DiningHallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiningHallViewHolder holder, int position) {
        DiningHall diningHall = diningHalls.get(position);

        holder.diningHallName.setText(diningHall.getName());
        holder.diningHallLocation.setText(diningHall.getLocation());

        // Set status text and color
        if (diningHall.isOpen()) {
            holder.diningHallStatus.setText("Open");
            holder.diningHallStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.diningHallStatus.setText("Closed");
            holder.diningHallStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        // Set popular item
        holder.popularItem.setText(diningHall.getPopularItem());

        // Set busyness indicator
        holder.busynessIndicator.setProgress(diningHall.getBusynessLevel());

        // Set click listener for view menu button
        holder.viewMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, DiningHallDetailActivity.class);
            intent.putExtra("dining_hall_id", diningHall.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return diningHalls.size();
    }

    // Update the dining halls list
    public void updateDiningHalls(List<DiningHall> newDiningHalls) {
        this.diningHalls.clear();
        this.diningHalls.addAll(newDiningHalls);
        notifyDataSetChanged();
    }

    // ViewHolder class
    public static class DiningHallViewHolder extends RecyclerView.ViewHolder {
        TextView diningHallName;
        TextView diningHallLocation;
        TextView diningHallStatus;
        TextView popularItem;
        ProgressBar busynessIndicator;
        MaterialButton viewMenuButton;

        public DiningHallViewHolder(@NonNull View itemView) {
            super(itemView);
            diningHallName = itemView.findViewById(R.id.dining_hall_name);
            diningHallLocation = itemView.findViewById(R.id.dining_hall_location);
            diningHallStatus = itemView.findViewById(R.id.dining_hall_status);
            popularItem = itemView.findViewById(R.id.popular_item);
            busynessIndicator = itemView.findViewById(R.id.busyness_indicator);
            viewMenuButton = itemView.findViewById(R.id.view_menu_button);
        }
    }
}