package onetoone.Persons;

import java.util.*;

import jakarta.transaction.Transactional;
import onetoone.Login.Login;
import onetoone.TestingCenter.ExamInfoRepository;
import onetoone.TestingCenter.TestingCenter;
import onetoone.TestingCenter.TestingCenterRepository;
import onetoone.UserRoles.UserRoles;
import onetoone.StudentClasses.StudentClasses;
import onetoone.StudentClasses.StudentClassesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@Tag(name = "Person", description = "Person management API")
public class PersonController {

    @Autowired
    PersonRepository personRepository;

    @Autowired
    StudentClassesRepository classesRepository;

    @Autowired
    ExamInfoRepository examRepository;

    @Autowired
    TestingCenterRepository testingCenterRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    // ----------------- Basic CRUD operations -----------------

    @Operation(summary = "Get all persons", description = "Returns a list of all persons in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of persons",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class)))
    })
    @GetMapping(path = "/persons")
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @Operation(summary = "Get person by ID", description = "Returns a single person by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved person",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "404", description = "Person not found")
    })
    @GetMapping(path = "/persons/{id}")
    public Person getPersonById(
            @Parameter(description = "ID of the person to be retrieved", required = true) @PathVariable int id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }
        return person;
    }

    @Operation(summary = "Create a new person", description = "Creates a new person entity in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input, object invalid")
    })
    @PostMapping(path = "/persons")
    public ResponseEntity<String> createPerson(
            @Parameter(description = "Person object to be created", required = true,
                    schema = @Schema(implementation = Person.class)) @RequestBody Person person) {
        if (person == null) {
            return ResponseEntity.badRequest().body(failure);
        }

        // Default to STUDENT role if not specified
        if (person.getRole() == null) {
            person.setRole(UserRoles.STUDENT);
        }
        if(personRepository.equals(person.getId())){
            return ResponseEntity.badRequest().body("This user ID exist, Please make another one");
        }

        personRepository.save(person);
        return ResponseEntity.ok(success);
    }

    @Operation(summary = "Update an existing person", description = "Updates an existing person entity with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person successfully updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "400", description = "ID mismatch between path and request body")
    })
    @PutMapping("/persons/{id}")
    public ResponseEntity<?> updatePerson(
            @Parameter(description = "ID of the person to update", required = true) @PathVariable int id,
            @Parameter(description = "Updated person object", required = true,
                    schema = @Schema(implementation = Person.class)) @RequestBody Person request) {
        Person person = personRepository.findById(id);

        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure);
        } else if (person.getId() != id) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"Path variable id does not match person request id\"}");
        }

        personRepository.save(request);
        return ResponseEntity.ok(personRepository.findById(id));
    }

    @Operation(summary = "Delete a person", description = "Deletes a person entity by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error during deletion")
    })
    @DeleteMapping(path = "/persons/{id}")
    @Transactional
    public ResponseEntity<String> deletePerson(
            @Parameter(description = "ID of the person to delete", required = true) @PathVariable int id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure);
        }

        try {
            // Clear relationships first
            if (person.getSignupInfo() != null) {
                // Just remove the reference, don't manipulate the signup
                person.setSignupInfo(null);
            }

            if (person.getLogin() != null) {
                person.setLogin(null);
            }

            // Clear collections
            if (person.getEnrolledClasses() != null) {
                person.getEnrolledClasses().clear();
            }

            if (person.getClassesTeaching() != null) {
                person.getClassesTeaching().clear();
            }

            if (person.getFriends() != null) {
                person.getFriends().clear();
            }

            // Save these changes within the same transaction
            personRepository.save(person);

            // Delete the person
            personRepository.delete(person);

            return ResponseEntity.ok(success);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Failed to delete person: " + e.getMessage() + "\"}");
        }
    }

    // ----------------- Role-based operations -----------------

    @Operation(summary = "Get persons by role", description = "Returns a list of persons with a specific role (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of persons",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Not an admin")
    })
    @GetMapping("/persons/by-role/{role}")
    public ResponseEntity<?> getPersonsByRole(
            @Parameter(description = "Role to filter by", required = true) @PathVariable UserRoles role,
            @Parameter(description = "ID of the admin making the request", required = true)
            @RequestHeader(value = "Admin-Id", required = true) Integer adminId) {

        // Optional: Verify admin permissions
        if (adminId != null) {
            Person admin = personRepository.findById(adminId);
            if (admin == null || !admin.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"Only admins can view all users by role\"}");
            }
        }

        List<Person> persons = personRepository.findByRole(role);
        return ResponseEntity.ok(persons);
    }

    @Operation(summary = "Change person role", description = "Updates a person's role (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role successfully updated"),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not an admin")
    })
    @PutMapping("/persons/{id}/role")
    public ResponseEntity<String> changePersonRole(
            @Parameter(description = "ID of the person to change role", required = true) @PathVariable int id,
            @Parameter(description = "New role to assign", required = true) @RequestParam UserRoles newRole,
            @Parameter(description = "ID of the admin making the request", required = true)
            @RequestHeader("Admin-Id") int adminId) {

        // Verify admin permissions
        Person admin = personRepository.findById(adminId);
        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\":\"Only admins can change roles\"}");
        }

        Person person = personRepository.findById(id);
        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure);
        }

        person.setRole(newRole);
        personRepository.save(person);
        return ResponseEntity.ok("{\"message\":\"Role updated successfully\"}");
    }

    @Operation(summary = "Get classes taught by teacher", description = "Returns a list of classes taught by a specific teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of classes",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "400", description = "Person is not a teacher")
    })
    @GetMapping("/persons/{id}/classes/teaching")
    public ResponseEntity<?> getClassesTeaching(
            @Parameter(description = "ID of the teacher", required = true) @PathVariable int id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure);
        }

        if (!person.isTeacher()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"This person is not a teacher\"}");
        }

        return ResponseEntity.ok(classesRepository.findByTeacher(person));
    }

    @Operation(summary = "Get classes enrolled by student", description = "Returns a list of classes a student is enrolled in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of enrolled classes",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "400", description = "Person is not a student")
    })
    @GetMapping("/persons/{id}/classes/enrolled")
    public ResponseEntity<?> getClassesEnrolled(
            @Parameter(description = "ID of the student", required = true) @PathVariable int id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure);
        }

        if (!person.isStudent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"This person is not a student\"}");
        }

        return ResponseEntity.ok(classesRepository.findClassesByStudentId(id));
    }

    @Operation(summary = "Get admin dashboard", description = "Returns system statistics for admin dashboard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard data",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Access denied - Not an admin")
    })
    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getAdminDashboard(
            @Parameter(description = "ID of the admin making the request", required = true)
            @RequestHeader("Admin-Id") int adminId) {
        // Verify admin permissions
        Person admin = personRepository.findById(adminId);
        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\":\"Only admins can access the admin dashboard\"}");
        }

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalUsers", personRepository.count());
        dashboard.put("adminCount", personRepository.findByRole(UserRoles.ADMIN).size());
        dashboard.put("teacherCount", personRepository.findByRole(UserRoles.TEACHER).size());
        dashboard.put("studentCount", personRepository.findByRole(UserRoles.STUDENT).size());
        dashboard.put("totalClasses", classesRepository.count());

        return ResponseEntity.ok(dashboard);
    }
}