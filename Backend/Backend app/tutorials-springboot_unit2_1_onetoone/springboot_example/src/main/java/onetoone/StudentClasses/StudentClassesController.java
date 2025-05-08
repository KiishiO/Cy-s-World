package onetoone.StudentClasses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/classes")
@Tag(name = "Student Classes", description = "APIs for managing classes and student enrollments")
public class StudentClassesController {

    @Autowired
    private StudentClassesRepository classesRepository;

    @Autowired
    private PersonRepository personRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @Operation(summary = "Get all classes", description = "Retrieves a list of all classes in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all classes",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentClasses.class)))
    @GetMapping
    public List<StudentClasses> getAllClasses() {
        return classesRepository.findAll();
    }

    @Operation(summary = "Get class by ID", description = "Retrieves a specific class by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the class"),
            @ApiResponse(responseCode = "404", description = "Class with specified ID not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getClassById(
            @Parameter(description = "ID of the class to retrieve", required = true)
            @PathVariable int id) {
        StudentClasses studentClass = classesRepository.findById(id);
        if (studentClass == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(studentClass, HttpStatus.OK);
    }

    @Operation(summary = "Get students in class", description = "Retrieves all students enrolled in a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved students"),
            @ApiResponse(responseCode = "404", description = "Class with specified ID not found")
    })
    @GetMapping("/{id}/students")
    public ResponseEntity<?> getStudentsInClass(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable int id) {
        StudentClasses studentClass = classesRepository.findById(id);
        if (studentClass == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(studentClass.getStudents(), HttpStatus.OK);
    }

    @Operation(summary = "Get classes for student", description = "Retrieves all classes in which a specific student is enrolled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved classes"),
            @ApiResponse(responseCode = "404", description = "Student with specified ID not found")
    })
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getClassesForStudent(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable int studentId) {
        Person student = personRepository.findById(studentId);
        if (student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        List<StudentClasses> classes = classesRepository.findClassesByStudentId(studentId);
        return new ResponseEntity<>(classes, HttpStatus.OK);
    }

    @Operation(summary = "Get classes by teacher", description = "Retrieves all classes taught by a specific teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved classes"),
            @ApiResponse(responseCode = "404", description = "Teacher with specified ID not found")
    })
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getClassesByTeacher(
            @Parameter(description = "ID of the teacher", required = true)
            @PathVariable int teacherId) {
        Person teacher = personRepository.findById(teacherId);
        if (teacher == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }
        List<StudentClasses> classes = classesRepository.findClassesByTeacherId(teacherId);
        return new ResponseEntity<>(classes, HttpStatus.OK);
    }

    @Operation(summary = "Create a new class", description = "Creates a new class with specified details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Class successfully created"),
            @ApiResponse(responseCode = "400", description = "Teacher not found"),
            @ApiResponse(responseCode = "409", description = "Class with same name and teacher already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> createClass(
            @Parameter(description = "Class details", required = true)
            @RequestBody StudentClasses studentClass) {
        try {
            // Verify teacher exists
            if (studentClass.getTeacher() != null) {
                Person teacher = personRepository.findById(studentClass.getTeacher().getId());
                if (teacher == null) {
                    return new ResponseEntity<>("{\"message\":\"Teacher not found\"}", HttpStatus.BAD_REQUEST);
                }
                studentClass.setTeacher(teacher);

                // Check if class with same name and teacher already exists
                StudentClasses existingClass = classesRepository.findByClassNameAndTeacherId(
                        studentClass.getClassName(), teacher.getId());

                if (existingClass != null) {
                    return new ResponseEntity<>("{\"message\":\"A class with this name and teacher already exists\"}",
                            HttpStatus.CONFLICT);
                }

                StudentClasses existingClassName = classesRepository.findByClassName(
                        studentClass.getClassName());

                if(existingClassName != null){
                    return new ResponseEntity<>("{\"message\":\"This class name is already taken\"}",
                            HttpStatus.CONFLICT);
                }
            }

            classesRepository.save(studentClass);
            return new ResponseEntity<>(studentClass, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(failure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update a class", description = "Updates an existing class with new details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Class successfully updated"),
            @ApiResponse(responseCode = "400", description = "Teacher not found"),
            @ApiResponse(responseCode = "404", description = "Class with specified ID not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClass(
            @Parameter(description = "ID of the class to update", required = true)
            @PathVariable int id,
            @Parameter(description = "Updated class details", required = true)
            @RequestBody StudentClasses studentClass) {
        StudentClasses existingClass = classesRepository.findById(id);
        if (existingClass == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        // Verify teacher exists if provided
        if (studentClass.getTeacher() != null) {
            Person teacher = personRepository.findById(studentClass.getTeacher().getId());
            if (teacher == null) {
                return new ResponseEntity<>("{\"message\":\"Teacher not found\"}", HttpStatus.BAD_REQUEST);
            }
            studentClass.setTeacher(teacher);
        }

        // Ensure ID is preserved
        studentClass.setId(id);

        // Preserve existing students if not provided in request
        if (studentClass.getStudents() == null || studentClass.getStudents().isEmpty()) {
            studentClass.setStudents(existingClass.getStudents());
        }

        classesRepository.save(studentClass);
        return new ResponseEntity<>(classesRepository.findById(id), HttpStatus.OK);
    }

    @Operation(summary = "Delete a class", description = "Removes a class from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Class successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Class with specified ID not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClass(
            @Parameter(description = "ID of the class to delete", required = true)
            @PathVariable int id) {
        try{
            StudentClasses existingClass = classesRepository.findById(id);
            if (existingClass == null) {
                return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
            }

            existingClass.setStudents(new HashSet<>());
            classesRepository.save(existingClass);

            // Then delete the class
            classesRepository.deleteById(id);
            return new ResponseEntity<>(success, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace(); // Log the full error
            return new ResponseEntity<>("{\"message\":\"" + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Add student to class", description = "Enrolls a student in a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student successfully added to class"),
            @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<?> addStudentToClass(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable int classId,
            @Parameter(description = "ID of the student to add", required = true)
            @PathVariable int studentId) {
        StudentClasses studentClass = classesRepository.findById(classId);
        Person student = personRepository.findById(studentId);

        if (studentClass == null || student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        studentClass.addStudent(student);
        classesRepository.save(studentClass);

        return new ResponseEntity<>(success, HttpStatus.OK);
    }

    @Operation(summary = "Remove student from class", description = "Removes a student's enrollment from a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student successfully removed from class"),
            @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<?> removeStudentFromClass(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable int classId,
            @Parameter(description = "ID of the student to remove", required = true)
            @PathVariable int studentId) {
        StudentClasses studentClass = classesRepository.findById(classId);
        Person student = personRepository.findById(studentId);

        if (studentClass == null || student == null) {
            return new ResponseEntity<>(failure, HttpStatus.NOT_FOUND);
        }

        studentClass.removeStudent(student);
        classesRepository.save(studentClass);

        return new ResponseEntity<>(success, HttpStatus.OK);
    }

    @Operation(summary = "Get dashboard summary", description = "Retrieves summary information about all classes for admin dashboard")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard summary")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardSummary() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalClasses", classesRepository.count());
        dashboard.put("classes", classesRepository.findAll());

        return new ResponseEntity<>(dashboard, HttpStatus.OK);
    }
}