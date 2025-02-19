package onetoone.Login;

import onetoone.Persons.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


public interface LoginRepository extends JpaRepository<Login, Long> {

    Login findById(int id);

    Login deleteById(int id);

    Login findByEmailId(String emailId);


}
