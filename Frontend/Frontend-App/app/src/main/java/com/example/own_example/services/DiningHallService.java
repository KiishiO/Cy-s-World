package com.example.own_example.services;

import android.content.Context;
import android.util.Log;

import com.example.own_example.api.RetrofitClient;
import com.example.own_example.models.DiningHall;

import java.util.ArrayList;
import java.util.List;

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
        default void onDiningHallDeleted(long diningHallId) {}
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
        loadDiningHalls();
    }

    /**
     * Load all dining halls
     */
    public void loadDiningHalls() {
        RetrofitClient.getInstance().getApiService().getAllDiningHalls().enqueue(new Callback<List<DiningHall>>() {
            @Override
            public void onResponse(Call<List<DiningHall>> call, Response<List<DiningHall>> response) {
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
            }

            @Override
            public void onFailure(Call<List<DiningHall>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
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
    public void getDiningHallById(long id) {
        RetrofitClient.getInstance().getApiService().getDiningHallById((int) id).enqueue(new Callback<DiningHall>() {
            @Override
            public void onResponse(Call<DiningHall> call, Response<DiningHall> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setConnected(true);

                    // Process dining hall to add UI-specific properties
                    DiningHall processedDiningHall = processDiningHall(response.body());

                    if (listener != null) {
                        listener.onDiningHallLoaded(processedDiningHall);
                    }
                } else {
                    if (listener != null) {
                        listener.onError("Failed to get dining hall: " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<DiningHall> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                if (listener != null) {
                    listener.onError("Network error: " + t.getMessage());
                }
            }
        });
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
    public void updateDiningHall(long id, String name, String location, boolean isOpen,
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
    public void deleteDiningHall(long id) {
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
    public void addMenuItem(long diningHallId, String categoryName, String name, String description,
                            String[] allergens, boolean vegetarian, boolean vegan, boolean glutenFree) {
        if (!isConnected) {
            if (listener != null) {
                listener.onError("Not connected to service");
            }
            return;
        }

        try {
            // Create the menu item
            DiningHall.MenuItem menuItem = new DiningHall.MenuItem();
            menuItem.setName(name);
            menuItem.setDescription(description);
            menuItem.setVegetarian(vegetarian);
            menuItem.setVegan(vegan);
            menuItem.setGlutenFree(glutenFree);

            // Add allergens
            if (allergens != null) {
                for (String allergen : allergens) {
                    menuItem.addAllergen(allergen);
                }
            }

            // Set price (required by backend)
            menuItem.setPrice(5.99); // Default price

            // Add menu item to the dining hall
            RetrofitClient.getInstance().getApiService().addMenuItem((int) diningHallId, menuItem)
                    .enqueue(new Callback<DiningHall.MenuItem>() {
                        @Override
                        public void onResponse(Call<DiningHall.MenuItem> call, Response<DiningHall.MenuItem> response) {
                            if (response.isSuccessful()) {
                                // Refresh dining hall data
                                getDiningHallById(diningHallId);
                            } else {
                                if (listener != null) {
                                    listener.onError("Failed to add menu item: " + response.message());
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
    public void updateMenuItem(long diningHallId, String categoryName, int itemIndex, String name, String description,
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
    public void deleteMenuItem(long diningHallId, String categoryName, int itemIndex) {
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
    public void addMenuCategory(long diningHallId, String categoryName) {
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
    public void deleteMenuCategory(long diningHallId, String categoryName) {
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
        diningHall.setOpen(true); // Assume open by default
        diningHall.setHours("7:00 AM - 8:00 PM"); // Default hours

        // Set popular item based on first menu item if available
        if (diningHall.getMenuItems() != null && !diningHall.getMenuItems().isEmpty()) {
            diningHall.setPopularItem(diningHall.getMenuItems().get(0).getName());
        } else {
            diningHall.setPopularItem("No items available");
        }

        // Set default busyness level
        diningHall.setBusynessLevel(50);

        // Initialize menu categories based on menu items
        diningHall.initializeCategories();

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