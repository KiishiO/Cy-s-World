package onetoone.Login;

import onetoone.Persons.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for handling Login entity operations.
 */
@RestController
@RequestMapping("/Logins") // Base URL for all endpoints
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
    @GetMapping("/{id}")
    public ResponseEntity<Login> getLoginById(@PathVariable Long id) {
        return loginRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get a login by email.
     */
    @GetMapping("/{email}")
    public ResponseEntity<Login> getLoginByEmail(@PathVariable String email) {
        Optional<Login> login = loginService.getUserByEmail(email);
        return login.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/registerPassword/{id}")
    public ResponseEntity<Login> registerPassword(@PathVariable Long id, @RequestParam String password) {

        Optional<Login> login = loginService.getUserById(id);
        if (login.isPresent()) {
            Login updatedLogin = loginService.registerPassword(login.get(), password);
            return ResponseEntity.ok(updatedLogin);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Gets login at that currently active
     *
     */
//    @GetMapping("/active/{status}")
//    public ResponseEntity<List<Login>> getActiveLogins(@PathVariable Boolean status) {
//        List<Login> activeLogins = loginRepository.findByIfActive(status);
//
//        if (activeLogins.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        return ResponseEntity.ok(activeLogins);
//    }
    /**
     * Register a new login.
     */
    @PostMapping("/new")
    public ResponseEntity<Login> registerLogin(@RequestBody Login login) {
        if (login == null || login.getEmailId() == null || login.getName() == null || login.getPassword() == null) {
            return ResponseEntity.badRequest().body(null); // 400 Bad Request if login is invalid
        }
        Login savedLogin = loginService.registerUser(login);
        return ResponseEntity.ok(savedLogin);
    }

    /**
     * Update an existing login.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Login> updateLogin(@PathVariable Long id, @RequestBody Login updatedLogin) {
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
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLogin(@PathVariable Long id) {
        if (!loginRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        loginRepository.deleteById(id);
        return ResponseEntity.ok(success);
    }
}