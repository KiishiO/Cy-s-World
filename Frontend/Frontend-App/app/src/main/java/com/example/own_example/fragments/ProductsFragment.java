package com.example.own_example.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.BookstoreModel;
import com.example.own_example.models.ProductsModel;
import com.example.own_example.services.BookstoreService;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment {
    private static final String ARG_BOOKSTORE_ID = "bookstoreId";

    private int bookstoreId;
    private TextView tvBookstoreName, tvLocation;
    private Button btnAddProduct, btnEditBookstore, btnDeleteBookstore;
    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private List<ProductsModel> productsList;
    private BookstoreService bookstoreService;

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
        tvBookstoreName = view.findViewById(R.id.tvBookstoreName);
        tvLocation = view.findViewById(R.id.tvLocation);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnEditBookstore = view.findViewById(R.id.btnEditBookstore);
        btnDeleteBookstore = view.findViewById(R.id.btnDeleteBookstore);
        recyclerView = view.findViewById(R.id.recyclerViewProducts);

        // Setup RecyclerView
        productsList = new ArrayList<>();
        adapter = new ProductsAdapter(productsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Initialize service
        bookstoreService = new BookstoreService(getContext());

        // Load data
        loadBookstoreDetails();
        loadProducts();

        // Setup button clicks
        btnAddProduct.setOnClickListener(v -> showAddProductDialog());
        btnEditBookstore.setOnClickListener(v -> showEditBookstoreDialog());
        btnDeleteBookstore.setOnClickListener(v -> confirmDeleteBookstore());

        return view;
    }

    private void loadBookstoreDetails() {
        bookstoreService.getBookstoreById(bookstoreId, new BookstoreService.BookstoreCallback() {
            @Override
            public void onSuccess(BookstoreModel bookstore) {
                tvBookstoreName.setText(bookstore.getName());
                tvLocation.setText("Location: " + bookstore.getLocation());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error loading bookstore: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
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
                Toast.makeText(getContext(), "Error loading products: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddProductDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        EditText etItem = dialogView.findViewById(R.id.etProductItem);
        EditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String item = etItem.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (item.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid price",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            ProductsModel product = new ProductsModel(item, price);

            bookstoreService.addProductToBookstore(bookstoreId, product,
                    new BookstoreService.ProductCallback() {
                        @Override
                        public void onSuccess(ProductsModel product) {
                            Toast.makeText(getContext(), "Product added successfully",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadProducts();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(getContext(), "Error adding product: " + message,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditBookstoreDialog() {
        bookstoreService.getBookstoreById(bookstoreId, new BookstoreService.BookstoreCallback() {
            @Override
            public void onSuccess(BookstoreModel bookstore) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_bookstore, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(dialogView);

                EditText etName = dialogView.findViewById(R.id.etBookstoreName);
                EditText etLocation = dialogView.findViewById(R.id.etBookstoreLocation);
                Button btnSave = dialogView.findViewById(R.id.btnSave);
                Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                etName.setText(bookstore.getName());
                etLocation.setText(bookstore.getLocation());

                AlertDialog dialog = builder.create();

                btnSave.setOnClickListener(v -> {
                    String name = etName.getText().toString().trim();
                    String location = etLocation.getText().toString().trim();

                    if (name.isEmpty() || location.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BookstoreModel updatedBookstore = new BookstoreModel(name, location);

                    bookstoreService.updateBookstore(bookstoreId, updatedBookstore,
                            new BookstoreService.BookstoreCallback() {
                                @Override
                                public void onSuccess(BookstoreModel bookstore) {
                                    Toast.makeText(getContext(), "Bookstore updated successfully",
                                            Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    loadBookstoreDetails();
                                }

                                @Override
                                public void onError(String message) {
                                    Toast.makeText(getContext(), "Error updating bookstore: " + message,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                });

                btnCancel.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error loading bookstore: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteBookstore() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Bookstore")
                .setMessage("Are you sure you want to delete this bookstore?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    bookstoreService.deleteBookstore(bookstoreId, new BookstoreService.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Bookstore deleted successfully",
                                    Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(getContext(), "Error deleting bookstore: " + message,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Simple adapter for products - inner class to reduce file count
    private class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {
        private List<ProductsModel> productsList;

        public ProductsAdapter(List<ProductsModel> productsList) {
            this.productsList = productsList;
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
            holder.tvItem.setText(product.getItem());
            holder.tvPrice.setText("$" + String.format("%.2f", product.getPrice()));
        }

        @Override
        public int getItemCount() {
            return productsList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItem, tvPrice;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItem = itemView.findViewById(R.id.tvItem);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }
        }
    }
}