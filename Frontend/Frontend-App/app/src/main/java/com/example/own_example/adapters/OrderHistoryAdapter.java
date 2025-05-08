package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.DiningOrder;
import com.example.own_example.models.DiningOrderItem;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {

    private final List<DiningOrder> orders;
    private final Context context;

    public OrderHistoryAdapter(List<DiningOrder> orders, Context context) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {
        DiningOrder order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView diningHallNameText;
        private final TextView orderDateText;
        private final TextView orderIdText;
        private final TextView totalPriceText;
        private final RecyclerView orderItemsRecyclerView;

        public OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            diningHallNameText = itemView.findViewById(R.id.dining_hall_name);
            orderDateText = itemView.findViewById(R.id.order_date);
            orderIdText = itemView.findViewById(R.id.order_id);
            totalPriceText = itemView.findViewById(R.id.total_price);
            orderItemsRecyclerView = itemView.findViewById(R.id.order_items_recycler);

            // Set up nested RecyclerView
            orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }

        public void bind(DiningOrder order) {
            // For demo purposes, we'll use a placeholder name since the backend doesn't store dining hall name
            diningHallNameText.setText("ISU Dining Hall");

            // Format date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
            orderDateText.setText(order.getOrderDate().format(formatter));

            // Set order ID
            orderIdText.setText("Order #" + order.getId());

            // Calculate total price
            double totalPrice = 0.0;
            for (DiningOrderItem item : order.getItems()) {
                // In a real app, you'd get the price from the menu item
                // For demo, we'll use a fixed price of $5.99
                double itemPrice = 5.99 * item.getQuantity();
                totalPrice += itemPrice;
            }

            totalPriceText.setText(String.format("Total: $%.2f", totalPrice));

            // Set up items adapter
            OrderHistoryItemAdapter itemsAdapter = new OrderHistoryItemAdapter(order.getItems());
            orderItemsRecyclerView.setAdapter(itemsAdapter);
        }
    }
}