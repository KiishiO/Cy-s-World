package onetoone.Login;

import org.springframework.beans.factory.annotation.Autowired;
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
     * Get all users.
     */
    @GetMapping
    public List<Login> getAllUsers() {
        return loginRepository.findAll();
    }

    /**
     * Get user by ID.
     */
//    @GetMapping("/{id}")
//    public ResponseEntity<Login> getUserById(@PathVariable int id) {
//        return loginRepository.findById(id)
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }

    /**
     * Get user by email.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Login> getUserByEmail(@PathVariable String email) {
        Optional<Login> user = loginService.getUserByEmail(email);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Register a new user.
     */
    @PostMapping
    public ResponseEntity<Login> registerUser(@RequestBody Login login) {
        if (login == null) return ResponseEntity.badRequest().build();
        Login savedUser = loginService.registerUser(login);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * Update an existing user's information.
     */
//    @PutMapping("/{id}")
//    public ResponseEntity<Login> updateUser(@PathVariable int id, @RequestBody Login login) {
//        if (login == null) return ResponseEntity.badRequest().build();
//
//        return loginRepository.findById(id)
//                .map(existingUser -> {
//                    // Update fields
//                    existingUser.setName(login.getName());
//                    existingUser.setEmailId(login.getEmailId());
//                    existingUser.setIfActive(login.getIfActive());
//
//                    loginRepository.save(existingUser);
//                    return ResponseEntity.ok(existingUser);
//                })
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }

    /**
     * Delete a user by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        if (!loginRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        loginRepository.deleteById(id);
        return ResponseEntity.ok(success);
    }
}