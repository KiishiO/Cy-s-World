package onetoone.Profiles;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import onetoone.Login.Login;
import onetoone.Persons.Person;

@Entity
@Table(name = "profiles")
public class Profiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String userClasses;
    private char gradesFromClasses;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    @JsonManagedReference
    private Person person;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonBackReference
    private Login login;

    public Profiles(){

    }

    public Profiles(Long id, String name, String userClasses, char gradesFromClasses, Person person){
        this.id = id;
        this.name = name;
        this.userClasses = userClasses;
        this.gradesFromClasses = gradesFromClasses;
        this.person = person;

    }

    // =============================== Getters and Setters for each field ================================== //

    public long getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }
    public Person getPerson(){
        return person;
    }

    public void setPerson(Person person){
        this.person = person;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getUserClasses(){
        return userClasses;
    }

    public void setUserClasses(String userClasses){
        this.userClasses = userClasses;
    }

    public char getGradesFromClasses(){
        return gradesFromClasses;
    }

    public void setGradesFromClasses(char gradesFromClasses){
        this.gradesFromClasses = gradesFromClasses;

    }


}
