package coms309.cars;


/**
 * Provides the Definition/Structure for cars
 *
 * @author Sonia Patil
 */
public class Car {

    private String make;

    private String model;

    private String engine;

    private String vinNumber;

    public Car() {

    }

    public Car(String make, String model, String engine, String vinNumber) {
        this.make = make;
        this.model = model;
        this.engine = engine;
        this.vinNumber = vinNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getVinNumber() {
        return vinNumber;
    }

    public void setVinNumber(String vinNumber) {
        this.vinNumber = vinNumber;
    }

    @Override
    public String toString() {
        return model + " "
                + make + " "
                + engine + " "
                + vinNumber;
    }
}
