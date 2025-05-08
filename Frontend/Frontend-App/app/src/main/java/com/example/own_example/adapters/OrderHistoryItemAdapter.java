package com.example.own_example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.DiningOrderItem;

import java.util.List;

public class OrderHistoryItemAdapter extends RecyclerView.Adapter<OrderHistoryItemAdapter.OrderHistoryItemViewHolder> {

    private final List<DiningOrderItem> items;

    public OrderHistoryItemAdapter(List<DiningOrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OrderHistoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history_item, parent, false);
        return new OrderHistoryItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryItemViewHolder holder, int position) {
        DiningOrderItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderHistoryItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView quantityText;
        private final TextView nameText;
        private final TextView priceText;

        public OrderHistoryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            quantityText = itemView.findViewById(R.id.item_quantity);
            nameText = itemView.findViewById(R.id.item_name);
            priceText = itemView.findViewById(R.id.item_price);
        }

        public void bind(DiningOrderItem item) {
            quantityText.setText(item.getQuantity() + "x");

            // Set name (in a real app, this would come from the item)
            if (item.getMenuItems() != null) {
                nameText.setText("Menu Item #" + item.getMenuItems().getId());
            } else {
                nameText.setText("Unknown Item");
            }

            // Calculate price (fixed price for demo)
            double price = 5.99 * item.getQuantity();
            priceText.setText(String.format("$%.2f", price));
        }
    }
}