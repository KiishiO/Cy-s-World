package com.example.own_example.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing an assignment with its grade
 */
public class AssignmentModel implements Serializable {
    private int id;
    private int classId;
    private String className;
    private int studentId;
    private String studentName;
    private String assignmentName;
    private String assignmentDescription;
    private Double grade;
    private Double weightPercentage;
    private LocalDateTime submissionDate;
    private LocalDateTime gradedDate;
    private int gradedById;
    private String gradedByName;
    private String comments;

    // Formatters for displaying dates
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a");

    // Default constructor
    public AssignmentModel() {
    }

    // Constructor with fields
    public AssignmentModel(int id, int classId, String className, int studentId, String studentName,
                           String assignmentName, String assignmentDescription, Double grade,
                           Double weightPercentage, LocalDateTime submissionDate, LocalDateTime gradedDate,
                           int gradedById, String gradedByName, String comments) {
        this.id = id;
        this.classId = classId;
        this.className = className;
        this.studentId = studentId;
        this.studentName = studentName;
        this.assignmentName = assignmentName;
        this.assignmentDescription = assignmentDescription;
        this.grade = grade;
        this.weightPercentage = weightPercentage;
        this.submissionDate = submissionDate;
        this.gradedDate = gradedDate;
        this.gradedById = gradedById;
        this.gradedByName = gradedByName;
        this.comments = comments;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }

    public String getAssignmentDescription() {
        return assignmentDescription;
    }

    public void setAssignmentDescription(String assignmentDescription) {
        this.assignmentDescription = assignmentDescription;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public Double getWeightPercentage() {
        return weightPercentage;
    }

    public void setWeightPercentage(Double weightPercentage) {
        this.weightPercentage = weightPercentage;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public LocalDateTime getGradedDate() {
        return gradedDate;
    }

    public void setGradedDate(LocalDateTime gradedDate) {
        this.gradedDate = gradedDate;
    }

    public int getGradedById() {
        return gradedById;
    }

    public void setGradedById(int gradedById) {
        this.gradedById = gradedById;
    }

    public String getGradedByName() {
        return gradedByName;
    }

    public void setGradedByName(String gradedByName) {
        this.gradedByName = gradedByName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Returns a formatted string of the submission date
     */
    public String getFormattedSubmissionDate() {
        return submissionDate != null ? submissionDate.format(DATE_FORMATTER) : "Not submitted";
    }

    /**
     * Returns a formatted string of the graded date
     */
    public String getFormattedGradedDate() {
        return gradedDate != null ? gradedDate.format(DATE_FORMATTER) : "Not graded";
    }

    /**
     * Returns a formatted string of the grade
     */
    public String getFormattedGrade() {
        if (grade == null) {
            return "Not graded";
        }
        return String.format("%.1f", grade) + "/100";
    }

    /**
     * Returns a formatted string of the weight percentage
     */
    public String getFormattedWeight() {
        if (weightPercentage == null) {
            return "0%";
        }
        return String.format("%.1f%%", weightPercentage);
    }

    /**
     * Returns whether the assignment has been graded
     */
    public boolean isGraded() {
        return grade != null;
    }
}