package onetoone.DiningHallOrderingSystem;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import onetoone.DiningHall.MenuItems;

import onetoone.DiningHall.MenuItemsRepository;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sonia Patil
 */
@Service
public class DiningOrderService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DiningOrderRepository diningOrderRepository;

    @Autowired
    private MenuItemsRepository menuItemsRepository;


    public DiningOrder createDiningOrder(DiningOrder diningOrder) {
        int personId = diningOrder.getPerson().getId();
        Person person = personRepository.findById(personId);

        diningOrder.setPerson(person);
        diningOrder.setOrderDate(LocalDateTime.now());

        for (DiningOrderItem item : diningOrder.getItems()) {
            int menuItemId = item.getMenuItems().getId();
            MenuItems menuItem = menuItemsRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu Item not found"));
            item.setMenuItems(menuItem);
            item.setDiningOrder(diningOrder);
        }
        diningOrder.setItems(diningOrder.getItems());
        return diningOrderRepository.save(diningOrder);
    }

    public List<DiningOrder> getDiningOrders() {
        return diningOrderRepository.findAll();
    }
}
