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
            Signup signup1 = new Signup( "Sonia Patil",  "john@somemail.com", "123456789");
            Signup signup2 = new Signup( "Sonia Patil",  "john@somemail.com", "123456789");
            Signup signup3 = new Signup( "Sonia Patil",  "john@somemail.com", "123456789");
            Person1.setSignupInfo(signup1);
            Person2.setSignupInfo(signup2);
            Person3.setSignupInfo(signup3);
//            personRepository.save(Person1);
//            personRepository.save(Person2);
//            personRepository.save(Person3);

        };
    }

}
