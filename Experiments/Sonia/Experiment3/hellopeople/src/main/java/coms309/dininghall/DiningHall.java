package coms309.dininghall;

import java.util.HashMap;

public class DiningHall {
    private String name;
    private String location;
    private HashMap<String, Meal> mealList = new HashMap<>();

    public DiningHall(String name, String location) {
        this.name = name;
        this.location = location;
    }

    // Add a meal to the dining hall
    public void addMeal(Meal meal) {
        mealList.put(meal.getName(), meal);
    }

    // Get all meals
    public HashMap<String, Meal> getMeals() {
        return mealList;
    }

    // Get a specific meal by name
    public Meal getMeal(String mealName) {
        return mealList.get(mealName);
    }

    // Remove a meal
    public void removeMeal(String mealName) {
        mealList.remove(mealName);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
