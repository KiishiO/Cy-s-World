package onetoone.Signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/signup")
public class SignupController {

    @Autowired
    private SignupRepository signupRepository;

    // Create a new signup
    @PostMapping
    public ResponseEntity<Signup> createSignup(@RequestBody Signup signup) {
        if (signup.getUsername() == null || signup.getEmail() == null || signup.getPassword() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Signup savedSignup = signupRepository.save(signup);
        return ResponseEntity.ok(savedSignup);
    }

    // Get all signups
    @GetMapping
    public List<Signup> getAllSignups() {
        return signupRepository.findAll();
    }

    // Get a signup by ID
    @GetMapping("/{id}")
    public ResponseEntity<Signup> getSignupById(@PathVariable Long id) {
        Optional<Signup> signup = signupRepository.findById(id);
        return signup.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Update a signup
    @PutMapping("/{id}")
    public ResponseEntity<Signup> updateSignup(@PathVariable Long id, @RequestBody Signup updatedSignup) {
        Optional<Signup> existingSignupOptional = signupRepository.findById(id);

        if (existingSignupOptional.isPresent()) {
            Signup existingSignup = existingSignupOptional.get();
            existingSignup.setUsername(updatedSignup.getUsername());
            existingSignup.setEmail(updatedSignup.getEmail());
            existingSignup.setPassword(updatedSignup.getPassword());
            signupRepository.save(existingSignup);
            return ResponseEntity.ok(existingSignup);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a signup
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSignup(@PathVariable Long id) {
        if (signupRepository.existsById(id)) {
            signupRepository.deleteById(id);
            return ResponseEntity.ok("{\"message\": \"Signup deleted successfully\"}");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
