package onetoone.Persons;

import onetoone.Login.LoginRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

public interface PersonRepository extends JpaRepository<Person, Long> {
    
    Person findById(int id);

    void deleteById(int id);

    Person findBySignup_Id(int id);

    Person findByLaptop_Id(int id);
}
