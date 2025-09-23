package data.User;

public class BlackjackStats extends GameStats {
    // Stats
    private double moneyWon;
    private int betsWon;
    private int timesDoubledDown;
    private int timesSplit;
    private int timesHit;

    // Constructors
    // Basic constructor used by Hibernate
    protected BlackjackStats() { }
    // Constructor that manually adds all stats
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

    // Get Methods
    public double getMoneyWon() { return moneyWon; }
    public int getBetsWon() { return betsWon; }
    public int getTimesDoubledDown() { return timesDoubledDown; }
    public int getTimesSplit() { return timesSplit; }
    public int getTimesHit() { return timesHit; }

    // Set Methods (Only to be used by admins)
    public void setMoneyWon(double moneyWon) { this.moneyWon = moneyWon; }
    public void setBetsWon(int betsWon) { this.betsWon = betsWon; }
    public void setTimesDoubledDown(int timesDoubledDown) { this.timesDoubledDown = timesDoubledDown; }
    public void setTimesSplit(int timesSplit) { this.timesSplit = timesSplit; }
    public void setTimesHit(int timesHit) { this.timesHit = timesHit; }

    // toString Method
    @Override
    public String toString() {
        return "Money won: " + moneyWon + ", Bets won: " + betsWon + ", Times doubled down: " + timesDoubledDown + ", Times split: " + timesSplit + ", Times hit: " + timesHit;
    }
}
