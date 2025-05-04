package onetoone.DiningHallOrderingSystem;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onetoone.Persons.Person;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sonia Patil
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diningOrders")
public class DiningOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private LocalDateTime orderDate;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @OneToMany(mappedBy = "diningOrder", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DiningOrderItem> items = new ArrayList<>();
}
