package onetoone.StudyTable;
//import lombok.Builder;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.*;
//import lombok.*;

import onetoone.Persons.Person;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name= "StudyTable")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class StudyTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonBackReference(value = "creator-reference")
    private Person person;

    @ManyToMany
    @JoinTable(
            name = "study_table_participants",
            joinColumns = @JoinColumn(name = "study_table_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private List<Person> friend = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
//public StudyTable(Long id, Person person, Person friend){
//        this.id = id;
//        this.person = person;
//        this.friend = friend;
//}

                          //GETTERS & SETTERS\\
    public Person getPerson(){
        return person;
    }

    public void setPerson(Person person){
        this.person = person;
    }

    public List<Person> getFriend(){
        return friend;
    }

    public void setFriend(List<Person> friend){
        this.friend = friend;
    }

    public Status getStatus(){
        return status;
    }

    public void setStatus(Status status){
        this.status = status;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }


}
