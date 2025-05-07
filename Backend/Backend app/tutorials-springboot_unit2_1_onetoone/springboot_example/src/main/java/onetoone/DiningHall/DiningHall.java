package onetoone.DiningHall;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sonia Patil
 */
@Entity
public class DiningHall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String location;

    //list to hold the menu items at a dining hall
    @OneToMany(mappedBy = "diningHall", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<MenuItems> menuItems;

    public DiningHall(String name, String location) {
        this.name = name;
        this.location = location;
        menuItems = new ArrayList<>();
    }

    public DiningHall() {

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<MenuItems> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItems> menuItems) {
        this.menuItems = menuItems;
    }
}
