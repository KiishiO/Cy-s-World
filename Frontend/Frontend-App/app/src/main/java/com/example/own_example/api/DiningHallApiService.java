package com.example.own_example.api;

import com.example.own_example.models.DiningHall;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface DiningHallApiService {
    // Get all dining halls
    @GET("/dininghall")
    Call<List<DiningHall>> getAllDiningHalls();

    // Get dining hall by ID
    @GET("/dininghall/{id}")
    Call<DiningHall> getDiningHallById(@Path("id") int id);

    // Create a new dining hall
    @POST("/dininghall/new")
    Call<DiningHall> createDiningHall(@Body DiningHall diningHall);

    // Update a dining hall
    @PUT("/dininghall/{id}")
    Call<DiningHall> updateDiningHall(@Path("id") int id, @Body DiningHall diningHall);

    // Delete a dining hall
    @DELETE("/dininghall/{id}")
    Call<Void> deleteDiningHall(@Path("id") int id);

    // Search dining halls by name
    @GET("/dininghall/search/name")
    Call<List<DiningHall>> searchDiningHallsByName(@Query("name") String name);

    // Search dining halls by location
    @GET("/dininghall/search/location")
    Call<List<DiningHall>> searchDiningHallsByLocation(@Query("location") String location);

    // Search dining halls by menu item
    @GET("/dininghall/search/menuitem")
    Call<List<DiningHall>> searchDiningHallsByMenuItem(@Query("itemName") String itemName);

    // Search dining halls by max price
    @GET("/dininghall/search/price")
    Call<List<DiningHall>> searchDiningHallsByMaxPrice(@Query("maxPrice") double maxPrice);

    // Add a menu item to a dining hall
    @POST("/dininghall/{id}/menuitems")
    Call<DiningHall.MenuItem> addMenuItem(@Path("id") int diningHallId, @Body Map<String, Object> menuItem);

    // Get all menu items for a dining hall
    @GET("/dininghall/{id}/menuitems")
    Call<List<DiningHall.MenuItem>> getMenuItems(@Path("id") int diningHallId);
}