package onetoone.DiningHallOrderingSystem;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onetoone.DiningHall.MenuItems;

/**
 * Sonia Patil
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiningOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int quantity;

    @ManyToOne
    @JoinColumn
    private MenuItems menuItems;

    @ManyToOne
    @JoinColumn(name = "diningOrder_id")
    @JsonBackReference
    private DiningOrder diningOrder;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public MenuItems getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(MenuItems menuItems) {
        this.menuItems = menuItems;
    }

    public DiningOrder getDiningOrder() {
        return diningOrder;
    }

    public void setDiningOrder(DiningOrder diningOrder) {
        this.diningOrder = diningOrder;
    }
}
