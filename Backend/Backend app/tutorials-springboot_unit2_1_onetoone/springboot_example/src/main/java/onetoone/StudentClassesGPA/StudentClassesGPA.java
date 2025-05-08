package onetoone.StudentClassesGPA;

import jakarta.persistence.*;
import onetoone.Persons.Person;
import onetoone.StudentClasses.StudentClasses;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_classes_gpa")
public class StudentClassesGPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id")
    private StudentClasses studentClass;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Person student;

    private String assignmentName;
    private String assignmentDescription;
    private Double grade;
    private Double weightPercentage;
    private LocalDateTime submissionDate;
    private LocalDateTime gradedDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "graded_by")
    private Person gradedBy;

    private String comments;

    // Default constructor
    public StudentClassesGPA() {
    }

    // Constructor with fields
    public StudentClassesGPA(StudentClasses studentClass, Person student, String assignmentName,
                             String assignmentDescription, Double weightPercentage) {
        this.studentClass = studentClass;
        this.student = student;
        this.assignmentName = assignmentName;
        this.assignmentDescription = assignmentDescription;
        this.weightPercentage = weightPercentage;
        this.submissionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StudentClasses getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(StudentClasses studentClass) {
        this.studentClass = studentClass;
    }

    public Person getStudent() {
        return student;
    }

    public void setStudent(Person student) {
        this.student = student;
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

    public Person getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(Person gradedBy) {
        this.gradedBy = gradedBy;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}