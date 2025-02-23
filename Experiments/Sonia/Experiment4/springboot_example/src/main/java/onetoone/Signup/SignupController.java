package onetoone.Signup;

import onetoone.Persons.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


/**
 * @author Sonia Patil
 */

@RestController
@RequestMapping("/Signup") // Base URL for all endpoints
public class SignupController {

    @Autowired
    SignupRepository signupRepository;

    @Autowired
    private SignupService signupService;

    private final String success = "{\"message\":\"success\"}";
    private final String failure = "{\"message\":\"failure\"}";

    /**
     * Get all signups
     */
    @GetMapping(path = "/Signups")
    public ResponseEntity<List<Signup>> getAllSignups() {
        List<Signup> signups = signupRepository.findAll();
        if(signups.isEmpty()) {
            return ResponseEntity.noContent().build(); //Returns 204 if no users exist
        }
        return ResponseEntity.ok(signups);
    }

    /**
     * Create a new user (new user in signing up)
     */
    @PostMapping(path = "/new")
    public ResponseEntity<Signup> newUserSignup(@RequestBody Signup signup) {
        if(signup == null || signup.getEmailId() == null || signup.getName() == null || signup.getPassword() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        //Make sure the server knows that there is a new user
        Signup newSignup = signupService.newUser(signup);

        //return the new user with a 201 created status
        //return ResponseEntity.status((HttpStatus.CREATED).body(newSignup));
        return null;
    }
}
