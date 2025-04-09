package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.Bus;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.BusViewHolder> {

    private List<Bus> busList;
    private Context context;
    private OnBusClickListener listener;

    // Interface for handling bus item clicks
    public interface OnBusClickListener {
        void onBusClick(Bus bus);
        void onUpdateLocationClick(Bus bus);
        void onRateBusClick(Bus bus);
    }

    public BusAdapter(Context context, List<Bus> busList, OnBusClickListener listener) {
        this.context = context;
        this.busList = busList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bus, parent, false);
        return new BusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusViewHolder holder, int position) {
        Bus bus = busList.get(position);

        // Set bus information
        holder.busNumberName.setText("Bus " + bus.getBusNum() + ": " + bus.getBusName());

        // Set current stop info
        if (bus.getStopLocation() != null && !bus.getStopLocation().isEmpty()) {
            holder.currentStop.setText("Current Stop: " + bus.getStopLocation());
            holder.currentStop.setVisibility(View.VISIBLE);
        } else {
            holder.currentStop.setText("No current stop information");
            holder.currentStop.setVisibility(View.VISIBLE);
        }

        // Set last report time
        holder.lastUpdated.setText("Last Updated: " + bus.getLastReportTime());

        // Set bus rating
        holder.busRating.setText("Rating: " + bus.getFormattedRating());

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBusClick(bus);
            }
        });

        holder.updateLocationBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateLocationClick(bus);
            }
        });

        holder.rateBusBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRateBusClick(bus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return busList == null ? 0 : busList.size();
    }

    // Method to update the bus list
    public void updateBusList(List<Bus> newBusList) {
        this.busList = newBusList;
        notifyDataSetChanged();
    }

    // ViewHolder class
    static class BusViewHolder extends RecyclerView.ViewHolder {
        TextView busNumberName, currentStop, lastUpdated, busRating;
        MaterialButton updateLocationBtn, rateBusBtn;
        CardView busCard;

        public BusViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            busCard = itemView.findViewById(R.id.bus_card);
            busNumberName = itemView.findViewById(R.id.tv_bus_number_name);
            currentStop = itemView.findViewById(R.id.tv_current_stop);
            lastUpdated = itemView.findViewById(R.id.tv_last_updated);
            busRating = itemView.findViewById(R.id.tv_bus_rating);
            updateLocationBtn = itemView.findViewById(R.id.btn_update_location);
            rateBusBtn = itemView.findViewById(R.id.btn_rate_bus);
        }
    }
}