package data.Game.Crazy8;

import data.Game.MyCard;
import data.User.AppUser;
import data.User.Stats.Crazy8Stats;

import java.util.ArrayList;
import java.util.List;

public class Crazy8Player {
    private String username;
    private List<Crazy8Card> hand;
    private transient AppUser userRef;

    /**
     * Constructor for a Go Fish Player for the game logic does not keep stats
     * @param username (Generic Username)
     */
    public Crazy8Player(String username) {
        this.username = username;
        this.hand = new ArrayList<>();
    }

    /**
     * Constructor for a Go Fish Player using a given app user
     * @param user (AppUser)
     */
    public Crazy8Player(AppUser user) {
        this.username = user.getUsername();
        this.userRef = user;
        this.hand = new ArrayList<>();
    }

    /**
     * Gets userRef
     * @return (userRef)
     */
    public AppUser getUserRef() { return userRef; }

    /**
     * Gets stats associated with user
     * @return GoFishStats
     */
    public Crazy8Stats getCrazy8Stats() {
        if (userRef != null && userRef.getUserStats() != null) {
            return (Crazy8Stats) userRef.getUserStats().getGameStats("Crazy8");
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
    public List<Crazy8Card> getHand() {
        return hand;
    }

    /**
     * Gets hand size
     * @return hand size
     */
    public int getHandSize() { return hand.size(); }

    /**
     * Adds a card to the player's hand
     * @param card basic MyCard
     */
    public void addCard(Crazy8Card card) {
        hand.add(card);
    }

    /**
     * Removes a card from the player's hand
     * @param card basic MyCard
     */
    public void removeCard(Crazy8Card card) { hand.remove(card); }

}
