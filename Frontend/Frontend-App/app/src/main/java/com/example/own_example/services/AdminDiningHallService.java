package com.example.own_example.services;

import android.content.Context;
import android.util.Log;

import com.example.own_example.models.DiningHall;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing dining halls as an administrator
 */
public class AdminDiningHallService {
    private static final String TAG = "AdminDiningHallService";

    private Context context;
    private String adminUsername;
    private boolean isConnected = false;
    private AdminDiningHallListener listener;

    // Mock data storage for demonstration
    private List<DiningHall> diningHalls = new ArrayList<>();
    private long nextDiningHallId = 1;

    /**
     * Interface for admin dining hall operations callbacks
     */
    public interface AdminDiningHallListener {
        void onDiningHallsUpdated(List<DiningHall> diningHalls);
        void onDiningHallCreated(DiningHall diningHall);
        void onDiningHallUpdated(DiningHall diningHall);
        void onDiningHallDeleted(long diningHallId);
        void onConnectionStateChanged(boolean connected);
        void onError(String errorMessage);
    }

    public AdminDiningHallService(Context context, String adminUsername, AdminDiningHallListener listener) {
        this.context = context;
        this.adminUsername = adminUsername;
        this.listener = listener;

        // Initialize connection (in a real app, this would connect to a backend)
        initialize();
    }

    private void initialize() {
        // In a real app, this would establish connection to backend services
        // For now, we'll just simulate a connection
        setConnected(true);

        // Create some initial mock data
        createInitialMockData();
    }

    private void createInitialMockData() {
        // Create some mock dining halls if empty to help showcase features
        if (diningHalls.isEmpty()) {
            // UDCC
            DiningHall udcc = new DiningHall(nextDiningHallId++, "Union Drive Community Center",
                    "West Campus", true, "7:00 AM - 8:00 PM", "Brick Oven Pizza", 75);
            createMenuCategoriesForDiningHall(udcc);
            diningHalls.add(udcc);

            // Seasons
            DiningHall seasons = new DiningHall(nextDiningHallId++, "Seasons Dining",
                    "Central Campus", true, "6:30 AM - 7:30 PM", "Mongolian Grill", 60);
            createMenuCategoriesForDiningHall(seasons);
            diningHalls.add(seasons);

            // Memorial Union
            DiningHall memorial = new DiningHall(nextDiningHallId++, "Memorial Union",
                    "Central Campus", true, "7:30 AM - 8:00 PM", "Stir-Fry Rice Bowl", 85);
            createMenuCategoriesForDiningHall(memorial);
            diningHalls.add(memorial);
        }
    }

    private void createMenuCategoriesForDiningHall(DiningHall diningHall) {
        // Add basic menu categories for each dining hall
        DiningHall.MenuCategory breakfast = new DiningHall.MenuCategory("Breakfast");
        breakfast.addItem(createMenuItem("Scrambled Eggs", "Farm fresh eggs scrambled to perfection",
                new String[]{"eggs"}, true, false, true));
        breakfast.addItem(createMenuItem("Pancakes", "Fluffy buttermilk pancakes with maple syrup",
                new String[]{"wheat", "dairy"}, true, false, false));
        diningHall.addMenuCategory(breakfast);

        DiningHall.MenuCategory lunch = new DiningHall.MenuCategory("Lunch");
        lunch.addItem(createMenuItem("Cheeseburger", "Angus beef patty with cheddar on brioche bun",
                new String[]{"wheat", "dairy"}, false, false, false));
        lunch.addItem(createMenuItem("Garden Salad", "Mixed greens with seasonal vegetables",
                new String[]{"none"}, true, true, true));
        diningHall.addMenuCategory(lunch);

        DiningHall.MenuCategory dinner = new DiningHall.MenuCategory("Dinner");
        dinner.addItem(createMenuItem("Roasted Chicken", "Herb-roasted chicken with vegetables",
                new String[]{"none"}, false, false, true));
        dinner.addItem(createMenuItem("Vegetable Pasta", "Pasta with seasonal vegetables and sauce",
                new String[]{"wheat", "dairy"}, true, false, false));
        diningHall.addMenuCategory(dinner);
    }

    private DiningHall.MenuItem createMenuItem(String name, String description, String[] allergens,
                                               boolean vegetarian, boolean vegan, boolean glutenFree) {
        DiningHall.MenuItem item = new DiningHall.MenuItem(name, description);
        item.setVegetarian(vegetarian);
        item.setVegan(vegan);
        item.setGlutenFree(glutenFree);

        for (String allergen : allergens) {
            item.addAllergen(allergen);
        }

        return item;
    }

