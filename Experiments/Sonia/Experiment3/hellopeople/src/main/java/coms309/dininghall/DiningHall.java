package coms309.dininghall;

public class DiningHall {
    private String name;
    private Menu menu;

    public DiningHall(String name) {
        this.name = name;
        this.menu = menu;
    }

    public void addDishToMenu(String name, String description) {
        Food newDish = new Food(name, description);
        menu.addDish(newDish);
    }

    public void showMenu() {
        System.out.println("Dining Hall: " + name);
        menu.displayMenu();
    }

}
