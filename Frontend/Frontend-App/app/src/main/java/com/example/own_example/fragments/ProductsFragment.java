package com.example.own_example.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.BookstoreActivity;
import com.example.own_example.R;
import com.example.own_example.adapters.ProductsAdapter;
import com.example.own_example.models.ProductsModel;
import com.example.own_example.services.BookstoreService;
import com.example.own_example.services.OrderService;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment implements ProductsAdapter.ProductClickListener {

    private static final String ARG_BOOKSTORE_ID = "bookstore_id";

    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private List<ProductsModel> productsList;
    private BookstoreService bookstoreService;
    private OrderService orderService;
    private int bookstoreId;

    public static ProductsFragment newInstance(int bookstoreId) {
        ProductsFragment fragment = new ProductsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOKSTORE_ID, bookstoreId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookstoreId = getArguments().getInt(ARG_BOOKSTORE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewProducts);

        // Initialize services
        bookstoreService = new BookstoreService(getContext());
        orderService = new OrderService(getContext());

        // Setup RecyclerView
        productsList = new ArrayList<>();
        adapter = new ProductsAdapter(productsList, getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Load products
        loadProducts();

        return view;
    }

    private void loadProducts() {
        bookstoreService.getProducts(bookstoreId, new BookstoreService.ProductListCallback() {
            @Override
            public void onSuccess(List<ProductsModel> products) {
                productsList.clear();
                productsList.addAll(products);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error loading products: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAddToCartClick(ProductsModel product) {
        orderService.addToCart(product, 1);
        Toast.makeText(getContext(), product.getItem() + " added to cart", Toast.LENGTH_SHORT).show();

        // Instead of directly calling onResume(), use this approach:
        if (getActivity() instanceof BookstoreActivity) {
            // Signal the activity that we need to update the cart badge
            ((BookstoreActivity) getActivity()).updateCartBadge();
        }
    }
}