package com.example.own_example.models;

import org.json.JSONException;
import org.json.JSONObject;

public class BookstoreModel {
    private int id;
    private String name;
    private String location;

    public BookstoreModel() {
    }

    public BookstoreModel(String name, String location) {
        this.name = name;
        this.location = location;
    }

    // Parse from JSON
    public static BookstoreModel fromJson(JSONObject jsonObject) throws JSONException {
        BookstoreModel model = new BookstoreModel();
        model.id = jsonObject.getInt("id");
        model.name = jsonObject.getString("name");
        model.location = jsonObject.getString("location");
        return model;
    }

    // Convert to JSON
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (id > 0) {
            jsonObject.put("id", id);
        }
        jsonObject.put("name", name);
        jsonObject.put("location", location);
        return jsonObject;
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
}