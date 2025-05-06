package com.example.own_example.services;

import android.content.Context;

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

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<BookstoreModel> bookstores = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject bookstoreJson = response.getJSONObject(i);
                                BookstoreModel bookstore = BookstoreModel.fromJson(bookstoreJson);
                                bookstores.add(bookstore);
                            }
                            callback.onSuccess(bookstores);
                        } catch (JSONException e) {
                            callback.onError("JSON parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Get bookstore by ID
    public void getBookstoreById(int id, final BookstoreCallback callback) {
        String url = BASE_URL + "/bookstore/" + id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            BookstoreModel bookstore = BookstoreModel.fromJson(response);
                            callback.onSuccess(bookstore);
                        } catch (JSONException e) {
                            callback.onError("JSON parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Create bookstore
    public void createBookstore(BookstoreModel bookstore, final BookstoreCallback callback) {
        String url = BASE_URL + "/bookstore";

        try {
            JSONObject jsonBody = bookstore.toJson();

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                BookstoreModel newBookstore = BookstoreModel.fromJson(response);
                                callback.onSuccess(newBookstore);
                            } catch (JSONException e) {
                                callback.onError("JSON parsing error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onError("Network error: " + error.getMessage());
                        }
                    }
            );

            VolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // Update bookstore
    public void updateBookstore(int id, BookstoreModel bookstore, final BookstoreCallback callback) {
        String url = BASE_URL + "/bookstore/" + id;

        try {
            JSONObject jsonBody = bookstore.toJson();

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                BookstoreModel updatedBookstore = BookstoreModel.fromJson(response);
                                callback.onSuccess(updatedBookstore);
                            } catch (JSONException e) {
                                callback.onError("JSON parsing error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onError("Network error: " + error.getMessage());
                        }
                    }
            );

            VolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // Delete bookstore
    public void deleteBookstore(int id, final VoidCallback callback) {
        String url = BASE_URL + "/bookstore/" + id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Add product to bookstore
    public void addProductToBookstore(int bookstoreId, ProductsModel product, final ProductCallback callback) {
        String url = BASE_URL + "/bookstore/" + bookstoreId + "/item";

        try {
            JSONObject jsonBody = product.toJson();

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                ProductsModel newProduct = ProductsModel.fromJson(response);
                                callback.onSuccess(newProduct);
                            } catch (JSONException e) {
                                callback.onError("JSON parsing error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onError("Network error: " + error.getMessage());
                        }
                    }
            );

            VolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // Get products from bookstore
    public void getProducts(int bookstoreId, final ProductListCallback callback) {
        String url = BASE_URL + "/bookstore/" + bookstoreId + "/products";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<ProductsModel> products = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject productJson = response.getJSONObject(i);
                                ProductsModel product = ProductsModel.fromJson(productJson);
                                products.add(product);
                            }
                            callback.onSuccess(products);
                        } catch (JSONException e) {
                            callback.onError("JSON parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}