package coms309.dininghall;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dininghalls")
public class DiningHallController {

    private static final Map<String, Menu> diningHalls = new HashMap<>();

    @PostMapping("/")
    public String createDiningHall(@RequestParam String hallName) {
        if(diningHalls.containsKey(hallName)) {
            return "Dining hall already exists.";
        } else {
            diningHalls.put(hallName, new Menu());
            return "Dining hall: " + hallName + "created successfully!";
        }
    }

    @PostMapping("/{hallName}/menu")
    public String addFoodToMenu(@PathVariable String hallName, @RequestParam String name, @RequestParam String description) {
        if(!diningHalls.containsKey(hallName)) {
            return "Dining hall " + hallName + "does not exist.";
        }
        Menu menu = diningHalls.get(hallName);
        Food newFood = new Food(name, description);
        menu.addDish(newFood);
        return "Dish added to " + hallName + "menu!";
    }

    @GetMapping("{hallName}/menu")
    public Menu getMenu(@PathVariable String hallName) {
        return diningHalls.getOrDefault(hallName, new Menu());
    }


}
