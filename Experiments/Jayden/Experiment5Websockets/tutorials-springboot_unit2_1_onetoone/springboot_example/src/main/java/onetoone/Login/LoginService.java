package onetoone.Login;

import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Service
public class LoginService {

    private final LoginRepository loginRepository;
    private final PersonRepository personRepository;

    @Autowired
    public LoginService(LoginRepository loginRepository, PersonRepository personRepository) {
        this.loginRepository = loginRepository;
        this.personRepository = personRepository;
    }

    public Login registerPassword(Login login, String password) {
        if (login.getPassword() != null) {
            login.setPassword(password);
            return loginRepository.save(login);
        }
        return null; // Or throw an exception if needed
    }



    /**
     * Registers a new user and ensures Person association.
     */
    public Login registerUser(Login login) {
        if (login.getPerson() != null && login.getPerson().getId() == 0) {
            // If Person is new, save the Person first
            personRepository.save(login.getPerson());
        }

        // Ensure bidirectional mapping if necessary
        if (login.getPerson() != null) {
            login.getPerson().setLogin(login); // Link Login to the Person
        }

        // Save the Login entity
        return loginRepository.save(login);
    }

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

//    public Optional<Login> findById(Long id) {
//        return loginRepository.findById(id); // Fetches login object by ID from the database
//    }

//    public Login registerPassword(Login login, String password) {
//        login.setPassword(password); // Update password field
//        return loginRepository.save(login); // Save and return updated object
//    }
}