package onetoone.Persons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


import onetoone.DiningHallOrderingSystem.DiningOrder;

import onetoone.BookstoreOrderingSystem.Order;

import onetoone.Laptops.Laptop;
import onetoone.Login.Login;
import onetoone.Signup.Signup;
import onetoone.StudentClasses.StudentClasses;
import onetoone.TestingCenter.ExamInfo;
import onetoone.UserRoles.UserRoles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Sonia Patil
 * @author Jayden Sorter
 * 
 */
@Entity
@Table(name = "Person")
public class Person {

     /* 
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;
    private String name;
    private String phoneNumber;
    private boolean ifActive;
//    private String roles;

    @Enumerated(EnumType.STRING)
    private UserRoles role;

    /*
     * @OneToOne creates a relation between the current entity/table(Laptop) with the entity/table defined below it(Person)
     * cascade is responsible propagating all changes, even to children of the class Eg: changes made to laptop within a Person object will be reflected
     * in the database (more info : https://www.baeldung.com/jpa-cascade-types)
     * @JoinColumn defines the ownership of the foreign key i.e. the Person table will have a field called laptop_id
     */
    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "laptop_id")
    @JsonIgnore
    private Laptop laptop;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @ManyToOne(cascade = CascadeType.ALL)
    @OneToOne(mappedBy = "person")
    @JsonIgnore
    private Login login; // Associating `Person` with `Login`

    @OneToOne
    @JoinColumn
    private Signup signup;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "person_friends",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private List<Person> friends = new ArrayList<>();

    // For teachers: classes they teach
    @OneToMany(mappedBy = "teacher")
    @JsonIgnore
    private Set<StudentClasses> classesTeaching = new HashSet<>();

    // For students: classes they are enrolled in
    @ManyToMany
    @JoinTable(
            name = "student_classes",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    @JsonIgnore
    private Set<StudentClasses> enrolledClasses = new HashSet<>();

    @ManyToMany(mappedBy = "persons")  // This is the inverse side of the relationship
    private List<ExamInfo> examInfos = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
    private List<DiningOrder> diningOrders = new ArrayList<>();

    // =============================== Constructors ================================== //

    public Person() {
        this.ifActive = true;
        this.role = UserRoles.STUDENT;
    }

    public Person(String name, String phoneNumber, UserRoles role) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.ifActive = true;
        this.role = role;
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

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public boolean getIsActive(){
        return ifActive;
    }

    public void setIfActive(boolean ifActive){
        this.ifActive = ifActive;
    }

//    public Laptop getLaptop(){
//        return laptop;
//    }
//
//    public void setLaptop(Laptop laptop){
//        this.laptop = laptop;
//    }
    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public Signup getSignupInfo(){
        return signup;
    }

    public void setSignupInfo(Signup signup){
        this.signup = signup;
    }


//    public String getRoles(){
//        return roles;
//    }
//    public void setRoles(String roles){
//        this.roles = roles;
//    }

    public UserRoles getRole(){
        return role;
    }

    public void setRole(UserRoles role) {
        this.role = role;
    }

    public List<Person> getFriends() {
        return friends;
    }

    public void setFriends(List<Person> friends) {
        this.friends = friends;
    }

    public Set<StudentClasses> getClassesTeaching() {
        return classesTeaching;
    }

    public void setClassesTeaching(Set<StudentClasses> classesTeaching) {
        this.classesTeaching = classesTeaching;
    }

    public Set<StudentClasses> getEnrolledClasses() {
        return enrolledClasses;
    }

    public void setEnrolledClasses(Set<StudentClasses> enrolledClasses) {
        this.enrolledClasses = enrolledClasses;
    }

    @JsonIgnore
    // Helper methods to determine user type
    public boolean isAdmin() {
        return this.role == UserRoles.ADMIN;
    }
    @JsonIgnore
    public boolean isTeacher() {
        return this.role == UserRoles.TEACHER;
    }
    @JsonIgnore
    public boolean isStudent() {
        return this.role == UserRoles.STUDENT;
    }

}