    /**
     * Load all dining halls
     */
    public void loadDiningHalls() {
        if (!isConnected) {
            listener.onError("Not connected to service");
            return;
        }

        // In a real app, this would fetch from a server
        // Just return our mock data
        listener.onDiningHallsUpdated(new ArrayList<>(diningHalls));
    }

    /**
     * Create a new dining hall
     */
    public void createDiningHall(String name, String location, boolean isOpen,
                                 String hours, String popularItem, int busynessLevel) {
        if (!isConnected) {
            listener.onError("Not connected to service");
            return;
        }

        try {
            // Create new dining hall
            DiningHall newDiningHall = new DiningHall(
                    nextDiningHallId++, name, location, isOpen, hours, popularItem, busynessLevel);

            // Add default menu categories
            createMenuCategoriesForDiningHall(newDiningHall);

            // Add to list
            diningHalls.add(newDiningHall);

            // Notify listener
            listener.onDiningHallCreated(newDiningHall);
        } catch (Exception e) {
            Log.e(TAG, "Error creating dining hall", e);
            listener.onError("Failed to create dining hall: " + e.getMessage());
        }
    }

    /**
     * Update an existing dining hall
     */
    public void updateDiningHall(long id, String name, String location, boolean isOpen,
                                 String hours, String popularItem, int busynessLevel) {
        if (!isConnected) {
            listener.onError("Not connected to service");
            return;
        }

        try {
            // Find dining hall
            DiningHall diningHallToUpdate = null;
            for (DiningHall diningHall : diningHalls) {
                if (diningHall.getId() == id) {
                    diningHallToUpdate = diningHall;
                    break;
                }
            }

            if (diningHallToUpdate == null) {
                listener.onError("Dining hall not found");
                return;
            }

            // Update fields
            diningHallToUpdate.setName(name);
            diningHallToUpdate.setLocation(location);
            diningHallToUpdate.setOpen(isOpen);
            diningHallToUpdate.setHours(hours);
            diningHallToUpdate.setPopularItem(popularItem);
            diningHallToUpdate.setBusynessLevel(busynessLevel);

            // Notify listener
            listener.onDiningHallUpdated(diningHallToUpdate);
        } catch (Exception e) {
            Log.e(TAG, "Error updating dining hall", e);
            listener.onError("Failed to update dining hall: " + e.getMessage());
        }
    }

    /**
     * Delete a dining hall
     */
    public void deleteDiningHall(long id) {
        if (!isConnected) {
            listener.onError("Not connected to service");
            return;
        }

        try {
            // Find dining hall index
            int indexToRemove = -1;
            for (int i = 0; i < diningHalls.size(); i++) {
                if (diningHalls.get(i).getId() == id) {
                    indexToRemove = i;
                    break;
                }
            }

            if (indexToRemove == -1) {
                listener.onError("Dining hall not found");
                return;
            }

            // Remove from list
            diningHalls.remove(indexToRemove);

            // Notify listener
            listener.onDiningHallDeleted(id);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting dining hall", e);
            listener.onError("Failed to delete dining hall: " + e.getMessage());
        }
    }

    /**
     * Get a specific dining hall by ID
     */
    public DiningHall getDiningHallById(long id) {
        for (DiningHall diningHall : diningHalls) {
            if (diningHall.getId() == id) {
                return diningHall;
            }
        }
        return null;
    }

    /**
     * Add a menu item to a dining hall
     */
    public void addMenuItem(long diningHallId, String categoryName, String name, String description,
                            String[] allergens, boolean vegetarian, boolean vegan, boolean glutenFree) {
        if (!isConnected) {
            listener.onError("Not connected to service");
            return;
        }

        try {
            // Find dining hall
            DiningHall diningHall = getDiningHallById(diningHallId);
            if (diningHall == null) {
                listener.onError("Dining hall not found");
                return;
            }

            // Find or create category
            DiningHall.MenuCategory category = diningHall.getMenuCategoryByName(categoryName);
            if (category == null) {
                category = new DiningHall.MenuCategory(categoryName);
                diningHall.addMenuCategory(category);
            }

            // Create and add menu item
            DiningHall.MenuItem menuItem = createMenuItem(name, description, allergens, vegetarian, vegan, glutenFree);
            category.addItem(menuItem);

            // Notify listener
            listener.onDiningHallUpdated(diningHall);
        } catch (Exception e) {
            Log.e(TAG, "Error adding menu item", e);
            listener.onError("Failed to add menu item: " + e.getMessage());
        }
    }

