package coms309;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
class WelcomeController {

    List<Integer> CardValues = new ArrayList<>();

    @GetMapping("/")
    public String base() {
        return "Hello and welcome to COMS 309";
    }
    
    @GetMapping("/{name}")
    public String welcome(@PathVariable String name) {
        return "Hello and welcome to COMS 309: " + name;
    }

    // Adding another welcome statement
    @GetMapping("/welcome")
    public String welcome() {return "Welcome to this page"; }

    // Gets all the cards
    @GetMapping("/cards")
    public List<Integer> getCardValues() {return CardValues; }

    // Adding a card to the page
    @PutMapping("/addCard/{cardValue}")
    public void addCard(@PathVariable int cardValue) { CardValues.add(cardValue); }

    // Removes a card from the list
    @DeleteMapping("/cards/{cardValue}")
    public void deleteCard(@PathVariable int cardValue) { CardValues.remove(cardValue); }
}
