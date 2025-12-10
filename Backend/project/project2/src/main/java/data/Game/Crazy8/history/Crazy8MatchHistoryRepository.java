package data.Game.Crazy8.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Crazy8MatchHistoryRepository extends JpaRepository<Crazy8MatchHistoryEntity, Long> {
    Crazy8MatchHistoryEntity findByMatchId(String matchId);

    /**
     * Returns all matches where the player participated
     */
    @Query("SELECT m FROM Crazy8MatchHistoryEntity m JOIN m.events e WHERE e.player = :username")
    List<Crazy8MatchHistoryEntity> findAllByPlayer(String username);
}
