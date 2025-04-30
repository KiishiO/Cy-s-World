package com.example.own_example;

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

import com.example.own_example.adapters.OrderItemAdapter;
import com.example.own_example.models.DiningHall;
import com.example.own_example.models.OrderItem;
import com.example.own_example.services.DiningHallService;
import com.example.own_example.services.OrderingService;
import com.example.own_example.services.UserService;

import java.util.ArrayList;
import java.util.List;

public class OrderingActivity extends AppCompatActivity implements DiningHallService.DiningHallListener, OrderingService.OrderListener {

    private static final String TAG = "OrderingActivity";

    private int diningHallId;
    private DiningHall currentDiningHall;

    private ImageButton backButton;
    private TextView diningHallNameText;
    private RecyclerView menuItemsRecyclerView;
    private Button placeOrderButton;
    private TextView totalPriceText;

    private OrderItemAdapter orderItemAdapter;
    private List<OrderItem> orderItems = new ArrayList<>();

    private DiningHallService diningHallService;
    private OrderingService orderingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);

        // Get dining hall ID from intent
        diningHallId = getIntent().getIntExtra("dining_hall_id", -1);
        if (diningHallId == -1) {
            Toast.makeText(this, "Invalid dining hall", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize services
        diningHallService = DiningHallService.getInstance(this);
        diningHallService.setListener(this);

        orderingService = OrderingService.getInstance(this);
        orderingService.setListener(this);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        diningHallNameText = findViewById(R.id.dining_hall_name);
        menuItemsRecyclerView = findViewById(R.id.menu_items_recycler);
        placeOrderButton = findViewById(R.id.place_order_button);
        totalPriceText = findViewById(R.id.total_price);

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Set up RecyclerView
        menuItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderItemAdapter = new OrderItemAdapter(orderItems, this,
                // Quantity changed listener
                (orderItem, newQuantity) -> {
                    orderItem.setQuantity(newQuantity);
                    updateTotalPrice();
                });
        menuItemsRecyclerView.setAdapter(orderItemAdapter);

        // Set up place order button
        placeOrderButton.setOnClickListener(v -> placeOrder());

        // Load dining hall data
        diningHallService.getDiningHallById(diningHallId);
    }

    private void updateDiningHallInfo() {
        diningHallNameText.setText(currentDiningHall.getName() + " - Order");

        // Initialize order items from menu items
        orderItems.clear();

        for (DiningHall.MenuCategory category : currentDiningHall.getMenuCategories()) {
            for (DiningHall.MenuItem menuItem : category.getItems()) {
                OrderItem orderItem = new OrderItem(
                        menuItem.getId(),
                        menuItem.getName(),
                        menuItem.getDescription(),
                        menuItem.getPrice(),
                        0  // Initial quantity
                );
                orderItems.add(orderItem);
            }
        }

        orderItemAdapter.notifyDataSetChanged();
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = 0.0;

        for (OrderItem item : orderItems) {
            total += item.getPrice() * item.getQuantity();
        }

        totalPriceText.setText(String.format("Total: $%.2f", total));

        // Enable place order button only if there are items in order
        boolean hasItems = false;
        for (OrderItem item : orderItems) {
            if (item.getQuantity() > 0) {
                hasItems = true;
                break;
            }
        }

        placeOrderButton.setEnabled(hasItems);
    }

    private void placeOrder() {
        // Create list of selected items
        List<OrderItem> selectedItems = new ArrayList<>();
        for (OrderItem item : orderItems) {
            if (item.getQuantity() > 0) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user ID
        int userId = Integer.parseInt(UserService.getInstance().getCurrentUserId());

        // Place order
        orderingService.placeOrder(diningHallId, userId, selectedItems);
    }

    // DiningHallService.DiningHallListener implementation
    @Override
    public void onDiningHallsLoaded(List<DiningHall> diningHalls) {
        // Not used in this activity
    }

    @Override
    public void onDiningHallLoaded(DiningHall diningHall) {
        currentDiningHall = diningHall;
        runOnUiThread(this::updateDiningHallInfo);
    }

    @Override
    public void onError(String errorMessage) {
        Log.e(TAG, "Error: " + errorMessage);
        runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    // OrderingService.OrderListener implementation
    @Override
    public void onOrderPlaced(int orderId) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public void onOrderError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Order error: " + errorMessage, Toast.LENGTH_SHORT).show();
        });
    }
}