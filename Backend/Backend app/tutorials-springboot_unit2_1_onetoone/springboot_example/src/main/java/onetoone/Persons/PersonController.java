package onetoone.Persons;

import java.util.*;

import jakarta.transaction.Transactional;
import onetoone.Login.Login;
import onetoone.TestingCenter.TestingCenter;
import onetoone.TestingCenter.TestingCenterRepository;
import onetoone.TestingSystem.Exam;
import onetoone.TestingSystem.ExamRepository;
import onetoone.TestingSystem.TestingSystem;
import onetoone.TestingSystem.TestingSystemRepository;
import onetoone.UserRoles.UserRoles;
import onetoone.StudentClasses.StudentClasses;
import onetoone.StudentClasses.StudentClassesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PersonController {

    @Autowired
    PersonRepository personRepository;

    @Autowired
    StudentClassesRepository classesRepository;

    @Autowired
    ExamRepository examRepository;

    @Autowired
    TestingSystemRepository testingSystemRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    // ----------------- Basic CRUD operations -----------------

    @GetMapping(path = "/persons")
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @GetMapping(path = "/persons/{id}")
    public Person getPersonById(@PathVariable int id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }
        return person;
    }

    @PostMapping(path = "/persons")
    public ResponseEntity<String> createPerson(@RequestBody Person person) {
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

    @DeleteMapping(path = "/persons/{id}")
    @Transactional
    public ResponseEntity<String> deletePerson(@PathVariable int id) {
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

    // Admin: Get all persons by role
    @GetMapping("/persons/by-role/{role}")
    public ResponseEntity<?> getPersonsByRole(
            @PathVariable UserRoles role,
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

    // Admin: Change a person's role
    @PutMapping("/persons/{id}/role")
    public ResponseEntity<String> changePersonRole(
            @PathVariable int id,
            @RequestParam UserRoles newRole,
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

    // Teacher: Get all classes taught by a teacher
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

    // Student: Get all classes enrolled in by a student
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

    // Admin: Get dashboard with user statistics
    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getAdminDashboard(@RequestHeader("Admin-Id") int adminId) {
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

    //sign up for a new exam at a testing center
    @PostMapping("/persons/{id}/newexam")
    public ResponseEntity<?> addNewExam(@RequestParam int examId, @PathVariable int personId, @RequestParam int centerId) {
        Person person = personRepository.findById(personId);
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        TestingSystem center = testingSystemRepository.findById(centerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if(!exam.getTestingSystem().contains(center)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"This exam does not exist\"}");
        }

        if(person.getExams() == null) {
            person.setExams(new HashSet<>());
        }

        if(person.getExams().contains(exam)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"You have already signed up for this exam.\"}");
        }

        person.getExams().add(exam);
        personRepository.save(person);

        return ResponseEntity.ok("Signed up successfuly for exam " + exam.getSubject() + "at center " + center.getLocation());

    }

}