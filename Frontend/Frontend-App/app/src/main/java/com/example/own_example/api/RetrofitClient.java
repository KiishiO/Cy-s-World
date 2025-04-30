package com.example.own_example.api;

import com.example.own_example.models.DiningHall;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Base URL
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080/";

    // Singleton instance
    private static RetrofitClient instance;

    // Retrofit instance
    private Retrofit retrofit;

    // API service interfaces
    private DiningHallApiService apiService;
    private DiningOrderApiService diningOrderApiService;

    // Private constructor for singleton
    private RetrofitClient() {
        // Create OkHttpClient with longer timeouts
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        // Create a custom Gson instance with proper type adapters
        Gson gson = new GsonBuilder()
                .setLenient()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        // Skip DiningHall reference in MenuItem to prevent circular references
                        return (f.getDeclaringClass() == DiningHall.MenuItem.class &&
                                f.getName().equals("diningHall")) ||
                                // Also handle any other potential circular references
                                (f.getDeclaringClass() == DiningHall.class &&
                                        f.getName().equals("menuItems"));
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .serializeNulls() // Handle null values properly
                .create();

        // Initialize Retrofit with our custom OkHttpClient and Gson
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Create API services
        apiService = retrofit.create(DiningHallApiService.class);
        diningOrderApiService = retrofit.create(DiningOrderApiService.class);
    }

    // Get singleton instance
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    // Get API services
    public DiningHallApiService getApiService() {
        return apiService;
    }

    public DiningOrderApiService getDiningOrderApiService() {
        return diningOrderApiService;
    }
}