package com.example.own_example;

public class StudyGroupMember {
    private String id;
    private String name;
    private String email;
    private boolean isActive;

    public StudyGroupMember() {
        this.isActive = true;
    }

    public StudyGroupMember(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isActive = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getStatus() {
        return isActive ? "Active" : "Pending";
    }
}