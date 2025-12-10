package data.User.Stats;

import data.User.AppUser;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of all gameplay statistics for a particular user.
 * This entity maintains a mapping from game names to {@link GameStats} objects,
 * enabling the system to track per-game performance for each user.
 */
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
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @MapKeyColumn(name = "game_name")
    private Map<String, GameStats> gameStats = new HashMap<>();

    /**
     * Default constructor initializes all supported game statistics
     * so that every user starts with complete stat records.
     */
    public UserStats() {
        addGameStats("Euchre", new EuchreStats());
        addGameStats("Blackjack", new BlackjackStats());
        addGameStats("GoFish", new GoFishStats());
        addGameStats("Crazy8", new Crazy8Stats());
    }

    /**
     * Secondary constructor that exists only for compatibility
     * (currently not used but retained for future extensibility).
     *
     * @param holder unused parameter placeholder
     */
    public UserStats(int holder) {

    }


    /**
     * Adds a game statistics entry for a specific game.
     * Automatically links the stats object back to this {@link UserStats} instance.
     *
     * @param gameName the unique name of the game
     * @param stats the statistics object to associate with the user
     */
    public void addGameStats(String gameName, GameStats stats) {
        stats.setUserStats(this);
        gameStats.put(gameName, stats);
    }

    /**
     * Retrieves the statistics for a specific game.
     *
     * @param gameName the name of the game
     * @return the {@link GameStats} associated with that game, or {@code null} if not found
     */
    public GameStats getGameStats(String gameName) {
        return gameStats.get(gameName);
    }

    /**
     * Retrieves a map of all game statistics for the user.
     *
     * @return an unfiltered map containing all game-name/stat pairs
     */
    public Map<String, GameStats> getAllGameStats() {
        return gameStats;
    }

    /**
     * @return the {@link AppUser} linked to these statistics
     */
    public AppUser getAppUser() { return this.appUser; }

    /**
     * Assigns the associated {@link AppUser}.
     *
     * @param user the user whose statistics these are
     */
    public void setAppUser(AppUser user) { appUser = user; }

    /**
     * @return the internal ID for this UserStats entity
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the internal ID for this statistics record.
     *
     * @param id unique identifier
     */
    public void setId(long id) {
        this.id = id;
    }

}
