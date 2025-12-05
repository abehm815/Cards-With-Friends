package data.Game.BlackJack;

import data.Game.BlackJack.BlackJackCard;
import data.User.AppUser;
import data.User.Stats.BlackjackStats;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in a Blackjack game. Each player may have one or more hands
 * (in the case of splits), chip balances, bets per hand, and standing state per hand.
 * <p>
 * This class also supports linking to an {@link AppUser} for persistent statistics tracking.
 */
public class BlackJackPlayer {
    /** The username of the player. */
    private String username;
    /** A list of hands, where each hand is a list of cards. Used for split hands. */
    private List<List<BlackJackCard>> hands;
    /** Index pointing to the player's current active hand. */
    private int currentHandIndex = 0;
    /** Chip count for the player. */
    private int chips;
    /** The bet placed on each hand, aligned with {@code hands}. */
    private List<Integer> betOnCurrentHand;
    /** Whether each hand has stood, aligned with {@code hands}. */
    private List<Boolean> hasStoodForHand;
    /** Whether the player has placed a bet this round. */
    private boolean hasBet;
    /** A reference to the owning AppUser for stats updates. Not serialized. */
    private transient AppUser userRef;

    /**
     * Constructs a Blackjack player using a username and starting chip amount.
     *
     * @param username the player's username
     * @param chips    the starting chip balance
     */
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
     * Constructs a Blackjack player using an AppUser reference and chip balance.
     *
     * @param appUser the associated user
     * @param chips   starting chips
     */
    public BlackJackPlayer(AppUser appUser, int chips) {
        this.username = appUser.getUsername();
        this.hands = new ArrayList<>();
        this.hands.add(new ArrayList<>()); // first hand
        this.chips = chips;
        this.betOnCurrentHand = new ArrayList<>();
        this.betOnCurrentHand.add(0); // default bet for first hand
        this.hasStoodForHand = new ArrayList<>();
        this.hasStoodForHand.add(false); // first hand has not stood
        this.userRef = appUser;
    }

    /**
     * Retrieves the Blackjack statistics associated with this player.
     *
     * @return BlackjackStats instance or null if unavailable
     */
    public BlackjackStats getBlackJackStats() {
        if (userRef != null && userRef.getUserStats() != null) {
            return (BlackjackStats) userRef.getUserStats().getGameStats("Blackjack");
        }
        return null;
    }

    /**
     * Gets the AppUser object this player corresponds to.
     *
     * @return the AppUser reference, or null if not set
     */
    public AppUser getUserRef() {
        return userRef;
    }

    /**
     * Gets the username of the player.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the current active hand for the player.
     *
     * @return the current hand as a list of cards
     */
    public List<BlackJackCard> getHand() {
        return hands.get(currentHandIndex);
    }

    /**
     * Gets all hands belonging to the player, including split hands.
     *
     * @return list of all hands
     */
    public List<List<BlackJackCard>> getHands() {
        return hands;
    }

    /**
     * Gets the index of the player's current hand.
     *
     * @return the index of the active hand
     */
    public int getCurrentHandIndex() {
        return currentHandIndex;
    }

    /**
     * Sets the index of the player's current active hand.
     *
     * @param currentHandIndex the new hand index
     */
    public void setCurrentHandIndex(int currentHandIndex) {
        this.currentHandIndex = currentHandIndex;
    }

    /**
     * Gets the player's current chip count.
     *
     * @return chip total
     */
    public int getChips() {
        return chips;
    }

    /**
     * Sets the player's chip count.
     *
     * @param chips new chip value
     */
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

    /**
     * Gets the bet amount placed on the current hand.
     *
     * @return bet value
     */
    public int getBetOnCurrentHand() {
        return betOnCurrentHand.get(currentHandIndex);
    }

    /**
     * Sets the bet amount for the current hand.
     *
     * @param bet the bet value to assign
     */
    public void setBetOnCurrentHand(int bet) {
        betOnCurrentHand.set(currentHandIndex, bet);
    }


    /**
     * Adds a new hand to the player (used when splitting).
     *
     * @param hand the new hand of cards
     * @param bet  the bet associated with the new hand
     */
    public void addHand(List<BlackJackCard> hand, int bet) {
        hands.add(hand);
        betOnCurrentHand.add(bet);
        hasStoodForHand.add(false);
    }

    /**
     * Gets all bets for every hand.
     *
     * @return list of bets
     */
    public List<Integer> getBets() {
        return betOnCurrentHand;
    }


    /**
     * Checks whether the current hand has stood.
     *
     * @return true if the current hand has stood
     */
    public boolean getHasStoodForHand() {
        return hasStoodForHand.get(currentHandIndex);
    }


    /**
     * Sets whether the current hand has stood.
     *
     * @param hasStood true if the player stands on this hand
     */
    public void setHasStood(Boolean hasStood) {
        hasStoodForHand.set(currentHandIndex, hasStood);
    }


    /**
     * Gets whether the player has placed a bet this round.
     *
     * @return true if the player has bet
     */
    public boolean getHasBet() {
        return hasBet;
    }

    /**
     * Sets whether the player has placed a bet this round.
     *
     * @param hasBet the bet state
     */
    public void setHasBet(boolean hasBet) {
        this.hasBet = hasBet;
    }

    /**
     * Calculates the value of the current hand.
     *
     * @return the hand value
     */
    public int getHandValue() {
        return calculateHandValue(getHand());
    }

    /**
     * Calculates the value of a specific hand.
     *
     * @param hand list of Blackjack cards
     * @return total hand value following Blackjack rules
     */
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

    /**
     * Determines whether the player can split their current hand.
     *
     * @return true if the hand contains two equal-value cards and the player has enough chips
     */
    public boolean canSplit() {
        List<BlackJackCard> hand = getHand();
        return (hand.size() == 2 && ((hand.get(0).getValue()) == (hand.get(1).getValue())) && (chips >= (betOnCurrentHand.get(currentHandIndex))));
        // now checks for chip balance and if cards are same value
    }

    /**
     * Checks whether the player has enough chips to double down.
     *
     * @return true if doubling is affordable
     */
    public boolean canDouble(){
        return chips >= (betOnCurrentHand.get(currentHandIndex)*2);
    }


    /**
     * Advances the player to the next hand (used after completing actions on a split hand).
     */
    public void moveToNextHand() {
        if (currentHandIndex + 1 < hands.size()) {
            currentHandIndex++;
        }
    }

    /**
     * Checks whether the current hand is the final hand the player has.
     *
     * @return true if on the last hand
     */
    public boolean isOnLastHand() {
        return currentHandIndex >= hands.size() - 1;
    }

    public void resetAllBets() {
        for (int i = 0; i < betOnCurrentHand.size(); i++) {
            betOnCurrentHand.set(i, 0);
        }
    }

    /**
     * Resets all bets to zero across every hand.
     */
    public void resetAllHasStood() {
        for (int i = 0; i < hasStoodForHand.size(); i++) {
            hasStoodForHand.set(i, false);
        }
    }
}