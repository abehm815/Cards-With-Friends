package data.User;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("BLACKJACK")
public class BlackjackStats extends GameStats {

    private double moneyWon;
    private int betsWon;
    private int timesDoubledDown;
    private int timesSplit;
    private int timesHit;

    protected BlackjackStats() { }

    public BlackjackStats(double moneyWon, int betsWon, int timesDoubledDown, int timesSplit, int timesHit) {
        this.moneyWon = moneyWon;
        this.betsWon = betsWon;
        this.timesDoubledDown = timesDoubledDown;
        this.timesSplit = timesSplit;
        this.timesHit = timesHit;
    }

    // Increment Methods
    public void addMoneyWon(double moneyWon) { this.moneyWon += moneyWon; }
    public void addBetWon() { betsWon++; }
    public void addTimeDoubledDown() { timesDoubledDown++; }
    public void addTimeSplit() { timesSplit++; }
    public void addTimeHit() { timesHit++; }

    // Getters and Setters
    public double getMoneyWon() { return moneyWon; }
    public int getBetsWon() { return betsWon; }
    public int getTimesDoubledDown() { return timesDoubledDown; }
    public int getTimesSplit() { return timesSplit; }
    public int getTimesHit() { return timesHit; }

    public void setMoneyWon(double moneyWon) { this.moneyWon = moneyWon; }
    public void setBetsWon(int betsWon) { this.betsWon = betsWon; }
    public void setTimesDoubledDown(int timesDoubledDown) { this.timesDoubledDown = timesDoubledDown; }
    public void setTimesSplit(int timesSplit) { this.timesSplit = timesSplit; }
    public void setTimesHit(int timesHit) { this.timesHit = timesHit; }

    @Override
    public String toString() {
        return "Money won: " + moneyWon +
                ", Bets won: " + betsWon +
                ", Times doubled down: " + timesDoubledDown +
                ", Times split: " + timesSplit +
                ", Times hit: " + timesHit +
                ", Games Played: " + getGamesPlayed();
    }
}
