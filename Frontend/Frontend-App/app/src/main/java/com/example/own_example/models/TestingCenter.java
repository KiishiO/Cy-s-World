package com.example.own_example.models;

import java.util.List;

public class TestingCenter {
    private int id;
    private String centerName;
    private String location;
    private String centerDescription;
    private List<ExamInfo> examInfo;

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

    public List<ExamInfo> getExamInfo() {
        return examInfo;
    }

    public void setExamInfo(List<ExamInfo> examInfo) {
        this.examInfo = examInfo;
    }
}