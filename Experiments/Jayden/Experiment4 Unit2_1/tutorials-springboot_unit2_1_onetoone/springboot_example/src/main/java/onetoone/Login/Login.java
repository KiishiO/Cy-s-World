package onetoone.Login;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import onetoone.Signup.Signup;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "login")
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String password;

    @Column(unique = true, nullable = false)
    private String emailId;

    @Column(nullable = false)
    private boolean ifActive;

    @ManyToOne //Allows to GET dummy data
//    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    @JsonManagedReference
    private Person person;  // Storing related person details

//    @ManyToOne(fetch = FetchType.EAGER) //Allows to GET dummy data
@ManyToOne
@JoinColumn(name = "signup_id")
@JsonBackReference // Prevents infinite recursion in Login
private Signup signup;

    // =============================== Constructors ================================== //

    public Login() {
        this.ifActive = true;  // Default active state
    }

    public Login(String name, String emailId, String password, Person person) {
        this.name = name;
        this.emailId = emailId;
        this.password = password;
        this.ifActive = true;
        this.person = person;

    }

    // =============================== Getters and Setters ================================== //

    public Long getId() {
        return id;
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

    public boolean getIfActive() {
        return ifActive;
    }

    public void setIfActive(boolean ifActive) {
        this.ifActive = ifActive;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
