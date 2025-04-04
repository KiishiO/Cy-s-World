package onetoone.Profiles;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import onetoone.Login.Login;
import onetoone.Persons.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "profiles")
public class Profiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(name = "profile_classes", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "class_name")
    private List<String> userClasses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_grades", joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "class_name")
    @Column(name = "grade")
    private Map<String, String> gradesFromClasses = new HashMap<>();


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    @JsonManagedReference
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER)
    @OneToOne(cascade = CascadeType.ALL)
    @JsonBackReference
    private Login login;

    public Profiles(){

    }

    public Profiles(Long id, String name, List<String> userClasses, Map<String, String> gradesFromClasses, Person person) {
        this.id = id;
        this.name = name;
        this.userClasses = userClasses;
        this.gradesFromClasses = gradesFromClasses;
        this.person = person;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUserClasses() {
        return userClasses;
    }

    public void setUserClasses(List<String> userClasses) {
        this.userClasses = userClasses;
    }

    public Map<String, String> getGradesFromClasses() {
        return gradesFromClasses;
    }

    public void setGradesFromClasses(Map<String, String> gradesFromClasses) {
        this.gradesFromClasses = gradesFromClasses;
    }
}