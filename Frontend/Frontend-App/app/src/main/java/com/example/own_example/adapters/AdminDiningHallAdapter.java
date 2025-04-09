package com.example.own_example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.DiningHall;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminDiningHallAdapter extends RecyclerView.Adapter<AdminDiningHallAdapter.DiningHallViewHolder> {

    private List<DiningHall> diningHalls;
    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;
    private final OnManageMenuClickListener manageMenuListener;

    // Interfaces for click listeners
    public interface OnEditClickListener {
        void onEditClick(DiningHall diningHall);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(DiningHall diningHall);
    }

    public interface OnManageMenuClickListener {
        void onManageMenuClick(DiningHall diningHall);
    }

    public AdminDiningHallAdapter(List<DiningHall> diningHalls,
                                  OnEditClickListener editListener,
                                  OnDeleteClickListener deleteListener,
                                  OnManageMenuClickListener manageMenuListener) {
        this.diningHalls = diningHalls;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.manageMenuListener = manageMenuListener;
    }

    @NonNull
    @Override
    public DiningHallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_dining_hall, parent, false);
        return new DiningHallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiningHallViewHolder holder, int position) {
        DiningHall diningHall = diningHalls.get(position);

        // Bind data to views
        holder.nameText.setText(diningHall.getName());
        holder.locationText.setText(diningHall.getLocation());
        holder.hoursText.setText(diningHall.getHours());

        // Set status text and color
        if (diningHall.isOpen()) {
            holder.statusText.setText("Open");
            holder.statusText.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.statusText.setText("Closed");
            holder.statusText.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }

        // Set busyness indicator
        holder.busynessIndicator.setProgress(diningHall.getBusynessLevel());

        // Set popular item
        holder.popularItemText.setText(diningHall.getPopularItem());

        // Set menu item count
        int itemCount = diningHall.getTotalMenuItemCount();
        holder.menuItemCountText.setText(itemCount + " menu items");

        // Set button click listeners
        holder.editButton.setOnClickListener(v -> editListener.onEditClick(diningHall));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(diningHall));
        holder.manageMenuButton.setOnClickListener(v -> manageMenuListener.onManageMenuClick(diningHall));
    }

    @Override
    public int getItemCount() {
        return diningHalls.size();
    }

    /**
     * ViewHolder for dining hall items
     */
    static class DiningHallViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView locationText;
        TextView hoursText;
        TextView statusText;
        TextView popularItemText;
        TextView menuItemCountText;
        ProgressBar busynessIndicator;
        ImageButton editButton;
        ImageButton deleteButton;
        MaterialButton manageMenuButton;

        public DiningHallViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.admin_dining_hall_name);
            locationText = itemView.findViewById(R.id.admin_dining_hall_location);
            hoursText = itemView.findViewById(R.id.admin_dining_hall_hours);
            statusText = itemView.findViewById(R.id.admin_dining_hall_status);
            popularItemText = itemView.findViewById(R.id.admin_dining_hall_popular_item);
            menuItemCountText = itemView.findViewById(R.id.admin_dining_hall_menu_count);
            busynessIndicator = itemView.findViewById(R.id.admin_dining_hall_busyness);
            editButton = itemView.findViewById(R.id.admin_dining_hall_edit_button);
            deleteButton = itemView.findViewById(R.id.admin_dining_hall_delete_button);
            manageMenuButton = itemView.findViewById(R.id.admin_dining_hall_manage_menu_button);
        }
    }
}