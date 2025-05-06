package com.example.own_example;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.own_example.models.OrderModels.OrderModel;
import com.example.own_example.services.OrderService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderConfirmationActivity extends AppCompatActivity {

    private TextView tvOrderNumber, tvOrderDate, tvOrderTotal, tvEstimatedDelivery;
    private Button btnContinueShopping;
    private OrderService orderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        // Initialize views
        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvEstimatedDelivery = findViewById(R.id.tvEstimatedDelivery);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);

        // Initialize service
        orderService = new OrderService(this);

        // Get order ID from intent
        int orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId != -1) {
            displayOrderDetails(orderId);
        }

        // Setup continue shopping button
        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(OrderConfirmationActivity.this, StudentDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(OrderConfirmationActivity.this, StudentDashboardActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_dining) {
                Intent intent = new Intent(OrderConfirmationActivity.this, DiningHallActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_buses) {
                Intent intent = new Intent(OrderConfirmationActivity.this, BusActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void displayOrderDetails(int orderId) {
        // For simplicity, just display the ID
        tvOrderNumber.setText(String.valueOf(orderId));

        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        String currentDate = dateFormat.format(System.currentTimeMillis());
        tvOrderDate.setText(currentDate);

        // Set estimated delivery (3 days from now)
        String estimatedDate = dateFormat.format(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000);
        tvEstimatedDelivery.setText(estimatedDate);

        // Set a placeholder total
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        tvOrderTotal.setText(formatter.format(0.00)); // This would ideally be retrieved from the order
    }
}