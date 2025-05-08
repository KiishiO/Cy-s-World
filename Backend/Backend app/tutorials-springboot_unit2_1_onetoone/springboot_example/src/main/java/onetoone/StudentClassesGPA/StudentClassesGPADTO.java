package onetoone.StudentClassesGPA;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for StudentClassesGPA to simplify API requests and responses.
 */
public class StudentClassesGPADTO {
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

    // Default constructor
    public StudentClassesGPADTO() {
    }

    // Constructor with fields
    public StudentClassesGPADTO(int id, int classId, String className, int studentId, String studentName,
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

    // Create DTO from Entity
    public static StudentClassesGPADTO fromEntity(StudentClassesGPA entity) {
        StudentClassesGPADTO dto = new StudentClassesGPADTO();
        dto.setId(entity.getId());

        if (entity.getStudentClass() != null) {
            dto.setClassId(entity.getStudentClass().getId());
            dto.setClassName(entity.getStudentClass().getClassName());
        }

        if (entity.getStudent() != null) {
            dto.setStudentId(entity.getStudent().getId());
            dto.setStudentName(entity.getStudent().getName());
        }

        dto.setAssignmentName(entity.getAssignmentName());
        dto.setAssignmentDescription(entity.getAssignmentDescription());
        dto.setGrade(entity.getGrade());
        dto.setWeightPercentage(entity.getWeightPercentage());
        dto.setSubmissionDate(entity.getSubmissionDate());
        dto.setGradedDate(entity.getGradedDate());

        if (entity.getGradedBy() != null) {
            dto.setGradedById(entity.getGradedBy().getId());
            dto.setGradedByName(entity.getGradedBy().getName());
        }

        dto.setComments(entity.getComments());

        return dto;
    }

    // Getters and Setters
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
}