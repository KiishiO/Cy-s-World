package com.example.own_example.services;

import android.content.Context;
import android.util.Log;

import com.example.own_example.api.RetrofitClient;
import com.example.own_example.models.DiningHall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiningHallService {

    private static final String TAG = "DiningHallService";
    private Context context;
    private String username;
    private boolean isConnected = false;

    // Instance for singleton pattern
    private static DiningHallService instance;

    // Callbacks
    private DiningHallListener listener;

    /**
     * Callback interface for dining hall operations
     */
    public interface DiningHallListener {
        // Basic callbacks used by all clients
        void onDiningHallsLoaded(List<DiningHall> diningHalls);
        void onDiningHallLoaded(DiningHall diningHall);
        void onError(String errorMessage);

        // Admin-specific callbacks
        default void onDiningHallCreated(DiningHall diningHall) {}
        default void onDiningHallUpdated(DiningHall diningHall) {}
        default void onDiningHallDeleted(int diningHallId) {}
        default void onConnectionStateChanged(boolean connected) {}
    }

    // Private constructor for singleton
    private DiningHallService(Context context) {
        this.context = context.getApplicationContext();
    }

    // Get singleton instance
    public static synchronized DiningHallService getInstance(Context context) {
        if (instance == null) {
            instance = new DiningHallService(context);
        }
        return instance;
    }

    /**
     * Set the listener for callbacks
     */
    public void setListener(DiningHallListener listener) {
        this.listener = listener;
    }

    /**
     * Set the username (for admin operations)
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Initialize the service and check connection
     */
    public void initialize() {
        // Test connection to backend by trying to load dining halls
        RetrofitClient.getInstance().getApiService().getAllDiningHalls().enqueue(new Callback<List<DiningHall>>() {
            @Override
            public void onResponse(Call<List<DiningHall>> call, Response<List<DiningHall>> response) {
                if (response.isSuccessful()) {
                    setConnected(true);  // Set connected to true here
                    // Rest of your code
                    loadDiningHalls();
                } else {
                    setConnected(false);
                }
            }

            @Override
            public void onFailure(Call<List<DiningHall>> call, Throwable t) {
                setConnected(false);
            }
        });
    }

    /**
     * Load all dining halls
     */
    public void loadDiningHalls() {
        RetrofitClient.getInstance().getApiService().getAllDiningHalls().enqueue(new Callback<List<DiningHall>>() {
            @Override
            public void onResponse(Call<List<DiningHall>> call, Response<List<DiningHall>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        setConnected(true);
                        // Process dining halls to add UI-specific properties
                        List<DiningHall> processedDiningHalls = processDiningHalls(response.body());
                        if (listener != null) {
                            listener.onDiningHallsLoaded(processedDiningHalls);
                        }
                    } else {
                        setConnected(false);
                        if (listener != null) {
                            listener.onError("Failed to get dining halls: " + response.message());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing dining halls", e);
                    setConnected(false);
                    if (listener != null) {
                        listener.onError("Error processing dining halls: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DiningHall>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);

                // Additional logging to get more details
                if (t instanceof com.google.gson.JsonSyntaxException) {
                    Log.e(TAG, "JSON Syntax Error: " + t.getMessage());
                    // Extract the response body if possible
                    try {
                        // This assumes you have access to the raw response
                        // If not, you may need to add a custom interceptor to log responses
                        // See below for an example interceptor
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to log response body", e);
                    }
                }

                setConnected(false);
                if (listener != null) {
                    listener.onError("Network error: " + t.getMessage());
                }
            }
        });
    }

    /**
     * Get a specific dining hall by ID
     */
    public void getDiningHallById(int id) {
        try {
            Log.d(TAG, "Getting dining hall with ID: " + id);
            RetrofitClient.getInstance().getApiService().getDiningHallById(id)
                    .enqueue(new Callback<DiningHall>() {
                        @Override
                        public void onResponse(Call<DiningHall> call, Response<DiningHall> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    final DiningHall diningHall = response.body();
                                    Log.d(TAG, "Retrieved dining hall: " + diningHall.getName());

                                    // Now fetch menu items separately
                                    RetrofitClient.getInstance().getApiService().getMenuItems(id)
                                            .enqueue(new Callback<List<DiningHall.MenuItem>>() {
                                                @Override
                                                public void onResponse(Call<List<DiningHall.MenuItem>> call, Response<List<DiningHall.MenuItem>> response) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        List<DiningHall.MenuItem> menuItems = response.body();
                                                        Log.d(TAG, "Retrieved " + menuItems.size() + " menu items");

                                                        // Set the menu items on the dining hall
                                                        diningHall.setMenuItems(menuItems);

                                                        // Process and send to listener
                                                        DiningHall processedDiningHall = processDiningHall(diningHall);
                                                        if (listener != null) {
                                                            listener.onDiningHallLoaded(processedDiningHall);
                                                        }
                                                    } else {
                                                        // Process without menu items
                                                        DiningHall processedDiningHall = processDiningHall(diningHall);
                                                        if (listener != null) {
                                                            listener.onDiningHallLoaded(processedDiningHall);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<List<DiningHall.MenuItem>> call, Throwable t) {
                                                    Log.e(TAG, "Failed to get menu items", t);
                                                    // Process without menu items
                                                    DiningHall processedDiningHall = processDiningHall(diningHall);
                                                    if (listener != null) {
                                                        listener.onDiningHallLoaded(processedDiningHall);
                                                    }
                                                }
                                            });
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing dining hall", e);
                                    if (listener != null) {
                                        listener.onError("Error processing dining hall: " + e.getMessage());
                                    }
                                }
                            } else {
                                Log.e(TAG, "Failed to get dining hall: " + response.message());
                                if (listener != null) {
                                    listener.onError("Failed to get dining hall: " + response.message());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DiningHall> call, Throwable t) {
                            Log.e(TAG, "API call failed", t);
                            if (listener != null) {
                                listener.onError("Failed to get dining hall: " + t.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getDiningHallById", e);
            if (listener != null) {
                listener.onError("Error retrieving dining hall: " + e.getMessage());
            }
        }
    }

    /**
     * Create a new dining hall (admin only)
     */
    public void createDiningHall(String name, String location, boolean isOpen,
                                 String hours, String popularItem, int busynessLevel) {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Not connected to service");
            }
            return;
        }

        try {
            // Create dining hall object
            DiningHall newDiningHall = new DiningHall();
            newDiningHall.setName(name);
            newDiningHall.setLocation(location);
            newDiningHall.setOpen(isOpen);
            newDiningHall.setHours(hours);
            newDiningHall.setPopularItem(popularItem);
            newDiningHall.setBusynessLevel(busynessLevel);

            // Call API to create dining hall
            RetrofitClient.getInstance().getApiService().createDiningHall(newDiningHall)
                    .enqueue(new Callback<DiningHall>() {
                        @Override
                        public void onResponse(Call<DiningHall> call, Response<DiningHall> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                DiningHall createdDiningHall = processDiningHall(response.body());
                                if (listener != null) {
                                    listener.onDiningHallCreated(createdDiningHall);
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError("Failed to create dining hall: " + response.message());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DiningHall> call, Throwable t) {
                            Log.e(TAG, "Error creating dining hall", t);
                            if (listener != null) {
                                listener.onError("Failed to create dining hall: " + t.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error creating dining hall", e);
            if (listener != null) {
                listener.onError("Failed to create dining hall: " + e.getMessage());
            }
        }
    }

    /**
     * Update an existing dining hall (admin only)
     */
    public void updateDiningHall(int id, String name, String location, boolean isOpen,
                                 String hours, String popularItem, int busynessLevel) {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Not connected to service");
            }
            return;
        }

        try {
            // Get the current dining hall
            RetrofitClient.getInstance().getApiService().getDiningHallById((int) id)
                    .enqueue(new Callback<DiningHall>() {
                        @Override
                        public void onResponse(Call<DiningHall> call, Response<DiningHall> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                // Update the fields we want to change
                                DiningHall diningHall = response.body();
                                diningHall.setName(name);
                                diningHall.setLocation(location);
                                // Note: isOpen, hours, popularItem, busynessLevel are UI-only fields
                                // not in the backend model

                                // Call API to update dining hall
                                RetrofitClient.getInstance().getApiService().updateDiningHall(diningHall.getId(), diningHall)
                                        .enqueue(new Callback<DiningHall>() {
                                            @Override
                                            public void onResponse(Call<DiningHall> call, Response<DiningHall> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    // Process the updated dining hall and add UI fields
                                                    DiningHall updatedDiningHall = processDiningHall(response.body());
                                                    updatedDiningHall.setOpen(isOpen);
                                                    updatedDiningHall.setHours(hours);
                                                    updatedDiningHall.setPopularItem(popularItem);
                                                    updatedDiningHall.setBusynessLevel(busynessLevel);

                                                    if (listener != null) {
                                                        listener.onDiningHallUpdated(updatedDiningHall);
                                                    }
                                                } else {
                                                    if (listener != null) {
                                                        listener.onError("Failed to update dining hall: " + response.message());
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<DiningHall> call, Throwable t) {
                                                Log.e(TAG, "Error updating dining hall", t);
                                                if (listener != null) {
                                                    listener.onError("Failed to update dining hall: " + t.getMessage());
                                                }
                                            }
                                        });
                            } else {
                                if (listener != null) {
                                    listener.onError("Dining hall not found");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DiningHall> call, Throwable t) {
                            Log.e(TAG, "Error getting dining hall for update", t);
                            if (listener != null) {
                                listener.onError("Failed to get dining hall: " + t.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error updating dining hall", e);
            if (listener != null) {
                listener.onError("Failed to update dining hall: " + e.getMessage());
            }
        }
    }

    /**
     * Delete a dining hall (admin only)
     */
    public void deleteDiningHall(int id) {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Not connected to service");
            }
            return;
        }

        try {
            RetrofitClient.getInstance().getApiService().deleteDiningHall((int) id)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                if (listener != null) {
                                    listener.onDiningHallDeleted(id);
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError("Failed to delete dining hall: " + response.message());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e(TAG, "Error deleting dining hall", t);
                            if (listener != null) {
                                listener.onError("Failed to delete dining hall: " + t.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting dining hall", e);
            if (listener != null) {
                listener.onError("Failed to delete dining hall: " + e.getMessage());
            }
        }
    }

    /**
     * Add a menu item to a dining hall (admin only)
     */
    public void addMenuItem(int diningHallId, String categoryName, String name, String description,
                            String[] allergens, boolean vegetarian, boolean vegan, boolean glutenFree) {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Not connected to service");
            }
            return;
        }

        try {
            // Create the menuItem with the correct structure for the backend
            Map<String, Object> menuItemMap = new HashMap<>();
            menuItemMap.put("name", name);
            menuItemMap.put("description", description);
            menuItemMap.put("menuType", categoryName);  // This is crucial!

            // Add menu item via API
            RetrofitClient.getInstance().getApiService().addMenuItem(diningHallId, menuItemMap)
                    .enqueue(new Callback<DiningHall.MenuItem>() {
                        @Override
                        public void onResponse(Call<DiningHall.MenuItem> call, Response<DiningHall.MenuItem> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Menu item added successfully to category: " + categoryName);
                                if (response.body() != null) {
                                    Log.d(TAG, "Added item: " + response.body().getName() +
                                            ", MenuType: " + response.body().getMenuType());
                                }

                                // Refresh dining hall to update UI
                                getDiningHallById(diningHallId);
                            } else {
                                // Handle error
                                String errorMsg = "Failed to add menu item: " + response.message();
                                Log.e(TAG, errorMsg);
                                if (listener != null) {
                                    listener.onError(errorMsg);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DiningHall.MenuItem> call, Throwable t) {
                            Log.e(TAG, "Error adding menu item", t);
                            if (listener != null) {
                                listener.onError("Failed to add menu item: " + t.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error adding menu item", e);
            if (listener != null) {
                listener.onError("Failed to add menu item: " + e.getMessage());
            }
        }
    }



    /**
     * Update a menu item
     * Note: This functionality is not directly supported by the backend API
     * We would need to delete and recreate the item or add custom endpoints
     */
    public void updateMenuItem(int diningHallId, String categoryName, int itemIndex, String name, String description,
                               String[] allergens, boolean vegetarian, boolean vegan, boolean glutenFree) {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Not connected to service");
            }
            return;
        }

        // Since the backend doesn't support direct updates to menu items,
        // we'll implement a custom solution that works with our UI

        try {
            // First, get the current dining hall to find the right menu item
            RetrofitClient.getInstance().getApiService().getDiningHallById((int) diningHallId)
                    .enqueue(new Callback<DiningHall>() {
                        @Override
                        public void onResponse(Call<DiningHall> call, Response<DiningHall> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                DiningHall diningHall = processDiningHall(response.body());

                                // Find the menu category
                                DiningHall.MenuCategory category = null;
                                for (DiningHall.MenuCategory cat : diningHall.getMenuCategories()) {
                                    if (cat.getName().equals(categoryName)) {
                                        category = cat;
                                        break;
                                    }
                                }

                                // If we found the category and the item index is valid
                                if (category != null && itemIndex >= 0 && itemIndex < category.getItems().size()) {
                                    // Update the item in our UI model (this won't persist to backend)
                                    DiningHall.MenuItem item = category.getItems().get(itemIndex);
                                    item.setName(name);
                                    item.setDescription(description);
                                    item.setVegetarian(vegetarian);
                                    item.setVegan(vegan);
                                    item.setGlutenFree(glutenFree);

                                    // Clear and set new allergens
                                    item.setAllergens(new ArrayList<>());
                                    if (allergens != null) {
                                        for (String allergen : allergens) {
                                            item.addAllergen(allergen);
                                        }
                                    }

                                    // Notify of the update
                                    if (listener != null) {
                                        listener.onDiningHallUpdated(diningHall);
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onError("Category or menu item not found");
                                    }
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError("Failed to get dining hall for menu item update");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DiningHall> call, Throwable t) {
                            Log.e(TAG, "Error getting dining hall for menu item update", t);
                            if (listener != null) {
                                listener.onError("Failed to get dining hall: " + t.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error updating menu item", e);
            if (listener != null) {
                listener.onError("Failed to update menu item: " + e.getMessage());
            }
        }
    }

    /**
     * Delete a menu item
     * Note: This functionality is not directly supported by the backend API
     * We would need custom endpoints
     */
    public void deleteMenuItem(int diningHallId, String categoryName, int itemIndex) {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Not connected to service");
            }
            return;
        }

        // Since the backend doesn't support direct deletion of menu items,
        // we'll implement a custom solution that works with our UI

        try {
            // First, get the current dining hall to find the right menu item
            RetrofitClient.getInstance().getApiService().getDiningHallById((int) diningHallId)
                    .enqueue(new Callback<DiningHall>() {
                        @Override
                        public void onResponse(Call<DiningHall> call, Response<DiningHall> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                DiningHall diningHall = processDiningHall(response.body());

                                // Find the menu category
                                DiningHall.MenuCategory category = null;
                                for (DiningHall.MenuCategory cat : diningHall.getMenuCategories()) {
                                    if (cat.getName().equals(categoryName)) {
                                        category = cat;
                                        break;
                                    }
                                }

                                // If we found the category and the item index is valid
                                if (category != null && itemIndex >= 0 && itemIndex < category.getItems().size()) {
                                    // Remove the item from our UI model (this won't persist to backend)
                                    category.getItems().remove(itemIndex);

                                    // Notify of the update
                                    if (listener != null) {
                                        listener.onDiningHallUpdated(diningHall);
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onError("Category or menu item not found");
                                    }
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError("Failed to get dining hall for menu item deletion");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DiningHall> call, Throwable t) {
                            Log.e(TAG, "Error getting dining hall for menu item deletion", t);
                            if (listener != null) {
                                listener.onError("Failed to get dining hall: " + t.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting menu item", e);
            if (listener != null) {
                listener.onError("Failed to delete menu item: " + e.getMessage());
            }
        }
    }

    /**
     * Add a new menu category
     * Note: Categories are not directly supported by the backend model
     * This is handled in the UI layer only
     */
    public void addMenuCategory(int diningHallId, String categoryName) {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Not connected to service");
            }
            return;
        }

        try {
            // First, get the current dining hall
            RetrofitClient.getInstance().getApiService().getDiningHallById((int) diningHallId)
                    .enqueue(new Callback<DiningHall>() {
                        @Override
                        public void onResponse(Call<DiningHall> call, Response<DiningHall> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                DiningHall diningHall = processDiningHall(response.body());

                                // Check if category already exists
                                boolean categoryExists = false;
                                for (DiningHall.MenuCategory cat : diningHall.getMenuCategories()) {
                                    if (cat.getName().equalsIgnoreCase(categoryName)) {
                                        categoryExists = true;
                                        break;
                                    }
                                }

                                if (categoryExists) {
                                    if (listener != null) {
                                        listener.onError("Category already exists");
                                    }
                                    return;
                                }

                                // Add new category to UI model
                                diningHall.addMenuCategory(new DiningHall.MenuCategory(categoryName));

                                // Notify of the update
                                if (listener != null) {
                                    listener.onDiningHallUpdated(diningHall);
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError("Failed to get dining hall for adding category");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DiningHall> call, Throwable t) {
                            Log.e(TAG, "Error getting dining hall for adding category", t);
                            if (listener != null) {
                                listener.onError("Failed to get dining hall: " + t.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error adding menu category", e);
            if (listener != null) {
                listener.onError("Failed to add menu category: " + e.getMessage());
            }
        }
    }

    /**
     * Delete a menu category
     * Note: Categories are not directly supported by the backend model
     * This is handled in the UI layer only
     */
    public void deleteMenuCategory(int diningHallId, String categoryName) {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Not connected to service");
            }
            return;
        }

        try {
            // First, get the current dining hall
            RetrofitClient.getInstance().getApiService().getDiningHallById((int) diningHallId)
                    .enqueue(new Callback<DiningHall>() {
                        @Override
                        public void onResponse(Call<DiningHall> call, Response<DiningHall> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                DiningHall diningHall = processDiningHall(response.body());

                                // Find the category to delete
                                int categoryIndex = -1;
                                for (int i = 0; i < diningHall.getMenuCategories().size(); i++) {
                                    if (diningHall.getMenuCategories().get(i).getName().equals(categoryName)) {
                                        categoryIndex = i;
                                        break;
                                    }
                                }

                                // If found, remove it
                                if (categoryIndex != -1) {
                                    diningHall.getMenuCategories().remove(categoryIndex);

                                    // Notify of the update
                                    if (listener != null) {
                                        listener.onDiningHallUpdated(diningHall);
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onError("Category not found");
                                    }
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError("Failed to get dining hall for deleting category");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DiningHall> call, Throwable t) {
                            Log.e(TAG, "Error getting dining hall for deleting category", t);
                            if (listener != null) {
                                listener.onError("Failed to get dining hall: " + t.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting menu category", e);
            if (listener != null) {
                listener.onError("Failed to delete menu category: " + e.getMessage());
            }
        }
    }

    /**
     * Process raw dining halls from API to add UI-specific properties
     */
    private List<DiningHall> processDiningHalls(List<DiningHall> diningHalls) {
        List<DiningHall> processedDiningHalls = new ArrayList<>();

        for (DiningHall diningHall : diningHalls) {
            processedDiningHalls.add(processDiningHall(diningHall));
        }

        return processedDiningHalls;
    }

    /**
     * Process a single dining hall from API to add UI-specific properties
     */
    private DiningHall processDiningHall(DiningHall diningHall) {
        // Set default UI values
        diningHall.setOpen(true);
        diningHall.setHours("7:00 AM - 8:00 PM");

        // Set popular item
        if (diningHall.getMenuItems() != null && !diningHall.getMenuItems().isEmpty()) {
            diningHall.setPopularItem(diningHall.getMenuItems().get(0).getName());
        } else {
            diningHall.setPopularItem("No items available");
        }

        diningHall.setBusynessLevel(50);

        // Log for debugging
        Log.d(TAG, "Processing dining hall with " +
                (diningHall.getMenuItems() != null ? diningHall.getMenuItems().size() : 0) +
                " menu items");

        // Create categories based on menuType
        Map<String, DiningHall.MenuCategory> categoryMap = new HashMap<>();

        // Make sure we have at least a Main Menu category
        categoryMap.put("Main Menu", new DiningHall.MenuCategory("Main Menu"));

        // Process menu items if available
        if (diningHall.getMenuItems() != null) {
            for (DiningHall.MenuItem item : diningHall.getMenuItems()) {
                String categoryName = item.getMenuType();
                if (categoryName == null || categoryName.isEmpty()) {
                    categoryName = "Main Menu";
                    item.setMenuType(categoryName);
                }

                // Ensure category exists
                if (!categoryMap.containsKey(categoryName)) {
                    categoryMap.put(categoryName, new DiningHall.MenuCategory(categoryName));
                }

                // Add item to appropriate category
                DiningHall.MenuCategory category = categoryMap.get(categoryName);
                category.addItem(item);

                Log.d(TAG, "Added item " + item.getName() + " to category " + categoryName);
            }
        }

        // Set the categories
        List<DiningHall.MenuCategory> categories = new ArrayList<>(categoryMap.values());
        diningHall.setMenuCategories(categories);

        // Log for debugging
        for (DiningHall.MenuCategory category : categories) {
            Log.d(TAG, "Category: " + category.getName() + " has " +
                    category.getItems().size() + " items");
        }

        return diningHall;
    }

    /**
     * Helper method to create a menu item
     */
    public DiningHall.MenuItem createMenuItem(String name, String description, double price,
                                              String[] allergens, boolean vegetarian,
                                              boolean vegan, boolean glutenFree) {
        DiningHall.MenuItem item = new DiningHall.MenuItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setVegetarian(vegetarian);
        item.setVegan(vegan);
        item.setGlutenFree(glutenFree);

        if (allergens != null) {
            for (String allergen : allergens) {
                item.addAllergen(allergen);
            }
        }

        return item;
    }

    private void setConnected(boolean connected) {
        this.isConnected = connected;
        if (listener != null) {
            listener.onConnectionStateChanged(connected);
        }
    }

    /**
     * Check if the service is connected to the backend
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Disconnect from the service
     */
    public void disconnect() {
        setConnected(false);
    }
}