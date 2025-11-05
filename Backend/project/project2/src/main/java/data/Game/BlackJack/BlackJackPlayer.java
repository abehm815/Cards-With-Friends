package data.Game.BlackJack;

import data.Game.MyCard;

import java.util.ArrayList;
import java.util.List;

public class BlackJackPlayer {
    private String username;
    private List<List<BlackJackCard>> hands;
    private int currentHandIndex = 0;
    private int chips;
    private List<Integer> betOnCurrentHand;
    private List<Boolean> hasStoodForHand;
    private boolean hasBet;

    public BlackJackPlayer(String username, int chips) {
        this.username = username;
        this.hands = new ArrayList<>();
        this.hands.add(new ArrayList<>()); // first hand
        this.chips = chips;
        this.betOnCurrentHand = new ArrayList<>();
        this.betOnCurrentHand.add(0); // default bet for first hand
        this.hasStoodForHand = new ArrayList<>();
        this.hasStoodForHand.add(false); // first hand has not stood
    }

    /**
     * Gets username
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets hand
     *
     * @return hand
     */
    public List<BlackJackCard> getHand() {
        return hands.get(currentHandIndex);
    }

    public List<List<BlackJackCard>> getHands() {
        return hands;
    }

    public int getCurrentHandIndex() {
        return currentHandIndex;
    }

    /**
     * Gets chips
     *
     * @return chips
     */
    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    /**
     * Adds a card to the player's hand
     *
     * @param card basic MyCard
     */
    public void addCard(BlackJackCard card) {
        hands.get(currentHandIndex).add(card);
    }


    public int getBetOnCurrentHand() {
        return betOnCurrentHand.get(currentHandIndex);
    }

    public void setBetOnCurrentHand(int bet) {
        betOnCurrentHand.set(currentHandIndex, bet);
    }

    public void addHand(List<BlackJackCard> hand, int bet) {
        hands.add(hand);
        betOnCurrentHand.add(bet);
        hasStoodForHand.add(false);
    }

    public List<Integer> getBets() {
        return betOnCurrentHand;
    }

    public boolean getHasStoodForHand() {
        return hasStoodForHand.get(currentHandIndex);
    }


    public void setHasStood(Boolean hasStood) {
        hasStoodForHand.set(currentHandIndex, hasStood);
    }

    public boolean getHasBet() {
        return hasBet;
    }

    public void setHasBet(boolean hasBet) {
        this.hasBet = hasBet;
    }

    public int getHandValue() {
        return calculateHandValue(getHand());
    }

    public int getHandValue(List<BlackJackCard> hand) {
        return calculateHandValue(hand);
    }

    /**
     * Calculates the total Blackjack value of the player's hand.
     * Handles Aces as 1 or 11 optimally.
     *
     * @return total hand value
     */
    public int calculateHandValue(List<BlackJackCard> hand) {
        int value = 0;
        int aceCount = 0;

        for (BlackJackCard card : hand) {
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

    public boolean canSplit() {
        List<BlackJackCard> hand = getHand();
        return hand.size() == 2 && ((hand.get(0).getValue()) == (hand.get(1).getValue()));
    }

    public void moveToNextHand() {
        if (currentHandIndex + 1 < hands.size()) {
            currentHandIndex++;
        }
    }

    public boolean isOnLastHand() {
        return currentHandIndex >= hands.size() - 1;
    }

    public void resetAllBets() {
        for (int i = 0; i < betOnCurrentHand.size(); i++) {
            betOnCurrentHand.set(i, 0);
        }
    }
    public void resetAllHasStood() {
        for (int i = 0; i < hasStoodForHand.size(); i++) {
            hasStoodForHand.set(i, false);
        }
    }
}