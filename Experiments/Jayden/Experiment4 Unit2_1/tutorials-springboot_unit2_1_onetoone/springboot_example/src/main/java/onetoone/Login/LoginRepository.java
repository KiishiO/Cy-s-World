package onetoone.Login;

import onetoone.Persons.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface LoginRepository extends JpaRepository<Login, Long> {

    Login findById(int id);

    Login deleteById(int id);

//    Login getEmailId(String email);

//    Login findPerson(Person person);

    Login findByEmailId(String emailId);

    Login findByPassword(String password);
//    List<Login> findByIfActive(Boolean ifActive);}
}