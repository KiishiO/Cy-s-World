package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.OrderItem;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private final List<OrderItem> items;
    private final Context context;
    private final QuantityChangedListener quantityChangedListener;

    public interface QuantityChangedListener {
        void onQuantityChanged(OrderItem item, int newQuantity);
    }

    public OrderItemAdapter(List<OrderItem> items, Context context, QuantityChangedListener listener) {
        this.items = items;
        this.context = context;
        this.quantityChangedListener = listener;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_menu, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView descriptionText;
        private final TextView priceText;
        private final TextView quantityText;
        private final ImageButton decreaseButton;
        private final ImageButton increaseButton;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.menu_item_name);
            descriptionText = itemView.findViewById(R.id.menu_item_description);
            priceText = itemView.findViewById(R.id.menu_item_price);
            quantityText = itemView.findViewById(R.id.quantity_text);
            decreaseButton = itemView.findViewById(R.id.decrease_button);
            increaseButton = itemView.findViewById(R.id.increase_button);
        }

        public void bind(OrderItem item) {
            nameText.setText(item.getName());
            descriptionText.setText(item.getDescription());
            priceText.setText(String.format("$%.2f", item.getPrice()));
            updateQuantity(item);

            decreaseButton.setOnClickListener(v -> {
                int newQuantity = Math.max(0, item.getQuantity() - 1);
                if (newQuantity != item.getQuantity()) {
                    quantityChangedListener.onQuantityChanged(item, newQuantity);
                    updateQuantity(item);
                }
            });

            increaseButton.setOnClickListener(v -> {
                int newQuantity = Math.min(99, item.getQuantity() + 1);
                if (newQuantity != item.getQuantity()) {
                    quantityChangedListener.onQuantityChanged(item, newQuantity);
                    updateQuantity(item);
                }
            });
        }

        private void updateQuantity(OrderItem item) {
            quantityText.setText(String.valueOf(item.getQuantity()));
            decreaseButton.setEnabled(item.getQuantity() > 0);
        }
    }
}