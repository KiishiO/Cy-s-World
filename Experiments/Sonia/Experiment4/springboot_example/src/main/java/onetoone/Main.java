package onetoone;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import onetoone.Signup.Signup;
import onetoone.Signup.SignupRepository;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

@SpringBootApplication
class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    // Create 3 Persons with their machines
    /**
     * 
     * @param personRepository repository for the Person entity
     * @param signupRepository repository for the Laptop entity
     * Creates a commandLine runner to enter dummy data into the database
     * As mentioned in Person.java just associating the Laptop object with the Person will save it into the database because of the CascadeType
     */
    @Bean
    CommandLineRunner initPerson(PersonRepository personRepository, SignupRepository signupRepository) {
        return args -> {
            Person Person1 = new Person("John", "john@somemail.com");
            Person Person2 = new Person("Jane", "jane@somemail.com");
            Person Person3 = new Person("Justin", "justin@somemail.com");
            Signup laptop1 = new Signup( "test", "john@somemail.com", "123456789", Person1);
            Signup laptop2 = new Signup( "test", "john@somemail.com", "123456789", Person2);
            Signup laptop3 = new Signup( "test", "john@somemail.com", "123456789", Person3);
            Person1.setSignupInfo(laptop1);
            Person2.setSignupInfo(laptop2);
            Person3.setSignupInfo(laptop3);
            personRepository.save(Person1);
            personRepository.save(Person2);
            personRepository.save(Person3);

        };
    }

}
