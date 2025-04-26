package onetoone.Signup;

import java.util.List;

import jakarta.transaction.Transactional;
import onetoone.Login.Login;
import onetoone.Persons.Person;
import onetoone.UserRoles.UserRoles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import onetoone.Persons.PersonRepository;

/**
 *
 * @author Vivek Bengre
 *
 */

@RestController
@RequestMapping("/signup")
public class SignupController {

    @Autowired
    SignupRepository signupRepository;

    @Autowired
    PersonRepository personRepository;

    // Admin and teacher authentication codes
    private static final String ADMIN_AUTH_CODE = "admin-secret-2025";
    private static final String TEACHER_AUTH_CODE = "teacher-access-2025";

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping
    public ResponseEntity<List<Signup>> getAllSignUp() {
        List<Signup> signup = signupRepository.findAll();
        if (signup.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 if no users exist
        }
        return ResponseEntity.ok(signup);
    }

    @GetMapping("/{id}")
    Signup getSignupById(@PathVariable int id){
        return signupRepository.findById(id);
    }

    @PostMapping("/Newsignup")
    public ResponseEntity<String> createSignup(@RequestBody Signup signup,
                                               @RequestParam(required = false) String authCode) {
        if (signup == null || signup.getUsername() == null || signup.getEmail() == null) {
            return ResponseEntity.badRequest().body(failure);
        }

        // Validate role-based signup with authentication code
        if (isRoleRestricted(signup.getRole())) {
            if (authCode == null || !isValidAuthCode(signup.getRole(), authCode)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"Invalid authentication code for " + signup.getRole() + " role\"}");
            }
        }

        // If no role specified, default to STUDENT
        if (signup.getRole() == null) {
            signup.setRole(UserRoles.STUDENT);
        }

        // Create new Person entity with appropriate role
        Person newPerson = new Person(signup.getFirstAndLastName(), signup.getEmail(), signup.getRole());
        signupRepository.save(signup);
        newPerson.setSignupInfo(signup);
        personRepository.save(newPerson);

        return ResponseEntity.ok(success);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Signup> updateSignupInfo(@PathVariable int id, @RequestBody Signup request,
                                                   @RequestParam(required = false) String authCode) {
        Signup currentSignup = signupRepository.findById(id);
        if(currentSignup == null) {
            return ResponseEntity.notFound().build();
        }


        // If role is being updated and is restricted, validate auth code
        if (request.getRole() != null && request.getRole() != currentSignup.getRole()
                && isRoleRestricted(request.getRole())) {
            if (authCode == null || !isValidAuthCode(request.getRole(), authCode)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // Update role if auth code is valid
            currentSignup.setRole(request.getRole());

            // Update the Person's role as well
            Person person = personRepository.findBySignup_Id(id);
            if (person != null) {
                person.setRole(request.getRole());
                personRepository.save(person);
            }
        }

        // Update other fields
        if(request.getUsername() != null) {
            currentSignup.setUsername(request.getUsername());
        }
        if(request.getEmail() != null) {
            currentSignup.setEmail(request.getEmail());
        }
        if(request.getFirstAndLastName() != null) {
            currentSignup.setFirstAndLastName(request.getFirstAndLastName());
        }
        //ensures that the user does not save the same password when they are changing it for security purposes
        if(request.getPassword() != null && !request.getPassword().equals(currentSignup.getPassword())) {
            currentSignup.setPassword(request.getPassword());
        }

        signupRepository.save(currentSignup);
        return ResponseEntity.ok(signupRepository.findById(id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> deleteSignupInfo(@PathVariable int id,
                                                   @RequestParam(required = false) String authCode) {

        // Only admins can delete users
        if (authCode == null || !ADMIN_AUTH_CODE.equals(authCode)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\":\"Only administrators can delete user accounts\"}");
        }

        try {
            // Find the signup
            Signup signup = signupRepository.findById(id);
            if (signup == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"message\":\"Signup not found\"}");
            }

            // Find any associated person
            Person person = personRepository.findBySignup_Id(id);

            // Find any associated login
            Login login = null;
            if (signup.getLogin() != null) {
                login = signup.getLogin();
                signup.setLogin(null);
            }

            // Handle person relationships first
            if (person != null) {
                // Remove reference to signup
                person.setSignupInfo(null);

                // Remove login reference if exists
                if (person.getLogin() != null) {
                    person.setLogin(null);
                }

                // Handle collections
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
            }

            // Update the database with these relationship changes
            if (signup != null) {
                signupRepository.save(signup);
            }

            // Now delete the entities in the correct order to avoid constraint violations
            if (login != null) {
                // Need to inject LoginRepository
                // loginRepository.delete(login);
            }

            if (person != null) {
                personRepository.delete(person);
            }

            if (signup != null) {
                signupRepository.delete(signup);
            }

            return ResponseEntity.ok(success);
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Failed to delete user: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Check if the role requires special authentication
     */
    private boolean isRoleRestricted(UserRoles role) {
        return role == UserRoles.ADMIN || role == UserRoles.TEACHER;
    }

    /**
     * Validate authentication code for restricted roles
     */
    private boolean isValidAuthCode(UserRoles role, String authCode) {
        if (role == UserRoles.ADMIN) {
            return ADMIN_AUTH_CODE.equals(authCode);
        } else if (role == UserRoles.TEACHER) {
            return TEACHER_AUTH_CODE.equals(authCode);
        }
        return true; // Non-restricted roles don't need validation
    }
}