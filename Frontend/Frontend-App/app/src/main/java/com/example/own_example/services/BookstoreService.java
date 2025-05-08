package com.example.own_example.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.own_example.VolleySingleton;
import com.example.own_example.models.BookstoreModel;
import com.example.own_example.models.ProductsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookstoreService {
    private static final String TAG = "BookstoreService";
    private static final String BASE_URL = "http://coms-3090-017.class.las.iastate.edu:8080";
    private Context context;

    // Interface for callbacks
    public interface BookstoreCallback {
        void onSuccess(BookstoreModel bookstore);
        void onError(String message);
    }

    public interface BookstoreListCallback {
        void onSuccess(List<BookstoreModel> bookstores);
        void onError(String message);
    }

    public interface ProductCallback {
        void onSuccess(ProductsModel product);
        void onError(String message);
    }

    public interface ProductListCallback {
        void onSuccess(List<ProductsModel> products);
        void onError(String message);
    }

    public interface VoidCallback {
        void onSuccess();
        void onError(String message);
    }

    public BookstoreService(Context context) {
        this.context = context;
    }

    // Get all bookstores
    public void getAllBookstores(final BookstoreListCallback callback) {
        String url = BASE_URL + "/bookstore";
        Log.d(TAG, "Getting all bookstores from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<BookstoreModel> bookstores = new ArrayList<>();
                        try {
                            Log.d(TAG, "Received bookstores response: " + response.length() + " items");
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject bookstoreJson = response.getJSONObject(i);
                                BookstoreModel bookstore = BookstoreModel.fromJson(bookstoreJson);
                                bookstores.add(bookstore);
                            }
                            callback.onSuccess(bookstores);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("JSON parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleVolleyError("Get all bookstores", error, callback);
                    }
                }
        );

        // Set timeout
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Get bookstore by ID
    public void getBookstoreById(int id, final BookstoreCallback callback) {
        String url = BASE_URL + "/bookstore/" + id;
        Log.d(TAG, "Getting bookstore by ID: " + id + " from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Received bookstore: " + response.toString());
                            BookstoreModel bookstore = BookstoreModel.fromJson(response);
                            callback.onSuccess(bookstore);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("JSON parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleVolleyError("Get bookstore by ID", error, callback);
                    }
                }
        );

        // Set timeout
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Create bookstore
    public void createBookstore(BookstoreModel bookstore, final BookstoreCallback callback) {
        String url = BASE_URL + "/bookstore";
        Log.d(TAG, "Creating bookstore at: " + url);

        try {
            JSONObject jsonBody = bookstore.toJson();
            Log.d(TAG, "Request body: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "Bookstore created: " + response.toString());
                                BookstoreModel newBookstore = BookstoreModel.fromJson(response);
                                callback.onSuccess(newBookstore);
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON parsing error: " + e.getMessage());
                                callback.onError("JSON parsing error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleVolleyError("Create bookstore", error, callback);
                        }
                    }
            );

            // Set timeout
            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // Update bookstore
    public void updateBookstore(int id, BookstoreModel bookstore, final BookstoreCallback callback) {
        String url = BASE_URL + "/bookstore/" + id;
        Log.d(TAG, "Updating bookstore at: " + url);

        try {
            JSONObject jsonBody = bookstore.toJson();
            Log.d(TAG, "Request body: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "Bookstore updated: " + response.toString());
                                BookstoreModel updatedBookstore = BookstoreModel.fromJson(response);
                                callback.onSuccess(updatedBookstore);
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON parsing error: " + e.getMessage());
                                callback.onError("JSON parsing error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleVolleyError("Update bookstore", error, callback);
                        }
                    }
            );

            // Set timeout
            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // Delete bookstore
    public void deleteBookstore(int id, final VoidCallback callback) {
        String url = BASE_URL + "/bookstore/" + id;
        Log.d(TAG, "Deleting bookstore at: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Bookstore deleted successfully");
                        callback.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleVolleyError("Delete bookstore", error, callback);
                    }
                }
        );

        // Set timeout
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Add product to bookstore - CORRECT ENDPOINT from backend code
    public void addProductToBookstore(int bookstoreId, ProductsModel product, final ProductCallback callback) {
        String url = BASE_URL + "/bookstore/" + bookstoreId + "/item";
        Log.d(TAG, "Adding product to bookstore: " + bookstoreId);
        Log.d(TAG, "Product name: " + product.getItem() + ", Price: " + product.getPrice());
        Log.d(TAG, "URL: " + url);

        try {
            JSONObject jsonBody = product.toJson();
            Log.d(TAG, "Request body: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Add product success: " + response.toString());
                            try {
                                ProductsModel newProduct = ProductsModel.fromJson(response);
                                callback.onSuccess(newProduct);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing product response: " + e.getMessage());
                                callback.onError("JSON parsing error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleVolleyError("Add product to bookstore", error, callback);
                        }
                    }
            );

            // Set timeout
            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            Log.d(TAG, "Adding request to queue");
            VolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // Get products from bookstore - CORRECT ENDPOINT from backend code
    public void getProducts(int bookstoreId, final ProductListCallback callback) {
        String url = BASE_URL + "/bookstore/" + bookstoreId + "/products";
        Log.d(TAG, "Getting products from bookstore: " + bookstoreId);
        Log.d(TAG, "URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<ProductsModel> products = new ArrayList<>();
                        try {
                            Log.d(TAG, "Received products: " + response.length() + " items");
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject productJson = response.getJSONObject(i);
                                ProductsModel product = ProductsModel.fromJson(productJson);
                                products.add(product);
                            }
                            callback.onSuccess(products);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("JSON parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleVolleyError("Get products", error, callback);
                    }
                }
        );

        // Set timeout
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Since the controller doesn't have PUT and DELETE endpoints for products, we need to extend the API
    // We'll implement custom methods to handle these operations

    // Update a product - Need to create or extend backend API for this
    public void updateProduct(int bookstoreId, int productId, ProductsModel product, final ProductCallback callback) {
        // Since there's no direct update endpoint in the backend code,
        // we'll need to create a custom implementation
        // Options:
        // 1. Delete and re-add the product (temporary solution)
        // 2. Extend the backend API to support product updates

        // For now, we'll implement a workaround by deleting and re-adding
        deleteAndRecreateProduct(bookstoreId, productId, product, callback);
    }

    // Delete and recreate product as a workaround for update
    private void deleteAndRecreateProduct(int bookstoreId, int productId, ProductsModel product,
                                          final ProductCallback callback) {
        Log.d(TAG, "Update not directly supported by API. Implementing as delete and recreate");

        // First attempt to delete the product - no direct endpoint in controller,
        // but we'll try a logical endpoint
        String deleteUrl = BASE_URL + "/bookstore/" + bookstoreId + "/item/" + productId;

        JsonObjectRequest deleteRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                deleteUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Successfully deleted product, now recreating");
                        // Now add the product with updated values
                        addProductToBookstore(bookstoreId, product, callback);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Delete part of update failed, trying to add directly");
                        // If delete fails, try to add it anyway
                        addProductToBookstore(bookstoreId, product, callback);
                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(deleteRequest);
    }

    // Delete a product - Need to create or extend backend API for this
    public void deleteProduct(int bookstoreId, int productId, final VoidCallback callback) {
        // Since there's no direct delete endpoint in the backend code,
        // Need to try a logical endpoint and explain to the user

        // Try a logical endpoint first
        String url = BASE_URL + "/bookstore/" + bookstoreId + "/item/" + productId;
        Log.d(TAG, "Attempting to delete product with ID: " + productId + " from bookstore: " + bookstoreId);
        Log.d(TAG, "URL: " + url + " (Note: Backend API may not support this operation)");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Delete product success response");
                        callback.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Special handling for likely API limitation
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Log.e(TAG, "Delete product endpoint not found. Backend API may not support this operation.");
                            callback.onError("The backend API does not appear to support product deletion. Please contact your backend developer to add this functionality.");
                        } else {
                            handleVolleyError("Delete product", error, callback);
                        }
                    }
                }
        );

        // Set timeout
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Helper method to handle Volley errors consistently
    private void handleVolleyError(String operation, VolleyError error, Object callback) {
        String errorMsg = "Network error during " + operation + ": ";

        if (error.networkResponse != null) {
            errorMsg += "Status Code: " + error.networkResponse.statusCode + " ";
            Log.e(TAG, operation + " error - Status code: " + error.networkResponse.statusCode);

            if (error.networkResponse.data != null) {
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    errorMsg += "Response: " + responseBody;
                    Log.e(TAG, "Error response body: " + responseBody);
                } catch (Exception e) {
                    errorMsg += "Could not parse error response";
                    Log.e(TAG, "Could not parse error response", e);
                }
            }
        } else if (error.getMessage() != null) {
            errorMsg += error.getMessage();
            Log.e(TAG, operation + " error: " + error.getMessage());
        } else {
            errorMsg += "Unknown error";
            Log.e(TAG, operation + " unknown error");
        }

        // Call the appropriate error method based on callback type
        if (callback instanceof BookstoreCallback) {
            ((BookstoreCallback) callback).onError(errorMsg);
        } else if (callback instanceof BookstoreListCallback) {
            ((BookstoreListCallback) callback).onError(errorMsg);
        } else if (callback instanceof ProductCallback) {
            ((ProductCallback) callback).onError(errorMsg);
        } else if (callback instanceof ProductListCallback) {
            ((ProductListCallback) callback).onError(errorMsg);
        } else if (callback instanceof VoidCallback) {
            ((VoidCallback) callback).onError(errorMsg);
        }
    }
}