package onetoone.DiningHall;
import java.util.List;


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
    private double price;

    @ManyToOne
    @JoinColumn(name = "dininghall_id")
    private DiningHall diningHall;

    public MenuItems() {

    }

    public MenuItems(String name, double price, DiningHall diningHall) {
        this.name = name;
        this.price = price;
        this.diningHall = diningHall;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public DiningHall getDiningHall() {
        return diningHall;
    }

    public void setDiningHall(DiningHall diningHall) {
        this.diningHall = diningHall;
    }
}
