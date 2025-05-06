package com.example.own_example.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.own_example.VolleySingleton;
import com.example.own_example.models.OrderModels.CartItemModel;
import com.example.own_example.models.OrderModels.OrderModel;
import com.example.own_example.models.ProductsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for cart management and order operations
 */
public class OrderService {
    private static final String TAG = "OrderService";
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080";
    private static final String PREF_NAME = "CartPreferences";
    private Context context;
    private List<CartItemModel> cartItems;

    public interface OrderCallback {
        void onSuccess(OrderModel order);
        void onError(String message);
    }

    public interface OrderListCallback {
        void onSuccess(List<OrderModel> orders);
        void onError(String message);
    }

    public OrderService(Context context) {
        this.context = context;
        this.cartItems = new ArrayList<>();
        loadCartFromPreferences();
    }

    // --- CART MANAGEMENT ---

    /**
     * Add a product to the cart
     */
    public void addToCart(ProductsModel product, int quantity) {
        // Check if product already in cart
        for (CartItemModel item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                // Update quantity
                item.setQuantity(item.getQuantity() + quantity);
                saveCartToPreferences();
                return;
            }
        }

        // Add new item
        cartItems.add(new CartItemModel(product, quantity));
        saveCartToPreferences();
    }

    /**
     * Update quantity of a cart item
     */
    public void updateCartItemQuantity(int productId, int quantity) {
        for (CartItemModel item : cartItems) {
            if (item.getProduct().getId() == productId) {
                if (quantity <= 0) {
                    // Remove item if quantity is 0 or negative
                    cartItems.remove(item);
                } else {
                    // Update quantity
                    item.setQuantity(quantity);
                }
                saveCartToPreferences();
                return;
            }
        }
    }

    /**
     * Remove a product from the cart
     */
    public void removeFromCart(int productId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getProduct().getId() == productId) {
                cartItems.remove(i);
                saveCartToPreferences();
                return;
            }
        }
    }

    /**
     * Clear the cart
     */
    public void clearCart() {
        cartItems.clear();
        saveCartToPreferences();
    }

    /**
     * Get all items in the cart
     */
    public List<CartItemModel> getCartItems() {
        return cartItems;
    }

    /**
     * Get total price of all items in the cart
     */
    public double getCartTotal() {
        double total = 0;
        for (CartItemModel item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * Get number of items in the cart
     */
    public int getCartItemCount() {
        int count = 0;
        for (CartItemModel item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    /**
     * Save cart to SharedPreferences
     */
    private void saveCartToPreferences() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            JSONArray cartArray = new JSONArray();
            for (CartItemModel item : cartItems) {
                JSONObject itemJson = new JSONObject();
                JSONObject productJson = new JSONObject();

                productJson.put("id", item.getProduct().getId());
                productJson.put("item", item.getProduct().getItem());
                productJson.put("price", item.getProduct().getPrice());

                itemJson.put("product", productJson);
                itemJson.put("quantity", item.getQuantity());

                cartArray.put(itemJson);
            }

            editor.putString("cart", cartArray.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load cart from SharedPreferences
     */
    private void loadCartFromPreferences() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String cartJson = prefs.getString("cart", "");

        if (!cartJson.isEmpty()) {
            try {
                JSONArray cartArray = new JSONArray(cartJson);
                cartItems.clear();

                for (int i = 0; i < cartArray.length(); i++) {
                    JSONObject itemJson = cartArray.getJSONObject(i);
                    JSONObject productJson = itemJson.getJSONObject("product");

                    int id = productJson.getInt("id");
                    String name = productJson.getString("item");
                    double price = productJson.getDouble("price");

                    // Create product using the constructor available and set id separately
                    ProductsModel product = new ProductsModel(name, price);
                    product.setId(id);

                    int quantity = itemJson.getInt("quantity");

                    cartItems.add(new CartItemModel(product, quantity));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // --- ORDER API CALLS ---

    /**
     * Create an order
     */
    public void createOrder(int personId, final OrderCallback callback) {
        if (cartItems.isEmpty()) {
            callback.onError("Cart is empty");
            return;
        }

        String url = BASE_URL + "/orders";

        try {
            // Create order from cart
            OrderModel order = new OrderModel(cartItems, personId);
            JSONObject orderJson = order.toJson();

            // Log the request payload
            Log.d(TAG, "Creating order with URL: " + url);
            Log.d(TAG, "Order payload: " + orderJson.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    orderJson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Order created successfully: " + response.toString());
                            try {
                                OrderModel newOrder = OrderModel.fromJson(response);
                                // Clear cart after successful order
                                clearCart();
                                callback.onSuccess(newOrder);
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
                                callback.onError("JSON parsing error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String errorMsg = "Network error";
                            if (error.networkResponse != null) {
                                errorMsg += ": " + error.networkResponse.statusCode;

                                // If there's a response body, log it
                                if (error.networkResponse.data != null) {
                                    try {
                                        String responseBody = new String(error.networkResponse.data, "utf-8");
                                        Log.e(TAG, "Error response body: " + responseBody);
                                        errorMsg += " - " + responseBody;
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing error response", e);
                                    }
                                }
                            } else if (error.getMessage() != null) {
                                errorMsg += ": " + error.getMessage();
                            }

                            Log.e(TAG, "Error creating order: " + errorMsg, error);
                            callback.onError(errorMsg);
                        }
                    }
            );

            VolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage(), e);
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    /**
     * Get all orders
     */
    public void getAllOrders(final OrderListCallback callback) {
        String url = BASE_URL + "/orders";

        Log.d(TAG, "Fetching all orders from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "Orders fetched successfully: " + response.toString());
                        List<OrderModel> orders = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject orderJson = response.getJSONObject(i);
                                OrderModel order = OrderModel.fromJson(orderJson);
                                orders.add(order);
                            }
                            callback.onSuccess(orders);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
                            callback.onError("JSON parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = "Network error";
                        if (error.networkResponse != null) {
                            errorMsg += ": " + error.networkResponse.statusCode;
                        } else if (error.getMessage() != null) {
                            errorMsg += ": " + error.getMessage();
                        }

                        Log.e(TAG, "Error fetching orders: " + errorMsg, error);
                        callback.onError(errorMsg);
                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}