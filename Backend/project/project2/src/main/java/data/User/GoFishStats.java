package data.User;

// Holds the stats for Go Fish games
public class GoFishStats extends GameStats {
    // Stats
    private int timesWentFishing;
    private int questionsAsked;
    private int booksCollected;
    private int gamesWon;

    // Constructors
    // Basic Constructor, used by hibernate
    protected GoFishStats() { }
    // Constructor that manually adds all stats
    public GoFishStats(int fishing, int questionsAsked, int books, int gamesWon) {
        this.timesWentFishing = fishing;
        this.questionsAsked = questionsAsked;
        this.booksCollected = books;
        this.gamesWon = gamesWon;
    }

    // Increment Methods
    public void addWentFishing() {timesWentFishing++;}
    public void addQuestionAsked() {questionsAsked++;}
    public void addBookCollected() {booksCollected++;}
    public void addGameWon() {gamesWon++;}

    // Get Methods
    public int getTimesWentFishing() {return timesWentFishing;}
    public int getQuestionsAsked() {return questionsAsked;}
    public int getBooksCollected() {return booksCollected;}
    public int getGamesWon() {return gamesWon;}

    // Set Methods (Should only be used by admins to fix errors
    public void setTimesWentFishing(int timesWentFishing) {
        this.timesWentFishing = timesWentFishing;
    }
    public void setQuestionsAsked(int questionsAsked) {
        this.questionsAsked = questionsAsked;
    }
    public void setBooksCollected(int booksCollected) {
        this.booksCollected = booksCollected;
    }
    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    // To string method
    @Override
    public String toString() {
        return "Times Gone Fishing: " + timesWentFishing + ", Questions Asked: " + questionsAsked + ", Books Collected: " + booksCollected + ", Games Won: " + gamesWon;
    }
}
