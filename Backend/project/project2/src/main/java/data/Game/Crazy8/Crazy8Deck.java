package data.Game.Crazy8;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of Crazy 8s cards.
 * <p>
 * Supports multiple combined decks, shuffling, dealing cards, and querying size.
 * Cards are instances of {@link Crazy8Card}.
 */
public class Crazy8Deck {

    /**
     * The list of cards currently in the deck.
     */
    private List<Crazy8Card> cards;

    /**
     * Creates a Crazy 8s deck containing the specified number of standard decks.
     * <p>
     * Each deck includes all card values from 2–14 (with 11–14 representing special cards)
     * for each of the four colors (R, G, B, Y).
     *
     * <ul>
     *     <li>11 → Reverse</li>
     *     <li>12 → Skip</li>
     *     <li>13 → Draw 2</li>
     *     <li>14 → Draw 4 / pick color</li>
     *     <li>8  → Wild / pick color</li>
     * </ul>
     *
     * @param numberOfDecks the number of 52-card Crazy 8 decks to include
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
     * Deals the top card from the deck.
     *
     * @param isPlayable whether the dealt card should be marked as playable
     * @return the dealt {@link Crazy8Card}, or {@code null} if the deck is empty
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
     * Returns the number of cards remaining in the deck.
     *
     * @return the deck size
     */
    public int size() {
        return cards.size();
    }
}
