package onetoone.Profiles;

import jakarta.persistence.*;

@Entity
@Table(name = "profiles")
public class Profiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String userClasses;
    private char gradesFromClasses;
    private String emailId;

    public Profiles(){

    }

    public Profiles(Long id, String name, String userClasses, char gradesFromClasses){
        this.id = id;
        this.name = name;
        this.userClasses = userClasses;
        this.gradesFromClasses = gradesFromClasses;

    }

    // =============================== Getters and Setters for each field ================================== //

    public long getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }









}
