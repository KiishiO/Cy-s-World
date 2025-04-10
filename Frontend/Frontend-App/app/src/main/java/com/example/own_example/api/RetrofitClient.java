package com.example.own_example.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Base URL
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/";

    // Singleton instance
    private static RetrofitClient instance;

    // Retrofit instance
    private Retrofit retrofit;

    // API service interface
    private DiningHallApiService apiService;

    // Private constructor for singleton
    private RetrofitClient() {
        // Initialize Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create API service
        apiService = retrofit.create(DiningHallApiService.class);
    }

    // Get singleton instance
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    // Get API service
    public DiningHallApiService getApiService() {
        return apiService;
    }
}