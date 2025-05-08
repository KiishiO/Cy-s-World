package com.example.own_example.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TestingCenter {

    @SerializedName("id")
    private int id;

    @SerializedName("centerName")
    private String centerName;

    @SerializedName("location")
    private String location;

    @SerializedName("centerDescription")
    private String centerDescription;

    @SerializedName("examInfo2")
    private List<ExamInfo> examInfo2 = new ArrayList<>();

    // Constructors
    public TestingCenter() {
    }

    public TestingCenter(String centerName, String location, String centerDescription) {
        this.centerName = centerName;
        this.location = location;
        this.centerDescription = centerDescription;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCenterDescription() {
        return centerDescription;
    }

    public void setCenterDescription(String centerDescription) {
        this.centerDescription = centerDescription;
    }

    public List<ExamInfo> getExamInfo2() {
        return examInfo2;
    }

    public void setExamInfo2(List<ExamInfo> examInfo2) {
        this.examInfo2 = examInfo2;
    }

    // Helper methods
    public int getExamCount() {
        return examInfo2 != null ? examInfo2.size() : 0;
    }

    @Override
    public String toString() {
        return "TestingCenter{" +
                "id=" + id +
                ", centerName='" + centerName + '\'' +
                ", location='" + location + '\'' +
                ", examCount=" + getExamCount() +
                '}';
    }
}