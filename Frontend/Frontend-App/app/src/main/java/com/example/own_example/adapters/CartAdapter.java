package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.OrderModels.CartItemModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItemModel> cartItems;
    private Context context;
    private CartItemListener listener;

    public interface CartItemListener {
        void onIncreaseQuantity(int position);
        void onDecreaseQuantity(int position);
        void onRemoveItem(int position);
    }

    public CartAdapter(List<CartItemModel> cartItems, Context context, CartItemListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItemModel item = cartItems.get(position);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);

        holder.tvName.setText(item.getProduct().getItem());
        holder.tvPrice.setText(formatter.format(item.getProduct().getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvSubtotal.setText("Subtotal: " + formatter.format(item.getSubtotal()));

        // Set listeners
        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIncreaseQuantity(holder.getAdapterPosition());
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecreaseQuantity(holder.getAdapterPosition());
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity, tvSubtotal;
        Button btnIncrease, btnDecrease;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvCartItemSubtotal);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}