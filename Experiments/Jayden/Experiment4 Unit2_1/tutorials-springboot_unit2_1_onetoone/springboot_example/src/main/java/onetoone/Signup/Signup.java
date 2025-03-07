package onetoone.Signup;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import onetoone.Login.Login;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;

/**
 *
 * @author Vivek Bengre
 */
@Entity
public class Signup {

    /*
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String email;
    private String password;
    private String firstAndLastName;

    /*
     * @OneToOne creates a relation between the current entity/table(Laptop) with the entity/table defined below it(Person)
     * @JsonIgnore is to assure that there is no infinite loop while returning either Person/laptop objects (laptop->Person->laptop->...)
     */
    @OneToOne(mappedBy = "signup")
   // @JsonManagedReference
    @JsonIgnore
    private Person person;


    @OneToOne(mappedBy = "signup")
    //@JsonManagedReference // Prevents infinite recursion in Signup
    @JsonIgnore
    private Login login;

    public Signup(String firstAndLastName, String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstAndLastName = firstAndLastName;
        //this.person = person;
        //createNewPerson

    }

    public Signup() {
    }

    // =============================== Getters and Setters for each field ================================== //


    public String getFirstAndLastName() {
        return firstAndLastName;
    }

    public void setFirstAndLastName(String firstAndLastName) {
        this.firstAndLastName = firstAndLastName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Login getLogin() {
        return login;
    }
    public void setLogin(Login login) {
        this.login = login;
    }
}
