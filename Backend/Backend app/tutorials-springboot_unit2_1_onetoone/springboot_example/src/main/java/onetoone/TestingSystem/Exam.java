package onetoone.TestingSystem;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import onetoone.Persons.Person;
import onetoone.TestingCenter.TestingCenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String subject;

    @ManyToMany(mappedBy = "exams")
    @JsonBackReference(value = "person-exams")
    private Set<Person> people = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "exam_testing_system",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "testing_system_id")
    )
    @JsonManagedReference(value = "exam-systems")
    private Set<TestingSystem> testingSystem = new HashSet<>();

    public Exam() {
    }

    public Exam(String subject) {
        this.subject = subject;
    }
    // Getters and Setters


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Person> getPeople() {
        return people;
    }

    public void setPeople(Set<Person> people) {
        this.people = people;
    }

    public Set<TestingSystem> getTestingSystem() {
        return testingSystem;
    }

    public void setTestingSystem(Set<TestingSystem> testingSystem) {
        this.testingSystem = testingSystem;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}

