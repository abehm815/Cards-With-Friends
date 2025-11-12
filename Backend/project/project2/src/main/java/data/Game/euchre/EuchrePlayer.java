package data.Game.euchre;

import data.Game.MyCard;
import data.User.AppUser;
import data.User.Stats.EuchreStats;

import java.util.ArrayList;
import java.util.List;

public class EuchrePlayer {
    private String username;
    private List<MyCard> hand;
    private List<List<MyCard>> tricks;
    private transient AppUser userRef;

    /**
     * Constructor for a Euchre Player for the game logic does not keep stats
     * @param username (Generic Username)
     */
    public EuchrePlayer(String username) {
        this.username = username;
        this.hand = new ArrayList<>();
        this.tricks = new ArrayList<>();
    }

    /**
     * Constructor for a Euchre Player using a given app user
     * @param user (AppUser)
     */
    public EuchrePlayer(AppUser user) {
        this.username = user.getUsername();
        this.userRef = user;
        this.hand = new ArrayList<>();
        this.tricks = new ArrayList<>();
    }

    /**
     * Gets userRef
     * @return (userRef)
     */
    public AppUser getUserRef() {
        return userRef;
    }

    /**
     * Gets stats associated with user
     * @return EuchreStats
     */
    public EuchreStats getStats() {
        if (userRef != null && userRef.getUserStats() != null) {
            return (EuchreStats) userRef.getUserStats().getGameStats("Euchre");
        }
        return null;
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
     * Gets hand size
     * @return hand size
     */
    public int getHandSize() {
        return hand.size();
    }

    /**
     * Gets held tricks
     * @return tricks
     */
    public List<List<MyCard>> getTricks() {
        return tricks;
    }

    /**
     * Adds a trick to the held tricks
     * @param trick (list of 4 MyCards)
     */
    public void addTrick (List<MyCard> trick) {
        tricks.add(trick);
    }

    /**
     * Returns how many tricks a player has
     * @return size of tricks
     */
    public int getTrickCount() {
        return tricks.size();
    }

    /**
     * Clears all tricks held
     */
    public void clearTricks() {
        for (List<MyCard> trick : tricks) {
            tricks.remove(trick);
        }
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
    public void removeCard(MyCard card) {
        hand.remove(card);
    }

    /**
     * Checks what cards can be played depending on what the lead was
     * in the event of not having any playable cards (no lead suits in hand),
     * all cards can be played
     * @param leadSuit (Current Lead Suit)
     * @return (list of playable cards)
     */
    public List<MyCard> checkPlayableCards(char leadSuit) {
        // Create new list to hold playable cards
        List<MyCard> playable = new ArrayList<>();

        // Fill list with cards that match the lead suit
        for (MyCard card : hand) {
            if (card.getSuit() == leadSuit) {
                playable.add(card);
            }
        }

        // If no lead suits in hand, add all cards to playable
        if (playable.isEmpty()) {
            playable.addAll(hand);
        }

        return playable;
    }
}
