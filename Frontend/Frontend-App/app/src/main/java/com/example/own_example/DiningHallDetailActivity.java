package com.example.own_example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.MenuItemAdapter;
import com.example.own_example.models.DiningHall;
import com.example.own_example.services.DiningHallService;
import com.example.own_example.services.UserService;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class DiningHallDetailActivity extends AppCompatActivity implements DiningHallService.DiningHallListener {

    private static final String TAG = "DiningHallDetailActivity";

    private int diningHallId;
    private DiningHall currentDiningHall;

    private ImageButton backButton;
    private Button orderNowButton;
    private TextView diningHallNameText;
    private TextView hoursValue;
    private TextView locationValue;
    private TextView statusValue;
    private TabLayout menuCategoriesTabs;
    private RecyclerView menuItemsRecyclerView;

    private MenuItemAdapter menuItemAdapter;
    private List<DiningHall.MenuItem> menuItems = new ArrayList<>();

    private DiningHallService diningHallService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_hall_detail);

        Log.d(TAG, "DiningHallDetailActivity onCreate");

        // Get dining hall ID from intent
        diningHallId = getIntent().getIntExtra("dining_hall_id", -1);
        if (diningHallId == -1) {
            Toast.makeText(this, "Invalid dining hall", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize service
        diningHallService = DiningHallService.getInstance(this);
        diningHallService.setListener(this);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        orderNowButton = findViewById(R.id.order_now_button);
        diningHallNameText = findViewById(R.id.dining_hall_name);
        hoursValue = findViewById(R.id.hours_value);
        locationValue = findViewById(R.id.location_value);
        statusValue = findViewById(R.id.status_value);
        menuCategoriesTabs = findViewById(R.id.menu_categories);
        menuItemsRecyclerView = findViewById(R.id.menu_items_recycler);

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Set up order now button
        orderNowButton.setOnClickListener(v -> launchOrderingActivity());

        // Set up RecyclerView
        menuItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuItemAdapter = new MenuItemAdapter(menuItems, this);
        menuItemsRecyclerView.setAdapter(menuItemAdapter);

        // Set up tab selection listener
        menuCategoriesTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (currentDiningHall != null) {
                    updateMenuItems(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });

        // Load dining hall data
        diningHallService.getDiningHallById(diningHallId);
    }

    private void launchOrderingActivity() {
        // Check if the dining hall is open
        if (currentDiningHall != null && !currentDiningHall.isOpen()) {
            Toast.makeText(this, "Sorry, this dining hall is currently closed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Launch ordering activity
        Intent intent = new Intent(this, OrderingActivity.class);
        intent.putExtra("dining_hall_id", diningHallId);
        startActivity(intent);
    }

    private void updateUI() {
        runOnUiThread(() -> {
            // Set basic info
            diningHallNameText.setText(currentDiningHall.getName());
            hoursValue.setText(currentDiningHall.getHours());
            locationValue.setText(currentDiningHall.getLocation());

            // Set status text and color
            if (currentDiningHall.isOpen()) {
                statusValue.setText("Open");
                statusValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                orderNowButton.setEnabled(true);
            } else {
                statusValue.setText("Closed");
                statusValue.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                orderNowButton.setEnabled(false);
            }

            // Set up tabs
            menuCategoriesTabs.removeAllTabs();
            for (DiningHall.MenuCategory category : currentDiningHall.getMenuCategories()) {
                menuCategoriesTabs.addTab(menuCategoriesTabs.newTab().setText(category.getName()));
            }

            // Load first category if available
            if (!currentDiningHall.getMenuCategories().isEmpty()) {
                updateMenuItems(0);
            }
        });
    }

    private void updateMenuItems(int categoryPosition) {
        if (categoryPosition < 0 || categoryPosition >= currentDiningHall.getMenuCategories().size()) {
            return;
        }

        DiningHall.MenuCategory category = currentDiningHall.getMenuCategories().get(categoryPosition);

        // Update menu items
        menuItems.clear();
        menuItems.addAll(category.getItems());
        menuItemAdapter.notifyDataSetChanged();
    }

    // DiningHallService.DiningHallListener implementation

    @Override
    public void onDiningHallsLoaded(List<DiningHall> diningHalls) {
        // Not used in this activity
    }

    @Override
    public void onDiningHallLoaded(DiningHall diningHall) {
        currentDiningHall = diningHall;
        updateUI();
    }

    @Override
    public void onError(String errorMessage) {
        Log.e(TAG, "Error: " + errorMessage);
        runOnUiThread(() -> {
            Toast.makeText(this, "Could not load dining hall details: " + errorMessage, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}