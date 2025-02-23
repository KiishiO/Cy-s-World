package onetoone.Signup;

import java.util.List;

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
public class SignupController {

    @Autowired
    SignupRepository signupRepository;

    @Autowired
    PersonRepository personRepository;
    
    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = "/signup")
    List<Signup> getAllSignups(){
        return signupRepository.findAll();
    }

    @GetMapping(path = "/signup/{id}")
    Signup getSignupById(@PathVariable int id){
        return signupRepository.findById(id);
    }

    @PostMapping(path = "/signups")
    String createSignup(@RequestBody Signup signup){
        if (signup == null)
            return failure;
        signupRepository.save(signup);
        return success;
    }

    @PutMapping(path = "/signup/{id}")
    Signup updateSignupInfo(@PathVariable int id, @RequestBody Signup request){
        Signup signup = signupRepository.findById(id);
        if(signup == null)
            return null;
        signupRepository.save(request);
        return signupRepository.findById(id);
    }

    @DeleteMapping(path = "/signup/{id}")
    String deleteSignupInfo(@PathVariable int id){

        // Check if there is an object depending on Person and then remove the dependency
        Person person = personRepository.findBySignup_Id(id);
        person.setSignupInfo(null);
        personRepository.save(person);

        // delete the laptop if the changes have not been reflected by the above statement
        signupRepository.deleteById(id);
        return success;
    }
}
