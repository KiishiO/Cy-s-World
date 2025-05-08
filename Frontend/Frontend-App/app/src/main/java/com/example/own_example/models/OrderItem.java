package com.example.own_example.models;

public class OrderItem {
    private int menuItemId;
    private String name;
    private String description;
    private double price;
    private int quantity;

    public OrderItem(int menuItemId, String name, String description, double price, int quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}