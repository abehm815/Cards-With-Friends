package data.User.Stats;
import jakarta.persistence.*;

/**
 * Represents gameplay statistics for the Crazy 8's card game.
 * <p>
 * Tracks counts for actions such as drawing cards, placing cards,
 * playing special cards (+2, +4, Reverse, Skip, Crazy 8), and the number
 * of games won. Extends {@link GameStats} to inherit shared game statistics.
 * </p>
 */
@Entity
@DiscriminatorValue("CRAZY8")
public class Crazy8Stats extends GameStats{
    private int timesDrewCard;
    private int cardsPlaced;
    private int crazy8Played;
    private int skipsPlayed;
    private int reversePlayed;
    private int plus2Played;
    private int plus4Played;
    private int gamesWon;

    /**
     * Default constructor for JPA and general initialization.
     */
    public Crazy8Stats() {}

    /**
     * Constructs a Crazy8Stats object with custom initial values.
     *
     * @param timesDrewCard number of times a card was drawn
     * @param cardsPlaced number of cards placed
     * @param crazy8Played number of Crazy 8 cards played
     * @param skipsPlayed number of Skip cards played
     * @param reversePlayed number of Reverse cards played
     * @param plus2Played number of +2 cards played
     * @param plus4Played number of +4 cards played
     * @param gamesWon number of games won
     */
    public Crazy8Stats(int timesDrewCard, int cardsPlaced, int crazy8Played, int skipsPlayed,
                       int reversePlayed, int plus2Played, int plus4Played, int gamesWon) {
        this.timesDrewCard = timesDrewCard;
        this.cardsPlaced = cardsPlaced;
        this.crazy8Played = crazy8Played;
        this.skipsPlayed = skipsPlayed;
        this.reversePlayed = reversePlayed;
        this.plus2Played = plus2Played;
        this.plus4Played = plus4Played;
        this.gamesWon = gamesWon;
    }

    // Increment Methods
    /** Increments the count of cards drawn by one. */
    public void addCardDrawn() {
        timesDrewCard++;
    }

    /** Increments the count of cards placed by one. */
    public void addCardPlaced() {
        cardsPlaced++;
    }

    /** Increments the count of Crazy 8 cards played by one. */
    public void addCrazy8Played() {
        crazy8Played++;
    }

    /** Increments the count of Skip cards played by one. */
    public void addSkipPlayed() {
        skipsPlayed++;
    }

    /** Increments the count of Reverse cards played by one. */
    public void addReversePlayed() {
        reversePlayed++;
    }

    /** Increments the count of +2 cards played by one. */
    public void addPlus2Played() {
        plus2Played++;
    }

    /** Increments the count of +4 cards played by one. */
    public void addPlus4Played() {
        plus4Played++;
    }

    /** Increments the number of games won by one. */
    public void addGameWon() {
        gamesWon++;
    }

    // Getters

    /** @return number of times a card was drawn */
    public int getTimesDrewCard() {
        return timesDrewCard;
    }

    /** @return number of cards placed */
    public int getCardsPlaced() {
        return cardsPlaced;
    }

    /** @return number of Crazy 8 cards played */
    public int getCrazy8Played() {
        return crazy8Played;
    }

    /** @return number of Skip cards played */
    public int getSkipsPlayed() {
        return skipsPlayed;
    }

    /** @return number of Reverse cards played */
    public int getReversePlayed() {
        return reversePlayed;
    }

    /** @return number of +2 cards played */
    public int getPlus2Played() {
        return plus2Played;
    }

    /** @return number of +4 cards played */
    public int getPlus4Played() {
        return plus4Played;
    }

    /** @return number of games won */
    public int getGamesWon() {
        return gamesWon;
    }

    // Setters

    /** @param timesDrewCard number of times a card was drawn */
    public void setTimesDrewCard(int timesDrewCard) {
        this.timesDrewCard = timesDrewCard;
    }

    /** @param cardsPlaced number of cards placed */
    public void setCardsPlaced(int cardsPlaced) {
        this.cardsPlaced = cardsPlaced;
    }

    /** @param crazy8Played number of Crazy 8 cards played */
    public void setCrazy8Played(int crazy8Played) {
        this.crazy8Played = crazy8Played;
    }

    /** @param skipsPlayed number of Skip cards played */
    public void setSkipsPlayed(int skipsPlayed) {
        this.skipsPlayed = skipsPlayed;
    }

    /** @param reversePlayed number of Reverse cards played */
    public void setReversePlayed(int reversePlayed) {
        this.reversePlayed = reversePlayed;
    }

    /** @param plus2Played number of +2 cards played */
    public void setPlus2Played(int plus2Played) {
        this.plus2Played = plus2Played;
    }

    /** @param plus4Played number of +4 cards played */
    public void setPlus4Played(int plus4Played) {
        this.plus4Played = plus4Played;
    }

    /** @param gamesWon number of games won */
    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    /**
     * Returns a formatted string summarizing Crazy 8 statistics.
     *
     * @return string representation of all recorded statistics
     */
    @Override
    public String toString() {
        return "Cards Drawn: " + timesDrewCard +
                ", Cards Placed: " + cardsPlaced +
                ", Crazy 8s Played: " + crazy8Played +
                ", Skips Played: " + skipsPlayed +
                ", Reverses Played: " + reversePlayed +
                ", +2 Played: " + plus2Played +
                ", +4 Played: " + plus4Played +
                ", Games Won: " + gamesWon;
    }
}
