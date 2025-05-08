package com.example.own_example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class OrderConfirmationActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvOrderTotal, tvEstimatedDelivery;
    private Button btnContinueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        // Initialize views
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvEstimatedDelivery = findViewById(R.id.tvEstimatedDelivery);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);

        // Display order details
        displayOrderDetails();

        // Set up continue shopping button
        btnContinueShopping.setOnClickListener(v -> {
            // Navigate back to BookstoreActivity
            Intent intent = new Intent(OrderConfirmationActivity.this, BookstoreActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the stack so back button doesn't go to cart
            startActivity(intent);
            finish();
        });

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(OrderConfirmationActivity.this, StudentDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_dining) {
                    Intent intent = new Intent(OrderConfirmationActivity.this, DiningHallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_buses) {
                    Intent intent = new Intent(OrderConfirmationActivity.this, BusActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }

                return false;
            });
        }
    }

    private void displayOrderDetails() {
        // Generate a random order ID
        Random random = new Random();
        int orderId = 10000 + random.nextInt(90000); // 5-digit order number
        tvOrderId.setText("Order #" + orderId);

        // Set today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        String today = dateFormat.format(new Date());
        tvOrderDate.setText("Date: " + today);

        // Set estimated delivery date (3 days from now)
        Date deliveryDate = new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000);
        String estimatedDelivery = dateFormat.format(deliveryDate);
        tvEstimatedDelivery.setText("Estimated Delivery: " + estimatedDelivery);

        // Set order total - retrieve from intent extras if available, otherwise generate a random amount
        double total = getIntent().getDoubleExtra("order_total", 0.0);
        if (total == 0.0) {
            // Generate a random amount between $10 and $100 if no total was provided
            total = 10.0 + random.nextDouble() * 90.0;
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        tvOrderTotal.setText("Total: " + formatter.format(total));
    }
}