package onetoone.DiningHall;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Sonia Patil
 */
@Repository
public interface DiningHallRepository extends JpaRepository<DiningHall, Integer> {

    // Find dining halls by name
    List<DiningHall> findByNameContainingIgnoreCase(String name);

    // Find dining halls by location
    List<DiningHall> findByLocationContainingIgnoreCase(String location);

    // Find dining hall that has a specific menu item (by menu item name)
    List<DiningHall> findByMenuItemsNameContainingIgnoreCase(String menuItemName);

    // Find dining halls with menu items below a certain price
    //List<DiningHall> findDistinctByMenuItemsPriceLessThanEqual(double maxPrice);
}
