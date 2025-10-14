package data.Game.goFish;

import data.User.MyCard;

import java.util.ArrayList;
import java.util.List;

public class GoFishPlayer {
    private String username;
    private List<MyCard> hand;
    private List<Integer> completedBooks;

    /**
     * Constructor for a Go Fish Player for the game logic
     * @param username (Username in SQL Database)
     */
    public GoFishPlayer(String username) {
        this.username = username;
        this.hand = new ArrayList<>();
        this.completedBooks = new ArrayList<>();
    }

    /**
     * Gets username
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets hand
     * @return hand
     */
    public List<MyCard> getHand() {
        return hand;
    }

    /**
     * Gets completed books
     * @return completedBooks
     */
    public List<Integer> getCompletedBooks() {
        return completedBooks;
    }

    /**
     * Adds a card to the player's hand
     * @param card basic MyCard
     */
    public void addCard(MyCard card) {
        hand.add(card);
    }

    /**
     * Removes a card from the player's hand
     * @param card basic MyCard
     */
    public void removeCard(MyCard card) { hand.remove(card); }

    /**
     * Checks the player's hand for a card of the same value, returns the card object if value matches
     * else it returns null which will be checked for in the ask statement
     * @param value rank of card in question
     * @return MyCard object
     */
    public MyCard checkForCard(int value) {
        for (MyCard card : hand) {
            if (card.value == value) {
                return card;
            }
        }
        return null;
    }

    /**
     * Goes through the player's current hand and looks for a pair of same valued cards,
     * if one is found, the cards will be removed from the hand and the player's completed books
     * will be increased
     */
    public void cleanMatchesInHand() {
        int[] cardCount = new int[14];
        // Count all up how much of each value the player has in hand
        for (MyCard card : hand) {
            cardCount[card.value]++;
        }

        // Check if any of the values in the array is 2 or greater
        for (int i = 0; i < cardCount.length; i++) {
            // Remove cards from hand and add to books collected
            if (cardCount[i] >= 2) {
                // Grab first card of the pair and remove it
                MyCard card = checkForCard(i);
                removeCard(card);

                // Grab second card of the pair and remove it
                card = checkForCard(i);
                removeCard(card);

                // Add card rank pair to books collected
                completedBooks.add(i);
            }
        }
    }
}
