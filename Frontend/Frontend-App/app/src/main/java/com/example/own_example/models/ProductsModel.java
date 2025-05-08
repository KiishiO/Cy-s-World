package com.example.own_example.models;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductsModel {
    private int id;
    private String item;
    private double price;

    public ProductsModel() {
    }

    public ProductsModel(String item, double price) {
        this.item = item;
        this.price = price;
    }

    // Parse from JSON
    public static ProductsModel fromJson(JSONObject jsonObject) throws JSONException {
        ProductsModel model = new ProductsModel();
        model.id = jsonObject.getInt("id");
        model.item = jsonObject.getString("item");
        model.price = jsonObject.getDouble("price");
        return model;
    }

    // Convert to JSON
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (id > 0) {
            jsonObject.put("id", id);
        }
        jsonObject.put("item", item);
        jsonObject.put("price", price);
        return jsonObject;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}