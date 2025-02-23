package onetoone.Persons;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import onetoone.Laptops.Laptop;
import onetoone.Signup.Signup;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

@Entity
public class Person {

     /* 
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String emailId;
    private boolean newSignup;

    /*
     * @OneToOne creates a relation between the current entity/table(Laptop) with the entity/table defined below it(Person)
     * cascade is responsible propagating all changes, even to children of the class Eg: changes made to laptop within a Person object will be reflected
     * in the database (more info : https://www.baeldung.com/jpa-cascade-types)
     * @JoinColumn defines the ownership of the foreign key i.e. the Person table will have a field called laptop_id
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "laptop_id")
    private Laptop laptop;

    /*
    Associates all the Persons with when they Signup
     */
    @ManyToOne(fetch = FetchType.EAGER)
    //@ManyToOne(cascade = CascadeType.ALL)
    //@OneToOne(cascade = CascadeType.ALL)
    @JsonBackReference
    private Signup signup;

    public Person(String name, String emailId) {
        this.name = name;
        this.emailId = emailId;
        this.newSignup = true;
    }

    public Person() {
        this.newSignup = true;
    }

    // =============================== Getters and Setters for each field ================================== //

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmailId(){
        return emailId;
    }

    public void setEmailId(String emailId){
        this.emailId = emailId;
    }

    public Laptop getLaptop(){
        return laptop;
    }

    public void setLaptop(Laptop laptop){
        this.laptop = laptop;
    }

    public boolean isNewSignup() {
        return newSignup;
    }

    public void setNewSignup(boolean newSignup) {
        this.newSignup = newSignup;
    }

    public Signup getSignup() {
        return signup;
    }

    public void setSignup(Signup signup) {
        this.signup = signup;
    }
}
