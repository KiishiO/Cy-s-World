package com.example.own_example.fragments;

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
import com.example.own_example.services.BookstoreService;

import java.util.ArrayList;
import java.util.List;

public class BookstoreListFragment extends Fragment {
    private EditText etName, etLocation;
    private Button btnAddBookstore;
    private RecyclerView recyclerView;
    private BookstoreAdapter adapter;
    private List<BookstoreModel> bookstoreList;
    private BookstoreService bookstoreService;


    public interface OnBookstoreClickListener {
        void onBookstoreClick(BookstoreModel bookstore);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookstore_list, container, false);

        // Initialize views
        etName = view.findViewById(R.id.etBookstoreName);
        etLocation = view.findViewById(R.id.etBookstoreLocation);
        btnAddBookstore = view.findViewById(R.id.btnAddBookstore);
        recyclerView = view.findViewById(R.id.recyclerViewBookstores);

        // Setup RecyclerView
        bookstoreList = new ArrayList<>();
        adapter = new BookstoreAdapter(bookstoreList, getContext(), this::onBookstoreClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Initialize service
        bookstoreService = new BookstoreService(getContext());

        // Load bookstores
        loadBookstores();

        // Setup button click
        btnAddBookstore.setOnClickListener(v -> addBookstore());

        return view;
    }

    private void loadBookstores() {
        bookstoreService.getAllBookstores(new BookstoreService.BookstoreListCallback() {
            @Override
            public void onSuccess(List<BookstoreModel> bookstores) {
                bookstoreList.clear();
                bookstoreList.addAll(bookstores);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error loading bookstores: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBookstore() {
        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        BookstoreModel bookstore = new BookstoreModel(name, location);

        bookstoreService.createBookstore(bookstore, new BookstoreService.BookstoreCallback() {
            @Override
            public void onSuccess(BookstoreModel bookstore) {
                Toast.makeText(getContext(), "Bookstore added successfully",
                        Toast.LENGTH_SHORT).show();
                etName.setText("");
                etLocation.setText("");
                loadBookstores();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error adding bookstore: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onBookstoreClick(BookstoreModel bookstore) {
        Fragment fragment = ProductsFragment.newInstance(bookstore.getId());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    // Simple adapter for bookstores
    private class BookstoreAdapter extends RecyclerView.Adapter<BookstoreAdapter.ViewHolder> {
        private List<BookstoreModel> bookstoreList;
        private Context context;
        private OnBookstoreClickListener listener;

        public BookstoreAdapter(List<BookstoreModel> bookstoreList,
                                Context context,
                                OnBookstoreClickListener listener) {
            this.bookstoreList = bookstoreList;
            this.context = context;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bookstore, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BookstoreModel bookstore = bookstoreList.get(position);
            holder.tvName.setText(bookstore.getName());
            holder.tvLocation.setText(bookstore.getLocation());

            holder.itemView.setOnClickListener(v ->
                    listener.onBookstoreClick(bookstore));
        }

        @Override
        public int getItemCount() {
            return bookstoreList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvLocation;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvBookstoreName);
                tvLocation = itemView.findViewById(R.id.tvBookstoreLocation);
            }
        }
    }
}