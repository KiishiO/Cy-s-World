package onetoone.DiningHall;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * @author Sonia Patil
 */
@Entity
public class MenuItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;
    private String menuType;

    @ManyToOne
    @JoinColumn(name = "dininghall_id")
    @JsonIgnore
    private DiningHall diningHall;

    public MenuItems() {

    }

    public MenuItems(String name, String description, String menuType) {
        this.name = name;
        this.description = description;
        //this.diningHall = diningHall;
        this.menuType = menuType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DiningHall getDiningHall() {
        return diningHall;
    }

    public void setDiningHall(DiningHall diningHall) {
        this.diningHall = diningHall;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }
}
