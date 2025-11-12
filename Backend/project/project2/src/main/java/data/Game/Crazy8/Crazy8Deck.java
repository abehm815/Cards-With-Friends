package data.Game.Crazy8;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Crazy8Deck {
    private List<Crazy8Card> cards;

    /**
     * Basic constructor for a normal 52 card deck
     */
    public Crazy8Deck(int numberOfDecks) {
        cards = new ArrayList<>();
        for (int j = 0; j < numberOfDecks; j++) {
            char[] colors = {'R', 'G', 'B', 'Y'};

            for (char s : colors) {
                for (int i = 2; i <= 14; i++) {
                    cards.add(new Crazy8Card(i, s));
                    //11 == reverse
                    //12 == skip
                    //13 == draw 2
                    //14 == draw 4 and pick color
                    //8  == pick color
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
        for(Crazy8Card card : cards){
            System.out.println(card.toString());
        }
    }

    /**
     * Deals a card by removing it from the deck and returning a card object
     */
    public Crazy8Card dealCard(boolean isPlayable) {
        if(!cards.isEmpty()) {
            Crazy8Card dealtCard = cards.remove(cards.size()-1);
            dealtCard.setIsPlayable(isPlayable);
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
