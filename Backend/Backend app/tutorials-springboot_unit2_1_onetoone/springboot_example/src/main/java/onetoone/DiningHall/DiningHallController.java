package onetoone.DiningHall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Sonia Patil
 */
@RestController
@RequestMapping("/dininghall")
public class DiningHallController {

    @Autowired
    private DiningHallRepository diningHallRepository;

    // Get all dining halls
    @GetMapping
    public List<DiningHall> getAllDiningHalls() {
        return diningHallRepository.findAll();
    }

    // Get dining hall by ID
    @GetMapping("/{id}")
    public ResponseEntity<DiningHall> getDiningHallById(@PathVariable int id) {
        Optional<DiningHall> diningHall = diningHallRepository.findById(id);
        return diningHall.map(ResponseEntity::ok) //if theres a value, http 200 (ok) status
                .orElseGet(() -> ResponseEntity.notFound().build()); //if optional is empty, http 404 (not found) status
    }

    // Create a new dining hall
    @PostMapping("/new")
    public ResponseEntity<DiningHall> createDiningHall(@RequestBody DiningHall diningHall) {
        DiningHall savedDiningHall = diningHallRepository.save(diningHall);
        return new ResponseEntity<>(savedDiningHall, HttpStatus.CREATED);
    }

    // Update dining hall
    @PutMapping("/{id}")
    public ResponseEntity<DiningHall> updateDiningHall(@PathVariable int id, @RequestBody DiningHall diningHallDetails) {
        return diningHallRepository.findById(id)
                .map(existingDiningHall -> {
                    existingDiningHall.setName(diningHallDetails.getName());
                    existingDiningHall.setLocation(diningHallDetails.getLocation());
                    DiningHall updatedDiningHall = diningHallRepository.save(existingDiningHall);
                    return ResponseEntity.ok(updatedDiningHall);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete dining hall
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiningHall(@PathVariable int id) {
        return diningHallRepository.findById(id)
                .map(diningHall -> {
                    diningHallRepository.delete(diningHall);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Search dining halls by name
    @GetMapping("/search/name")
    public List<DiningHall> searchDiningHallsByName(@RequestParam String name) {
        return diningHallRepository.findByNameContainingIgnoreCase(name);
    }

    // Search dining halls by location
    @GetMapping("/search/location")
    public List<DiningHall> searchDiningHallsByLocation(@RequestParam String location) {
        return diningHallRepository.findByLocationContainingIgnoreCase(location);
    }

    // Search dining halls by menu item name
    @GetMapping("/search/menuitem")
    public List<DiningHall> searchDiningHallsByMenuItem(@RequestParam String itemName) {
        return diningHallRepository.findByMenuItemsNameContainingIgnoreCase(itemName);
    }

    // Search dining halls with items below a certain price
    @GetMapping("/search/price")
    public List<DiningHall> searchDiningHallsByMaxPrice(@RequestParam double maxPrice) {
        return diningHallRepository.findDistinctByMenuItemsPriceLessThanEqual(maxPrice);
    }

    // Add a menu item to a dining hall
    @PostMapping("/{id}/menuitems")
    public ResponseEntity<MenuItems> addMenuItem(@PathVariable int id, @RequestBody MenuItems menuItem) {
        return diningHallRepository.findById(id)
                .map(diningHall -> {
                    menuItem.setDiningHall(diningHall);
                    diningHall.getMenuItems().add(menuItem);
                    diningHallRepository.save(diningHall);
                    return new ResponseEntity<>(menuItem, HttpStatus.CREATED);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get all menu items for a dining hall
    @GetMapping("/{id}/menuitems")
    public ResponseEntity<List<MenuItems>> getMenuItems(@PathVariable int id) {
        return diningHallRepository.findById(id)
                .map(diningHall -> ResponseEntity.ok(diningHall.getMenuItems()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}