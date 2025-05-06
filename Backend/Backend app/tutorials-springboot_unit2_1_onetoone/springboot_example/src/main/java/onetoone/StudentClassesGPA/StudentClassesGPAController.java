package onetoone.StudentClassesGPA;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import onetoone.StudentClasses.StudentClasses;
import onetoone.StudentClasses.StudentClassesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gpa")
@Tag(name = "Student GPA", description = "APIs for managing student grades and GPAs")
public class StudentClassesGPAController {

    @Autowired
    private StudentClassesGPARepository gpaRepository;

    @Autowired
    private StudentClassesRepository classesRepository;

    @Autowired
    private PersonRepository personRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @Operation(summary = "Get all assignments and grades", description = "Retrieves all assignments and grades in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all assignments and grades",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentClassesGPA.class)))
    @GetMapping
    public List<StudentClassesGPA> getAllGrades() {
        return gpaRepository.findAll();
    }

    @Operation(summary = "Get assignment/grade by ID", description = "Retrieves a specific assignment/grade by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the assignment/grade"),
            @ApiResponse(responseCode = "404", description = "Assignment/grade with specified ID not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getGradeById(
            @Parameter(description = "ID of the assignment/grade to retrieve", required = true)
            @PathVariable int id) {
        StudentClassesGPA grade = gpaRepository.findById(id);
        if (grade == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(grade, HttpStatus.OK);
    }

    @Operation(summary = "Get all assignments for a class", description = "Retrieves all assignments for a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assignments"),
            @ApiResponse(responseCode = "404", description = "Class with specified ID not found")
    })
    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getGradesByClass(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable int classId) {
        StudentClasses studentClass = classesRepository.findById(classId);
        if (studentClass == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        List<StudentClassesGPA> grades = gpaRepository.findByStudentClassId(classId);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @Operation(summary = "Get all assignments for a student", description = "Retrieves all assignments for a specific student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assignments"),
            @ApiResponse(responseCode = "404", description = "Student with specified ID not found")
    })
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getGradesByStudent(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable int studentId) {
        Person student = personRepository.findById(studentId);
        if (student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        List<StudentClassesGPA> grades = gpaRepository.findByStudentId(studentId);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @Operation(summary = "Get student grades for a class", description = "Retrieves all grades for a specific student in a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved grades"),
            @ApiResponse(responseCode = "404", description = "Class or student with specified ID not found")
    })
    @GetMapping("/class/{classId}/student/{studentId}")
    public ResponseEntity<?> getStudentGradesForClass(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable int classId,
            @Parameter(description = "ID of the student", required = true)
            @PathVariable int studentId) {
        StudentClasses studentClass = classesRepository.findById(classId);
        Person student = personRepository.findById(studentId);

        if (studentClass == null || student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        List<StudentClassesGPA> grades = gpaRepository.findByStudentClassIdAndStudentId(classId, studentId);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @Operation(summary = "Get student overall grade for a class", description = "Calculates and retrieves the overall grade for a specific student in a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated overall grade"),
            @ApiResponse(responseCode = "404", description = "Class or student with specified ID not found")
    })
    @GetMapping("/class/{classId}/student/{studentId}/overall")
    public ResponseEntity<?> getStudentOverallGrade(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable int classId,
            @Parameter(description = "ID of the student", required = true)
            @PathVariable int studentId) {
        StudentClasses studentClass = classesRepository.findById(classId);
        Person student = personRepository.findById(studentId);

        if (studentClass == null || student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        Double overallGrade = gpaRepository.calculateOverallGradeForStudent(classId, studentId);
        Long totalAssignments = gpaRepository.countAssignmentsForStudent(classId, studentId);
        Long gradedAssignments = gpaRepository.countGradedAssignmentsForStudent(classId, studentId);

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("studentName", student.getName());
        result.put("classId", classId);
        result.put("className", studentClass.getClassName());
        result.put("overallGrade", overallGrade);
        result.put("totalAssignments", totalAssignments);
        result.put("gradedAssignments", gradedAssignments);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get class average grade", description = "Calculates and retrieves the average grade for all students in a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated class average"),
            @ApiResponse(responseCode = "404", description = "Class with specified ID not found")
    })
    @GetMapping("/class/{classId}/average")
    public ResponseEntity<?> getClassAverageGrade(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable int classId) {
        StudentClasses studentClass = classesRepository.findById(classId);

        if (studentClass == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        Double classAverage = gpaRepository.calculateClassAverage(classId);

        Map<String, Object> result = new HashMap<>();
        result.put("classId", classId);
        result.put("className", studentClass.getClassName());
        result.put("classAverage", classAverage);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Create a new assignment", description = "Creates a new assignment for students in a class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assignment successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Class or student not found"),
            @ApiResponse(responseCode = "409", description = "Assignment already exists")
    })
    @PostMapping
    public ResponseEntity<?> createAssignment(
            @Parameter(description = "Assignment details", required = true)
            @RequestBody StudentClassesGPA gpa) {
        try {
            // Verify class exists
            if (gpa.getStudentClass() == null) {
                return new ResponseEntity<>("{\"message\":\"Class is required\"}", HttpStatus.BAD_REQUEST);
            }

            StudentClasses studentClass = classesRepository.findById(gpa.getStudentClass().getId());
            if (studentClass == null) {
                return new ResponseEntity<>("{\"message\":\"Class not found\"}", HttpStatus.NOT_FOUND);
            }
            gpa.setStudentClass(studentClass);

            // Verify student exists
            if (gpa.getStudent() == null) {
                return new ResponseEntity<>("{\"message\":\"Student is required\"}", HttpStatus.BAD_REQUEST);
            }

            Person student = personRepository.findById(gpa.getStudent().getId());
            if (student == null) {
                return new ResponseEntity<>("{\"message\":\"Student not found\"}", HttpStatus.NOT_FOUND);
            }
            gpa.setStudent(student);

            // Check if assignment with same name already exists for this student and class
            if (gpa.getAssignmentName() == null || gpa.getAssignmentName().isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Assignment name is required\"}", HttpStatus.BAD_REQUEST);
            }

            StudentClassesGPA existingAssignment = gpaRepository.findByClassStudentAndAssignment(
                    studentClass.getId(), student.getId(), gpa.getAssignmentName());

            if (existingAssignment != null) {
                return new ResponseEntity<>("{\"message\":\"An assignment with this name already exists for this student in this class\"}",
                        HttpStatus.CONFLICT);
            }

            // Set submission date if not provided
            if (gpa.getSubmissionDate() == null) {
                gpa.setSubmissionDate(LocalDateTime.now());
            }

            // Set graded date if grade is provided
            if (gpa.getGrade() != null) {
                gpa.setGradedDate(LocalDateTime.now());
            }

            // Validate weight percentage
            if (gpa.getWeightPercentage() == null) {
                return new ResponseEntity<>("{\"message\":\"Weight percentage is required\"}", HttpStatus.BAD_REQUEST);
            }

            if (gpa.getWeightPercentage() <= 0 || gpa.getWeightPercentage() > 100) {
                return new ResponseEntity<>("{\"message\":\"Weight percentage must be between 0 and 100\"}", HttpStatus.BAD_REQUEST);
            }

            // Set graded by if grade is provided
            if (gpa.getGrade() != null && gpa.getGradedBy() != null) {
                Person teacher = personRepository.findById(gpa.getGradedBy().getId());
                if (teacher == null) {
                    return new ResponseEntity<>("{\"message\":\"Teacher not found\"}", HttpStatus.NOT_FOUND);
                }
                gpa.setGradedBy(teacher);
            }

            gpaRepository.save(gpa);
            return new ResponseEntity<>(gpa, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Grade an assignment", description = "Updates an assignment with a grade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment successfully graded"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PutMapping("/{id}/grade")
    public ResponseEntity<?> gradeAssignment(
            @Parameter(description = "ID of the assignment to grade", required = true)
            @PathVariable int id,
            @Parameter(description = "Grade details", required = true)
            @RequestBody Map<String, Object> gradeRequest) {
        try {
            StudentClassesGPA assignment = gpaRepository.findById(id);
            if (assignment == null) {
                return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
            }

            // Get grade from request
            if (!gradeRequest.containsKey("grade")) {
                return new ResponseEntity<>("{\"message\":\"Grade is required\"}", HttpStatus.BAD_REQUEST);
            }

            Double grade = Double.parseDouble(gradeRequest.get("grade").toString());
            if (grade < 0 || grade > 100) {
                return new ResponseEntity<>("{\"message\":\"Grade must be between 0 and 100\"}", HttpStatus.BAD_REQUEST);
            }

            assignment.setGrade(grade);
            assignment.setGradedDate(LocalDateTime.now());

            // Get teacher who graded
            if (gradeRequest.containsKey("gradedBy")) {
                int teacherId = Integer.parseInt(gradeRequest.get("gradedBy").toString());
                Person teacher = personRepository.findById(teacherId);
                if (teacher == null) {
                    return new ResponseEntity<>("{\"message\":\"Teacher not found\"}", HttpStatus.NOT_FOUND);
                }
                assignment.setGradedBy(teacher);
            }

            // Add comments if provided
            if (gradeRequest.containsKey("comments")) {
                assignment.setComments(gradeRequest.get("comments").toString());
            }

            gpaRepository.save(assignment);
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update an assignment", description = "Updates an existing assignment with new details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment successfully updated"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssignment(
            @Parameter(description = "ID of the assignment to update", required = true)
            @PathVariable int id,
            @Parameter(description = "Updated assignment details", required = true)
            @RequestBody StudentClassesGPA gpa) {
        try {
            StudentClassesGPA existingAssignment = gpaRepository.findById(id);
            if (existingAssignment == null) {
                return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
            }

            // Ensure ID is preserved
            gpa.setId(id);

            // If updating grade, set graded date
            if (gpa.getGrade() != null && !gpa.getGrade().equals(existingAssignment.getGrade())) {
                gpa.setGradedDate(LocalDateTime.now());
            } else if (gpa.getGrade() == null) {
                // Keep existing grade and graded date if not provided
                gpa.setGrade(existingAssignment.getGrade());
                gpa.setGradedDate(existingAssignment.getGradedDate());
            }

            // Keep student and class info if not provided
            if (gpa.getStudentClass() == null) {
                gpa.setStudentClass(existingAssignment.getStudentClass());
            } else {
                StudentClasses studentClass = classesRepository.findById(gpa.getStudentClass().getId());
                if (studentClass == null) {
                    return new ResponseEntity<>("{\"message\":\"Class not found\"}", HttpStatus.NOT_FOUND);
                }
                gpa.setStudentClass(studentClass);
            }

            if (gpa.getStudent() == null) {
                gpa.setStudent(existingAssignment.getStudent());
            } else {
                Person student = personRepository.findById(gpa.getStudent().getId());
                if (student == null) {
                    return new ResponseEntity<>("{\"message\":\"Student not found\"}", HttpStatus.NOT_FOUND);
                }
                gpa.setStudent(student);
            }

            // Validate graded by if provided
            if (gpa.getGradedBy() != null) {
                Person teacher = personRepository.findById(gpa.getGradedBy().getId());
                if (teacher == null) {
                    return new ResponseEntity<>("{\"message\":\"Teacher not found\"}", HttpStatus.NOT_FOUND);
                }
                gpa.setGradedBy(teacher);
            } else if (existingAssignment.getGradedBy() != null) {
                gpa.setGradedBy(existingAssignment.getGradedBy());
            }

            // Keep submission date if not provided
            if (gpa.getSubmissionDate() == null) {
                gpa.setSubmissionDate(existingAssignment.getSubmissionDate());
            }

            gpaRepository.save(gpa);
            return new ResponseEntity<>(gpa, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Delete an assignment", description = "Removes an assignment from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(
            @Parameter(description = "ID of the assignment to delete", required = true)
            @PathVariable int id) {
        StudentClassesGPA assignment = gpaRepository.findById(id);
        if (assignment == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        gpaRepository.deleteById(id);
        return new ResponseEntity<>(success, HttpStatus.OK);
    }

    @Operation(summary = "Delete all assignments for a student in a class", description = "Removes all assignments for a specific student in a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    @DeleteMapping("/class/{classId}/student/{studentId}")
    public ResponseEntity<?> deleteAllStudentAssignmentsInClass(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable int classId,
            @Parameter(description = "ID of the student", required = true)
            @PathVariable int studentId) {
        StudentClasses studentClass = classesRepository.findById(classId);
        Person student = personRepository.findById(studentId);

        if (studentClass == null || student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        List<StudentClassesGPA> assignments = gpaRepository.findByStudentClassIdAndStudentId(classId, studentId);
        for (StudentClassesGPA assignment : assignments) {
            gpaRepository.deleteById(assignment.getId());
        }

        return new ResponseEntity<>(success, HttpStatus.OK);
    }
}