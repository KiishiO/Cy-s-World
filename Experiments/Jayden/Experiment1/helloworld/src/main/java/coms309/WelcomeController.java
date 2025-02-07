package coms309;

import com.sun.net.httpserver.Headers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.servlet.function.ServerRequest;


@RestController
@RequestMapping("/welcome")
public class WelcomeController {

    // Default welcome message
    @GetMapping
    public String getWelcomeMessage() {
        return "Hello and welcome to COMS 309! Please provide your name for a personalized greeting.";
    }

    // Personalized welcome message by path variable
    @GetMapping("/{name}")
    public String getPersonalizedWelcome(@PathVariable String name) {
        return "Hello and welcome to COMS 309, " + name + "!";
    }

    // Greeting with optional query parameters
    @GetMapping("/custom")
    public String getCustomGreeting(
            @RequestParam(value = "name", required = false, defaultValue = "Guest") String name,
            @RequestParam(value = "course", required = false, defaultValue = "COMS 309") String course) {
        return "Hello " + name + "! Welcome to " + course + ".";
    }
}
