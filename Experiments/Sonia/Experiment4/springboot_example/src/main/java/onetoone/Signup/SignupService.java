package onetoone.Signup;

import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Service
public class SignupService {

    private final SignupRepository signupRepository;
    private final PersonRepository personRepository;

    @Autowired
    public SignupService(SignupRepository signupRepository, PersonRepository personRepository) {
        this.signupRepository = signupRepository;
        this.personRepository = personRepository;
    }

    /*
    This is for the field that checks if the password is the same while signing up.
     */
    public Signup checkPassword(Signup signup, String password) {
        if(signup.getPassword() != null) {
            if(signup.getPassword() == password) {
                return signupRepository.save(signup);
            }
        }
        return null; //for now
    }

    public Signup newUser(Signup signup) {
        //need to create another check that checks if the user doesn't already exist
        if(signup.getPerson() != null && signup.getPerson().getId() == 0) {
            //creates a new user if the person doesn't already exist and puts them in the repository
            personRepository.save(signup.getPerson());
        }

        if(signup.getPerson() != null) {
            //signup.getPerson().setSignup(signup);
        }
        return signupRepository.save(signup);
    }

}
