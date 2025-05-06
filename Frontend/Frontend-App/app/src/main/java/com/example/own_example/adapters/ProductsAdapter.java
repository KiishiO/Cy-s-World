package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.ProductsModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private List<ProductsModel> productsList;
    private Context context;
    private ProductClickListener listener;

    public interface ProductClickListener {
        void onAddToCartClick(ProductsModel product);
    }

    public ProductsAdapter(List<ProductsModel> productsList, Context context, ProductClickListener listener) {
        this.productsList = productsList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductsModel product = productsList.get(position);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);

        holder.tvItem.setText(product.getItem());
        holder.tvPrice.setText(formatter.format(product.getPrice()));

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem, tvPrice;
        Button btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tvItem);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}