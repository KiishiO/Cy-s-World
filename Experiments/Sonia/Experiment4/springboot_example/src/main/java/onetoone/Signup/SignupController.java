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






}
