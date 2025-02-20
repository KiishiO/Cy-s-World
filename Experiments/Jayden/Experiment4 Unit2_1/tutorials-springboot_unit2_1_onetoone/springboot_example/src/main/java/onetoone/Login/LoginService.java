package onetoone.Login;

import onetoone.Persons.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoginService {

    private final LoginRepository loginRepository;

    @Autowired
    public LoginService(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    /**
     * Registers a new user and ensures Person association.
     */
    public Login registerUser(Login login) {
        if (login.getPerson() != null) {
            // Ensure bidirectional mapping if necessary
            login.getPerson().setLogin(login);
        }
        return loginRepository.save(login);
    }

//    public List<Login> getIfActive(Boolean ifactive){
//        return loginRepository.findByIfActive(ifactive);
//    }
    /**
     * Gets a user by ID, returns Optional to handle cases where the user doesn't exist.
     */
    public Optional<Login> getUserById(Long id) {
        return loginRepository.findById(id);
    }

    /**
     * Gets a user by email, useful for login operations.
     */
    public Optional<Login> getUserByEmail(String email) {
        return Optional.ofNullable(loginRepository.findByEmailId(email));
    }

    /**
     * Updates an existing Login and optionally updates the associated Person.
     */
    public Optional<Login> updateUser(Long id, Login updatedLogin) {
        return loginRepository.findById(id)
                .map(existingLogin -> {
                    existingLogin.setName(updatedLogin.getName());
                    existingLogin.setEmailId(updatedLogin.getEmailId());
                    existingLogin.setIfActive(updatedLogin.getIfActive());

                    // Handle updating the associated Person
                    if (updatedLogin.getPerson() != null) {
                        Person updatedPerson = updatedLogin.getPerson();
                        if (existingLogin.getPerson() != null) {
                            existingLogin.getPerson().setName(updatedPerson.getName());
                            existingLogin.getPerson().setPhoneNumber(updatedPerson.getPhoneNumber());
                        } else {
                            existingLogin.setPerson(updatedPerson);
                        }
                    }

                    return loginRepository.save(existingLogin);
                });
    }

    /**
     * Deletes a user by ID and ensures person association is removed.
     */
    public boolean deleteUserById(Long id) {
        Optional<Login> login = loginRepository.findById(id);
        if (login.isPresent()) {
            loginRepository.deleteById(id);
            return true;
        }
        return false;
    }
}