package Backend.User;

// This is a base class that has methods that all the other stat classes will use
public abstract class GameStats {
    public int gamesPlayed;

    public void addGamePlayed() {
        gamesPlayed++;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }
}
