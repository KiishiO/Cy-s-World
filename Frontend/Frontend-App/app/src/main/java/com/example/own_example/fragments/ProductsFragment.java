package com.example.own_example.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment implements ProductsAdapter.ProductClickListener {

    private static final String ARG_BOOKSTORE_ID = "bookstore_id";
    private static final String TAG = "ProductsFragment";

    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private List<ProductsModel> productsList;
    private BookstoreService bookstoreService;
    private OrderService orderService;
    private int bookstoreId;
    private Button btnAddProduct;
    private FloatingActionButton fabAddProduct;

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
        fabAddProduct = view.findViewById(R.id.fabAddProduct);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);

        // Initialize services
        bookstoreService = new BookstoreService(getContext());
        orderService = new OrderService(getContext());

        // Setup RecyclerView
        productsList = new ArrayList<>();
        adapter = new ProductsAdapter(productsList, getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup FAB for adding products
        fabAddProduct.setOnClickListener(v -> showAddProductDialog(null));

        // Setup button for adding products
        btnAddProduct.setOnClickListener(v -> showAddProductDialog(null));

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

    @Override
    public void onEditClick(ProductsModel product) {
        showAddProductDialog(product);
    }

    @Override
    public void onDeleteClick(ProductsModel product) {
        confirmDelete(product);
    }

    private void confirmDelete(ProductsModel product) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getItem() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProduct(ProductsModel product) {
        // Pass the bookstoreId along with the productId
        bookstoreService.deleteProduct(bookstoreId, product.getId(), new BookstoreService.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                loadProducts(); // Refresh list
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error deleting product: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Method to show dialog for adding or editing a product
    private void showAddProductDialog(ProductsModel productToEdit) {
        boolean isEdit = productToEdit != null;

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_product);
        dialog.setCancelable(true);

        // Set dialog title based on whether we're adding or editing
        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        if (isEdit) {
            tvTitle.setText("Edit Product");
        } else {
            tvTitle.setText("Add New Product");
        }

        TextInputEditText etProductItem = dialog.findViewById(R.id.etProductItem);
        TextInputEditText etProductPrice = dialog.findViewById(R.id.etProductPrice);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Populate fields if editing
        if (isEdit) {
            etProductItem.setText(productToEdit.getItem());
            etProductPrice.setText(String.valueOf(productToEdit.getPrice()));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            // Validate inputs
            String item = etProductItem.getText().toString().trim();
            String priceStr = etProductPrice.getText().toString().trim();

            if (item.isEmpty()) {
                etProductItem.setError("Product name is required");
                return;
            }

            if (priceStr.isEmpty()) {
                etProductPrice.setError("Price is required");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                etProductPrice.setError("Invalid price format");
                return;
            }

            // Create or update product
            if (isEdit) {
                ProductsModel updatedProduct = new ProductsModel(item, price);
                updatedProduct.setId(productToEdit.getId());
                updateProduct(updatedProduct);
            } else {
                ProductsModel newProduct = new ProductsModel(item, price);
                addProductToBookstore(newProduct);
            }

            // Close dialog
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateProduct(ProductsModel product) {
        Log.d(TAG, "Updating product: " + product.getItem() + ", price: " + product.getPrice());

        // Pass the bookstoreId along with the productId
        bookstoreService.updateProduct(bookstoreId, product.getId(), product, new BookstoreService.ProductCallback() {
            @Override
            public void onSuccess(ProductsModel updatedProduct) {
                Log.d(TAG, "Product updated successfully with ID: " + updatedProduct.getId());
                Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();

                // Refresh the product list
                loadProducts();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error updating product: " + message);
                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addProductToBookstore(ProductsModel product) {
        Log.d(TAG, "Adding product: " + product.getItem() + ", price: " + product.getPrice());

        bookstoreService.addProductToBookstore(bookstoreId, product, new BookstoreService.ProductCallback() {
            @Override
            public void onSuccess(ProductsModel newProduct) {
                Log.d(TAG, "Product added successfully with ID: " + newProduct.getId());
                Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();

                // Refresh the product list
                loadProducts();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error adding product: " + message);
                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}