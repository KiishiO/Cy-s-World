package com.example.own_example.models;

import java.util.ArrayList;
import java.util.List;

public class DiningHall {
    private long id;
    private String name;
    private String location;
    private boolean isOpen;
    private String hours;
    private String popularItem;
    private int busynessLevel; // 0-100
    private List<MenuCategory> menuCategories;

    public DiningHall() {
        this.menuCategories = new ArrayList<>();
    }

    public DiningHall(long id, String name, String location, boolean isOpen, String hours, String popularItem, int busynessLevel) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.isOpen = isOpen;
        this.hours = hours;
        this.popularItem = popularItem;
        this.busynessLevel = busynessLevel;
        this.menuCategories = new ArrayList<>();
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getPopularItem() {
        return popularItem;
    }

    public void setPopularItem(String popularItem) {
        this.popularItem = popularItem;
    }

    public int getBusynessLevel() {
        return busynessLevel;
    }

    public void setBusynessLevel(int busynessLevel) {
        this.busynessLevel = busynessLevel;
    }

    public List<MenuCategory> getMenuCategories() {
        return menuCategories;
    }

    public void setMenuCategories(List<MenuCategory> menuCategories) {
        this.menuCategories = menuCategories;
    }

    public void addMenuCategory(MenuCategory category) {
        this.menuCategories.add(category);
    }

    /**
     * Class representing a menu category (Breakfast, Lunch, Dinner, etc.)
     */
    public static class MenuCategory {
        private String name;
        private List<MenuItem> items;

        public MenuCategory(String name) {
            this.name = name;
            this.items = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<MenuItem> getItems() {
            return items;
        }

        public void setItems(List<MenuItem> items) {
            this.items = items;
        }

        public void addItem(MenuItem item) {
            this.items.add(item);
        }
    }

    /**
     * Class representing a menu item with dietary information
     */
    public static class MenuItem {
        private String name;
        private String description;
        private List<String> allergens;
        private boolean isVegetarian;
        private boolean isVegan;
        private boolean isGlutenFree;
        private String nutritionalInfo;
        private int calories;
        private double protein; // in grams
        private double carbs;   // in grams
        private double fat;     // in grams

        public MenuItem(String name, String description) {
            this.name = name;
            this.description = description;
            this.allergens = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getAllergens() {
            return allergens;
        }

        public void setAllergens(List<String> allergens) {
            this.allergens = allergens;
        }

        public void addAllergen(String allergen) {
            this.allergens.add(allergen);
        }

        public boolean isVegetarian() {
            return isVegetarian;
        }

        public void setVegetarian(boolean vegetarian) {
            isVegetarian = vegetarian;
        }

        public boolean isVegan() {
            return isVegan;
        }

        public void setVegan(boolean vegan) {
            isVegan = vegan;
        }

        public boolean isGlutenFree() {
            return isGlutenFree;
        }

        public void setGlutenFree(boolean glutenFree) {
            isGlutenFree = glutenFree;
        }

        public String getNutritionalInfo() {
            return nutritionalInfo;
        }

        public void setNutritionalInfo(String nutritionalInfo) {
            this.nutritionalInfo = nutritionalInfo;
        }

        public int getCalories() {
            return calories;
        }

        public void setCalories(int calories) {
            this.calories = calories;
        }

        public double getProtein() {
            return protein;
        }

        public void setProtein(double protein) {
            this.protein = protein;
        }

        public double getCarbs() {
            return carbs;
        }

        public void setCarbs(double carbs) {
            this.carbs = carbs;
        }

        public double getFat() {
            return fat;
        }

        public void setFat(double fat) {
            this.fat = fat;
        }

        /**
         * Generate a formatted nutritional info string
         * @return Formatted string with nutritional information
         */
        public String getNutritionalInfoFormatted() {
            StringBuilder sb = new StringBuilder();
            sb.append("Calories: ").append(calories).append("\n");
            sb.append("Protein: ").append(protein).append("g\n");
            sb.append("Carbohydrates: ").append(carbs).append("g\n");
            sb.append("Fat: ").append(fat).append("g");
            return sb.toString();
        }

        /**
         * Check if this menu item contains a specific allergen
         * @param allergen Allergen to check for
         * @return True if the item contains the allergen
         */
        public boolean containsAllergen(String allergen) {
            return allergens.contains(allergen.toLowerCase());
        }
    }

    /**
     * Get a formatted string with the dining hall hours and status
     * @return Formatted string with hours and status
     */
    public String getFormattedHoursAndStatus() {
        String status = isOpen ? "Open" : "Closed";
        return hours + " • " + status;
    }

    /**
     * Check if the dining hall has a specific menu category
     * @param categoryName Name of the category to check for
     * @return True if the dining hall has the category
     */
    public boolean hasMenuCategory(String categoryName) {
        for (MenuCategory category : menuCategories) {
            if (category.getName().equalsIgnoreCase(categoryName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a menu category by name
     * @param categoryName Name of the category to get
     * @return The menu category, or null if not found
     */
    public MenuCategory getMenuCategoryByName(String categoryName) {
        for (MenuCategory category : menuCategories) {
            if (category.getName().equalsIgnoreCase(categoryName)) {
                return category;
            }
        }
        return null;
    }

    /**
     * Count the total number of menu items across all categories
     * @return Total number of menu items
     */
    public int getTotalMenuItemCount() {
        int count = 0;
        for (MenuCategory category : menuCategories) {
            count += category.getItems().size();
        }
        return count;
    }

    /**
     * Get a string describing how busy the dining hall is based on the busyness level
     * @return String description of busyness
     */
    public String getBusynessDescription() {
        if (busynessLevel < 30) {
            return "Not busy";
        } else if (busynessLevel < 60) {
            return "Moderately busy";
        } else if (busynessLevel < 85) {
            return "Busy";
        } else {
            return "Very busy";
        }
    }
}