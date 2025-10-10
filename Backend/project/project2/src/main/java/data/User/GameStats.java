package data.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

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

    public void addGamePlayed() {
        gamesPlayed++;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserStats getUserStats() {
        return userStats;
    }

    public void setUserStats(UserStats userStats) {
        this.userStats = userStats;
    }
}
