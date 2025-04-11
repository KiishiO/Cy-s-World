package com.example.own_example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.own_example.fragments.MenuCategoryFragment;
import com.example.own_example.models.DiningHall;
import com.example.own_example.services.DiningHallService;
import com.example.own_example.services.UserService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminDiningMenuActivity extends AppCompatActivity implements DiningHallService.DiningHallListener {

    private static final String TAG = "AdminDiningMenuActivity";

    private int diningHallId;
    private String diningHallName;
    private DiningHall currentDiningHall;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton addMenuItemButton;
    private FloatingActionButton addCategoryButton;

    private DiningHallService diningHallService;
    private MenuPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dining_menu);

        // Check if the user is an admin using SharedPreferences directly
        // (This is the approach that's working in your other admin activities)
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userRoleStr = prefs.getString("user_role", "STUDENT");

        try {
            UserRoles userRole = UserRoles.valueOf(userRoleStr);
            if (userRole != UserRoles.ADMIN) {
                Toast.makeText(this, "You don't have permission to access this feature", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        // Get dining hall ID from intent
        diningHallId = getIntent().getIntExtra("dining_hall_id", -1);
        diningHallName = getIntent().getStringExtra("dining_hall_name");

        if (diningHallId == -1) {
            Toast.makeText(this, "Invalid dining hall", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        toolbar = findViewById(R.id.admin_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Menu: " + diningHallName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = findViewById(R.id.menu_category_tabs);
        viewPager = findViewById(R.id.menu_view_pager);
        addMenuItemButton = findViewById(R.id.add_menu_item_button);
        addCategoryButton = findViewById(R.id.add_category_button);

        // Initialize service
        diningHallService = DiningHallService.getInstance(this);
        diningHallService.setListener(this);
        diningHallService.setUsername(UserService.getInstance().getCurrentUsername());

        // Set up FAB click listeners
        addMenuItemButton.setOnClickListener(v -> showAddMenuItemDialog());
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());

        // Load dining hall data
        loadDiningHallData();
    }

    private void loadDiningHallData() {
        // Load dining hall using the unified service
        diningHallService.getDiningHallById(diningHallId);
    }

    private void refreshUI() {
        runOnUiThread(() -> {
            // Clear existing tabs
            tabLayout.removeAllTabs();

            // Setup ViewPager
            pagerAdapter = new MenuPagerAdapter(getSupportFragmentManager(),
                    currentDiningHall.getMenuCategories());
            viewPager.setAdapter(pagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
        });
    }

    private void showAddMenuItemDialog() {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_menu_item, null);

        // Find views
        Spinner categorySpinner = dialogView.findViewById(R.id.menu_item_category_spinner);
        EditText nameInput = dialogView.findViewById(R.id.menu_item_name_input);
        EditText descriptionInput = dialogView.findViewById(R.id.menu_item_description_input);
        EditText allergensInput = dialogView.findViewById(R.id.menu_item_allergens_input);
        CheckBox vegetarianCheck = dialogView.findViewById(R.id.menu_item_vegetarian_check);
        CheckBox veganCheck = dialogView.findViewById(R.id.menu_item_vegan_check);
        CheckBox glutenFreeCheck = dialogView.findViewById(R.id.menu_item_gluten_free_check);

        // Set up category spinner
        List<String> categoryNames = new ArrayList<>();
        for (DiningHall.MenuCategory category : currentDiningHall.getMenuCategories()) {
            categoryNames.add(category.getName());
        }

        if (categoryNames.isEmpty()) {
            Toast.makeText(this, "Please add a menu category first", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Set default category to current tab if possible
        int currentTab = tabLayout.getSelectedTabPosition();
        if (currentTab >= 0 && currentTab < categoryNames.size()) {
            categorySpinner.setSelection(currentTab);
        }

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Add Menu Item")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set positive button click listener
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            // Get input values
            String categoryName = categorySpinner.getSelectedItem().toString();
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String allergensText = allergensInput.getText().toString().trim();
            boolean isVegetarian = vegetarianCheck.isChecked();
            boolean isVegan = veganCheck.isChecked();
            boolean isGlutenFree = glutenFreeCheck.isChecked();

            // Validate inputs
            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Name and description are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse allergens
            String[] allergens;
            if (allergensText.isEmpty()) {
                allergens = new String[]{"none"};
            } else {
                allergens = allergensText.split(",\\s*");
            }

            // Add menu item using the unified service
            diningHallService.addMenuItem(
                    diningHallId,
                    categoryName,
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

    private void showAddCategoryDialog() {
        // Create edit text for category name
        final EditText input = new EditText(this);
        input.setHint("Category Name (e.g., Breakfast, Lunch, Dinner)");

        // Create the dialog
        new AlertDialog.Builder(this)
                .setTitle("Add Menu Category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String categoryName = input.getText().toString().trim();
                    if (!categoryName.isEmpty()) {
                        diningHallService.addMenuCategory(diningHallId, categoryName);
                    } else {
                        Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Adapter for menu categories in ViewPager
     */
    private class MenuPagerAdapter extends FragmentPagerAdapter {
        private List<DiningHall.MenuCategory> categories;

        public MenuPagerAdapter(FragmentManager fm, List<DiningHall.MenuCategory> categories) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.categories = categories;
        }

        @Override
        public Fragment getItem(int position) {
            DiningHall.MenuCategory category = categories.get(position);
            return MenuCategoryFragment.newInstance(diningHallId, category.getName(), position);
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categories.get(position).getName();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (diningHallService != null) {
            diningHallService.disconnect();
        }
    }

    // DiningHallService.DiningHallListener implementation

    @Override
    public void onDiningHallsLoaded(List<DiningHall> diningHalls) {
        // Not used in this activity
    }

    @Override
    public void onDiningHallLoaded(DiningHall diningHall) {
        if (diningHall.getId() == diningHallId) {
            currentDiningHall = diningHall;
            refreshUI();
        }
    }

    @Override
    public void onDiningHallUpdated(DiningHall updatedDiningHall) {
        if (updatedDiningHall.getId() == diningHallId) {
            currentDiningHall = updatedDiningHall;
            refreshUI();
        }
    }

    @Override
    public void onConnectionStateChanged(boolean connected) {
        // Update UI based on connection state
        runOnUiThread(() -> {
            addMenuItemButton.setEnabled(connected);
            addCategoryButton.setEnabled(connected);
        });
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });
    }
}