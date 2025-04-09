package com.example.own_example.models;

public class ExamInfo {
    private int id;
    private String examName;
    private String examDescription;
    private TestingCenter testingCenter;

    public ExamInfo() {
    }

    public ExamInfo(String examName, String examDescription, TestingCenter testingCenter) {
        this.examName = examName;
        this.examDescription = examDescription;
        this.testingCenter = testingCenter;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
    }

    public TestingCenter getTestingCenter() {
        return testingCenter;
    }

    public void setTestingCenter(TestingCenter testingCenter) {
        this.testingCenter = testingCenter;
    }
}