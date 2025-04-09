package onetoone.Bookstore;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sonia Patil
 */
@Entity
public class Bookstore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private double location;

    //list to hold the products that are available at the bookstore
    @OneToMany(mappedBy = "bookstore", cascade = CascadeType.ALL)
    private List<Products> products;

    public Bookstore() {}

    public Bookstore(String name, double location) {
        this.name = name;
        this.location = location;
        products = new ArrayList<>();
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

    public double getLocation() {
        return location;
    }

    public void setLocation(double location) {
        this.location = location;
    }

    public List<Products> getProducts() {
        return products;
    }

    public void setProducts(List<Products> products) {
        this.products = products;
    }
}

