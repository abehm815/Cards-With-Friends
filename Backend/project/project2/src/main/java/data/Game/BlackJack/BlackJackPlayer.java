package data.Game.BlackJack;

import data.User.MyCard;

import java.util.ArrayList;
import java.util.List;

public class BlackJackPlayer {
    private String username;
    private List<MyCard> hand;
    private int chips;
    private int betOnCurrentHand;

    public BlackJackPlayer(String username,int chips) {
        this.username = username;
        this.hand = new ArrayList<>();
        this.chips = chips;
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
     * Gets chips
     * @return chips
     */
    public int getChips() {
        return chips;
    }

    /**
     * Adds a card to the player's hand
     * @param card basic MyCard
     */
    public void addCard(MyCard card) {
        hand.add(card);
    }


    public int  getBetOnCurrentHand() {
        return betOnCurrentHand;
    }
    public void setBetOnCurrentHand(int betOnCurrentHand) {
        this.betOnCurrentHand = betOnCurrentHand;
    }

    /**
     * Calculates the total Blackjack value of the player's hand.
     * Handles Aces as 1 or 11 optimally.
     * @return total hand value
     */
    public int getHandValue() {
        int value = 0;
        int aceCount = 0;

        for (MyCard card : hand) {
            int rank = card.getValue(); // assuming MyCard stores numeric ranks (2–14 where 11=J, 12=Q, 13=K, 14=A)
            if (rank >= 11 && rank <= 13) {
                value += 10; // J, Q, K
            } else if (rank == 14) {
                value += 11; // Ace as 11 for now
                aceCount++;
            } else {
                value += rank;
            }
        }

        while (value > 21 && aceCount > 0) {
            value -= 10; // count one Ace as 1 instead of 11
            aceCount--;
        }
        return value;
    }
}
