package data.User.Stats;

import data.User.AppUser;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(mappedBy = "userStats")
    private AppUser appUser;

    @OneToMany(
            mappedBy = "userStats",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @MapKeyColumn(name = "game_name")
    private Map<String, GameStats> gameStats = new HashMap<>();

    public UserStats() {
        addGameStats("Euchre", new EuchreStats());
        addGameStats("Blackjack", new BlackjackStats());
        addGameStats("GoFish", new GoFishStats());
        addGameStats("Crazy8", new Crazy8Stats());
    }

    public UserStats(int holder) {

    }

    public void addGameStats(String gameName, GameStats stats) {
        stats.setUserStats(this);
        gameStats.put(gameName, stats);
    }

    public GameStats getGameStats(String gameName) {
        return gameStats.get(gameName);
    }

    public Map<String, GameStats> getAllGameStats() {
        return gameStats;
    }

    public AppUser getAppUser() { return this.appUser; }
    public void setAppUser(AppUser user) { appUser = user; }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
