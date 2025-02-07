package coms309.dininghall;

public class Meal {
    private String name;
    private String description;

    public Meal() {
    }

    public Meal(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Meal{name='" + name + "', description='" + description + "'}";
    }

}
