package onetoone.Signup;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.*;
import onetoone.Persons.Person;


@Entity
@Table(name = "signup")
public class Signup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String password;

    @Column(unique = true, nullable = false)
    private String emailId;

    @Column(nullable = false)
    private boolean newSignup;

    @ManyToOne(fetch = FetchType.EAGER) //Allows to GET dummy data
    //@OneToOne(cascade = CascadeType.All)
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    @JsonManagedReference
    private Person person; //Storing related person details

    public Signup() {
        this.newSignup = true;
    }

    public Signup(String name, String emailId, String password, Person person) {
        this.name = name;
        this.emailId = emailId;
        this.person = person;
        this.password = password;
        this.newSignup = true;
    }

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isNewSignup() {
        return newSignup;
    }

    public void setNewSignup(boolean newSignup) {
        this.newSignup = newSignup;
    }
}
