package onetoone.Login;

import onetoone.Persons.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for handling Login entity operations.
 */
@RestController
@RequestMapping("/Logins")
@Tag(name = "Login", description = "Login management API")
public class LoginController {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private LoginService loginService;

    private final String success = "{\"message\":\"success\"}";
    private final String failure = "{\"message\":\"failure\"}";

    /**
     * Get all logins.
     */
    @Operation(summary = "Get all logins", description = "Returns a list of all login entities in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of logins",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Login.class))),
            @ApiResponse(responseCode = "204", description = "No logins exist in the system")
    })
    @GetMapping
    public ResponseEntity<List<Login>> getAllLogins() {
        List<Login> logins = loginRepository.findAll();
        if (logins.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 if no users exist
        }
        return ResponseEntity.ok(logins);
    }

    /**
     * Get a login by ID.
     */
    @Operation(summary = "Get login by ID", description = "Returns a single login based on its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved login",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Login.class))),
            @ApiResponse(responseCode = "404", description = "Login not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Login> getLoginById(
            @Parameter(description = "ID of the login to be retrieved", required = true) @PathVariable Long id) {
        return loginRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get a login by email.
     */
    @Operation(summary = "Get login by email", description = "Returns a single login based on its email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved login",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Login.class))),
            @ApiResponse(responseCode = "404", description = "Login not found")
    })
    @GetMapping("/{email}")
    public ResponseEntity<Login> getLoginByEmail(
            @Parameter(description = "Email of the login to be retrieved", required = true) @PathVariable String email) {
        Optional<Login> login = loginService.getUserByEmail(email);
        return login.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Register password for a login", description = "Updates a login with a new password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully registered",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Login.class))),
            @ApiResponse(responseCode = "404", description = "Login not found")
    })
    @PutMapping("/registerPassword/{id}")
    public ResponseEntity<Login> registerPassword(
            @Parameter(description = "ID of the login to update", required = true) @PathVariable Long id,
            @Parameter(description = "New password to register", required = true) @RequestParam String password) {

        Optional<Login> login = loginService.getUserById(id);
        if (login.isPresent()) {
            Login updatedLogin = loginService.registerPassword(login.get(), password);
            return ResponseEntity.ok(updatedLogin);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Register a new login.
     */
    @Operation(summary = "Register a new login", description = "Creates a new login entity in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Login successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Login.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input, object invalid")
    })
    @PostMapping(path = "/new", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Login> registerLogin(
            @Parameter(description = "Login object to be created", required = true,
                    schema = @Schema(implementation = Login.class)) @RequestBody Login login) {
        if (login == null || login.getEmailId() == null || login.getName() == null || login.getPassword() == null) {
            return ResponseEntity.badRequest().body(null); // 400 Bad Request if login is invalid
        }

        // Call the service to register the user
        Login savedLogin = loginService.registerUser(login);

        // Return the created login with a 201 Created status
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLogin); // 201 Created status
    }

    /**
     * Update an existing login.
     */
    @Operation(summary = "Update an existing login", description = "Updates an existing login entity with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successfully updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Login.class))),
            @ApiResponse(responseCode = "404", description = "Login not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Login> updateLogin(
            @Parameter(description = "ID of the login to update", required = true) @PathVariable Long id,
            @Parameter(description = "Updated login object", required = true,
                    schema = @Schema(implementation = Login.class)) @RequestBody Login updatedLogin) {
        return loginRepository.findById(id)
                .map(existingLogin -> {
                    // Update basic Login details
                    existingLogin.setName(updatedLogin.getName());
                    existingLogin.setEmailId(updatedLogin.getEmailId());
                    existingLogin.setIfActive(updatedLogin.getIfActive());
                    existingLogin.setPassword(updatedLogin.getPassword());

                    // Check if the request contains a new Person
                    if (updatedLogin.getPerson() != null) {
                        Person updatedPerson = updatedLogin.getPerson();

                        if (existingLogin.getPerson() != null) {
                            // Update existing Person fields
                            existingLogin.getPerson().setName(updatedPerson.getName());
                            existingLogin.getPerson().setPhoneNumber(updatedPerson.getPhoneNumber());
                        } else {
                            // Assign a new Person if none exists
                            existingLogin.setPerson(updatedPerson);
                        }
                    }

                    loginRepository.save(existingLogin);
                    return ResponseEntity.ok(existingLogin);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a login by ID.
     */
    @Operation(summary = "Delete a login", description = "Deletes a login entity by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successfully deleted",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Login not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLogin(
            @Parameter(description = "ID of the login to delete", required = true) @PathVariable Long id) {
        if (!loginRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        loginRepository.deleteById(id);
        return ResponseEntity.ok(success);
    }

    @Operation(summary = "Authenticate a user", description = "Validates user credentials for login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "Login request containing email and password", required = true,
                    schema = @Schema(implementation = LoginRequest.class)) @RequestBody LoginRequest loginRequest) {
        try {
            Optional<Login> login = loginService.getUserByEmail(loginRequest.getEmailId());
            if (login.isPresent()) {
                // For now, just check if user exists
                return ResponseEntity.ok().body("{\"message\":\"Login successful\"}");
            } else {
                return ResponseEntity.status(401).body("{\"message\":\"Invalid credentials\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"message\":\"Server error\"}");
        }
    }
}