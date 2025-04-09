package onetoone.StudentClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import onetoone.Persons.Person;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "classes")
public class StudentClasses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String className;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Person teacher;



    @ElementCollection
    @CollectionTable(name = "class_schedule", joinColumns = @JoinColumn(name = "class_id"))
    private Set<ClassSchedule> schedules = new HashSet<>();

    private String location;

    @ManyToMany
    @JoinTable(
            name = "student_class_enrollment",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonIgnore
    private Set<Person> students = new HashSet<>();

    // Default constructor
    public StudentClasses() {
    }

    // Constructor with fields
    public StudentClasses(String className, Person teacher, String location) {
        this.className = className;
        this.teacher = teacher;
        this.location = location;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Person getTeacher() {
        return teacher;
    }

    public void setTeacher(Person teacher) {
        this.teacher = teacher;
    }

    public Set<ClassSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(Set<ClassSchedule> schedules) {
        this.schedules = schedules;
    }

    public void addSchedule(ClassSchedule schedule) {
        this.schedules.add(schedule);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Set<Person> getStudents() {
        return students;
    }

    public void setStudents(Set<Person> students) {
        this.students = students;
    }

    public void addStudent(Person student) {
        this.students.add(student);
    }

    public void removeStudent(Person student) {
        this.students.remove(student);
    }
}

@Embeddable
class ClassSchedule {
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    // Default constructor
    public ClassSchedule() {
    }

    // Constructor with fields
    public ClassSchedule(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}