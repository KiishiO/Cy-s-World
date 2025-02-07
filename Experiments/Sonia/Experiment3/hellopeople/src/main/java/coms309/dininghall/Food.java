package coms309.dininghall;

public class Food {
    private String item;
    private String description;

    public Food(String item, String description){
        this.item = item;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }
    @Override
    public String toString() {
        return item + " - " + description;
    }

}
