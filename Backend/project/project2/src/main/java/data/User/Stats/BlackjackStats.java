package data.User.Stats;

import jakarta.persistence.*;

/**
 * Represents gameplay statistics specific to Blackjack.
 * <p>
 * This class extends {@link GameStats} and stores a variety of metrics such as
 * money won, number of winning bets, frequency of specific actions (hit, split,
 * double down), and total games won. It is mapped as a JPA entity using
 * single-table inheritance.
 */
@Entity
@DiscriminatorValue("BLACKJACK")
public class BlackjackStats extends GameStats {

    private int moneyWon;
    private int betsWon;
    private int timesDoubledDown;
    private int timesSplit;
    private int timesHit;
    @Column(nullable = false)
    private int gamesWon;

    /**
     * Basic stats constructor
     */
    public BlackjackStats() {}

    /**
     * Stats constructor with custom values
     * @param moneyWon money won
     * @param betsWon bets won
     * @param timesDoubledDown times doubled down
     * @param timesSplit times split
     * @param timesHit times hit
     * @param gamesWon games won
     */
    public BlackjackStats(int moneyWon, int betsWon, int timesDoubledDown, int timesSplit, int timesHit, int gamesWon) {
        this.moneyWon = moneyWon;
        this.betsWon = betsWon;
        this.timesDoubledDown = timesDoubledDown;
        this.timesSplit = timesSplit;
        this.timesHit = timesHit;
        this.gamesWon = gamesWon;
    }

    // Increment Methods
    /**
     * Adds a specified amount of money to the total money won.
     *
     * @param moneyWon amount of money to add
     */
    public void addMoneyWon(int moneyWon) { this.moneyWon += moneyWon; }

    /** Increments the number of bets won by one. */
    public void addBetWon() { betsWon++; }

    /** Increments the number of times the user doubled down. */
    public void addTimeDoubledDown() { timesDoubledDown++; }

    /** Increments the number of times the user split their hand. */
    public void addTimeSplit() { timesSplit++; }

    /** Increments the number of times the user hit. */
    public void addTimeHit() { timesHit++; }

    /** Increments the number of games won by one. */
    public void addGameWon() { gamesWon++; }

    // Getters and Setters
    /**
     * @return total amount of money won
     */
    public int getMoneyWon() { return moneyWon; }

    /**
     * @return number of bets won
     */
    public int getBetsWon() { return betsWon; }

    /**
     * @return number of times the user doubled down
     */
    public int getTimesDoubledDown() { return timesDoubledDown; }

    /**
     * @return number of times the user split their hand
     */
    public int getTimesSplit() { return timesSplit; }

    /**
     * @return number of times the user hit
     */
    public int getTimesHit() { return timesHit; }

    /**
     * @return number of Blackjack games won
     */
    public int getGamesWon() { return gamesWon; }

    // Setters

    /**
     * Sets the total money won.
     *
     * @param moneyWon new money won value
     */
    public void setMoneyWon(int moneyWon) { this.moneyWon = moneyWon; }

    /**
     * Sets the number of bets won.
     *
     * @param betsWon new number of bets won
     */
    public void setBetsWon(int betsWon) { this.betsWon = betsWon; }

    /**
     * Sets the number of times doubled down.
     *
     * @param timesDoubledDown new value
     */
    public void setTimesDoubledDown(int timesDoubledDown) { this.timesDoubledDown = timesDoubledDown; }

    /**
     * Sets the number of times split.
     *
     * @param timesSplit new value
     */
    public void setTimesSplit(int timesSplit) { this.timesSplit = timesSplit; }

    /**
     * Sets the number of times hit.
     *
     * @param timesHit new value
     */
    public void setTimesHit(int timesHit) { this.timesHit = timesHit; }

    /**
     * Sets the number of games won.
     *
     * @param gamesWon new games won value
     */
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    /**
     * Returns a human-readable summary of all tracked Blackjack statistics.
     *
     * @return formatted statistics summary
     */
    @Override
    public String toString() {
        return "Money won: " + moneyWon +
                ", Bets won: " + betsWon +
                ", Times doubled down: " + timesDoubledDown +
                ", Times split: " + timesSplit +
                ", Times hit: " + timesHit +
                ", Games Played: " + getGamesPlayed() +
                ", Games Won: " + gamesWon;
    }
}
