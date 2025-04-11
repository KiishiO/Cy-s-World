package onetoone.TestingCenter;

import jakarta.persistence.*;
import onetoone.Persons.Person;
import onetoone.TestingCenter.TestingCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sonia Patil
 */
@Entity
public class ExamInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String examName;
    private String examDescription;

    @ManyToOne
    @JoinColumn(name = "testingCenter_id")
    private TestingCenter testingCenter;

//    @ManyToMany
//    @JoinTable(
//            name = "person_exams",
//            joinColumns = @JoinColumn(name = "exam_id"),
//            inverseJoinColumns = @JoinColumn(name = "person_id")
//    )
//    private List<Person> persons = new ArrayList<>();

    public ExamInfo() {}

    //constructor
    public ExamInfo(String examName, String examDescription, TestingCenter testingCenter) {
        this.examName = examName;
        this.examDescription = examDescription;
        this.testingCenter = testingCenter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
    }

    public TestingCenter getTestingCenter() {
        return testingCenter;
    }

    public void setTestingCenter(TestingCenter testingCenter) {
        this.testingCenter = testingCenter;
    }
}
