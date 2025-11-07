package data.User.Stats;

import jakarta.persistence.*;

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

    public BlackjackStats() {}


    public BlackjackStats(int moneyWon, int betsWon, int timesDoubledDown, int timesSplit, int timesHit, int gamesWon) {
        this.moneyWon = moneyWon;
        this.betsWon = betsWon;
        this.timesDoubledDown = timesDoubledDown;
        this.timesSplit = timesSplit;
        this.timesHit = timesHit;
        this.gamesWon = gamesWon;
    }

    // Increment Methods
    public void addMoneyWon(int moneyWon) { this.moneyWon += moneyWon; }
    public void addBetWon() { betsWon++; }
    public void addTimeDoubledDown() { timesDoubledDown++; }
    public void addTimeSplit() { timesSplit++; }
    public void addTimeHit() { timesHit++; }
    public void addGameWon() { gamesWon++; }

    // Getters and Setters
    public int getMoneyWon() { return moneyWon; }
    public int getBetsWon() { return betsWon; }
    public int getTimesDoubledDown() { return timesDoubledDown; }
    public int getTimesSplit() { return timesSplit; }
    public int getTimesHit() { return timesHit; }
    public int getGamesWon() { return gamesWon; }

    public void setMoneyWon(int moneyWon) { this.moneyWon = moneyWon; }
    public void setBetsWon(int betsWon) { this.betsWon = betsWon; }
    public void setTimesDoubledDown(int timesDoubledDown) { this.timesDoubledDown = timesDoubledDown; }
    public void setTimesSplit(int timesSplit) { this.timesSplit = timesSplit; }
    public void setTimesHit(int timesHit) { this.timesHit = timesHit; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

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
