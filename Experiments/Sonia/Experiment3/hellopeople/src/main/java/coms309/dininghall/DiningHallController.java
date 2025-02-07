package coms309.dininghall;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;


@RestController
//@RequestMapping("/dininghalls")
public class DiningHallController {

    private static final HashMap<String, DiningHall> diningHalls = new HashMap<>();

    // Create a new dining hall
    @PostMapping("/dininghalls")
    public String createDiningHall(@RequestBody DiningHall diningHall) {
        System.out.println(diningHall);
        diningHalls.put(diningHall.getName(), diningHall);
        return "Dining Hall " + diningHall.getName() + " created.";
    }

    // Get all dining halls
    @GetMapping("/dininghalls")
    public HashMap<String, DiningHall> getAllDiningHalls() {
        return diningHalls;
    }

    // Get a specific dining hall by name
    @GetMapping("/dininghalls/{diningHallName}")
    public DiningHall getDiningHall(@PathVariable String diningHallName) {
        return diningHalls.get(diningHallName);
    }

    // Add a meal to a specific dining hall
    @PostMapping("/dininghalls/{diningHallName}/meals")
    public String addMeal(@PathVariable String diningHallName, @RequestBody Meal meal) {
        DiningHall diningHall = diningHalls.get(diningHallName);
        if (diningHall != null) {
            diningHall.addMeal(meal);
            return "Meal " + meal.getName() + " added to " + diningHallName + ".";
        } else {
            return "Dining hall not found.";
        }
    }

    // Get all meals from a specific dining hall
    @GetMapping("/dininghalls/{diningHallName}/meals")
    public HashMap<String, Meal> getMeals(@PathVariable String diningHallName) {
        DiningHall diningHall = diningHalls.get(diningHallName);
        if (diningHall != null) {
            return diningHall.getMeals();
        } else {
            return null;
        }
    }

    // Get a specific meal from a dining hall
    @GetMapping("/dininghalls/{diningHallName}/meals/{mealName}")
    public Meal getMeal(@PathVariable String diningHallName, @PathVariable String mealName) {
        DiningHall diningHall = diningHalls.get(diningHallName);
        if (diningHall != null) {
            return diningHall.getMeal(mealName);
        } else {
            return null;
        }
    }

    // Remove a meal from a dining hall
    @DeleteMapping("/dininghalls/{diningHallName}/meals/{mealName}")
    public String removeMeal(@PathVariable String diningHallName, @PathVariable String mealName) {
        DiningHall diningHall = diningHalls.get(diningHallName);
        if (diningHall != null) {
            diningHall.removeMeal(mealName);
            return "Meal " + mealName + " removed from " + diningHallName + ".";
        } else {
            return "Dining hall or meal not found.";
        }
    }

    // Delete a dining hall
    @DeleteMapping("/dininghalls/{diningHallName}")
    public String deleteDiningHall(@PathVariable String diningHallName) {
        diningHalls.remove(diningHallName);
        return "Dining hall " + diningHallName + " deleted.";
    }
}