    /**
     * Update a menu item
     */
    public void updateMenuItem(long diningHallId, String categoryName, int itemIndex, String name, String description,
                               String[] allergens, boolean vegetarian, boolean vegan, boolean glutenFree) {
        if (!isConnected) {
            listener.onError("Not connected to service");
            return;
        }

        try {
            // Find dining hall
            DiningHall diningHall = getDiningHallById(diningHallId);
            if (diningHall == null) {
                listener.onError("Dining hall not found");
                return;
            }

            // Find category
            DiningHall.MenuCategory category = diningHall.getMenuCategoryByName(categoryName);
            if (category == null) {
                listener.onError("Menu category not found");
                return;
            }

            // Validate item index
            if (itemIndex < 0 || itemIndex >= category.getItems().size()) {
                listener.onError("Menu item index out of range");
                return;
            }

            // Update menu item
            DiningHall.MenuItem menuItem = category.getItems().get(itemIndex);
            menuItem.setName(name);
            menuItem.setDescription(description);
            menuItem.setAllergens(new ArrayList<>());
            for (String allergen : allergens) {
                menuItem.addAllergen(allergen);
            }
            menuItem.setVegetarian(vegetarian);
            menuItem.setVegan(vegan);
            menuItem.setGlutenFree(glutenFree);

            // Notify listener
            listener.onDiningHallUpdated(diningHall);
        } catch (Exception e) {
            Log.e(TAG, "Error updating menu item", e);
            listener.onError("Failed to update menu item: " + e.getMessage());
        }
    }

    /**
     * Delete a menu item
     */
    public void deleteMenuItem(long diningHallId, String categoryName, int itemIndex) {
        if (!isConnected) {
            listener.onError("Not connected to service");
            return;
        }

        try {
            // Find dining hall
            DiningHall diningHall = getDiningHallById(diningHallId);
            if (diningHall == null) {
                listener.onError("Dining hall not found");
                return;
            }

            // Find category
            DiningHall.MenuCategory category = diningHall.getMenuCategoryByName(categoryName);
            if (category == null) {
                listener.onError("Menu category not found");
                return;
            }

            // Validate item index
            if (itemIndex < 0 || itemIndex >= category.getItems().size()) {
                listener.onError("Menu item index out of range");
                return;
            }

            // Remove menu item
            category.getItems().remove(itemIndex);

            // Notify listener
            listener.onDiningHallUpdated(diningHall);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting menu item", e);
            listener.onError("Failed to delete menu item: " + e.getMessage());
        }
    }

    /**
     * Add a new menu category
     */
    public void addMenuCategory(long diningHallId, String categoryName) {
        if (!isConnected) {
            listener.onError("Not connected to service");
            return;
        }

        try {
            // Find dining hall
            DiningHall diningHall = getDiningHallById(diningHallId);
            if (diningHall == null) {
                listener.onError("Dining hall not found");
                return;
            }

            // Check if category already exists
            if (diningHall.hasMenuCategory(categoryName)) {
                listener.onError("Category already exists");
                return;
            }

            // Add new category
            diningHall.addMenuCategory(new DiningHall.MenuCategory(categoryName));

            // Notify listener
            listener.onDiningHallUpdated(diningHall);
        } catch (Exception e) {
            Log.e(TAG, "Error adding menu category", e);
            listener.onError("Failed to add menu category: " + e.getMessage());
        }
    }

    /**
     * Delete a menu category
     */
    public void deleteMenuCategory(long diningHallId, String categoryName) {
        if (!isConnected) {
            listener.onError("Not connected to service");
            return;
        }

        try {
            // Find dining hall
            DiningHall diningHall = getDiningHallById(diningHallId);
            if (diningHall == null) {
                listener.onError("Dining hall not found");
                return;
            }

            // Find category index
            int categoryIndex = -1;
            for (int i = 0; i < diningHall.getMenuCategories().size(); i++) {
                if (diningHall.getMenuCategories().get(i).getName().equals(categoryName)) {
                    categoryIndex = i;
                    break;
                }
            }

            if (categoryIndex == -1) {
                listener.onError("Category not found");
                return;
            }

            // Remove category
            diningHall.getMenuCategories().remove(categoryIndex);

            // Notify listener
            listener.onDiningHallUpdated(diningHall);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting menu category", e);
            listener.onError("Failed to delete menu category: " + e.getMessage());
        }
    }

    private void setConnected(boolean connected) {
        this.isConnected = connected;
        listener.onConnectionStateChanged(connected);
    }

    /**
     * Disconnect from the service
     */
    public void disconnect() {
        setConnected(false);
    }
}