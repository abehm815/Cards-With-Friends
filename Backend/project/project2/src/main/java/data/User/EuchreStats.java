package data.User;

public class EuchreStats extends GameStats{
    // Stats
    private int gamesWon;
    private int tricksTaken;
    private int timesPickedUp;
    private int timesGoneAlone;
    private int sweepsWon;

    // Constructors
    // Basic Constructor used by Hibernate
    protected EuchreStats() { }
    // Constructor that manually adds all stats
    public EuchreStats (int gamesWon, int tricksTaken, int timesPickedUp, int timesGoneAlone, int sweepsWon) {
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

    // Get Methods
    public int getGamesWon() { return gamesWon; }
    public int getTricksTaken() { return tricksTaken; }
    public int getTimesPickedUp() { return timesPickedUp; }
    public int getTimesGoneAlone() { return timesGoneAlone; }
    public int getSweepsWon() { return sweepsWon; }

    // Set Methods (Should only be used by admins)
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }
    public void setTricksTaken(int tricksTaken) { this.tricksTaken = tricksTaken; }
    public void setTimesPickedUp(int timesPickedUp) { this.timesPickedUp = timesPickedUp; }
    public void setTimesGoneAlone(int timesGoneAlone) { this.timesGoneAlone = timesGoneAlone; }
    public void setSweepsWon(int sweepsWon) { this.sweepsWon = sweepsWon; }

    // toString Method
    @Override
    public String toString() {
        return "Games won: " + gamesWon + ", Tricks taken: " + tricksTaken + ", Times picked up: " + timesPickedUp + ", Times gone alone: " + timesGoneAlone + ", Sweeps won: " + sweepsWon;
    }
}
