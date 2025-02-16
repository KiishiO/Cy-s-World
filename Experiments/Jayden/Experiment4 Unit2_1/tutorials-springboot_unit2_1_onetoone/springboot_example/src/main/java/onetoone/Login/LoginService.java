package onetoone.Login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private LoginRepository loginRepository;

    public Login registerUser(Login login) {
        return loginRepository.save(login);
    }

    public Optional<Login> getUserById(int id){
        return Optional.ofNullable(loginRepository.findById(id));
    }

    public Optional<Login> deleteUserById(int id){
        return Optional.ofNullable(loginRepository.deleteById(id));
    }

    public Optional<Login> getUserByEmail(String email) {
        return Optional.ofNullable(loginRepository.findByEmailId(email));
    }
}