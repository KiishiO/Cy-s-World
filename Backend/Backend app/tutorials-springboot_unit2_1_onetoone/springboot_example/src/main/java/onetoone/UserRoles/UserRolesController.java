package onetoone.UserRoles;

import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequestMapping("/roles")
@Tag(name = "User Roles", description = "User role management API")
public class UserRolesController {
    @Autowired
    private PersonRepository personRepository;

    @Operation(summary = "Get all available roles", description = "Returns an array of all available user roles in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of roles",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserRoles.class))))
    })
    @GetMapping
    public UserRoles[] getAllRoles() {
        return UserRoles.values();
    }

    @Operation(summary = "Get users by role", description = "Returns a list of users with a specific role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Person.class))))
    })
    @GetMapping("/{role}/users")
    public ResponseEntity<?> getUsersByRole(
            @Parameter(description = "Role to filter by", required = true) @PathVariable UserRoles role) {
        List<Person> users = personRepository.findByRole(role);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Assign role to user", description = "Assigns a role to an existing user (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role successfully assigned"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not an admin"),
            @ApiResponse(responseCode = "404", description = "Person not found")
    })
    @PutMapping("/assign/{personId}")
    public ResponseEntity<String> assignRole(
            @Parameter(description = "ID of the person to assign a role", required = true) @PathVariable int personId,
            @Parameter(description = "Role to assign", required = true) @RequestParam UserRoles role,
            @Parameter(description = "ID of the admin making the request", required = true)
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

    @Operation(summary = "Approve signup and assign role",
            description = "Approves a signup request and assigns an initial role (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account activated with assigned role"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not an admin"),
            @ApiResponse(responseCode = "404", description = "No person found with this signup ID")
    })
    @PostMapping("/approve-signup/{signupId}")
    public ResponseEntity<String> approveSignupWithRole(
            @Parameter(description = "ID of the signup to approve", required = true) @PathVariable int signupId,
            @Parameter(description = "Role to assign", required = true) @RequestParam UserRoles role,
            @Parameter(description = "ID of the admin making the request", required = true)
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