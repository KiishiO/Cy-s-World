package onetoone.Persons;

import java.util.*;

import io.swagger.v3.oas.annotations.tags.Tag;
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

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Sonia Patil, Jayden Sorter
 */
@RestController
@Tag(name = "Person Management API")
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

    @Operation(summary = "Get all persons", description = "Retrieve a list of all persons")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of persons", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class)))
    @GetMapping(path = "/persons")
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @Operation(summary = "Get person by ID", description = "Retrieve a person by their unique ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved person", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class)))
    @ApiResponse(responseCode = "404", description = "Person not found", content = @Content(mediaType = "application/json"))
    @GetMapping(path = "/persons/{id}")
    public Person getPersonById(@PathVariable int id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }
        return person;
    }

    @Operation(summary = "Create a new person", description = "Create a new person in the system")
    @ApiResponse(responseCode = "200", description = "Person created successfully", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "Bad request due to invalid data", content = @Content(mediaType = "application/json"))
    @PostMapping(path = "/persons")
    public ResponseEntity<String> createPerson(@RequestBody Person person) {
        if (person == null) {
            return ResponseEntity.badRequest().body(failure);
        }

        if (person.getRole() == null) {
            person.setRole(UserRoles.STUDENT);
        }
        if(personRepository.equals(person.getId())){
            return ResponseEntity.badRequest().body("This user ID exist, Please make another one");
        }

        personRepository.save(person);
        return ResponseEntity.ok(success);
    }

    @Operation(summary = "Update a person's information", description = "Update the details of an existing person by ID")
    @ApiResponse(responseCode = "200", description = "Person updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class)))
    @ApiResponse(responseCode = "400", description = "Bad request due to mismatched IDs or invalid data", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Person not found", content = @Content(mediaType = "application/json"))
    @PutMapping("/persons/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable int id, @RequestBody Person request) {
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

    @Operation(summary = "Delete a person", description = "Delete a person by their unique ID")
    @ApiResponse(responseCode = "200", description = "Person deleted successfully", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Person not found", content = @Content(mediaType = "application/json"))
    @Transactional
    @DeleteMapping(path = "/persons/{id}")
    public ResponseEntity<String> deletePerson(@PathVariable int id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure);
        }

        try {
            // Clear relationships first
            if (person.getSignupInfo() != null) {
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

            personRepository.save(person);
            personRepository.delete(person);

            return ResponseEntity.ok(success);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Failed to delete person: " + e.getMessage() + "\"}");
        }
    }

    // ----------------- Role-based operations -----------------

    @Operation(summary = "Get all persons by role", description = "Retrieve a list of persons filtered by role")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved persons by role", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Person.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden, only admin can access this resource", content = @Content(mediaType = "application/json"))
    @GetMapping("/persons/by-role/{role}")
    public ResponseEntity<?> getPersonsByRole(
            @PathVariable UserRoles role,
            @RequestHeader(value = "Admin-Id", required = true) Integer adminId) {

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

    @Operation(summary = "Change a person's role", description = "Allows an admin to change a person's role")
    @ApiResponse(responseCode = "200", description = "Role updated successfully", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Forbidden, only admin can change roles", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Person not found", content = @Content(mediaType = "application/json"))
    @PutMapping("/persons/{id}/role")
    public ResponseEntity<String> changePersonRole(
            @PathVariable int id,
            @RequestParam UserRoles newRole,
            @RequestHeader("Admin-Id") int adminId) {

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

    @Operation(summary = "Get all classes taught by a teacher", description = "Retrieve all classes taught by a specific teacher")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved classes taught by teacher", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentClasses.class)))
    @ApiResponse(responseCode = "404", description = "Teacher not found", content = @Content(mediaType = "application/json"))
    @GetMapping("/persons/{id}/classes/teaching")
    public ResponseEntity<?> getClassesTeaching(@PathVariable int id) {
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

    @Operation(summary = "Get all classes enrolled by a student", description = "Retrieve all classes a specific student is enrolled in")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved enrolled classes", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentClasses.class)))
    @ApiResponse(responseCode = "404", description = "Student not found", content = @Content(mediaType = "application/json"))
    @GetMapping("/persons/{id}/classes/enrolled")
    public ResponseEntity<?> getClassesEnrolled(@PathVariable int id) {
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

    @Operation(summary = "Get admin dashboard", description = "Retrieve the admin dashboard with user statistics")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved admin dashboard", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Forbidden, only admin can access this resource", content = @Content(mediaType = "application/json"))
    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getAdminDashboard(@RequestHeader("Admin-Id") int adminId) {
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
