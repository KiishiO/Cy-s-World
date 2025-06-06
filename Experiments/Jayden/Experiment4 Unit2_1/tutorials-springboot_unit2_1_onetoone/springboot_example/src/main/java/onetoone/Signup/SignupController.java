package onetoone.Signup;

import java.util.List;

import onetoone.Login.Login;
import onetoone.Persons.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import onetoone.Persons.Person;
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
    String createSignup(@RequestBody Signup signup){
        if (signup == null || signup.getUsername() == null || signup.getEmail() == null || signup.getRoles() == null)
            return failure;


        Person newPerson = new Person(signup.getFirstAndLastName(), signup.getEmail(), signup.getRoles());
        signupRepository.save(signup);
        newPerson.setSignupInfo(signup);
        personRepository.save(newPerson);

        return success;
    }

    @PutMapping("/{id}")
    Signup updateSignupInfo(@PathVariable int id, @RequestBody Signup request){
        Signup currentSignup = signupRepository.findById(id);
        if(currentSignup == null)
            return null;

        //check if the information that is being updated can be updated
        if(request.getUsername() != null) {
            currentSignup.setUsername(request.getUsername());
        }
        if(request.getEmail() != null) {
            currentSignup.setEmail(request.getEmail());
        }
        //ensures that the user does not save the same password when they are changing it for security purposes
        if(request.getPassword() != null && !request.getPassword().equals(currentSignup.getPassword())) {
            currentSignup.setPassword(request.getPassword());
        }
        signupRepository.save(currentSignup);
        return signupRepository.findById(id);
    }

    @DeleteMapping("/{id}")
    String deleteSignupInfo(@PathVariable int id){

        // Check if there is an object depending on Person and then remove the dependency
        Person person = personRepository.findBySignup_Id(id);
        person.setSignupInfo(null);
        //personRepository.save(person);
        personRepository.deleteById(id);

        // delete the laptop if the changes have not been reflected by the above statement
        signupRepository.deleteById(id);
        return success;
    }
}
