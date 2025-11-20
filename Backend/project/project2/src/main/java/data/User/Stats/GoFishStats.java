package data.User.Stats;

import jakarta.persistence.*;

/**
 * Represents statistics specific to the Go Fish game.
 * Extends {@link GameStats} and stores detailed performance metrics
 * such as questions asked, books collected, and times the user had to go fishing.
 */
@Entity
@DiscriminatorValue("GO_FISH")
public class GoFishStats extends GameStats {

    private int timesWentFishing;
    private int questionsAsked;
    private int booksCollected;
    private int gamesWon;

    /**
     * Default constructor required by JPA.
     */
    public GoFishStats() { }

    /**
     * Creates a new instance of GoFishStats with initialized values.
     *
     * @param fishing number of times the user went fishing
     * @param questionsAsked total number of questions asked
     * @param books number of books collected
     * @param gamesWon number of games won
     */
    public GoFishStats(int fishing, int questionsAsked, int books, int gamesWon) {
        this.timesWentFishing = fishing;
        this.questionsAsked = questionsAsked;
        this.booksCollected = books;
        this.gamesWon = gamesWon;
    }

    // Increment Methods

    /** Increments the count of times the player went fishing. */
    public void addWentFishing() { timesWentFishing++; }

    /** Increments the number of questions asked. */
    public void addQuestionAsked() { questionsAsked++; }

    /** Increments the count of books collected. */
    public void addBookCollected() { booksCollected++; }

    /** Increments the number of games won. */
    public void addGameWon() { gamesWon++; }

    // Getters and Setters

    /**
     * @return number of times the player went fishing
     */
    public int getTimesWentFishing() { return timesWentFishing; }

    /**
     * @return number of questions asked by the player
     */
    public int getQuestionsAsked() { return questionsAsked; }

    /**
     * @return number of books collected by the player
     */
    public int getBooksCollected() { return booksCollected; }

    /**
     * @return number of games won by the player
     */
    public int getGamesWon() { return gamesWon; }


    /**
     * @param timesWentFishing new value for times the player went fishing
     */
    public void setTimesWentFishing(int timesWentFishing) { this.timesWentFishing = timesWentFishing; }

    /**
     * @param questionsAsked new value for questions asked
     */
    public void setQuestionsAsked(int questionsAsked) { this.questionsAsked = questionsAsked; }

    /**
     * @param booksCollected new value for books collected
     */
    public void setBooksCollected(int booksCollected) { this.booksCollected = booksCollected; }

    /**
     * @param gamesWon new value for games won
     */
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    /**
     * Creates a descriptive text summary of the Go Fish statistics.
     *
     * @return formatted string containing Go Fish metrics
     */
    @Override
    public String toString() {
        return "Times Gone Fishing: " + timesWentFishing +
                ", Questions Asked: " + questionsAsked +
                ", Books Collected: " + booksCollected +
                ", Games Won: " + gamesWon +
                ", Games Played: " + getGamesPlayed();
    }
}
