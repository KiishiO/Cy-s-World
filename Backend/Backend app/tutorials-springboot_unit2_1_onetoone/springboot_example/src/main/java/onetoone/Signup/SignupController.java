package onetoone.Signup;

import java.util.List;

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
 * @author Sonia Patil
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

    //@GetMapping(path = "/signup")
    List<Signup> getAllSignups(){
        return signupRepository.findAll();
    }

    //@GetMapping(path = "/signup/{id}")
    Signup getSignupById(@PathVariable int id){
        return signupRepository.findById(id);
    }

    @PostMapping("/signup")
    String createSignup(@RequestBody Signup signup){
        if (signup == null || signup.getUsername() == null || signup.getEmail() == null)
            return failure;

        Person newPerson = new Person(signup.getFirstAndLastName(), signup.getEmail());
        newPerson.setSignupInfo(signup);
        personRepository.save(newPerson);
        signupRepository.save(signup);

        return success;
    }

    @PutMapping("/signup/{id}")
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

    @DeleteMapping("/signup/{id}")
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
