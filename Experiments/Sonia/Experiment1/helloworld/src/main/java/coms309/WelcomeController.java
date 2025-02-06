package coms309;

import org.springframework.web.bind.annotation.*;

@RestController
class WelcomeController {

    private String name;

    @GetMapping("/")
    public String welcome() {
        return "Hello and welcome to COMS 309";
    }

    @GetMapping("/{name}")
    public String welcome(@PathVariable String name) {
        return "Hello and welcome to COMS 309: " + name;
    }

    @PostMapping("/")
    public @ResponseBody String addName(@RequestBody String name) {
        this.name = name;
        System.out.println(name + " added");
        return name;
    }
}
