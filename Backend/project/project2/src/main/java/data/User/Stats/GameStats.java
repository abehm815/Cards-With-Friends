package data.User.Stats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Base class for tracking gameplay statistics across all supported games.
 * <p>
 * This abstract class serves as the parent for game-specific statistics entities
 * such as Blackjack, Crazy 8s, and Euchre. It uses a single-table inheritance
 * strategy, where all game stat types are stored within one database table and
 * differentiated by a discriminator column.
 * </p>
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "game_type", discriminatorType = DiscriminatorType.STRING)
public abstract class GameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int gamesPlayed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_stats_id", nullable = false)
    @JsonIgnore
    private UserStats userStats;

    /**
     * Increments the number of games played by one.
     */
    public void addGamePlayed() {
        gamesPlayed++;
    }

    /**
     * Sets the total number of games played.
     *
     * @param gamesPlayed number of games played
     */
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    /**
     * Returns the total number of games played.
     *
     * @return games played
     */
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    /**
     * Returns the unique identifier of this stats record.
     *
     * @return ID of the stats entry
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this stats record.
     *
     * @param id the ID to assign
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the associated {@link UserStats} object.
     *
     * @return the user's overall statistics container
     */
    public UserStats getUserStats() {
        return userStats;
    }

    /**
     * Sets the {@link UserStats} object that owns this stats entry.
     *
     * @param userStats the parent stats object
     */
    public void setUserStats(UserStats userStats) {
        this.userStats = userStats;
    }
}
