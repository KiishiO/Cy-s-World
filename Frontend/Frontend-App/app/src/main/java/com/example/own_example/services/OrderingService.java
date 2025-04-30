package com.example.own_example.services;

import android.content.Context;
import android.util.Log;

import com.example.own_example.api.DiningOrderApiService;
import com.example.own_example.api.RetrofitClient;
import com.example.own_example.models.DiningOrder;
import com.example.own_example.models.DiningOrderItem;
import com.example.own_example.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderingService {
    private static final String TAG = "OrderingService";

    private static OrderingService instance;
    private final Context context;
    private OrderListener listener;
    private OrderHistoryListener orderHistoryListener;

    // Retrofit service
    private final DiningOrderApiService apiService;

    public interface OrderListener {
        void onOrderPlaced(int orderId);
        void onOrderError(String errorMessage);
    }

    public interface OrderHistoryListener {
        void onOrderHistoryLoaded(List<DiningOrder> orderHistory);
        void onOrderHistoryError(String errorMessage);
    }

    private OrderingService(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getInstance().getDiningOrderApiService();
    }

    public static synchronized OrderingService getInstance(Context context) {
        if (instance == null) {
            instance = new OrderingService(context);
        }
        return instance;
    }

    public void setListener(OrderListener listener) {
        this.listener = listener;
    }

    public void setOrderHistoryListener(OrderHistoryListener listener) {
        this.orderHistoryListener = listener;
    }

    public void placeOrder(int diningHallId, int personId, List<OrderItem> items) {
        try {
            // Create order object
            DiningOrder order = new DiningOrder();

            // Set person
            DiningOrder.Person person = new DiningOrder.Person();
            person.setId(personId);
            order.setPerson(person);

            // Create order items
            List<DiningOrderItem> orderItems = new ArrayList<>();
            for (OrderItem item : items) {
                DiningOrderItem orderItem = new DiningOrderItem();
                orderItem.setQuantity(item.getQuantity());

                // Set menu item
                DiningOrderItem.MenuItem menuItem = new DiningOrderItem.MenuItem();
                menuItem.setId(item.getMenuItemId());
                orderItem.setMenuItems(menuItem);

                orderItems.add(orderItem);
            }

            order.setItems(orderItems);

            // Call API
            apiService.createDiningOrder(order).enqueue(new Callback<DiningOrder>() {
                @Override
                public void onResponse(Call<DiningOrder> call, Response<DiningOrder> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Order placed successfully");
                        if (listener != null) {
                            listener.onOrderPlaced(response.body().getId());
                        }
                    } else {
                        String error = "Failed to place order: " + response.message();
                        Log.e(TAG, error);
                        if (listener != null) {
                            listener.onOrderError(error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<DiningOrder> call, Throwable t) {
                    String error = "Network error: " + t.getMessage();
                    Log.e(TAG, error, t);
                    if (listener != null) {
                        listener.onOrderError(error);
                    }
                }
            });
        } catch (Exception e) {
            String error = "Error placing order: " + e.getMessage();
            Log.e(TAG, error, e);
            if (listener != null) {
                listener.onOrderError(error);
            }
        }
    }

    public void getOrderHistory(int userId) {
        try {
            apiService.getDiningOrders().enqueue(new Callback<List<DiningOrder>>() {
                @Override
                public void onResponse(Call<List<DiningOrder>> call, Response<List<DiningOrder>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Filter orders for current user
                        List<DiningOrder> userOrders = new ArrayList<>();
                        for (DiningOrder order : response.body()) {
                            if (order.getPerson() != null && order.getPerson().getId() == userId) {
                                userOrders.add(order);
                            }
                        }

                        if (orderHistoryListener != null) {
                            orderHistoryListener.onOrderHistoryLoaded(userOrders);
                        }
                    } else {
                        String error = "Failed to get order history: " + response.message();
                        Log.e(TAG, error);
                        if (orderHistoryListener != null) {
                            orderHistoryListener.onOrderHistoryError(error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<DiningOrder>> call, Throwable t) {
                    String error = "Network error: " + t.getMessage();
                    Log.e(TAG, error, t);
                    if (orderHistoryListener != null) {
                        orderHistoryListener.onOrderHistoryError(error);
                    }
                }
            });
        } catch (Exception e) {
            String error = "Error getting order history: " + e.getMessage();
            Log.e(TAG, error, e);
            if (orderHistoryListener != null) {
                orderHistoryListener.onOrderHistoryError(error);
            }
        }
    }
}