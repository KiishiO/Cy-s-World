package onetoone.Signup;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Column(unique = true, nullable = false)
    private String emailId;

    @Column(nullable = false)
    private boolean ifActive;

    @OneToOne(cascade = CascadeType.All)
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    @JsonManagedReference
    private Person person; //Storing related person details

    public Signup() {
        //do I need to add a boolean for this part we'll see
    }

    public Signup(String name, String emailId, Person person) {
        this.name = name;
        this.emailId = emailId;
        this.person = person;
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
}
