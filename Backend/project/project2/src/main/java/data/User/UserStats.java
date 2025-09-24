package data.User;

import java.util.HashMap;
import java.util.Map;

public class UserStats {
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
