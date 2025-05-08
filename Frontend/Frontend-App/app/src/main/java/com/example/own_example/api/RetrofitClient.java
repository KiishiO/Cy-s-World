package com.example.own_example.api;

import com.example.own_example.models.DiningHall;
import com.example.own_example.models.DiningOrder;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
                // Add LocalDateTime adapter to handle date serialization/deserialization
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return src == null ? null : new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return json == null ? null : LocalDateTime.parse(json.getAsString());
                    }
                })
                .registerTypeAdapter(DiningHall.MenuItem.class, new JsonDeserializer<DiningHall.MenuItem>() {
                    @Override
                    public DiningHall.MenuItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        JsonObject jsonObject = json.getAsJsonObject();

                        DiningHall.MenuItem menuItem = new DiningHall.MenuItem();

                        if (jsonObject.has("id")) menuItem.setId(jsonObject.get("id").getAsInt());
                        if (jsonObject.has("name")) menuItem.setName(jsonObject.get("name").getAsString());
                        if (jsonObject.has("description")) menuItem.setDescription(jsonObject.get("description").getAsString());
                        if (jsonObject.has("menuType")) menuItem.setMenuType(jsonObject.get("menuType").getAsString());

                        // Set defaults for UI-only properties
                        menuItem.setVegetarian(false);
                        menuItem.setVegan(false);
                        menuItem.setGlutenFree(false);
                        menuItem.setAllergens(new ArrayList<>());

                        return menuItem;
                    }
                })
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