package coms309;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for the player, it will have information on the player like name, current hand
 */
public class Player {
    /**
     * Players Name
     */
    public String name;

    /**
     * The player's hand
     */
    public List<MyCard> hand;

    /**
     * Constructor that creates a player with an empty hand
     * @param name
     */
    public Player(String name){
        this.name = name;
        this.hand = new ArrayList<>();
    }

    /**
     * Constructor that can create a player with a given hand
     * @param name
     * @param hand
     */
    public Player(String name, List<MyCard> hand) {
        this.name = name;
        this.hand = hand;
    }

    /**
     * Returns the players name
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the players name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Shows the player's current hand
     */
    public void showHand() {
        System.out.println(name + "'s Hand:");
        if (!hand.isEmpty()) {
            for (MyCard card : hand) {
                System.out.println(card.toString());
            }
        } else {
            System.out.println("No Cards in Hand");
        }
    }

    /**
     * Adds a card to the player's hand
     * @param card
     */
    public void addCard(MyCard card) {
        hand.add(card);
    }

    /**
     * Removes a card from the player's hand
     * @param card
     */
    public void removeCard(MyCard card) {
        hand.remove(card);
    }

    public String hasCard(int value) {
        for (MyCard c : hand) {
            if (c.getValue() == value) {
                return name + " has a " + Integer.toString(value);
            }
        }
        return name + " does not have a " + Integer.toString(value) + ". Go Fish!";
    }
}
