package onetoone.UserRoles;

import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class UserRolesController {
        @Autowired
        private PersonRepository personRepository;

        // Get all available roles
        @GetMapping
        public UserRoles[] getAllRoles() {
            return UserRoles.values();
        }

        // Get all users with a specific role
        @GetMapping("/{role}/users")
        public ResponseEntity<?> getUsersByRole(@PathVariable UserRoles role) {
            List<Person> users = personRepository.findByRole(role);
            return ResponseEntity.ok(users);
        }

        // Admin functionality to assign roles
        @PutMapping("/assign/{personId}")
        public ResponseEntity<String> assignRole(
                @PathVariable int personId,
                @RequestParam UserRoles role,
                @RequestHeader("Admin-Id") int adminId) {

            // Verify the requester is an admin
            Person admin = personRepository.findById(adminId);
            if (admin == null || admin.getRole() != UserRoles.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"Only administrators can assign roles\"}");
            }

            // Find the person to update
            Person person = personRepository.findById(personId);
            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"message\":\"Person not found\"}");
            }

            // Update the role
            person.setRole(role);

            personRepository.save(person);

            return ResponseEntity.ok("{\"message\":\"Role successfully assigned\"}");
        }

        // Admin functionality to assign initial role during account approval
        @PostMapping("/approve-signup/{signupId}")
        public ResponseEntity<String> approveSignupWithRole(
                @PathVariable int signupId,
                @RequestParam UserRoles role,
                @RequestHeader("Admin-Id") int adminId) {

            // Verify the requester is an admin
            Person admin = personRepository.findById(adminId);
            if (admin == null || admin.getRole() != UserRoles.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"Only administrators can approve signups\"}");
            }

            // Find the person with this signup ID
            Person person = personRepository.findBySignup_Id(signupId);
            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"message\":\"No person found with this signup ID\"}");
            }

            // Assign the role and activate the account
            person.setRole(role);
            person.setIfActive(true);
            personRepository.save(person);

            return ResponseEntity.ok("{\"message\":\"Account activated with " + role + " role\"}");
        }

    }