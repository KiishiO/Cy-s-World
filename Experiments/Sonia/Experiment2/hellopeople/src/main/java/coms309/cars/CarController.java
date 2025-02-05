package coms309.cars;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.HashMap;

/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Sonia Patil
 */

@RestController
public class CarController {

    HashMap<String, Car> carList = new HashMap<>();

    //CRUDL (create/read/update/delete/list)
    //use POST, GET, PUT, DELETE, GET, methods for CRUDL

    //THIS IS THE LIST OPERATION
    //gets all the cars in the list and returns in it JSON format
    //Controller takes no input
    //Springboot automatically converts the list to JSON format
    //because of @ResponseBody
    //Note: To LIST, we use the GET method
    @GetMapping("/car")
    public HashMap<String, Car> getAllCar(){return carList;}

    //THIS IS THE CREATE OPERATION
    //springboot automatically converts JSON input into a car object and
    //the method below enters it into the list.
    //It returns a string message in THIS example.
    //in this case because of @ResponseBody
    //Note: to CREATE we use POST method

    @PostMapping("/car")
    public String createCar(@RequestBody Car car) {
        System.out.println(car);
        carList.put(car.getModel(), car);
        return "New car "+ car.getModel() + " Saved";
    }

    //THIS IS THE READ OPERATION
    // Springboot get the PATHVARIABLE from the url
    // We extract the car from the HashMap
    // springboot automatically converts Car to JSON format when we return it
    // in this case because of @ResponseBody
    // Note: To READ we use GET method
    @GetMapping("/people/{model}")
    public Car getCar(@PathVariable String model) {
        Car c = carList.get(model);
        return c;
    }

    //THIS IS THE UPDATE OPERATION
    // We extract the person from the Hash and modify it.
    // Springboot automatically converts the Car to JSON format
    // Springboot gets the PATHVARIABLE from the URL
    // Here we are returning what we sent to the method
    // in this case because of @ResponseBody
    // Note: to UPDATE we use PUT method
    @PutMapping("/car/{model}")
    public Car updateCar(@PathVariable String model, @RequestBody Car c) {
        carList.replace(model, c);
        return carList.get(model);
    }

    //THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from URL
    // we return the entire list -- converted to JSON
    // in this case because of @ReponseBody
    //Note: TO DELETE we use delete method
    @DeleteMapping("/car/{model}")
    public HashMap<String, Car> deletePerson(@PathVariable String model){
        carList.remove(model);
        return carList;
    }
}
