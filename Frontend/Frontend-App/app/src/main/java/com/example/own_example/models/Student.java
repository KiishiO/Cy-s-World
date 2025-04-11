package com.example.own_example.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a student
 */
public class Student implements Serializable {
    private int id;
    private String name;
    private String email;
    private Map<Integer, String> grades; // Map of classId to grade

    // Default constructor for serialization
    public Student() {
        this.grades = new HashMap<>();
    }

    public Student(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.grades = new HashMap<>();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Map<Integer, String> getGrades() {
        return grades;
    }

    public void setGrades(Map<Integer, String> grades) {
        this.grades = grades;
    }

    /**
     * Add or update a grade for a specific class
     * @param classId The ID of the class
     * @param grade The grade to set
     */
    public void setGrade(int classId, String grade) {
        if (this.grades == null) {
            this.grades = new HashMap<>();
        }
        this.grades.put(classId, grade);
    }

    /**
     * Get the grade for a specific class
     * @param classId The ID of the class
     * @return The grade or "N/A" if no grade is set
     */
    public String getGrade(int classId) {
        if (this.grades == null || !this.grades.containsKey(classId)) {
            return "N/A";
        }
        return this.grades.get(classId);
    }
}