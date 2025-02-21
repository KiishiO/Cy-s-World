package onetoone.Signup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;

/**
 * @author Sonia Patil
 */

@RestController
@RequestMapping("/Signup") // Base URL for all endpoints
public class SignupController {

    @Autowired
    SignupRepository signupRepository;

    @Autowired
    private



}
