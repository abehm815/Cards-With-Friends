package data.User.Stats;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("CRAZY8")
public class Crazy8Stats extends GameStats{
    private int timesDrewCard;
    private int cardsPlaced;
    private int crazy8Played;
    private int skipsPlayed;
    private int reversePlayed;
    private int plus2Played;
    private int plus4Played;
    private int gamesWon;

    public Crazy8Stats() {}


    public Crazy8Stats(int timesDrewCard, int cardsPlaced, int crazy8Played, int skipsPlayed,
                       int reversePlayed, int plus2Played, int plus4Played, int gamesWon) {
        this.timesDrewCard = timesDrewCard;
        this.cardsPlaced = cardsPlaced;
        this.crazy8Played = crazy8Played;
        this.skipsPlayed = skipsPlayed;
        this.reversePlayed = reversePlayed;
        this.plus2Played = plus2Played;
        this.plus4Played = plus4Played;
        this.gamesWon = gamesWon;
    }

    // Increment Methods
    public void addCardDrawn() {
        timesDrewCard++;
    }

    public void addCardPlaced() {
        cardsPlaced++;
    }

    public void addCrazy8Played() {
        crazy8Played++;
    }

    public void addSkipPlayed() {
        skipsPlayed++;
    }

    public void addReversePlayed() {
        reversePlayed++;
    }

    public void addPlus2Played() {
        plus2Played++;
    }

    public void addPlus4Played() {
        plus4Played++;
    }

    public void addGameWon() {
        gamesWon++;
    }

    // Getters
    public int getTimesDrewCard() {
        return timesDrewCard;
    }

    public int getCardsPlaced() {
        return cardsPlaced;
    }

    public int getCrazy8Played() {
        return crazy8Played;
    }

    public int getSkipsPlayed() {
        return skipsPlayed;
    }

    public int getReversePlayed() {
        return reversePlayed;
    }

    public int getPlus2Played() {
        return plus2Played;
    }

    public int getPlus4Played() {
        return plus4Played;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    // Setters
    public void setTimesDrewCard(int timesDrewCard) {
        this.timesDrewCard = timesDrewCard;
    }

    public void setCardsPlaced(int cardsPlaced) {
        this.cardsPlaced = cardsPlaced;
    }

    public void setCrazy8Played(int crazy8Played) {
        this.crazy8Played = crazy8Played;
    }

    public void setSkipsPlayed(int skipsPlayed) {
        this.skipsPlayed = skipsPlayed;
    }

    public void setReversePlayed(int reversePlayed) {
        this.reversePlayed = reversePlayed;
    }

    public void setPlus2Played(int plus2Played) {
        this.plus2Played = plus2Played;
    }

    public void setPlus4Played(int plus4Played) {
        this.plus4Played = plus4Played;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    @Override
    public String toString() {
        return "Cards Drawn: " + timesDrewCard +
                ", Cards Placed: " + cardsPlaced +
                ", Crazy 8s Played: " + crazy8Played +
                ", Skips Played: " + skipsPlayed +
                ", Reverses Played: " + reversePlayed +
                ", +2 Played: " + plus2Played +
                ", +4 Played: " + plus4Played +
                ", Games Won: " + gamesWon;
    }
}
