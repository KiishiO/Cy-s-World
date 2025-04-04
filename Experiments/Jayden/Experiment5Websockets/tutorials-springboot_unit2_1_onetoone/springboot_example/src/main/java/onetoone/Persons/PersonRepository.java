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

<<<<<<<< HEAD:Experiments/Jayden/Experiment5Websockets/tutorials-springboot_unit2_1_onetoone/springboot_example/src/main/java/onetoone/Persons/PersonRepository.java
    Person findBySignup_Id(int id);

========
>>>>>>>> origin:Experiments/Sonia/Experiment6/springboot_example/src/main/java/onetoone/Persons/PersonRepository.java
    Person findByLaptop_Id(int id);
}
