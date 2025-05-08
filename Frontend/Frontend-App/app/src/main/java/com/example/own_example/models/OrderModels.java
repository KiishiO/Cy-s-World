package com.example.own_example.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Collection of models for the Order and Cart system
 */
public class OrderModels {

    /**
     * Represents an item in the shopping cart
     */
    public static class CartItemModel {
        private ProductsModel product;
        private int quantity;

        public CartItemModel(ProductsModel product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public ProductsModel getProduct() {
            return product;
        }

        public void setProduct(ProductsModel product) {
            this.product = product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getSubtotal() {
            return product.getPrice() * quantity;
        }
    }

    /**
     * Represents a person model to align with backend API
     */
    public static class PersonModel {
        private int id;

        public PersonModel() {
        }

        public PersonModel(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    /**
     * Represents an order
     */
    public static class OrderModel {
        private int id;
        private Date orderDate;
        private PersonModel person;  // Changed from int personId
        private List<OrderItemModel> items;
        private String status;
        private Date estimatedDelivery;

        public OrderModel() {
            this.items = new ArrayList<>();
            this.orderDate = new Date();
            // Default status
            this.status = "Pending";
            // Estimated delivery: 3 days from now
            this.estimatedDelivery = new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000);
        }

        // Constructor from cart
        public OrderModel(List<CartItemModel> cartItems, int personId) {
            this();
            this.person = new PersonModel(personId);  // Create Person object with ID
            for (CartItemModel cartItem : cartItems) {
                this.items.add(new OrderItemModel(0, cartItem.getProduct(), cartItem.getQuantity()));
            }
        }

        public JSONObject toJson() throws JSONException {
            JSONObject orderJson = new JSONObject();

            // Create person object with ID
            JSONObject personJson = new JSONObject();
            personJson.put("id", person.getId());
            orderJson.put("person", personJson);

            // Create items array
            JSONArray itemsArray = new JSONArray();
            for (OrderItemModel item : items) {
                itemsArray.put(item.toJson());
            }
            orderJson.put("items", itemsArray);

            return orderJson;
        }

        public static OrderModel fromJson(JSONObject jsonObject) throws JSONException {
            OrderModel order = new OrderModel();

            if (jsonObject.has("id")) {
                order.setId(jsonObject.getInt("id"));
            }

            if (jsonObject.has("person") && !jsonObject.isNull("person")) {
                JSONObject personJson = jsonObject.getJSONObject("person");
                int personId = personJson.getInt("id");
                order.setPerson(new PersonModel(personId));
            }

            if (jsonObject.has("orderDate") && !jsonObject.isNull("orderDate")) {
                // Parse date from string
                String dateStr = jsonObject.getString("orderDate");
                // Simple date parsing (may need to be adjusted based on actual format)
                order.setOrderDate(new Date(dateStr));
            }

            if (jsonObject.has("items") && !jsonObject.isNull("items")) {
                JSONArray itemsArray = jsonObject.getJSONArray("items");
                List<OrderItemModel> items = new ArrayList<>();

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemJson = itemsArray.getJSONObject(i);
                    items.add(OrderItemModel.fromJson(itemJson));
                }

                order.setItems(items);
            }

            if (jsonObject.has("status")) {
                order.setStatus(jsonObject.getString("status"));
            }

            return order;
        }

        // Updated getters and setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Date getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(Date orderDate) {
            this.orderDate = orderDate;
        }

        public PersonModel getPerson() {
            return person;
        }

        public void setPerson(PersonModel person) {
            this.person = person;
        }

        public List<OrderItemModel> getItems() {
            return items;
        }

        public void setItems(List<OrderItemModel> items) {
            this.items = items;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Date getEstimatedDelivery() {
            return estimatedDelivery;
        }

        public void setEstimatedDelivery(Date estimatedDelivery) {
            this.estimatedDelivery = estimatedDelivery;
        }

        public double getTotal() {
            double total = 0;
            for (OrderItemModel item : items) {
                total += item.getSubtotal();
            }
            return total;
        }

        // For backward compatibility
        public int getPersonId() {
            return person != null ? person.getId() : -1;
        }

        public void setPersonId(int personId) {
            this.person = new PersonModel(personId);
        }
    }

    /**
     * Represents an item in an order
     */
    public static class OrderItemModel {
        private int id;
        private ProductsModel product;
        private int quantity;

        public OrderItemModel(int id, ProductsModel product, int quantity) {
            this.id = id;
            this.product = product;
            this.quantity = quantity;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject orderItemJson = new JSONObject();

            JSONObject productJson = new JSONObject();
            productJson.put("id", product.getId());

            orderItemJson.put("product", productJson);
            orderItemJson.put("quantity", quantity);

            return orderItemJson;
        }

        public static OrderItemModel fromJson(JSONObject jsonObject) throws JSONException {
            int id = 0;
            if (jsonObject.has("id")) {
                id = jsonObject.getInt("id");
            }

            ProductsModel product = null;
            if (jsonObject.has("product") && !jsonObject.isNull("product")) {
                JSONObject productJson = jsonObject.getJSONObject("product");
                product = ProductsModel.fromJson(productJson);
            }

            int quantity = 1;
            if (jsonObject.has("quantity")) {
                quantity = jsonObject.getInt("quantity");
            }

            return new OrderItemModel(id, product, quantity);
        }

        // Getters and setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public ProductsModel getProduct() {
            return product;
        }

        public void setProduct(ProductsModel product) {
            this.product = product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getSubtotal() {
            return product.getPrice() * quantity;
        }
    }
}