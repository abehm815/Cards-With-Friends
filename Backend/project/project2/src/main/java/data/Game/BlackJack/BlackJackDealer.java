package data.Game.BlackJack;

import data.User.MyCard;
import java.util.ArrayList;
import java.util.List;

public class BlackJackDealer {
    private List<MyCard> hand;
    private boolean hitOnSoft17; // true = dealer hits on soft 17 (optional rule)

    public BlackJackDealer(boolean hitOnSoft17) {
        this.hand = new ArrayList<>();
        this.hitOnSoft17 = hitOnSoft17;
    }

    /** Default constructor (dealer stands on all 17s) */
    public BlackJackDealer() {
        this(false);
    }

    /** Adds a card to the dealer's hand */
    public void addCard(MyCard card) {
        hand.add(card);
    }

    /** Gets the dealer's hand */
    public List<MyCard> getHand() {
        return hand;
    }

    /** Clears the hand (for new round) */
    public void resetHand() {
        hand.clear();
    }

    /**
     * Calculates the dealer's hand value.
     * Handles Aces as 1 or 11 optimally.
     * @return total hand value
     */
    public int getHandValue() {
        int value = 0;
        int aceCount = 0;

        for (MyCard card : hand) {
            int rank = card.getValue(); // 2–14 (J=11, Q=12, K=13, A=14)
            if (rank >= 11 && rank <= 13) {
                value += 10;
            } else if (rank == 14) {
                value += 11;
                aceCount++;
            } else {
                value += rank;
            }
        }

        while (value > 21 && aceCount > 0) {
            value -= 10;
            aceCount--;
        }

        return value;
    }

    /** Checks if dealer has a "soft 17" (Ace counted as 11 and total = 17) */
    private boolean isSoft17() {
        int value = 0;
        int aceCount = 0;

        for (MyCard card : hand) {
            int rank = card.getValue();
            if (rank >= 11 && rank <= 13) {
                value += 10;
            } else if (rank == 14) {
                value += 11;
                aceCount++;
            } else {
                value += rank;
            }
        }

        return (value == 17 && aceCount > 0);
    }

    /**
     * Dealer drawing logic — keeps drawing until 17 or higher.
     * @param deck deck to draw from
     */
    public void playTurn(BlackJackDeck deck) {
        while (true) {
            int value = getHandValue();

            // Dealer stands at 17+ unless it's a soft 17 and hitOnSoft17 is true
            if (value > 17 || (value == 17 && (!hitOnSoft17 || !isSoft17()))) {
                break;
            }

            // Dealer hits
            addCard(deck.dealCard(true ));
        }
    }
}