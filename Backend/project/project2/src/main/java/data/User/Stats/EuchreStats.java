package data.User.Stats;

import jakarta.persistence.*;

/**
 * Stores gameplay statistics for the Euchre card game.
 * <p>
 * Tracks metrics such as games won, tricks taken, times the player picked up
 * the turned-up card, times they went alone, and sweeps won.
 * This class extends {@link GameStats} to incorporate shared game tracking details.
 * </p>
 */
@Entity
@DiscriminatorValue("EUCHRE")
public class EuchreStats extends GameStats {

    private int gamesWon;
    private int tricksTaken;
    private int timesPickedUp;
    private int timesGoneAlone;
    private int sweepsWon;

    /**
     * Protected default constructor for JPA use.
     */
    public EuchreStats() { }

    /**
     * Constructs a EuchreStats object with custom initial values.
     *
     * @param gamesWon number of games won
     * @param tricksTaken number of tricks taken
     * @param timesPickedUp number of times the player picked up the card
     * @param timesGoneAlone number of times the player went alone
     * @param sweepsWon number of sweeps won
     */
    public EuchreStats(int gamesWon, int tricksTaken, int timesPickedUp, int timesGoneAlone, int sweepsWon) {
        this.gamesWon = gamesWon;
        this.tricksTaken = tricksTaken;
        this.timesPickedUp = timesPickedUp;
        this.timesGoneAlone = timesGoneAlone;
        this.sweepsWon = sweepsWon;
    }

    // Increment Methods

    /** Increments the number of games won by one. */
    public void addGameWon() { gamesWon++; }

    /** Increments the number of tricks taken by one. */
    public void addTrickTaken() { tricksTaken++; }

    /** Increments the number of times the player picked up the card by one. */
    public void addTimePickedUp() { timesPickedUp++; }

    /** Increments the number of times the player went alone by one. */
    public void addTimeGoneAlone() { timesGoneAlone++; }

    /** Increments the number of sweeps won by one. */
    public void addSweepWon() { sweepsWon++; }

    // Getters and Setters

    /** @return number of games won */
    public int getGamesWon() { return gamesWon; }

    /** @return number of tricks taken */
    public int getTricksTaken() { return tricksTaken; }

    /** @return number of times the player picked up the card */
    public int getTimesPickedUp() { return timesPickedUp; }

    /** @return number of times the player went alone */
    public int getTimesGoneAlone() { return timesGoneAlone; }

    /** @return number of sweeps won */
    public int getSweepsWon() { return sweepsWon; }

    // Setters

    /** @param gamesWon number of games won */
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    /** @param tricksTaken number of tricks taken */
    public void setTricksTaken(int tricksTaken) { this.tricksTaken = tricksTaken; }

    /** @param timesPickedUp number of times the player picked up the card */
    public void setTimesPickedUp(int timesPickedUp) { this.timesPickedUp = timesPickedUp; }

    /** @param timesGoneAlone number of times the player went alone */
    public void setTimesGoneAlone(int timesGoneAlone) { this.timesGoneAlone = timesGoneAlone; }

    /** @param sweepsWon number of sweeps won */
    public void setSweepsWon(int sweepsWon) { this.sweepsWon = sweepsWon; }

    /**
     * Returns a formatted string summarizing Euchre statistics.
     *
     * @return string representation of all recorded statistics
     */
    @Override
    public String toString() {
        return "Games won: " + gamesWon +
                ", Tricks taken: " + tricksTaken +
                ", Times picked up: " + timesPickedUp +
                ", Times gone alone: " + timesGoneAlone +
                ", Sweeps won: " + sweepsWon +
                ", Games Played: " + getGamesPlayed();
    }
}
