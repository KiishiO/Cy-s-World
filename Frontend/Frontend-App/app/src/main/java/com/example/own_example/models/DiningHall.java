package com.example.own_example.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class DiningHall {

    private int id;
    private String name;
    private String location;

    @SerializedName("menuItems")
    private List<MenuItem> menuItems;

    // UI-specific fields (not in backend)
    private transient boolean isOpen;
    private transient String hours;
    private transient String popularItem;
    private transient int busynessLevel;
    private transient List<MenuCategory> menuCategories;

    public DiningHall() {
        menuItems = new ArrayList<>();
        menuCategories = new ArrayList<>();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
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

    public int getTotalMenuItemCount() {
        // Count all menu items
        if (menuItems == null) {
            return 0;
        }
        return menuItems.size();
    }

    public List<MenuCategory> getMenuCategories() {
        return menuCategories;
    }

    public void setMenuCategories(List<MenuCategory> menuCategories) {
        this.menuCategories = menuCategories;
    }

    // Helper method to get a category by name
    public MenuCategory getMenuCategoryByName(String name) {
        if (menuCategories != null) {
            for (MenuCategory category : menuCategories) {
                if (category.getName().equals(name)) {
                    return category;
                }
            }
        }
        return null;
    }

    // Add a menu category
    public void addMenuCategory(MenuCategory category) {
        menuCategories.add(category);
    }

    // Initialize categories based on menu items
    public void initializeCategories() {
        // Default category for uncategorized items
        MenuCategory defaultCategory = new MenuCategory("Main Menu");

        // Add all menu items to the default category for now
        if (menuItems != null) {
            for (MenuItem item : menuItems) {
                defaultCategory.addItem(item);
            }
        }

        // Clear and add the default category
        menuCategories.clear();
        menuCategories.add(defaultCategory);
    }

    // Inner class for menu items to match backend structure
    public static class MenuItem {
        private int id;
        private String name;
        private double price;
        private String menuType;

        // Prevent circular references with @JsonIgnore or @Expose annotations
        @SerializedName("diningHall")
        @Expose(serialize = false) // Only deserialize, don't serialize
        private transient DiningHall diningHall;

        // UI-specific fields
        @Expose(serialize = false)
        private transient String description;
        @Expose(serialize = false)
        private transient List<String> allergens;
        @Expose(serialize = false)
        private transient boolean vegetarian;
        @Expose(serialize = false)
        private transient boolean vegan;
        @Expose(serialize = false)
        private transient boolean glutenFree;

        public MenuItem() {
            allergens = new ArrayList<>();
        }

        // Getters and setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
        // Getters and setters
        public String getMenuType() { return menuType;}
        public void setMenuType(String menuType) { this.menuType = menuType;}
        public List<String> getAllergens() {
            return allergens;
        }

        public void setAllergens(List<String> allergens) {
            this.allergens = allergens;
        }

        public void addAllergen(String allergen) {
            allergens.add(allergen);
        }

        public boolean isVegetarian() {
            return vegetarian;
        }

        public void setVegetarian(boolean vegetarian) {
            this.vegetarian = vegetarian;
        }

        public boolean isVegan() {
            return vegan;
        }

        public void setVegan(boolean vegan) {
            this.vegan = vegan;
        }

        public boolean isGlutenFree() {
            return glutenFree;
        }

        public void setGlutenFree(boolean glutenFree) {
            this.glutenFree = glutenFree;
        }
    }

    // Inner class for menu categories (frontend only)
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
            if (items == null) {
                items = new ArrayList<>();
            }
            items.add(item);
        }
    }
}