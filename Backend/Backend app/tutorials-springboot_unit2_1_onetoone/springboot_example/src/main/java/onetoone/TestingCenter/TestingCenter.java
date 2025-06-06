package onetoone.TestingCenter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Sonia Patil
 */
@Entity
public class TestingCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String centerName;
    private String location;
    private String centerDescription;

//    //list to hold the exams at a testing center
//    @OneToMany(mappedBy = "testingCenter", cascade = CascadeType.ALL)
//    private List<ExamInfo> examInfo;

    // Many-to-many relationship with ExamInfo
    @ManyToMany(mappedBy = "testingCenters")
    @JsonBackReference
    private List<ExamInfo> examInfo2 = new ArrayList<>();


    public TestingCenter() {}

    //constructor
    public TestingCenter(String centerName, String location, String centerDescription) {
        this.centerName = centerName;
        this.location = location;
        this.centerDescription = centerDescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCenterDescription() {
        return centerDescription;
    }

    public void setCenterDescription(String centerDescription) {
        this.centerDescription = centerDescription;
    }


    public List<ExamInfo> getExamInfo2() {
        return examInfo2;
    }

    public void setExamInfo2(List<ExamInfo> examInfo2) {
        this.examInfo2 = examInfo2;
    }
}
