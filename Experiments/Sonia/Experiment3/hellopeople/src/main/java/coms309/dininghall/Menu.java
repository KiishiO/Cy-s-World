package coms309.dininghall;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private List<Food> dishes;

    public Menu(){
        this.dishes = new ArrayList<>();
    }

    /**
     * Add a dish to the menu
     * @param item
     */
    public void addDish(Food item) {
        dishes.add(item);
    }

    public void displayMenu() {
        if(dishes.isEmpty()) {
            System.out.println("The Menu is empty.");
        } else {
            System.out.println("---Menu---");
            for (Food item: dishes) {
                System.out.println(item);
            }
        }
    }
}
