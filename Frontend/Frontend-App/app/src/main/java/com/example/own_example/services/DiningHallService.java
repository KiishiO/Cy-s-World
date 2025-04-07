package com.example.own_example.services;

import android.content.Context;
import android.util.Log;

import com.example.own_example.models.DiningHall;

import java.util.ArrayList;
import java.util.List;

public class DiningHallService {

    private static final String TAG = "DiningHallService";
    private Context context;

    public DiningHallService(Context context) {
        this.context = context;
    }

    // Callback interface for getting dining halls
    public interface DiningHallsCallback {
        void onSuccess(List<DiningHall> diningHalls);
        void onError(String error);
    }

    // Callback interface for getting a single dining hall
    public interface DiningHallCallback {
        void onSuccess(DiningHall diningHall);
        void onError(String error);
    }

    /**
     * Get all dining halls
     */
    public void getDiningHalls(DiningHallsCallback callback) {
        try {
            // In a real app, this would make a network request or database query
            // For now, we'll return mock data
            callback.onSuccess(getMockDiningHalls());
        } catch (Exception e) {
            Log.e(TAG, "Error getting dining halls: " + e.getMessage());
            callback.onError("Failed to load dining halls: " + e.getMessage());
        }
    }

    /**
     * Get a specific dining hall by ID
     */
    public void getDiningHallById(long id, DiningHallCallback callback) {
        try {
            // In a real app, this would make a network request or database query
            List<DiningHall> mockHalls = getMockDiningHalls();
            for (DiningHall hall : mockHalls) {
                if (hall.getId() == id) {
                    callback.onSuccess(hall);
                    return;
                }
            }
            callback.onError("Dining hall not found with ID: " + id);
        } catch (Exception e) {
            Log.e(TAG, "Error getting dining hall: " + e.getMessage());
            callback.onError("Failed to load dining hall: " + e.getMessage());
        }
    }

