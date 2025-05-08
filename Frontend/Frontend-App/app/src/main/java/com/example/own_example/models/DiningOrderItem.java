package com.example.own_example.models;

public class DiningOrderItem {
    private int id;
    private int quantity;
    private MenuItem menuItems;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public MenuItem getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(MenuItem menuItems) {
        this.menuItems = menuItems;
    }

    public static class MenuItem {
        private int id = 0;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}