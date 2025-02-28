package onetoone.Persons;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Vivek Bengre
 *
 */

public interface PersonRepository extends JpaRepository<Person, Long> {

    Person findById(int id);

    void deleteById(int id);

    Person findBySignup_Id(int id);
}
