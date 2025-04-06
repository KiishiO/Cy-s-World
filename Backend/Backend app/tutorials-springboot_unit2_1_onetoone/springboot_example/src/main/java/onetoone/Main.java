package onetoone;

import jakarta.transaction.Transactional;
import onetoone.Login.Login;
import onetoone.Login.LoginRepository;
import onetoone.Signup.Signup;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//import onetoone.Laptops.Laptop;
//import onetoone.Laptops.LaptopRepository;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;

/**
 * 
 * @author Sonia Patil
 * 
 */ 

@SpringBootApplication
class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    // Create 3 Persons with their machines

//    /**
//     * @param PersonRepository repository for the Person entity
//     * @param laptopRepository repository for the Laptop entity
//     *                         Creates a commandLine runner to enter dummy data into the database
//     *                         As mentioned in Person.java just associating the Laptop object with the Person will save it into the database because of the CascadeType
//     */
//    @Bean
//    CommandLineRunner initPerson(PersonRepository PersonRepository, LaptopRepository laptopRepository) {
//        return args -> {
//            Person Person1 = new Person("John", "john@somemail.com");
//            Person Person2 = new Person("Jane", "jane@somemail.com");
//            Person Person3 = new Person("Justin", "justin@somemail.com");
//            Laptop laptop1 = new Laptop(2.5, 4, 8, "Lenovo", 300);
//            Laptop laptop2 = new Laptop(4.1, 8, 16, "Hp", 800);
//            Laptop laptop3 = new Laptop(3.5, 32, 32, "Dell", 2300);
//            Person1.setLaptop(laptop1);
//            Person2.setLaptop(laptop2);
//            Person3.setLaptop(laptop3);
//            PersonRepository.save(Person1);
//            PersonRepository.save(Person2);
//            PersonRepository.save(Person3);
//
//        };
//    }
//}
    //---------------------------------------------------------------------------//
    /**
     * Populates the H2 database with sample users for login functionality.
     *
     * @param loginRepository Repository for storing login details.
     * @param personRepository Repository for storing personal details.
     * @return CommandLineRunner to insert sample data.
     */


    @Bean
    @Transactional // Ensures entity relationships are properly persisted
    CommandLineRunner initData(LoginRepository loginRepository, PersonRepository personRepository) {
        return args -> {
            // Creating Person entities
            Person person1 = new Person("Michael Johnson", "515-789-9852");
            Person person2 = new Person("Sarah Adams", "515-888-3579");
            Person person3 = new Person("David Williams", "515-777-0707");

            // Saving Persons first (ensuring they exist before login is created)
            personRepository.save(person1);
            personRepository.save(person2);
            personRepository.save(person3);

            // Force database commit before proceeding
//            personRepository.flush();

            // Creating associated Login entities
            Login login1 = new Login("mjohnson", "mjohnson123@example.com", "MjOhNsOn", person1);
            Login login2 = new Login("sarah_a", "sarah123@example.com","A_HARAS", person2);
            Login login3 = new Login("dwilliams", "davidw@example.com","Davidw545", person3);

            Signup signup3 = new Signup( "Sonia Patil",  "john@somemail.com", "123456789", "password");

            // Saving Login details
            loginRepository.save(login1);
            loginRepository.save(login2);
            loginRepository.save(login3);

            // Logging to console (optional, for verification)
            System.out.println("Sample login and person data inserted into the database.");
        };
    }
}