package onetoone.Signup;

import onetoone.Persons.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SignupService {

    private final SignupRepository signupRepository;

    @Autowired
    public SignupRepository(SignupRepository signupRepository){
        this.signupRepository = signupRepository;
    }


}
