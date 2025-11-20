package data.Game.Crazy8;
import data.User.AppUser;
import data.User.Stats.Crazy8Stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in a Crazy 8s game.
 * <p>
 * A player may be created from a simple username (guest player) or from an
 * {@link AppUser} object when tied to backend user statistics. The player
 * stores a hand of {@link Crazy8Card} objects and optionally provides access
 * to {@link Crazy8Stats} for database-tracked players.
 */
public class Crazy8Player {
    /**
     * The player's username.
     */
    private String username;

    /**
     * The list of cards currently held by the player.
     */
    private List<Crazy8Card> hand;

    /**
     * A reference to the backend AppUser associated with this player.
     * <p>
     * Marked {@code transient} to avoid serialization.
     */
    private transient AppUser userRef;

    /**
     * Creates a Crazy 8 player with a generic username and no statistical tracking.
     *
     * @param username the player's display name
     */
    public Crazy8Player(String username) {
        this.username = username;
        this.hand = new ArrayList<>();
    }

    /**
     * Creates a Crazy 8 player backed by an {@link AppUser}, enabling stat tracking.
     *
     * @param user the backend user associated with this player
     */
    public Crazy8Player(AppUser user) {
        this.username = user.getUsername();
        this.userRef = user;
        this.hand = new ArrayList<>();
    }

    /**
     * Returns the backend user reference for this player.
     *
     * @return the associated {@link AppUser}, or {@code null} if none
     */
    public AppUser getUserRef() { return userRef; }

    /**
     * Returns the tracked Crazy 8 statistics for this user, if applicable.
     *
     * @return the associated {@link Crazy8Stats} object, or {@code null} if stats are unavailable
     */
    public Crazy8Stats getCrazy8Stats() {
        if (userRef != null && userRef.getUserStats() != null) {
            return (Crazy8Stats) userRef.getUserStats().getGameStats("Crazy8");
        }
        return null;
    }

    /**
     * Returns the username of the player.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the cards currently in the player's hand.
     *
     * @return a list of {@link Crazy8Card} objects
     */
    public List<Crazy8Card> getHand() {
        return hand;
    }

    /**
     * Returns the number of cards in the player's hand.
     *
     * @return the hand size
     */
    public int getHandSize() { return hand.size(); }

    /**
     * Adds a card to the player's hand.
     *
     * @param card the {@link Crazy8Card} to add
     */
    public void addCard(Crazy8Card card) {
        hand.add(card);
    }

    /**
     * Removes a card from the player's hand.
     * If the card appears multiple times, only the first occurrence is removed.
     *
     * @param card the {@link Crazy8Card} to remove
     */
    public void removeCard(Crazy8Card card) { hand.remove(card); }

}
