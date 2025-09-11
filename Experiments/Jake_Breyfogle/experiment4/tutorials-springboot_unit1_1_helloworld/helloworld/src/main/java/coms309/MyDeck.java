package coms309;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a basic deck class, it will have 52 cards and resemble a normal deck of playing cards
 */
public class MyDeck {
    private List<MyCard> cards;

    /**
     * Basic constructor for a normal 52 card deck
     */
    public MyDeck() {
        cards = new ArrayList<>();
        char[] suits = {'h', 'd', 's', 'c'};

        for (char s : suits) {
            for(int i = 2; i < 14; i++) {
                cards.add(new MyCard(i, s));
            }
        }
    }

    /**
     * Shuffles the deck of cards using the collections class
     */
    public void shuffle(){
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


}
