package onetoone.Persons;

import onetoone.Login.LoginRepository;
import onetoone.UserRoles.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    Person findById(int id);

    void deleteById(int id);

    Person findBySignup_Id(int id);

    Person findByLaptop_Id(int id);

    List<Person> findByRole(UserRoles role);

    int id(int id);
}
