package data.Game.goFish.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GoFishMatchHistoryRepository extends JpaRepository<GoFishMatchHistoryEntity, Long> {
    GoFishMatchHistoryEntity findByMatchId(String matchId);

    /**
     * Returns all matches where the player participated
     */
    @Query("SELECT m FROM GoFishMatchHistoryEntity m JOIN m.events e WHERE e.player = :username")
    List<GoFishMatchHistoryEntity> findAllByPlayer(String username);
}
