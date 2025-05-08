package com.example.own_example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.OrderHistoryAdapter;
import com.example.own_example.models.DiningOrder;
import com.example.own_example.services.OrderingService;
import com.example.own_example.services.UserService;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity implements OrderingService.OrderHistoryListener {

    private static final String TAG = "OrderHistoryActivity";

    private ImageButton backButton;
    private TextView titleText;
    private RecyclerView orderHistoryRecyclerView;
    private View emptyStateView;

    private OrderHistoryAdapter orderHistoryAdapter;
    private List<DiningOrder> orders = new ArrayList<>();

    private OrderingService orderingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // Initialize service
        orderingService = OrderingService.getInstance(this);
        orderingService.setOrderHistoryListener(this);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        orderHistoryRecyclerView = findViewById(R.id.order_history_recycler);
        emptyStateView = findViewById(R.id.empty_state_view);

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Set up RecyclerView
        orderHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderHistoryAdapter = new OrderHistoryAdapter(orders, this);
        orderHistoryRecyclerView.setAdapter(orderHistoryAdapter);

        // Load orders
        int userId = UserService.getInstance().getCurrentUserId();
        orderingService.getOrderHistory(userId);
    }

    private void updateUI() {
        if (orders.isEmpty()) {
            orderHistoryRecyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            orderHistoryRecyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            orderHistoryAdapter.notifyDataSetChanged();
        }
    }

    // OrderingService.OrderHistoryListener implementation
    @Override
    public void onOrderHistoryLoaded(List<DiningOrder> orderHistory) {
        orders.clear();
        orders.addAll(orderHistory);
        runOnUiThread(this::updateUI);
    }

    @Override
    public void onOrderHistoryError(String errorMessage) {
        Log.e(TAG, "Error: " + errorMessage);
        runOnUiThread(() -> {
            Toast.makeText(this, "Error loading order history: " + errorMessage, Toast.LENGTH_SHORT).show();
            updateUI();
        });
    }
}