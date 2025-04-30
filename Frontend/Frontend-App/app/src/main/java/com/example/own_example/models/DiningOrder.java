package com.example.own_example.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DiningOrder {
    private int id;
    private LocalDateTime orderDate;
    private Person person;
    private List<DiningOrderItem> items = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public List<DiningOrderItem> getItems() {
        return items;
    }

    public void setItems(List<DiningOrderItem> items) {
        this.items = items;
    }

    public static class Person {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}