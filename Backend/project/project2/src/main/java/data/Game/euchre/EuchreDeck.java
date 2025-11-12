package data.Game.euchre;

import data.Game.MyCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class creates the Euchre deck that will be used in a game
 * a special note with this deck is that the only cards used in it are
 * 9-Ace.
 */
public class EuchreDeck {
    private List<MyCard> cards;

    /**
     * Constructor for a Euchre Deck
     */
    public EuchreDeck() {
        cards = new ArrayList<>();
        char[] suits = {'h', 'd', 's', 'c'};

        for (char s : suits) {
            for(int i = 9; i < 14; i++) {
                cards.add(new MyCard(i, s));
            }
        }
    }

    /**
     * Shuffles the deck
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Prints out all the cards currently in the deck
     */
    public void printDeck() {
        for(MyCard card : cards){
            System.out.println(card.toString());
        }
    }

    /**
     * Deals a card by removing it from the deck and returning a card object
     */
    public MyCard dealCard() {
        if(!cards.isEmpty()) {
            return cards.remove(cards.size()-1);
        }
        System.out.println("Deck is empty");
        return null;
    }

    /**
     * Returns how many cards are left in the deck
     */
    public int size() {
        return cards.size();
    }

    /**
     * Tells the user if the deck is empty
     * @return (t/f)
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
