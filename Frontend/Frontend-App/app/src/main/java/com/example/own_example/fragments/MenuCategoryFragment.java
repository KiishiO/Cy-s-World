package com.example.own_example.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.adapters.AdminMenuItemAdapter;
import com.example.own_example.models.DiningHall;
import com.example.own_example.services.DiningHallService;
import com.example.own_example.services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuCategoryFragment extends Fragment implements DiningHallService.DiningHallListener, AdminMenuItemAdapter.MenuItemListener {

    private static final String ARG_DINING_HALL_ID = "dining_hall_id";
    private static final String ARG_CATEGORY_NAME = "category_name";
    private static final String ARG_POSITION = "position";

    private int diningHallId;
    private String categoryName;
    private int position;

    private RecyclerView menuItemsRecyclerView;
    private TextView emptyStateTextView;
    private AdminMenuItemAdapter menuItemAdapter;
    private DiningHallService diningHallService;

    public MenuCategoryFragment() {
        // Required empty public constructor
    }

    public static MenuCategoryFragment newInstance(int diningHallId, String categoryName, int position) {
        MenuCategoryFragment fragment = new MenuCategoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DINING_HALL_ID, diningHallId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            diningHallId = getArguments().getInt(ARG_DINING_HALL_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            position = getArguments().getInt(ARG_POSITION);
        }

        // Initialize service
        diningHallService = DiningHallService.getInstance(getContext());
        diningHallService.setListener(this);
        diningHallService.setUsername(UserService.getInstance().getCurrentUsername());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        menuItemsRecyclerView = view.findViewById(R.id.menu_items_recycler_view);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);

        // Set up RecyclerView
        menuItemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menuItemAdapter = new AdminMenuItemAdapter(new ArrayList<>(), this);
        menuItemsRecyclerView.setAdapter(menuItemAdapter);

        // Load data
        loadMenuItems();
    }

    private void loadMenuItems() {
        // Get the dining hall to access the menu category
        diningHallService.getDiningHallById(diningHallId);
    }

    public void refreshMenuItems(DiningHall diningHall) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            Log.d("MenuCategoryFragment", "Refreshing menu items for category: " + categoryName);

            // Log all categories for debugging
            if (diningHall.getMenuCategories() != null) {
                for (DiningHall.MenuCategory cat : diningHall.getMenuCategories()) {
                    Log.d("MenuCategoryFragment", "Available category: " + cat.getName() +
                            " with " + cat.getItems().size() + " items");
                }
            }

            // Find the specific category
            DiningHall.MenuCategory category = null;
            for (DiningHall.MenuCategory cat : diningHall.getMenuCategories()) {
                if (cat.getName().equals(categoryName)) {
                    category = cat;
                    break;
                }
            }

            if (category != null && category.getItems() != null && !category.getItems().isEmpty()) {
                Log.d("MenuCategoryFragment", "Found category with items: " +
                        category.getItems().size());
                menuItemAdapter.updateMenuItems(category.getItems());
                menuItemsRecyclerView.setVisibility(View.VISIBLE);
                emptyStateTextView.setVisibility(View.GONE);
            } else {
                Log.d("MenuCategoryFragment", "Category has no items or not found");
                menuItemsRecyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEditMenuItem(DiningHall.MenuItem menuItem, int position) {
        if (getActivity() == null) return;

        // Inflate the dialog layout
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_edit_menu_item, null);

        // Find views
        TextView categoryTitle = dialogView.findViewById(R.id.menu_item_category_title);
        View categorySpinnerLayout = dialogView.findViewById(R.id.menu_item_category_layout);
        EditText nameInput = dialogView.findViewById(R.id.menu_item_name_input);
        EditText descriptionInput = dialogView.findViewById(R.id.menu_item_description_input);
        EditText allergensInput = dialogView.findViewById(R.id.menu_item_allergens_input);
        CheckBox vegetarianCheck = dialogView.findViewById(R.id.menu_item_vegetarian_check);
        CheckBox veganCheck = dialogView.findViewById(R.id.menu_item_vegan_check);
        CheckBox glutenFreeCheck = dialogView.findViewById(R.id.menu_item_gluten_free_check);

        // Hide spinner, show category title
        categorySpinnerLayout.setVisibility(View.GONE);
        categoryTitle.setVisibility(View.VISIBLE);
        categoryTitle.setText("Category: " + categoryName);

        // Set existing values
        nameInput.setText(menuItem.getName());
        descriptionInput.setText(menuItem.getDescription());

        // Set allergens
        if (menuItem.getAllergens() != null && !menuItem.getAllergens().isEmpty()
                && !menuItem.getAllergens().get(0).equals("none")) {
            allergensInput.setText(String.join(", ", menuItem.getAllergens()));
        }

        vegetarianCheck.setChecked(menuItem.isVegetarian());
        veganCheck.setChecked(menuItem.isVegan());
        glutenFreeCheck.setChecked(menuItem.isGlutenFree());

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Edit Menu Item")
                .setView(dialogView)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set positive button click listener
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            // Get input values
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String allergensText = allergensInput.getText().toString().trim();
            boolean isVegetarian = vegetarianCheck.isChecked();
            boolean isVegan = veganCheck.isChecked();
            boolean isGlutenFree = glutenFreeCheck.isChecked();

            // Validate inputs
            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(getActivity(), "Name and description are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse allergens
            String[] allergens;
            if (allergensText.isEmpty()) {
                allergens = new String[]{"none"};
            } else {
                allergens = allergensText.split(",\\s*");
            }

            // Update menu item
            // Note: The backend API doesn't support direct menu item updates
            // The dialog will appear and work but changes may not persist
            diningHallService.updateMenuItem(
                    diningHallId,
                    categoryName,
                    position,
                    name,
                    description,
                    allergens,
                    isVegetarian,
                    isVegan,
                    isGlutenFree
            );

            // Dismiss dialog
            dialog.dismiss();
        });
    }

    @Override
    public void onDeleteMenuItem(int position) {
        if (getActivity() == null) return;

        // Show confirmation dialog
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Menu Item")
                .setMessage("Are you sure you want to delete this menu item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete menu item
                    // Note: The backend API doesn't support direct menu item deletion
                    diningHallService.deleteMenuItem(diningHallId, categoryName, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // DiningHallService.DiningHallListener implementation

    @Override
    public void onDiningHallsLoaded(List<DiningHall> diningHalls) {
        // Not used in this fragment
    }

    @Override
    public void onDiningHallLoaded(DiningHall diningHall) {
        if (diningHall.getId() == diningHallId) {
            refreshMenuItems(diningHall);
        }
    }

    @Override
    public void onDiningHallUpdated(DiningHall updatedDiningHall) {
        if (updatedDiningHall.getId() == diningHallId) {
            refreshMenuItems(updatedDiningHall);
        }
    }

    @Override
    public void onError(String errorMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            });
        }
    }
}