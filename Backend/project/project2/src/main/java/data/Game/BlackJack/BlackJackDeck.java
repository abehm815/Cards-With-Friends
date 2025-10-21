package data.Game.BlackJack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlackJackDeck {
    private List<BlackJackCard> cards;

    /**
     * Basic constructor for a normal 52 card deck
     */
    public BlackJackDeck(int numberOfDecks) {
        cards = new ArrayList<>();
        for (int j = 0; j < numberOfDecks; j++) {
            char[] suits = {'h', 'd', 's', 'c'};

            for (char s : suits) {
                for (int i = 2; i < 14; i++) {
                    cards.add(new BlackJackCard(i, s));
                }
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
        for(BlackJackCard card : cards){
            System.out.println(card.toString());
        }
    }

    /**
     * Deals a card by removing it from the deck and returning a card object
     */
    public BlackJackCard dealCard(boolean isShown) {
        if(!cards.isEmpty()) {
             BlackJackCard dealtCard = cards.remove(cards.size()-1);
             dealtCard.setIsShowing(isShown);
             return dealtCard;
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


}

