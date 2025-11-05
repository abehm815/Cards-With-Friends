package data.User.Stats;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("GO_FISH")
public class GoFishStats extends GameStats {

    private int timesWentFishing;
    private int questionsAsked;
    private int booksCollected;
    private int gamesWon;

    public GoFishStats() { }

    public GoFishStats(int fishing, int questionsAsked, int books, int gamesWon) {
        this.timesWentFishing = fishing;
        this.questionsAsked = questionsAsked;
        this.booksCollected = books;
        this.gamesWon = gamesWon;
    }

    // Increment Methods
    public void addWentFishing() { timesWentFishing++; }
    public void addQuestionAsked() { questionsAsked++; }
    public void addBookCollected() { booksCollected++; }
    public void addGameWon() { gamesWon++; }

    // Getters and Setters
    public int getTimesWentFishing() { return timesWentFishing; }
    public int getQuestionsAsked() { return questionsAsked; }
    public int getBooksCollected() { return booksCollected; }
    public int getGamesWon() { return gamesWon; }

    public void setTimesWentFishing(int timesWentFishing) { this.timesWentFishing = timesWentFishing; }
    public void setQuestionsAsked(int questionsAsked) { this.questionsAsked = questionsAsked; }
    public void setBooksCollected(int booksCollected) { this.booksCollected = booksCollected; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    @Override
    public String toString() {
        return "Times Gone Fishing: " + timesWentFishing +
                ", Questions Asked: " + questionsAsked +
                ", Books Collected: " + booksCollected +
                ", Games Won: " + gamesWon +
                ", Games Played: " + getGamesPlayed();
    }
}
