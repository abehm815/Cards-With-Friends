package data.User;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "game_name")
    private Map<String, GameStats> gameStats = new HashMap<>();

    public UserStats() {
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
