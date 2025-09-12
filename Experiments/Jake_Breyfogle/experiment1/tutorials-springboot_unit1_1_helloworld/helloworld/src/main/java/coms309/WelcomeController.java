package coms309;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
class WelcomeController {

    List<Integer> CardValues = new ArrayList<>();
    List<Player> players = new ArrayList<>();

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
    public String deleteCard(@PathVariable int cardValue) {
        int index = 0;
        for (int value : CardValues) {
            if (value == cardValue) {
                CardValues.remove(index);
                return "Deleted card value " + Integer.toString(cardValue);
            }
            index++;
        }
        return "Card value not found";
    }

    // Adds a new player to the DB
    @PostMapping("/players")
    public String addPlayer(@RequestBody Player player) {
        System.out.println(player);
        players.add(player);
        return "New person "+ player.getName() + " Saved";
    }

    // Gets all players from the DB
    @GetMapping("/players")
    public List<Player> getPlayers() { return players; }

    // Deletes a player from the DB
    @DeleteMapping("/players/{name}")
    public String deletePlayer(@PathVariable String name) {
        for(Player p : players) {
            if(p.getName().equals(name)) {
                players.remove(p);
                return "Player " + name + " has been deleted";
            }
        }
        return "Player " + name + " not found";
    }

    // Updates a player in the DB
    @PutMapping("/players/{name}")
    public String updatePlayer(@PathVariable String name, @RequestBody Player updatedPlayer) {
        for (Player p : players) {
           if (p.getName().equals(name)) {
               players.remove(p);
               players.add(updatedPlayer);
               return "Player " + name + " has been updated";
           }
        }
        return "Player " + name + " not found";
    }
}
