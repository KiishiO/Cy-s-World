package com.example.own_example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.own_example.fragments.BookstoreListFragment;
import com.example.own_example.services.OrderService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BookstoreActivity extends AppCompatActivity {
    private static final String TAG = "BookstoreActivity";
    private ImageView cartIcon;
    private TextView cartBadgeCount;
    private OrderService orderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookstore);

        // Initialize services
        orderService = new OrderService(this);

        // Set up cart icon
        cartIcon = findViewById(R.id.cartIcon);
        cartBadgeCount = findViewById(R.id.cartBadgeCount);

        if (cartIcon != null) {
            cartIcon.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(BookstoreActivity.this, CartActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to CartActivity: " + e.getMessage());
                }
            });
        }

        // Update cart badge
        updateCartBadge();

        // Setup fragment container
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new BookstoreListFragment())
                    .commit();
        }

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(BookstoreActivity.this, StudentDashboardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_dining) {
                    Intent intent = new Intent(BookstoreActivity.this, DiningHallActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_buses) {
                    Intent intent = new Intent(BookstoreActivity.this, BusActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    // Changed from private to public so the fragment can call it
    public void updateCartBadge() {
        try {
            int itemCount = orderService.getCartItemCount();

            if (cartBadgeCount != null) {
                if (itemCount > 0) {
                    cartBadgeCount.setVisibility(View.VISIBLE);
                    cartBadgeCount.setText(String.valueOf(itemCount));
                } else {
                    cartBadgeCount.setVisibility(View.INVISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating cart badge: " + e.getMessage());
        }
    }
}