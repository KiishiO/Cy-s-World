package onetoone.DiningHall;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Sonia Patil
 */
@Repository
public interface MenuItemsRepository extends JpaRepository<MenuItems, Integer> {
}
