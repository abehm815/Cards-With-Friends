package data.Game.euchre;

import data.User.AppUser;
import data.User.Stats.EuchreStats;

import java.util.ArrayList;
import java.util.List;

public class EuchrePlayer {
    private String username;
    private List<EuchreCard> hand;
    private List<List<EuchreCard>> tricks;
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
    public List<EuchreCard> getHand() {
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
    public List<List<EuchreCard>> getTricks() {
        return tricks;
    }

    /**
     * Adds a trick to the held tricks
     * @param trick (list of 4 EuchreCards)
     */
    public void addTrick (List<EuchreCard> trick) {
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
        tricks.clear();
    }

    /**
     * Adds a card to the player's hand
     * @param card basic EuchreCard
     */
    public void addCard(EuchreCard card) {
        hand.add(card);
        card.setOwner(this);
    }

    /**
     * Removes a card from the player's hand
     * @param card basic EuchreCard
     */
    public void removeCard(EuchreCard card) {
        hand.remove(card);
    }

    /**
     * Checks what cards can be played depending on what the lead was
     * in the event of not having any playable cards (no lead suits in hand),
     * all cards can be played
     * @param leadSuit (Current Lead Suit)
     * @return (list of playable cards)
     */
    public List<EuchreCard> checkPlayableCards(char leadSuit, char trumpSuit) {
        List<EuchreCard> playable = new ArrayList<>();

        // First pass: find cards that follow suit
        for (EuchreCard card : hand) {
            if (card.getEffectiveSuit(trumpSuit) == leadSuit) {
                playable.add(card);
            }
        }

        // If none follow suit, all are playable
        if (playable.isEmpty()) {
            playable.addAll(hand);
        }

        return playable;
    }
}
