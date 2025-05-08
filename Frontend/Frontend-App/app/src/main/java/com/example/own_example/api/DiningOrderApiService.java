package com.example.own_example.api;

import com.example.own_example.models.DiningOrder;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface DiningOrderApiService {
    // Create a new dining order
    @POST("/diningOrders")
    Call<DiningOrder> createDiningOrder(@Body DiningOrder diningOrder);

    // Get all dining orders
    @GET("/diningOrders")
    Call<List<DiningOrder>> getDiningOrders();
}