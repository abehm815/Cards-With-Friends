package data.User;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("EUCHRE")
public class EuchreStats extends GameStats {

    private int gamesWon;
    private int tricksTaken;
    private int timesPickedUp;
    private int timesGoneAlone;
    private int sweepsWon;

    protected EuchreStats() { }

    public EuchreStats(int gamesWon, int tricksTaken, int timesPickedUp, int timesGoneAlone, int sweepsWon) {
        this.gamesWon = gamesWon;
        this.tricksTaken = tricksTaken;
        this.timesPickedUp = timesPickedUp;
        this.timesGoneAlone = timesGoneAlone;
        this.sweepsWon = sweepsWon;
    }

    // Increment Methods
    public void addGameWon() { gamesWon++; }
    public void addTrickTaken() { tricksTaken++; }
    public void addTimePickedUp() { timesPickedUp++; }
    public void addTimeGoneAlone() { timesGoneAlone++; }
    public void addSweepWon() { sweepsWon++; }

    // Getters and Setters
    public int getGamesWon() { return gamesWon; }
    public int getTricksTaken() { return tricksTaken; }
    public int getTimesPickedUp() { return timesPickedUp; }
    public int getTimesGoneAlone() { return timesGoneAlone; }
    public int getSweepsWon() { return sweepsWon; }

    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }
    public void setTricksTaken(int tricksTaken) { this.tricksTaken = tricksTaken; }
    public void setTimesPickedUp(int timesPickedUp) { this.timesPickedUp = timesPickedUp; }
    public void setTimesGoneAlone(int timesGoneAlone) { this.timesGoneAlone = timesGoneAlone; }
    public void setSweepsWon(int sweepsWon) { this.sweepsWon = sweepsWon; }

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
