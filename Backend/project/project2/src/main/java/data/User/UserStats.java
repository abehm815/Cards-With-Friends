package data.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.HashMap;
import java.util.Map;

@Entity
public class UserStats {
    public int getId() {
        return UserID;
    }

    public void setId(int UserID) {
        this.UserID = UserID;
    }

    @Id
    private int UserID;

    private Map<String, GameStats> gameStats;

    public UserStats() {
        this.gameStats = new HashMap<>();
    }

    public void addGameStats(String gameName, GameStats stats) {
        gameStats.put(gameName, stats);
    }

    public GameStats getGameStats(String gameName) {
        return gameStats.get(gameName);
    }

    public Map<String, GameStats> getAllGameStats() {
        return gameStats;
    }
}
