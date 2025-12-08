package data.Game.BlackJack.history;

import data.Game.goFish.history.GoFishMatchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BlackJackMatchHistoryRepository extends JpaRepository<BlackJackMatchHistoryEntity, Long> {
    BlackJackMatchHistoryEntity findByMatchId(String matchId);

    /**
     * Returns all matches where the player participated
     */
    @Query("SELECT m FROM BlackJackMatchHistoryEntity m JOIN m.events e WHERE e.player = :username")
    List<BlackJackMatchHistoryEntity> findAllByPlayer(String username);
}