    /**
     * Generate mock dining hall data
     * In a real app, this would be replaced with actual data from API or database
     */
    private List<DiningHall> getMockDiningHalls() {
        List<DiningHall> diningHalls = new ArrayList<>();

        // Union Drive Community Center
        DiningHall udcc = new DiningHall(1, "Union Drive Community Center", "West Campus", true,
                "7:00 AM - 8:00 PM", "Brick Oven Pizza", 75);

        // Add breakfast menu
        DiningHall.MenuCategory breakfast = new DiningHall.MenuCategory("Breakfast");
        breakfast.addItem(createMenuItem(
                "Scrambled Eggs",
                "Farm fresh eggs scrambled to perfection",
                new String[]{"eggs"},
                true, false, true));
        breakfast.addItem(createMenuItem(
                "Pancakes",
                "Fluffy buttermilk pancakes served with maple syrup",
                new String[]{"wheat", "dairy"},
                true, false, false));
        breakfast.addItem(createMenuItem(
                "Breakfast Burrito",
                "Flour tortilla filled with eggs, potatoes, cheese, and salsa",
                new String[]{"wheat", "eggs", "dairy"},
                true, false, false));
        udcc.addMenuCategory(breakfast);

        // Add lunch menu
        DiningHall.MenuCategory lunch = new DiningHall.MenuCategory("Lunch");
        lunch.addItem(createMenuItem(
                "Brick Oven Pizza",
                "Hand-tossed pizza baked in our brick oven with fresh mozzarella",
                new String[]{"wheat", "dairy"},
                true, false, false));
        lunch.addItem(createMenuItem(
                "Garden Salad",
                "Mixed greens with seasonal vegetables and choice of dressing",
                new String[]{"none"},
                true, true, true));
        lunch.addItem(createMenuItem(
                "Grilled Chicken Sandwich",
                "Grilled chicken breast with lettuce, tomato, and aioli on a brioche bun",
                new String[]{"wheat", "eggs"},
                false, false, false));
        udcc.addMenuCategory(lunch);

        // Add dinner menu
        DiningHall.MenuCategory dinner = new DiningHall.MenuCategory("Dinner");
        dinner.addItem(createMenuItem(
                "Pasta Primavera",
                "Whole grain pasta with seasonal vegetables in a light cream sauce",
                new String[]{"wheat", "dairy"},
                true, false, false));
        dinner.addItem(createMenuItem(
                "Grilled Salmon",
                "Atlantic salmon with lemon herb butter sauce",
                new String[]{"fish", "dairy"},
                false, false, true));
        dinner.addItem(createMenuItem(
                "Vegetable Stir Fry",
                "Fresh vegetables stir-fried with tofu in ginger soy sauce",
                new String[]{"soy"},
                true, true, true));
        udcc.addMenuCategory(dinner);

        diningHalls.add(udcc);

        // Seasons Dining
        DiningHall seasons = new DiningHall(2, "Seasons Dining", "Central Campus", true,
                "6:30 AM - 7:30 PM", "Mongolian Grill", 60);

        // Add breakfast menu
        DiningHall.MenuCategory seasonsBreakfast = new DiningHall.MenuCategory("Breakfast");
        seasonsBreakfast.addItem(createMenuItem(
                "Omelette Station",
                "Made-to-order omelettes with your choice of fillings",
                new String[]{"eggs"},
                true, false, true));
        seasonsBreakfast.addItem(createMenuItem(
                "Belgian Waffles",
                "Fresh Belgian waffles with fruit toppings and whipped cream",
                new String[]{"wheat", "dairy", "eggs"},
                true, false, false));
        seasons.addMenuCategory(seasonsBreakfast);

        // Add lunch/dinner menu
        DiningHall.MenuCategory seasonsLunch = new DiningHall.MenuCategory("Lunch");
        seasonsLunch.addItem(createMenuItem(
                "Mongolian Grill",
                "Choose your ingredients for a custom stir-fry prepared by our chefs",
                new String[]{"varies"},
                true, true, true));
        seasonsLunch.addItem(createMenuItem(
                "Deli Sandwich Bar",
                "Create your own sandwich with a variety of breads, meats, cheeses, and toppings",
                new String[]{"wheat", "dairy"},
                true, false, false));
        seasons.addMenuCategory(seasonsLunch);

        DiningHall.MenuCategory seasonsDinner = new DiningHall.MenuCategory("Dinner");
        seasonsDinner.addItem(createMenuItem(
                "Mongolian Grill",
                "Choose your ingredients for a custom stir-fry prepared by our chefs",
                new String[]{"varies"},
                true, true, true));
        seasonsDinner.addItem(createMenuItem(
                "Carved Prime Rib",
                "Slow-roasted prime rib carved to order",
                new String[]{"none"},
                false, false, true));
        seasonsDinner.addItem(createMenuItem(
                "Vegan Buddha Bowl",
                "Quinoa, roasted vegetables, and chickpeas with tahini dressing",
                new String[]{"sesame"},
                true, true, true));
        seasons.addMenuCategory(seasonsDinner);

        diningHalls.add(seasons);

        // Memorial Union
        DiningHall memorial = new DiningHall(3, "Memorial Union", "Central Campus", true,
                "7:30 AM - 8:00 PM", "Stir-Fry Rice Bowl", 85);

        DiningHall.MenuCategory memorialLunch = new DiningHall.MenuCategory("Lunch");
        memorialLunch.addItem(createMenuItem(
                "Stir-Fry Rice Bowl",
                "Jasmine rice with stir-fried vegetables and choice of protein",
                new String[]{"soy"},
                true, true, true));
        memorialLunch.addItem(createMenuItem(
                "Grilled Cheese & Soup",
                "Classic grilled cheese sandwich with tomato soup",
                new String[]{"wheat", "dairy"},
                true, false, false));
        memorial.addMenuCategory(memorialLunch);

        DiningHall.MenuCategory memorialDinner = new DiningHall.MenuCategory("Dinner");
        memorialDinner.addItem(createMenuItem(
                "Build-Your-Own Pasta",
                "Choose your pasta, sauce, and toppings for a custom pasta dish",
                new String[]{"wheat", "dairy"},
                true, false, false));
        memorialDinner.addItem(createMenuItem(
                "Rotisserie Chicken",
                "Herb-roasted rotisserie chicken with choice of sides",
                new String[]{"none"},
                false, false, true));
        memorial.addMenuCategory(memorialDinner);

        diningHalls.add(memorial);

        return diningHalls;
    }

    /**
     * Helper method to create a menu item
     */
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
}